plugins {
    `kotlin-dsl`
}

group = "com.lunacattus.plugins"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("screenAdaptation") {
            id = "com.lunacattus.screen-adaptation"
            implementationClass = "com.lunacattus.plugin.ScreenAdaptationPlugin"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
