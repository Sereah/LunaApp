plugins {
    alias(libs.plugins.app.android.application)
    alias(libs.plugins.app.android.application.jacoco)
    alias(libs.plugins.app.hilt)
}

android {
    namespace = "com.lunacattus.voice.vehiclecontrol"

    defaultConfig {
        applicationId = "com.lunacattus.voice.vehiclecontrol"
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        aidl = true
    }
}

dependencies {
    implementation(libs.common)
    implementation(libs.logger)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-service:2.10.0")

    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.mockk.android)
}