plugins {
    alias(libs.plugins.app.android.application)
    alias(libs.plugins.app.android.application.compose)
    alias(libs.plugins.app.android.application.jacoco)
    alias(libs.plugins.app.hilt)
}

android {
    namespace = "com.lunacattus.nav3test"

    defaultConfig {
        applicationId = "com.lunacattus.nav3test"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":ui-design"))
    implementation(project(":common"))
    implementation(project(":logger"))
}