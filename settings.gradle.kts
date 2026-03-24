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
        maven { url=uri("https://jitpack.io") }
        maven { url=uri("https://maven.mozilla.org/maven2/") }
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

include(":service:voice")

include(":feature:speech")
include(":app:conflux")
include(":app:mutilscreen")
include(":feature:voice")
include(":app:browser")
