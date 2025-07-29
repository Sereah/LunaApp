import com.lunacattus.convention.libs
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidViewConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            extensions.getByType<ApplicationExtension>().apply {
                buildFeatures {
                    viewBinding = true
                }

                dependencies {
                    "implementation"(libs.findLibrary("androidx.appcompat").get())
                    "implementation"(libs.findLibrary("material").get())
                    "implementation"(libs.findLibrary("hilt.navigation.fragment").get())
                    "implementation"(libs.findLibrary("androidx.navigation.fragment.ktx").get())
                    "implementation"(libs.findLibrary("androidx.navigation.ui.ktx").get())
                    "implementation"(libs.findLibrary("androidx.activity").get())
                    "implementation"(libs.findLibrary("androidx.constraintlayout").get())

                    "androidTestImplementation"(libs.findLibrary("androidx.junit").get())
                    "androidTestImplementation"(libs.findLibrary("androidx.espresso.core").get())
                    "androidTestImplementation"(libs.findLibrary("mockk.android").get())
                    "testImplementation"(libs.findLibrary("robolectric").get())
                    "testImplementation"(libs.findLibrary("junit").get())
                    "testImplementation"(libs.findLibrary("mockk").get())
                    "testImplementation"(libs.findLibrary("coroutines.test").get())
                }
            }
        }
    }
}