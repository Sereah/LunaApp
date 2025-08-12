import java.text.SimpleDateFormat

plugins {
    alias(libs.plugins.architecture.android.application)
    alias(libs.plugins.architecture.android.application.compose)
    alias(libs.plugins.architecture.hilt)
}

android {
    namespace = "com.lunacattus.app.galleray"

    defaultConfig {
        applicationId = "com.lunacattus.app.galleray"
        versionCode = 1
        versionName = "1.0"
    }

    applicationVariants.configureEach {
        val variant = this
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())
        variant.outputs.configureEach {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "Galleray-${variant.name}-${variant.versionName}-${timestamp}.apk"
        }
    }

    packaging {
        resources.excludes += "META-INF/*.md"
    }
}

dependencies {
    implementation(project(":app:domain"))
    implementation(project(":app:data"))
    implementation(project(":app:base"))
}