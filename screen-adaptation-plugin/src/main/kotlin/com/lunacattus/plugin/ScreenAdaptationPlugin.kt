package com.lunacattus.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.Serializable
import java.lang.StringBuilder
import java.util.Locale

interface TargetScreen : Serializable {
    val widthPx: Int
    val heightPx: Int
    val dpi: Int
}

data class TargetScreenImpl(
    override val widthPx: Int,
    override val heightPx: Int,
    override val dpi: Int
) : TargetScreen

abstract class ScreenAdaptationExtension {
    abstract val designWidthPx: Property<Int>
    abstract val designHeightPx: Property<Int>
    abstract val designDpi: Property<Int>
    
    abstract val targetScreens: ListProperty<TargetScreen>
    
    abstract val baseOn: Property<String>
    abstract val maxDp: Property<Int>

    abstract val dpNameFormat: Property<String>
    abstract val spNameFormat: Property<String>

    abstract val autoTargetDevice: Property<Boolean>
    
    init {
        baseOn.convention("width")
        dpNameFormat.convention("_{i}px")
        spNameFormat.convention("_font_{i}px")
        autoTargetDevice.convention(false)
    }
    
    fun target(widthPx: Int, heightPx: Int, dpi: Int) {
        targetScreens.add(TargetScreenImpl(widthPx, heightPx, dpi))
    }
}

class ScreenAdaptationPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("screenAdaptation", ScreenAdaptationExtension::class.java)
        val outputDirectory = project.layout.buildDirectory.dir("generated/res/screenAdaptation")
        
        val taskProvider = project.tasks.register("generateDimens", GenerateDimensTask::class.java)
        taskProvider.configure {
            group = "screen adaptation"
            description = "Generates sw-dimens based on screen parameters"
            
            designWidthPx.set(extension.designWidthPx)
            designHeightPx.set(extension.designHeightPx)
            designDpi.set(extension.designDpi)
            targetScreens.set(extension.targetScreens)
            baseOn.set(extension.baseOn)
            maxDp.set(extension.maxDp)
            dpNameFormat.set(extension.dpNameFormat)
            spNameFormat.set(extension.spNameFormat)
            autoTargetDevice.set(extension.autoTargetDevice)
            
            outputDir.set(outputDirectory)
            srcResDir.set(project.file("src/main/res"))
            
            // If auto-detection is on, we can't assume outputs are up-to-date since the device might change
            outputs.upToDateWhen { !autoTargetDevice.get() }
        }
        
        project.tasks.matching { it.name == "preBuild" }.configureEach {
            dependsOn(taskProvider)
        }
        
        val addResAction = {
            try {
                val android = project.extensions.findByName("android")
                if (android != null) {
                    val getSourceSets = android.javaClass.methods.first { it.name == "getSourceSets" }
                    val sourceSets = getSourceSets.invoke(android)
                    val getByName = sourceSets.javaClass.methods.first { it.name == "getByName" && it.parameterTypes.size == 1 }
                    val main = getByName.invoke(sourceSets, "main")
                    val getRes = main.javaClass.methods.first { it.name == "getRes" }
                    val res = getRes.invoke(main)
                    
                    val srcDir = res.javaClass.methods.first { it.name == "srcDir" && it.parameterTypes.size == 1 }
                    srcDir.invoke(res, outputDirectory.get().asFile)
                }
            } catch (e: Exception) {
                println("ScreenAdaptationPlugin: Failed to map generated res directory automatically:")
                e.printStackTrace()
            }
        }

        project.plugins.withId("com.android.application") { addResAction() }
        project.plugins.withId("com.android.library") { addResAction() }
    }
}

abstract class GenerateDimensTask : DefaultTask() {

    @get:Input
    abstract val designWidthPx: Property<Int>
    @get:Input
    abstract val designHeightPx: Property<Int>
    @get:Input
    abstract val designDpi: Property<Int>

    @get:Input
    abstract val targetScreens: ListProperty<TargetScreen>

    @get:Input
    abstract val baseOn: Property<String>

    @get:Input
    @get:Optional
    abstract val maxDp: Property<Int>

    @get:Input
    abstract val dpNameFormat: Property<String>

    @get:Input
    abstract val spNameFormat: Property<String>

    @get:Input
    abstract val autoTargetDevice: Property<Boolean>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val srcResDir: DirectoryProperty

    private fun pxToDp(px: Int, dpi: Int): Float = px / (dpi / 160f)

