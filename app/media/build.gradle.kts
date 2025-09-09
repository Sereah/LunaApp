import java.text.SimpleDateFormat

plugins {
    alias(libs.plugins.architecture.android.application)
    alias(libs.plugins.architecture.android.application.compose)
    alias(libs.plugins.architecture.android.application.jacoco)
    alias(libs.plugins.architecture.hilt)
}

android {
    namespace = "com.lunacattus.app.media"

    defaultConfig {
        applicationId = "com.lunacattus.app.media"
        versionCode = 1
        versionName = "1.0"
    }

    applicationVariants.configureEach {
        val variant = this
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())
        variant.outputs.configureEach {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "Media-${variant.name}-${variant.versionName}-${timestamp}.apk"
        }
    }

    packaging {
        resources.excludes += "META-INF/*.md"
    }
}

dependencies {
    implementation(project(":app:domain"))
    implementation(project(":app:data"))
    implementation(project(":speech"))

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.exoplayer.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.video)
    implementation(libs.haze.android)
}