import com.android.build.api.dsl.ApplicationExtension
import com.lunacattus.convention.configureAndroidCompose
import com.lunacattus.convention.configureTest
import com.lunacattus.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType

class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = libs.findPlugin("kotlin.compose").get().get().pluginId)
            apply(plugin = libs.findPlugin("kotlin.serialization").get().get().pluginId)

            val extensions = extensions.getByType<ApplicationExtension>()
            configureAndroidCompose(extensions)
            configureTest(extensions, true)
        }
    }
}