    @TaskAction
    fun generate() {
        println("\n" + "=".repeat(60))
        println("       SCREEN ADAPTATION PLUGIN - GENERATION START")
        println("=".repeat(60))

        val dWidthPx = designWidthPx.get()
        val dHeightPx = designHeightPx.get()
        val dDpi = designDpi.get()
        
        val dWidthDp = pxToDp(dWidthPx, dDpi)
        val dHeightDp = pxToDp(dHeightPx, dDpi)
        
        val isBaseOnWidth = baseOn.get().lowercase() == "width"
        val maxD = if (maxDp.getOrElse(0) > 0) maxDp.get() else maxOf(dWidthPx, dHeightPx)
        val dpFormat = dpNameFormat.get()
        val spFormat = spNameFormat.get()
        
        val finalTargets = targetScreens.get().toMutableList()
        val detectedMsgs = mutableListOf<String>()
        
        if (autoTargetDevice.get()) {
            try {
                val detectedList = detectAllDeviceMetrics()
                detectedList.forEach { detected ->
                    val msg = "Detected ADB device: ${detected.widthPx}x${detected.heightPx} @ ${detected.dpi}dpi"
                    println(">>> $msg")
                    
                    if (finalTargets.none { it.widthPx == detected.widthPx && it.heightPx == detected.heightPx && it.dpi == detected.dpi }) {
                        finalTargets.add(detected)
                    }
                    detectedMsgs.add(msg)
                }
            } catch (e: Exception) {
                println("ScreenAdaptationPlugin: Failed to detect devices via ADB: ${e.message}")
            }
        }

        val buildResDir = outputDir.get().asFile
        if (buildResDir.exists()) {
            buildResDir.deleteRecursively()
        }
        buildResDir.mkdirs()

        // 1. Generate base dimens into srcDir (permanent)
        val srcRoot = srcResDir.get().asFile
        generateDimensFile(srcRoot, "values", 1f, maxD, dpFormat, spFormat)
        
        // 2. Generate target dimens into buildDir (temporary)
        if (finalTargets.isEmpty()) {
            println("No target screens provided for screen adaptation.")
        }
        
        finalTargets.forEach { screen ->
            val tWidthDp = pxToDp(screen.widthPx, screen.dpi)
            val tHeightDp = pxToDp(screen.heightPx, screen.dpi)
            
            val scale = if (isBaseOnWidth) {
                tWidthDp / dWidthDp
            } else {
                tHeightDp / dHeightDp
            }
            
            val swDp = minOf(tWidthDp, tHeightDp).toInt()
            generateDimensFile(buildResDir, "values-sw${swDp}dp", scale, maxD, dpFormat, spFormat)
        }
        
        println("=".repeat(60))
        println("       SCREEN ADAPTATION PLUGIN - COMPLETED")
        detectedMsgs.forEach { println("       $it") }
        println("=".repeat(60) + "\n")
    }

    private fun detectAllDeviceMetrics(): List<TargetScreen> {
        val devicesOutput = runAdb("devices") ?: return emptyList()
        val serials = devicesOutput.lineSequence()
            .drop(1) // Drop "List of devices attached"
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.endsWith("device") }
            .map { it.substringBefore("\t") }
            .toList()

        val results = mutableListOf<TargetScreen>()
        serials.forEach { serial ->
            val sizeOutput = runAdbForDevice(serial, "shell wm size") ?: return@forEach
            val densityOutput = runAdbForDevice(serial, "shell wm density") ?: return@forEach

            val sizeMatch = Regex("(\\d+)x(\\d+)").find(sizeOutput) ?: return@forEach
            val (w, h) = sizeMatch.destructured
            
            val densityMatch = Regex("(\\d+)").find(densityOutput) ?: return@forEach
            val (dpi) = densityMatch.destructured
            
            val screen = TargetScreenImpl(w.toInt(), h.toInt(), dpi.toInt())
            results.add(screen)
            
            // Log to each device's Logcat
            runAdbForDevice(serial, "shell log -t ScreenAdaptationPlugin \"Detected　ADB device: ${w}x${h} @ ${dpi}dpi\"")
        }
        return results
    }

    private fun logToLogcat(tag: String, msg: String) {
        // This is kept for compatibility but detectAllDeviceMetrics handles it per-device now
    }

    private fun runAdbForDevice(serial: String, cmd: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("adb -s $serial $cmd")
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            if (process.exitValue() == 0) output else null
        } catch (e: Exception) {
            null
        }
    }

    private fun runAdb(cmd: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("adb $cmd")
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            if (process.exitValue() == 0) output else null
        } catch (e: Exception) {
            null
        }
    }
    
    private fun generateDimensFile(resDir: File, dirName: String, scale: Float, maxD: Int, dpFormat: String, spFormat: String) {
        val valuesDir = File(resDir, dirName)
        if (!valuesDir.exists()) {
            valuesDir.mkdirs()
        }
        
        val dimensFile = File(valuesDir, "dimens.xml")
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        sb.append("<resources>\n")
        
        for (i in 1..maxD) {
            val scaledDp = i * scale
            val formattedDp = String.format(Locale.US, "%.2f", scaledDp)
            val name = dpFormat.replace("${"$"}{i}", i.toString()).replace("{i}", i.toString())
            sb.append("    <dimen name=\"$name\">${formattedDp}dp</dimen>\n")
        }
        for (i in 1..maxD) {
            val scaledSp = i * scale
            val formattedSp = String.format(Locale.US, "%.2f", scaledSp)
            val name = spFormat.replace("${"$"}{i}", i.toString()).replace("{i}", i.toString())
            sb.append("    <dimen name=\"$name\">${formattedSp}sp</dimen>\n")
        }
        
        sb.append("</resources>\n")
        dimensFile.writeText(sb.toString())
        println("Generated dimens for $dirName in ${dimensFile.absolutePath}")
    }
}
