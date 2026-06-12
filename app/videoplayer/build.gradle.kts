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

}

dependencies {
    implementation(libs.common)
    implementation(libs.logger)
    implementation(project(":ui-design"))

    implementation(libs.gson)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.exoplayer.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.video)
    implementation(libs.haze.android)
}