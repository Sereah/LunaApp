plugins {
    alias(libs.plugins.app.android.application)
    alias(libs.plugins.app.android.application.compose)
    alias(libs.plugins.app.hilt)
    alias(libs.plugins.framework.jar)
}

frameworkJar {
    version = "13"
}

android {
    namespace = "com.lunacattus.mutilscreen"
    defaultConfig {
        applicationId = "com.lunacattus.mutilscreen"
        versionCode = 1
        versionName = "1.0"
    }

    applicationVariants.configureEach {
        val variant = this
        variant.outputs.configureEach {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "MutilScreenApp-${variant.name}-${variant.versionName}.apk"
        }
    }
}

dependencies {
    implementation(project(":ui-design"))
    implementation(project(":common"))
    implementation(project(":logger"))
    implementation(libs.haze.android)
    implementation(libs.haze.material)
}