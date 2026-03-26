import com.android.build.api.dsl.ApplicationExtension
import com.lunacattus.convention.configureAndroidView
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidApplicationViewConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extensions = extensions.getByType<ApplicationExtension>()
            configureAndroidView(extensions)
        }
    }
}