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
            id = libs.plugins.app.android.application.asProvider().get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }

        register("androidLibrary") {
            id = libs.plugins.app.android.library.asProvider().get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }

        register("androidApplicationCompose") {
            id = libs.plugins.app.android.application.compose.get().pluginId
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }

        register("androidLibraryCompose") {
            id = libs.plugins.app.android.library.compose.get().pluginId
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }

        register("androidApplicationView") {
            id = libs.plugins.app.android.application.view.get().pluginId
            implementationClass = "AndroidApplicationViewConventionPlugin"
        }

        register("androidLibraryView") {
            id = libs.plugins.app.android.library.view.get().pluginId
            implementationClass = "AndroidLibraryViewConventionPlugin"
        }

        register("androidApplicationJacoco") {
            id = libs.plugins.app.android.application.jacoco.get().pluginId
            implementationClass = "AndroidApplicationJacocoConventionPlugin"
        }

        register("androidLibraryJacoco") {
            id = libs.plugins.app.android.library.jacoco.get().pluginId
            implementationClass = "AndroidLibraryJacocoConventionPlugin"
        }

        register("hilt") {
            id = libs.plugins.app.hilt.get().pluginId
            implementationClass = "HiltConventionPlugin"
        }

        register("room") {
            id = libs.plugins.app.android.room.get().pluginId
            implementationClass = "AndroidRoomConventionPlugin"
        }

        register("frameworkJar") {
            id = libs.plugins.framework.jar.get().pluginId
            implementationClass = "FrameworkJarConventionPlugin"
        }
    }
}