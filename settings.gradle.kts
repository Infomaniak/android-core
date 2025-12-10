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
    ":AppVersionChecker",
    ":Auth",
    ":Avatar",
    ":Coil",
    ":CrossAppLogin:Back",
    ":CrossAppLogin:Front",
    ":DotLottie",
    ":FileTypes",
    ":FragmentNavigation",
    ":InAppReview",
    ":InAppStore",
    ":InAppUpdate",
    ":KSuite",
    ":Ktor",
    ":KSuite:KSuitePro",
    ":KSuite:MyKSuite",
    ":Matomo",
    ":Network",
    ":Network:Ktor",
    ":Network:Models",
    ":Notifications",
    ":Onboarding",
    ":RecyclerView",
    ":Sentry",
    ":Thumbnails",
    ":Ui:Compose:BasicButton",
    ":Ui:Compose:Basics",
    ":Ui:Compose:Margin",
    ":Ui:Compose:MaterialThemeFromXml",
    ":Ui:Compose:Preview",
    ":Ui:Compose:Theme",
    ":Ui:View",
    ":WebView",
)
