import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.lunacattus.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.architecture.android.application.asProvider().get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }

        register("androidLibrary") {
            id = libs.plugins.architecture.android.library.asProvider().get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }

        register("androidCompose") {
            id = libs.plugins.architecture.android.compose.get().pluginId
            implementationClass = "AndroidComposeConventionPlugin"
        }

        register("androidView") {
            id = libs.plugins.architecture.android.view.get().pluginId
            implementationClass = "AndroidViewConventionPlugin"
        }

        register("androidApplicationJacoco") {
            id = libs.plugins.architecture.android.application.jacoco.get().pluginId
            implementationClass = "AndroidApplicationJacocoConventionPlugin"
        }

        register("androidLibraryJacoco") {
            id = libs.plugins.architecture.android.library.jacoco.get().pluginId
            implementationClass = "AndroidLibraryJacocoConventionPlugin"
        }

        register("hilt") {
            id = libs.plugins.architecture.hilt.get().pluginId
            implementationClass = "HiltConventionPlugin"
        }

        register("room") {
            id = libs.plugins.architecture.android.room.get().pluginId
            implementationClass = "AndroidRoomConventionPlugin"
        }
    }
}