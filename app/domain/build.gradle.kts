plugins {
    alias(libs.plugins.app.android.library)
    alias(libs.plugins.app.android.library.jacoco)
    alias(libs.plugins.app.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.lunacattus.app.domain"
}

dependencies {
    api(project(":common"))
    api(project(":logger"))
}