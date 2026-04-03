plugins {
    alias(libs.plugins.app.android.application)
    alias(libs.plugins.app.android.application.view)
    alias(libs.plugins.app.hilt)
    id("com.lunacattus.screen-adaptation")
}

screenAdaptation {
    // 1. 配置设计图的宽、高、DPI
    designWidthPx.set(1920)
    designHeightPx.set(1080)
    designDpi.set(160)

    // 2. 方式一：在 Gradle 中手动配置任意个目标屏幕：target(widthPx, heightPx, dpi)
//    target(1920, 1080, 320)
    target(3402, 1620, 432)
    
    // 方式二：指定外部配置文件（自动读取 CSV 内的设备列表）
    targetConfigFile.set(file("devices.csv"))

    // (可选) 配置基于宽还是高适配，默认为 "width"
    baseOn.set("width")
    autoTargetDevice.set(true)
}

android {
    namespace = "com.lunacattus.app.browser"

    defaultConfig {
        applicationId = "com.lunacattus.app.browser"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":logger"))
    implementation(project(":ui-design"))
    implementation(libs.glide)
    implementation(libs.permissionX)
    implementation(libs.geckoview)
}