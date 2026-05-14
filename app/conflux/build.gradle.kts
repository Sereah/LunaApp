plugins {
    alias(libs.plugins.app.android.application)
    alias(libs.plugins.app.android.application.compose)
    alias(libs.plugins.app.hilt)
}

android {
    namespace = "com.lunacattus.conflux"

    defaultConfig {
        applicationId = "com.lunacattus.app.conflux"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":ui-design"))
    implementation(project(":common"))
    implementation(project(":network"))
    implementation(project(":record"))
    implementation(project(":logger"))
    debugImplementation(libs.leakcanary.android)
    implementation(libs.haze.android)
    implementation(libs.haze.material)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)
    implementation(libs.coil.compose)
    implementation(libs.gson)
//    implementation("com.google.mediapipe:tasks-genai:0.10.27")
//    implementation("com.google.ai.edge.litertlm:litertlm-android:latest.release")
    implementation("com.google.ai.edge.litertlm:litertlm-android:0.10.0")
}