plugins {
    alias(libs.plugins.app.android.library)
    alias(libs.plugins.app.android.library.jacoco)
    alias(libs.plugins.app.hilt)
    alias(libs.plugins.app.android.room)
    alias(libs.plugins.framework.jar)
}

frameworkJar {
    version = "12"
}

android {
    namespace = "com.lunacattus.app.data"
}

dependencies {
    implementation(libs.gson)

    implementation(project(":app:domain"))

    implementation(
        fileTree(
            mapOf(
                "dir" to "libs",
                "include" to listOf("*.jar", "*.aar")
            )
        )
    )
}