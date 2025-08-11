package com.lunacattus.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureTest(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    isCompose: Boolean
) {
    commonExtension.apply {
        dependencies {
            "androidTestImplementation"(libs.findLibrary("androidx.junit").get())
            "androidTestImplementation"(libs.findLibrary("androidx.espresso.core").get())
            "androidTestImplementation"(libs.findLibrary("mockk.android").get())
            "testImplementation"(libs.findLibrary("robolectric").get())
            "testImplementation"(libs.findLibrary("junit").get())
            "testImplementation"(libs.findLibrary("mockk").get())
            "testImplementation"(libs.findLibrary("coroutines.test").get())
            if (isCompose) {
                val bom = libs.findLibrary("androidx.compose.bom").get()
                "androidTestImplementation"(platform(bom))
                "androidTestImplementation"(
                    libs.findLibrary("androidx.compose.ui.test.junit4").get()
                )

                "debugImplementation"(libs.findLibrary("androidx.compose.ui.tooling").get())
                "debugImplementation"(
                    libs.findLibrary("androidx.compose.ui.test.manifest").get()
                )
            }
        }
    }
}