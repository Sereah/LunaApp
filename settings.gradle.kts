pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven { url=uri("https://jitpack.io") }
        maven { url=uri("https://maven.mozilla.org/maven2/") }
    }
}

rootProject.name = "LunaApp"

include(":record")
include(":ui-design")

include(":app:videoplayer")
include(":app:connection")
include(":app:media")
include(":app:statemachinedemo")

include(":app:gallery:presentation")
include(":app:gallery:domain")
include(":app:gallery:data")

include(":app:conflux")

// 自动激活 pre-commit hook（防止大文件被提交）
File(rootDir, ".githooks/pre-commit").takeIf { it.exists() }?.let {
    Runtime.getRuntime().exec("git config core.hooksPath .githooks", null, rootDir)
}
