package com.lunacattus.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureTest(
    commonExtension: CommonExtension
) {
    commonExtension.apply {
        dependencies {
            if (commonExtension is ApplicationExtension) {
                "androidTestImplementation"(libs.findLibrary("androidx.junit").get())
                "androidTestImplementation"(libs.findLibrary("androidx.espresso.core").get())
                "androidTestImplementation"(libs.findLibrary("mockk.android").get())
            }
            "testImplementation"(libs.findLibrary("robolectric").get())
            "testImplementation"(libs.findLibrary("junit").get())
            "testImplementation"(libs.findLibrary("mockk").get())
            "testImplementation"(libs.findLibrary("coroutines.test").get())
        }
    }
}