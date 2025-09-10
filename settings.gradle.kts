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
    }
}

rootProject.name = "LunaApp"
include(":app:videoplayer")
include(":app:domain")
include(":app:data")
include(":app:base")
include(":app:gallery")
include(":app:media")
include(":logger")
include(":common")
include(":feature:speech")
include(":service:media")
