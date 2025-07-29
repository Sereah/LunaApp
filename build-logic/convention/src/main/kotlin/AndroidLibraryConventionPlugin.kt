import com.lunacattus.convention.configureKotlinAndroid
import com.lunacattus.convention.libs
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = libs.findPlugin("android.library").get().get().pluginId)
            apply(plugin = libs.findPlugin("kotlin.android").get().get().pluginId)

            extensions.configure<LibraryExtension> {

                buildTypes {
                    release {
                        isMinifyEnabled = false
                    }
                    debug {
                        isMinifyEnabled = false
                    }
                }

                dependencies {
                    "testImplementation"(libs.findLibrary("junit").get())
                    "testImplementation"(libs.findLibrary("mockk").get())
                    "testImplementation"(libs.findLibrary("coroutines.test").get())
                }

                configureKotlinAndroid(this)
            }
        }
    }
}