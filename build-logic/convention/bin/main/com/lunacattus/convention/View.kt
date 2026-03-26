package com.lunacattus.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidView(
    commonExtension: CommonExtension,
) {

    commonExtension.apply {
        buildFeatures.apply {
            viewBinding = true
        }

        dependencies {
            "implementation"(libs.findLibrary("androidx.appcompat").get())
            "implementation"(libs.findLibrary("material").get())
            "implementation"(libs.findLibrary("androidx.constraintlayout").get())
            if (commonExtension is ApplicationExtension) {
                "implementation"(libs.findLibrary("hilt.navigation.fragment").get())
                "implementation"(libs.findLibrary("androidx.navigation.fragment.ktx").get())
                "implementation"(libs.findLibrary("androidx.navigation.ui.ktx").get())
                "implementation"(libs.findLibrary("androidx.activity").get())
            }
        }
    }
}