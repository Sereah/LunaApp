plugins {
    alias(libs.plugins.architecture.android.library)
    alias(libs.plugins.architecture.android.library.compose)
    alias(libs.plugins.architecture.android.library.view)
}

android {
    namespace = "com.lunacattus.ui_design"
}

dependencies {
    implementation(project(":common"))

    implementation(libs.coil.compose)
    implementation(libs.haze.android)
}