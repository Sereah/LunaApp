plugins {
    alias(libs.plugins.app.android.application)
    alias(libs.plugins.app.android.application.compose)
    alias(libs.plugins.app.hilt)
}

android {
    namespace = "com.lunacattus.conflux"

    defaultConfig {
        applicationId = "com.lunacattus.app.conflux"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":ui-design"))
    implementation(project(":common"))
    implementation(project(":logger"))
    implementation(project(":feature:voice"))
    implementation(libs.haze.android)
    implementation(libs.haze.material)
}