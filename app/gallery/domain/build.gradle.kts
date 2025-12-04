plugins {
    alias(libs.plugins.app.android.library)
    alias(libs.plugins.app.android.library.jacoco)
    alias(libs.plugins.app.hilt)
}

android {
    namespace = "com.lunacattus.app.domain"
}

dependencies {
    api(project(":common"))
    api(project(":logger"))
}