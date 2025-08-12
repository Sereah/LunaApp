import java.text.SimpleDateFormat

plugins {
    alias(libs.plugins.architecture.android.application)
    alias(libs.plugins.architecture.android.view)
    alias(libs.plugins.architecture.hilt)
    alias(libs.plugins.kotlin.android)
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

    packaging {
        resources.excludes += "META-INF/*.md"
    }
}

dependencies {
    implementation(project(":app:domain"))
    implementation(project(":app:data"))
    implementation(project(":app:base"))
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
}