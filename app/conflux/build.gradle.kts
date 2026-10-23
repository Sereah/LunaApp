plugins {
    alias(libs.plugins.app.android.application)
    alias(libs.plugins.app.android.application.compose)
    alias(libs.plugins.app.hilt)
    alias(libs.plugins.app.version)
}

appVersion {
    major = 1
    minor = 0
    patch = 1
}

android {
    namespace = "com.lunacattus.conflux"

    defaultConfig {
        applicationId = "com.lunacattus.app.conflux"
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(project(":ui-design"))
    implementation(libs.common)
    implementation(libs.network)
    implementation(project(":record"))
    implementation(libs.logger)
    debugImplementation(libs.leakcanary.android)
    implementation(libs.haze.android)
    implementation(libs.haze.material)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.coil.compose)
    implementation(libs.gson)
    implementation("com.google.ai.edge.litertlm:litertlm-android:0.10.0")
    implementation("com.lunacattus.android:llm:1.0.2")
    implementation("com.lunacattus.android:permission-compose:1.0.0")
}