import com.lunacattus.convention.libs
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = libs.findPlugin("ksp").get().get().pluginId)

            dependencies {
                "ksp"(libs.findLibrary("hilt.compiler").get())
            }

            /** Add support for Jvm Module */
            pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
                dependencies {
                    "implementation"(libs.findLibrary("hilt.core").get())
                }
            }

            /** Add support for Android modules, based on [AndroidBasePlugin] */
            pluginManager.withPlugin("com.android.base") {
                apply(plugin = libs.findPlugin("hilt.android").get().get().pluginId)
                dependencies {
                    "implementation"(libs.findLibrary("hilt.android").get())
                }
            }
        }
    }
}