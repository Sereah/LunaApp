package com.lunacattus.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension,
) {

    commonExtension.apply {
        buildFeatures.apply {
            compose = true
        }

        dependencies {
            val bom = libs.findLibrary("androidx.compose.bom").get()
            "implementation"(platform(bom))
            "implementation"(libs.findLibrary("androidx.compose.ui").get())
            "implementation"(libs.findLibrary("androidx.compose.ui.graphics").get())
            "implementation"(libs.findLibrary("androidx.compose.ui.tooling.preview").get())
            "implementation"(libs.findLibrary("androidx.compose.material3").get())
            "implementation"(libs.findLibrary("androidx.compose.material3.adaptive").get())
            "implementation"(libs.findLibrary("androidx.compose.material3.adaptive.navigation").get())
            "implementation"(libs.findLibrary("androidx.compose.material3.adaptive.navigation3").get())
            "implementation"(libs.findLibrary("androidx.compose.material3.navigationSuite").get())
            "implementation"(libs.findLibrary("androidx.compose.material3.windowSizeClass").get())
            "implementation"(libs.findLibrary("androidx.compose.material.iconsExtended").get())
            "implementation"(libs.findLibrary("androidx.compose.foundation").get())
            "implementation"(libs.findLibrary("androidx.compose.foundation.layout").get())
            "implementation"(libs.findLibrary("androidx.compose.animation").get())
            "debugImplementation"(libs.findLibrary("androidx.compose.ui.tooling").get())
            if (commonExtension is ApplicationExtension) {
                "implementation"(libs.findLibrary("androidx.activity.compose").get())
                "implementation"(libs.findLibrary("androidx.navigation.compose").get())
                "implementation"(libs.findLibrary("androidx.hilt.navigation.compose").get())
                //navigation3
                "implementation"(libs.findLibrary("androidx.navigation3.runtime").get())
                "implementation"(libs.findLibrary("androidx-navigation3-ui").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-navigation3").get())
                "implementation"(libs.findLibrary("kotlinx-serialization-core").get())

                "androidTestImplementation"(platform(bom))
                "androidTestImplementation"(
                    libs.findLibrary("androidx.compose.ui.test.junit4").get()
                )
            }
            "debugImplementation"(
                libs.findLibrary("androidx.compose.ui.test.manifest").get()
            )
        }
    }
}