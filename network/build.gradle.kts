import com.android.build.gradle.tasks.BundleAar

plugins {
    alias(libs.plugins.app.android.library)
    alias(libs.plugins.app.hilt)
}

android {
    namespace = "com.lunacattus.network"
}

val version = "1.0.0"

tasks.withType<BundleAar>().configureEach {
    val buildType = if (name.contains("release", ignoreCase = true)) "release" else "debug"
    val versionName = version

    archiveFileName.set("network-v${versionName}-${buildType}.aar")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":logger"))
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
