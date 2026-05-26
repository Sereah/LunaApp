plugins {
    alias(libs.plugins.app.android.application)
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
    implementation(project(":logger"))
    implementation(files("libs/statemachine-1.0.0.aar"))
}