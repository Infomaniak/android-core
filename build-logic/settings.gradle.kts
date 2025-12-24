dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("core") {
            from(files("../gradle/core.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"

include(":composite")
