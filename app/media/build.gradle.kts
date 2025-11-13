plugins {
    alias(libs.plugins.app.android.application)
    alias(libs.plugins.app.android.application.view)
    alias(libs.plugins.app.hilt)
}

android {
    namespace = "com.lunacattus.app.media"

    defaultConfig {
        applicationId = "com.lunacattus.app.media"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":app:domain"))
    implementation(project(":app:data"))
    implementation(project(":ui-design"))
    implementation(libs.glide)
    implementation(libs.permissionX)
}