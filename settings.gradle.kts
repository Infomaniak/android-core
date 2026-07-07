/*
 * Infomaniak Core - Android
 * Copyright (C) 2025-2026 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
//        mavenLocal() // Only used when we want to use a local version of a library (./gradlew publishToMavenLocal)
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
    ":AppLock",
    ":AppVersionChecker",
    ":Auth",
    ":Avatar",
    ":BugTracker",
    ":Coil",
    ":Common",
    ":DataValue",
    ":DataValue",
    ":CrossAppLogin:Back",
    ":CrossAppLogin:Front",
    ":DotLottie",
    ":File",
    ":FileTypes",
    ":FragmentNavigation",
    ":InAppReview",
    ":InAppUpdate",
    ":KSuite",
    ":Ktor",
    ":KSuite:KSuitePro",
    ":KSuite:MyKSuite",
    ":Login",
    ":Matomo",
    ":Network",
    ":Network:Ktor",
    ":Network:Models",
    ":Notifications",
    ":Notifications:Registration",
    ":Onboarding",
    ":PermissionManager",
    ":PrivacyManagement",
    ":RecyclerView",
    ":Sentry",
    ":SharedValues",
    ":Thumbnails",
    ":TwoFactorAuth:Back",
    ":TwoFactorAuth:Back:WithUserDb",
    ":TwoFactorAuth:Front",
    ":Ui:Compose:AccountBottomSheet",
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
