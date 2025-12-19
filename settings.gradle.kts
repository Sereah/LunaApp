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

include(":logger")
include(":common")
include(":ui-design")

include(":app:videoplayer")
include(":app:connection")
include(":app:media")
include(":app:statemachinedemo")

include(":app:gallery:presentation")
include(":app:gallery:domain")
include(":app:gallery:data")

include(":service:media")

include(":feature:speech")
