import java.text.SimpleDateFormat

plugins {
    alias(libs.plugins.architecture.android.application)
    alias(libs.plugins.architecture.android.view)
    alias(libs.plugins.architecture.android.application.jacoco)
    alias(libs.plugins.architecture.hilt)
}

android {
    namespace = "com.lunacattus.app.presentation.view"

    defaultConfig {
        applicationId = "com.lunacattus.app.player.view"
        versionCode = 1
        versionName = "1.0"
    }

    applicationVariants.configureEach {
        val variant = this
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())
        variant.outputs.configureEach {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "Player-${variant.name}-${variant.versionName}-${timestamp}.apk"
        }
    }

    packaging {
        resources.excludes += "META-INF/*.md"
    }
}

dependencies {
    implementation(project(":app:domain"))
    implementation(project(":app:data"))

    implementation("androidx.media3:media3-exoplayer:1.7.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.7.1")
    implementation("androidx.media3:media3-ui:1.7.1")
}