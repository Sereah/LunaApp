import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.artifact.ArtifactTransformationRequest
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val VERSION_COMPONENT_BASE = 100
private const val MIN_VERSION_COMPONENT = 0
private const val MAX_VERSION_COMPONENT = VERSION_COMPONENT_BASE - 1

abstract class AppVersionExtension {
    var major: Int = MIN_VERSION_COMPONENT
    var minor: Int = MIN_VERSION_COMPONENT
    var patch: Int = MIN_VERSION_COMPONENT
}

class AppVersionConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val versionExtension = extensions.create<AppVersionExtension>("appVersion")

            pluginManager.withPlugin("com.android.application") {
                val androidComponents = extensions.getByType<ApplicationAndroidComponentsExtension>()
                var versionName = ""

                androidComponents.finalizeDsl { androidExtension ->
                    versionName = configureApplicationVersion(
                        androidExtension,
                        versionExtension,
                    )
                }

                androidComponents.onVariants { variant ->
                    val outputFileName = "${project.name}-${variant.name.toKebabCase()}-" +
                        "$versionName.apk"
                    val renameTask = tasks.register<RenameApkTask>(
                        "rename${variant.name.replaceFirstChar { it.uppercase() }}Apk",
                    ) {
                        apkFileName.set(outputFileName)
                        outputDirectory.set(
                            layout.buildDirectory.dir("outputs/apk/${variant.name}"),
                        )
                    }
                    variant.artifacts.use(renameTask)
                        .wiredWithDirectories(
                            RenameApkTask::inputDirectory,
                            RenameApkTask::outputDirectory,
                        )
                        .toTransformMany(SingleArtifact.APK)
                        .also { transformationRequest ->
                            renameTask.configure {
                                this.transformationRequest.set(transformationRequest)
                            }
                        }
                }
            }
        }
    }
}

private fun Project.configureApplicationVersion(
    androidExtension: ApplicationExtension,
    versionExtension: AppVersionExtension,
): String {
    val versionComponents = listOf(
        "major" to versionExtension.major,
        "minor" to versionExtension.minor,
        "patch" to versionExtension.patch,
    )
    versionComponents.forEach { (name, value) ->
        if (value !in MIN_VERSION_COMPONENT..MAX_VERSION_COMPONENT) {
            throw GradleException(
                "appVersion.$name must be between $MIN_VERSION_COMPONENT and " +
                    "$MAX_VERSION_COMPONENT, but was $value",
            )
        }
    }

    val computedVersionCode = versionExtension.major * VERSION_COMPONENT_BASE * VERSION_COMPONENT_BASE +
        versionExtension.minor * VERSION_COMPONENT_BASE +
        versionExtension.patch
    val computedVersionName = buildString {
        append(versionExtension.major)
        append('.')
        append(versionExtension.minor)
        append('.')
        append(versionExtension.patch)
        append('-')
        append(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE))
    }

    androidExtension.defaultConfig {
        versionCode = computedVersionCode
        versionName = computedVersionName
    }

    return computedVersionName
}

private fun String.toKebabCase(): String = replace(
    Regex("([a-z])([A-Z])"),
    "$1-$2",
).lowercase(Locale.ROOT)

abstract class RenameApkTask : DefaultTask() {
    @get:InputDirectory
    abstract val inputDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val apkFileName: Property<String>

    @get:org.gradle.api.tasks.Internal
    abstract val transformationRequest: Property<ArtifactTransformationRequest<RenameApkTask>>

    @TaskAction
    fun renameApkFiles() {
        transformationRequest.get().submit(this) { builtArtifact ->
            val inputFile = File(builtArtifact.outputFile)
            val outputFile = outputDirectory.get().asFile.resolve(apkFileName.get())
            outputFile.parentFile.mkdirs()
            inputFile.copyTo(outputFile, overwrite = true)
            outputFile
        }
    }
}
