import java.text.SimpleDateFormat

plugins {
    alias(libs.plugins.architecture.android.application)
    alias(libs.plugins.architecture.android.view)
    alias(libs.plugins.architecture.android.application.jacoco)
    alias(libs.plugins.architecture.hilt)
}

android {
    namespace = "com.lunacattus.app.camera"

    defaultConfig {
        applicationId = "com.lunacattus.app.camera"
        versionCode = 1
        versionName = "1.0"
    }

    applicationVariants.configureEach {
        val variant = this
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())
        variant.outputs.configureEach {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "LunaCamera-${variant.name}-${variant.versionName}-${timestamp}.apk"
        }
    }

    packaging {
        resources.excludes += "META-INF/*.md"
    }
}

dependencies {
    implementation(project(":app:domain"))
    implementation(project(":app:data"))

    implementation("androidx.camera:camera-core:1.5.0-beta02")
    implementation("androidx.camera:camera-camera2:1.5.0-beta02")
    implementation("androidx.camera:camera-lifecycle:1.5.0-beta02")
    implementation("androidx.camera:camera-video:1.5.0-beta02")
    implementation("androidx.camera:camera-view:1.5.0-beta02")
    implementation("androidx.camera:camera-mlkit-vision:1.5.0-beta02")
    implementation("androidx.camera:camera-extensions:1.5.0-beta02")
}