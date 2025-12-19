import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// 1. 定义一个 extension class 来持有版本信息
open class FrameworkJarExtension {
    var version: String? = null
}

class FrameworkJarConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // 2. 在项目中创建 extension，命名为 "frameworkJar"
        val extension = project.extensions.create<FrameworkJarExtension>("frameworkJar")

        // 3. 在项目评估后，使用配置的版本
        project.afterEvaluate {
            val frameworkVersion = extension.version ?: "12" // 如果未指定，则默认为版本 "12"
            val frameworkJarFile = rootProject.file("frameworkLibs/framework-$frameworkVersion.jar")

            require(frameworkJarFile.exists()) {
                "Framework jar not found at: ${frameworkJarFile.path}"
            }

            val dependencyFiles = mutableListOf(frameworkJarFile)

            if ((frameworkVersion.toIntOrNull() ?: 0) >= 13) {
                val bluetoothJarFile = rootProject.file("frameworkLibs/bluetooth-$frameworkVersion.jar")
                require(bluetoothJarFile.exists()) {
                    "Bluetooth jar not found at: ${bluetoothJarFile.path}"
                }
                dependencyFiles.add(bluetoothJarFile)
            }

            dependencies {
                "compileOnly"(files(dependencyFiles))
                "ksp"(files(dependencyFiles))
            }
            tasks.withType<JavaCompile>().configureEach {
                val originalClasspath = options.bootstrapClasspath?.files ?: emptySet()
                options.bootstrapClasspath = files(dependencyFiles, originalClasspath)
            }
            tasks.withType<KotlinCompile>().configureEach {
                val currentLibraries = libraries
                libraries.setFrom(dependencyFiles, currentLibraries)
            }
        }
    }
}