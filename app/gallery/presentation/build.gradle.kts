import java.text.SimpleDateFormat

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

    applicationVariants.configureEach {
        val variant = this
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())
        variant.outputs.configureEach {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "Galleray-${variant.name}-${variant.versionName}-${timestamp}.apk"
        }
    }
}

dependencies {
    implementation(project(":app:gallery:domain"))
    implementation(project(":app:gallery:data"))
    implementation(project(":ui-design"))
    implementation(libs.glide)
    implementation(libs.permissionX)
}