plugins {
    alias(libs.plugins.app.android.library)
    alias(libs.plugins.app.hilt)
}

android {
    namespace = "com.lunacattus.common"
}

dependencies {
    implementation(project(":logger"))
    implementation(libs.kotlinx.coroutines.core)
}