import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.Serializable
import java.util.Locale

import java.lang.StringBuilder

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
    
    init {
        baseOn.convention("width")
        maxDp.convention(1000)
    }
    
    fun target(widthPx: Int, heightPx: Int, dpi: Int) {
        targetScreens.add(TargetScreenImpl(widthPx, heightPx, dpi))
    }
}

class ScreenAdaptationPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("screenAdaptation", ScreenAdaptationExtension::class.java)
        
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
        }
        
        project.tasks.matching { it.name == "preBuild" }.configureEach {
            dependsOn(taskProvider)
        }
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
    abstract val maxDp: Property<Int>

    private fun pxToDp(px: Int, dpi: Int): Float = px / (dpi / 160f)

    @TaskAction
    fun generate() {
        val dWidthPx = designWidthPx.get()
        val dHeightPx = designHeightPx.get()
        val dDpi = designDpi.get()
        
        val dWidthDp = pxToDp(dWidthPx, dDpi)
        val dHeightDp = pxToDp(dHeightPx, dDpi)
        
        val isBaseOnWidth = baseOn.get().lowercase() == "width"
        val maxD = maxDp.get()
        
        val resDir = project.file("src/main/res")
        if (!resDir.exists()) {
            resDir.mkdirs()
        }
        
        // Generate for default (design) screen
        generateDimensFile(resDir, "values", 1f, maxD)
        
        // Generate for target screens
        val targets = targetScreens.get()
        if (targets.isEmpty()) {
            println("No target screens provided for screen adaptation.")
        }
        
        targets.forEach { screen ->
            val tWidthDp = pxToDp(screen.widthPx, screen.dpi)
            val tHeightDp = pxToDp(screen.heightPx, screen.dpi)
            
            val scale = if (isBaseOnWidth) {
                tWidthDp / dWidthDp
            } else {
                tHeightDp / dHeightDp
            }
            
            val swDp = minOf(tWidthDp, tHeightDp).toInt()
            generateDimensFile(resDir, "values-sw${swDp}dp", scale, maxD)
        }
    }
    
    private fun generateDimensFile(resDir: File, dirName: String, scale: Float, maxD: Int) {
        val valuesDir = File(resDir, dirName)
        if (!valuesDir.exists()) {
            valuesDir.mkdirs()
        }
        
        val dimensFile = File(valuesDir, "dimens_auto.xml")
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        sb.append("<resources>\n")
        
        for (i in 1..maxD) {
            val scaledDp = i * scale
            // Ensure dot as decimal separator
            val formattedDp = String.format(Locale.US, "%.2f", scaledDp)
            sb.append("    <dimen name=\"dp_$i\">${formattedDp}dp</dimen>\n")
        }
        // Generate sp 
        for (i in 1..maxD) {
            val scaledSp = i * scale
            val formattedSp = String.format(Locale.US, "%.2f", scaledSp)
            sb.append("    <dimen name=\"sp_$i\">${formattedSp}sp</dimen>\n")
        }
        
        sb.append("</resources>\n")
        dimensFile.writeText(sb.toString())
        println("Generated dimens for $dirName in ${dimensFile.absolutePath}")
    }
}
