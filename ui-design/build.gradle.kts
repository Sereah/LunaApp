plugins {
    alias(libs.plugins.app.android.library)
    alias(libs.plugins.app.android.library.compose)
    alias(libs.plugins.app.android.library.view)
}

android {
    namespace = "com.lunacattus.ui_design"
}

dependencies {
    implementation(project(":common"))

    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.haze.android)
}