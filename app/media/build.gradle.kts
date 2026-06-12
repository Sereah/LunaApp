plugins {
    alias(libs.plugins.app.android.application)
    alias(libs.plugins.app.android.application.view)
    alias(libs.plugins.app.hilt)
    alias(libs.plugins.framework.jar)
}

frameworkJar {
    version = "13"
    custom = true
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
    implementation(libs.common)
    implementation(libs.logger)
    implementation(project(":ui-design"))
    implementation(libs.glide)
    implementation(libs.permissionX)
}