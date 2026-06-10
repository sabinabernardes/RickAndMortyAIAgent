pluginManagement {
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

rootProject.name = "RickAndMorty"
include(":app")
include(":core:navigation")
include(":core:designsystem")
include(":core:playground")
include(":core:network")
include(":core:logging")
include(":core:analytics")
include(":core:domain")
include(":feature")
include(":feature:home")
include(":feature:character_details")
include(":feature:chat")
include(":core:security")
include(":feature:auth")

