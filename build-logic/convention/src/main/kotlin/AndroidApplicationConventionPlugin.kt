import com.android.build.api.dsl.ApplicationExtension
import com.lunacattus.convention.configureKotlinAndroid
import com.lunacattus.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = libs.findPlugin("android.application").get().get().pluginId)
            apply(plugin = libs.findPlugin("kotlin.android").get().get().pluginId)

            extensions.configure<ApplicationExtension> {
                defaultConfig {
                    targetSdk = 35
                    testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
                }

                signingConfigs {
                    create("platform_system") {
                        storeFile = file("$rootDir/keystore/platform_system.jks")
                        storePassword = "123456"
                        keyAlias = "system"
                        keyPassword = "123456"
                    }
                }

                buildTypes {
                    release {
                        isMinifyEnabled = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                        signingConfig = null
                    }
                    debug {
                        isMinifyEnabled = false
                        signingConfig = null
                    }
                }

                flavorDimensions += "platform"
                productFlavors {
                    create("app") {
                        dimension = "platform"
                        signingConfig = signingConfigs.getByName("debug")
                    }
                    create("system") {
                        dimension = "platform"
                        signingConfig = signingConfigs.getByName("platform_system")
                    }
                }

                dependencies {
                    "implementation"(libs.findLibrary("androidx.core.ktx").get())
                }

                configureKotlinAndroid(this)
            }
        }
    }
}