import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.app.android.application)
    alias(libs.plugins.app.android.application.view)
    alias(libs.plugins.app.hilt)
}

android {
    namespace = "com.lunacattus.app.gallery"

    defaultConfig {
        applicationId = "com.lunacattus.app.gallery"
        versionCode = 1
        versionName = "1.0"
    }
}

base {
    val timestamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
    archivesName.set("Galleray-${android.defaultConfig.versionName}-${timestamp}")
}

dependencies {
    implementation(project(":app:gallery:domain"))
    implementation(project(":app:gallery:data"))
    implementation(project(":ui-design"))
    implementation(libs.glide)
    implementation(libs.permissionX)
}