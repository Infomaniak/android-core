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
    includeBuild("build-logic")
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

include(
    ":AppIntegrity",
    ":AppVersionChecker",
    ":Auth",
    ":Avatar",
    ":Coil",
    ":Common",
    ":CrossAppLogin:Back",
    ":CrossAppLogin:Front",
    ":DotLottie",
    ":FileTypes",
    ":FragmentNavigation",
    ":InAppReview",
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
    ":Notifications:Registration",
    ":Onboarding",
    ":RecyclerView",
    ":Sentry",
    ":SharedValues",
    ":Thumbnails",
    ":TwoFactorAuth:Back",
    ":TwoFactorAuth:Back:WithUserDb",
    ":TwoFactorAuth:Front",
    ":Ui:Compose:BasicButton",
    ":Ui:Compose:Basics",
    ":Ui:Compose:BottomStickyButtonScaffolds",
    ":Ui:Compose:Margin",
    ":Ui:Compose:MaterialThemeFromXml",
    ":Ui:Compose:Preview",
    ":Ui:Compose:Theme",
    ":Ui:View",
    ":Ui:View:EdgeToEdge",
    ":WebView",
)
