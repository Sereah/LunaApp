plugins {
    alias(libs.plugins.app.android.library)
    alias(libs.plugins.app.android.library.jacoco)
    alias(libs.plugins.app.hilt)
}

android {
    namespace = "com.lunacattus.app.domain"
}

dependencies {
    api(libs.common)
    api(libs.logger)
}