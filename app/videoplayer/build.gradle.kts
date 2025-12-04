import java.text.SimpleDateFormat

plugins {
    alias(libs.plugins.app.android.application)
    alias(libs.plugins.app.android.application.compose)
    alias(libs.plugins.app.android.application.jacoco)
    alias(libs.plugins.app.hilt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.app.android.room)
}

android {
    namespace = "com.lunacattus.app.player"

    defaultConfig {
        applicationId = "com.lunacattus.app.player"
        versionCode = 1
        versionName = "1.0"
    }

    applicationVariants.configureEach {
        val variant = this
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())
        variant.outputs.configureEach {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "Player-${variant.name}-${variant.versionName}-${timestamp}.apk"
        }
    }

}

dependencies {
    implementation(project(":common"))
    implementation(project(":logger"))
    implementation(project(":ui-design"))

    implementation(libs.gson)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.exoplayer.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.video)
    implementation(libs.haze.android)
}