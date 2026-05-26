plugins {
    alias(libs.plugins.app.android.library)
}

val libVersion = "1.0.0"

android {
    namespace = "com.lunacattus.statemachine"
}

dependencies {
    implementation(project(":logger"))
    implementation(libs.kotlinx.coroutines.core)
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    archiveBaseName.set(project.name)
    archiveVersion.set(libVersion)
    from(android.sourceSets["main"].java.srcDirs)
}

tasks.register("packageAar") {
    group = "publishing"
    description = "将 AAR 和源码 JAR 打包到 build/dist/${project.name}-${libVersion}/"

    dependsOn("assembleRelease", "sourcesJar")

    doLast {
        val distDir = layout.buildDirectory.dir("dist/${project.name}-${libVersion}").get().asFile
        distDir.mkdirs()

        copy {
            from(layout.buildDirectory.file("outputs/aar/${project.name}-release.aar"))
            into(distDir)
            rename { "${project.name}-${libVersion}.aar" }
        }

        copy {
            from(tasks.named("sourcesJar").get().outputs.files.singleFile)
            into(distDir)
            rename { "${project.name}-${libVersion}-sources.jar" }
        }

        logger.lifecycle("=== AAR 打包完成 ===")
        logger.lifecycle("输出目录: ${distDir.absolutePath}")
        logger.lifecycle("  ${project.name}-${libVersion}.aar")
        logger.lifecycle("  ${project.name}-${libVersion}-sources.jar")
    }
}
