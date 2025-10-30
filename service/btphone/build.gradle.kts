plugins {
    alias(libs.plugins.architecture.android.application)
    alias(libs.plugins.architecture.hilt)
}

android {
    namespace = "com.lunacattus.btphone"

    defaultConfig {
        applicationId = "com.lunacattus.btphone"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {

    compileOnly(files("ext/javalib-11.jar"))
    implementation(project(":logger"))
}

gradle.projectsEvaluated {
    tasks.withType<JavaCompile>().configureEach {
        val originalClasspath = options.bootstrapClasspath?.files ?: emptySet<File>()
        val newFileList = mutableListOf<File>().apply {
            add(project.file("ext/javalib-11.jar"))
            addAll(originalClasspath)
        }
        options.bootstrapClasspath = files(*newFileList.toTypedArray())
    }
}