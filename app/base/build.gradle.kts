plugins {
    alias(libs.plugins.architecture.android.library)
    alias(libs.plugins.architecture.android.library.compose)
}

android {
    namespace = "com.lunacattus.app.base"

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.coil.compose)
    implementation(libs.coil.video)
    implementation(libs.haze.android)
}