import java.text.SimpleDateFormat

plugins {
    alias(libs.plugins.architecture.android.application)
    alias(libs.plugins.architecture.hilt)
    alias(libs.plugins.architecture.android.application.compose)
}

android {
    namespace = "com.lunacattus.service.media"

    defaultConfig {
        applicationId = "com.lunacattus.service.media"
        versionCode = 1
        versionName = "1.0"
    }


    applicationVariants.configureEach {
        val variant = this
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())
        variant.outputs.configureEach {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "MediaService-${variant.name}-${variant.versionName}-${timestamp}.apk"
        }
    }
}

dependencies {
    implementation(project(":feature:speech"))
    implementation(project(":logger"))
    implementation(project(":common"))

//    debugImplementation(libs.leakcanary.android)

    compileOnly(files("ext/javalib-11.jar"))
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