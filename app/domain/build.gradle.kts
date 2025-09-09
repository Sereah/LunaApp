plugins {
    alias(libs.plugins.architecture.android.library)
    alias(libs.plugins.architecture.android.library.jacoco)
    alias(libs.plugins.architecture.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.lunacattus.app.domain"
}

dependencies {
    api(project(":common"))
    api(project(":logger"))
    api(project(":app:base"))
    api("androidx.paging:paging-runtime:3.3.6")
}