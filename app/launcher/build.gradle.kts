import java.text.SimpleDateFormat

plugins {
    alias(libs.plugins.architecture.android.application)
    alias(libs.plugins.architecture.android.application.compose)
    alias(libs.plugins.architecture.android.application.jacoco)
    alias(libs.plugins.architecture.hilt)
}

android {
    namespace = "com.lunacattus.launcher"

    defaultConfig {
        applicationId = "com.lunacattus.launcher"
        versionCode = 1
        versionName = "1.0"
    }

    applicationVariants.configureEach {
        val variant = this
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())
        variant.outputs.configureEach {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "LunaLauncher-${variant.name}-${variant.versionName}-${timestamp}.apk"
        }
    }
}

dependencies {
    implementation(project(":app:domain"))
    implementation(project(":app:data"))
    implementation(project(":ui-design"))
}