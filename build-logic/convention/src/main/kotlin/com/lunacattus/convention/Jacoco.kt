package com.lunacattus.convention

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.api.variant.SourceDirectories
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

private val coverageExclusions = listOf(
    // Android
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/androidx/**",
    "**/databinding*",
    // Kotlin
    "**/*$*",                   // 所有包含 $ 的方法
    "**/*\$default*",            // 默认参数生成的方法
    "**/*\$lambda*",             // lambda 表达式
    "**/*\$WhenMappings*",       // when 表达式映射
    "**/*_$*",                   // 属性访问器
    // hilt
    "**/*Module.*",
    "**/*_Hilt*.class",
    "**/Hilt_*.class",
    "**/hilt_aggregated_deps",
    "**/dagger",
    "**/di/**",
    // room
    "**/*Database.*",
    "**/*Dao_Impl*",
    "**/*Database_Impl*",
    "**/room/**",
    // model entity
    "**/entity/**",
    "**/model/**",
)

internal fun Project.configureJacoco(
    androidComponentsExtension: AndroidComponentsExtension<*, *, *>,
) {
    configure<JacocoPluginExtension> {
        toolVersion = libs.findVersion("jacoco").get().toString()
    }

    androidComponentsExtension.onVariants { variant ->
        val myObjFactory = project.objects
        val buildDir = layout.buildDirectory.get().asFile
        val allJars: ListProperty<RegularFile> = myObjFactory.listProperty(RegularFile::class.java)
        val allDirectories: ListProperty<Directory> =
            myObjFactory.listProperty(Directory::class.java)
        val reportTask =
            tasks.register(
                "create${variant.name.capitalize()}JacocoReport",
                JacocoReport::class,
            ) {
                group = "Jacoco"
                description = "Generates Jacoco coverage report for ${variant.name} variant."

                dependsOn("test${variant.name.capitalize()}UnitTest")
                if (androidComponentsExtension is ApplicationAndroidComponentsExtension) {
                    dependsOn("connected${variant.name.capitalize()}AndroidTest")
                }

                classDirectories.setFrom(
                    allJars,
                    allDirectories.map { dirs ->
                        dirs.map { dir ->
                            myObjFactory.fileTree().setDir(dir).exclude(coverageExclusions)
                        }
                    },
                )
                reports {
                    html.required = true
                }

                fun SourceDirectories.Flat?.toFilePaths(): Provider<List<String>> = this
                    ?.all
                    ?.map { directories -> directories.map { it.asFile.path } }
                    ?: provider { emptyList() }
                @Suppress("UnstableApiUsage")
                sourceDirectories.setFrom(
                    files(
                        variant.sources.java.toFilePaths(),
                        variant.sources.kotlin.toFilePaths()
                    ),
                )

                executionData.setFrom(
                    project.fileTree("$buildDir/outputs/unit_test_code_coverage/${variant.name}UnitTest")
                        .matching { include("**/*.exec") },

                    project.fileTree("$buildDir/outputs/code_coverage/${variant.name}AndroidTest")
                        .matching { include("**/*.ec") },
                )

                doLast {
                    val reportFile =
                        file("$buildDir/reports/jacoco/create${variant.name.capitalize()}JacocoReport/html/index.html")
                    val uri = reportFile.toURI()
                        .toString()
                        .replace("file:/", "file:///")
                    logger.lifecycle("Jacoco report: $uri")
                }
            }


        variant.artifacts.forScope(ScopedArtifacts.Scope.PROJECT)
            .use(reportTask)
            .toGet(
                ScopedArtifact.CLASSES,
                { _ -> allJars },
                { _ -> allDirectories },
            )
    }

    tasks.withType<Test>().configureEach {
        configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }
}
