plugins {
    alias(libs.plugins.app.android.application)
    alias(libs.plugins.app.android.application.compose)
    alias(libs.plugins.app.hilt)
}

android {
    namespace = "com.lunacattus.app.statemachinedemo"

    defaultConfig {
        applicationId = "com.lunacattus.app.statemachinedemo"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":logger"))
}