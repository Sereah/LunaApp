plugins {
    alias(libs.plugins.architecture.android.library)
    alias(libs.plugins.architecture.android.library.jacoco)
    alias(libs.plugins.architecture.hilt)
    alias(libs.plugins.architecture.android.room)
}

android {
    namespace = "com.lunacattus.app.data"
}

dependencies {
    implementation(project(":app:domain"))

    implementation(
        fileTree(
            mapOf(
                "dir" to "libs",
                "include" to listOf("*.jar", "*.aar")
            )
        )
    )

    compileOnly(
        fileTree(
            mapOf(
                "dir" to "ext",
                "include" to listOf("*.jar", "*.aar")
            )
        )
    )
}