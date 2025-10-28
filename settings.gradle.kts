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
        maven { url = uri("https://jitpack.io") }
    }

    versionCatalogs {
        create("core") {
            from(files("gradle/core.versions.toml"))
        }
    }
}

rootProject.name = "Core"

include(
    ":AppIntegrity",
    ":Auth",
    ":Avatar",
    ":Coil",
    ":Compose:BasicButton",
    ":Compose:Basics",
    ":Compose:Margin",
    ":Compose:MaterialThemeFromXml",
    ":Compose:Preview",
    ":Compose:Theme",
    ":CrossAppLogin:Back",
    ":CrossAppLogin:Front",
    ":FileTypes",
    ":FragmentNavigation",
    ":InAppReview",
    ":InAppStore",
    ":InAppUpdate",
    ":KSuite",
    ":KSuite:KSuitePro",
    ":KSuite:MyKSuite",
    ":Ktor",
    ":Matomo",
    ":Network",
    ":Network:Models",
    ":Notifications",
    ":Onboarding",
    ":RecyclerView",
    ":Sentry",
    ":Thumbnails",
    ":WebView",
)
