import com.android.build.api.dsl.LibraryExtension
import com.lunacattus.convention.configureAndroidView
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryViewConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extensions = extensions.getByType<LibraryExtension>()
            configureAndroidView(extensions)
        }
    }
}