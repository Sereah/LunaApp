import com.android.build.api.dsl.LibraryExtension
import com.lunacattus.convention.configureAndroidCompose
import com.lunacattus.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = libs.findPlugin("kotlin.compose").get().get().pluginId)

            val extensions = extensions.getByType<LibraryExtension>()
            configureAndroidCompose(extensions)
        }
    }
}