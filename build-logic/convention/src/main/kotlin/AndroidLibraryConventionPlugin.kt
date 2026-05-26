import com.android.build.api.dsl.LibraryExtension
import com.lunacattus.convention.configureKotlinAndroid
import com.lunacattus.convention.configureTest
import com.lunacattus.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = libs.findPlugin("android.library").get().get().pluginId)

            extensions.configure<LibraryExtension> {

                buildTypes {
                    release {
                        isMinifyEnabled = true
                    }
                    debug {
                        isMinifyEnabled = false
                    }
                }

                packaging {
                    resources.excludes += "META-INF/*.md"
                }

                configureKotlinAndroid(this)
                configureTest(this)
            }
        }
    }
}