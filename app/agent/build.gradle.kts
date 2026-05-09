plugins {
    alias(libs.plugins.app.android.application)
    alias(libs.plugins.app.android.application.compose)
    alias(libs.plugins.app.hilt)
}

android {
    namespace = "com.lunacattus.agent"

    defaultConfig {
        applicationId = "com.lunacattus.agent"
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += "arm64-v8a"
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(project(":ui-design"))
    implementation(project(":common"))
    implementation(project(":logger"))
    debugImplementation(libs.leakcanary.android)
    implementation(libs.haze.android)
    implementation(libs.haze.material)
    implementation(libs.coil.compose)
}