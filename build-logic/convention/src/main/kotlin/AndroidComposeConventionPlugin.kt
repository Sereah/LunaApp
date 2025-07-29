import com.lunacattus.convention.libs
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = libs.findPlugin("kotlin.compose").get().get().pluginId)
            apply(plugin = libs.findPlugin("kotlin.serialization").get().get().pluginId)

            extensions.getByType<ApplicationExtension>().apply {
                buildFeatures {
                    compose = true
                }

                dependencies {
                    val bom = libs.findLibrary("androidx.compose.bom").get()
                    "implementation"(platform(bom))
                    "implementation"(libs.findLibrary("androidx.compose.ui").get())
                    "implementation"(libs.findLibrary("androidx.compose.ui.graphics").get())
                    "implementation"(libs.findLibrary("androidx.compose.ui.tooling.preview").get())
                    "implementation"(libs.findLibrary("androidx.compose.material3").get())
                    "implementation"(libs.findLibrary("androidx.compose.material.iconsExtended").get())
                    "implementation"(libs.findLibrary("androidx.compose.foundation").get())
                    "implementation"(libs.findLibrary("androidx.compose.foundation.layout").get())
                    "implementation"(libs.findLibrary("androidx.hilt.navigation.compose").get())
                    "implementation"(libs.findLibrary("androidx.compose.animation").get())
                    "implementation"(libs.findLibrary("androidx.activity.compose").get())
                    "implementation"(libs.findLibrary("androidx.navigation.compose").get())

                    "androidTestImplementation"(platform(bom))
                    "androidTestImplementation"(
                        libs.findLibrary("androidx.compose.ui.test.junit4").get()
                    )
                    "androidTestImplementation"(libs.findLibrary("androidx.junit").get())
                    "androidTestImplementation"(libs.findLibrary("androidx.espresso.core").get())
                    "androidTestImplementation"(libs.findLibrary("mockk.android").get())
                    "testImplementation"(libs.findLibrary("robolectric").get())

                    "debugImplementation"(libs.findLibrary("androidx.compose.ui.tooling").get())
                    "debugImplementation"(
                        libs.findLibrary("androidx.compose.ui.test.manifest").get()
                    )
                }
            }
        }
    }
}