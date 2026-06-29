plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(core.gradle.build.tools)
}

gradlePlugin {
    plugins {
        register("infomaniakApplication") {
            id = "com.infomaniak.core.application"
            implementationClass = "com.infomaniak.core.application.InfomaniakApplicationPlugin"
        }
    }
}
