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
include(":app:domain")
include(":app:data")
include(":app:videoplayer")
include(":app:gallery")
include(":app:media")

include(":service:media")

include(":feature:speech")

include(":logger")
include(":common")
include(":ui-design")
include(":app:launcher")
