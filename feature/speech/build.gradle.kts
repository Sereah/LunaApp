plugins {
    alias(libs.plugins.architecture.android.library)
    alias(libs.plugins.architecture.hilt)
}

android {
    namespace = "com.lunacattus.speech"

    defaultConfig {
        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf("arm64-v8a")
        }
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":logger"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    implementation(libs.okhttp)
    implementation(libs.gson)
}