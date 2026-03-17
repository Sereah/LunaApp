import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.app.android.application)
    alias(libs.plugins.app.android.application.compose)
    alias(libs.plugins.app.android.application.jacoco)
    alias(libs.plugins.app.hilt)
    alias(libs.plugins.framework.jar)
}

frameworkJar {
    version = "13"
}

android {
    namespace = "com.lunacattus.connection"

    defaultConfig {
        applicationId = "com.lunacattus.app.connection"
        versionCode = 1
        versionName = "1.0"
    }
}

base {
    val timestamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
    archivesName.set("Connection-${android.defaultConfig.versionName}-${timestamp}")
}

dependencies {
    implementation(project(":ui-design"))
    implementation(project(":common"))
    implementation(project(":logger"))
    implementation(libs.haze.android)
    implementation(libs.haze.material)
}