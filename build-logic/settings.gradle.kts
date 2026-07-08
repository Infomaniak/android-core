dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
    versionCatalogs {
        create("core") {
            from(files("../gradle/core.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"

include(
    ":convention",
    ":composite",
    ":composeLint",
)
