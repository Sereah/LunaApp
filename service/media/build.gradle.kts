import java.text.SimpleDateFormat

plugins {
    alias(libs.plugins.architecture.android.application)
    alias(libs.plugins.architecture.hilt)
}

android {
    namespace = "com.lunacattus.service.media"

    defaultConfig {
        applicationId = "com.lunacattus.service.media"
        versionCode = 1
        versionName = "1.0"
    }


    applicationVariants.configureEach {
        val variant = this
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())
        variant.outputs.configureEach {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "MediaService-${variant.name}-${variant.versionName}-${timestamp}.apk"
        }
    }

    packaging {
        resources.excludes += "META-INF/*.md"
    }
}

dependencies {
    implementation(project(":feature:speech"))
    implementation(project(":logger"))
    implementation(project(":common"))

//    debugImplementation(libs.leakcanary.android)
}