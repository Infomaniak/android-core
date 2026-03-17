plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(core.gradle.build.tools)
}

gradlePlugin {
    plugins {
        register("coreComposeLint") {
            id = "com.infomaniak.core.compose.lint"
            implementationClass = "com.infomaniak.core.compose.lint.ComposeLintPlugin"
        }
    }
}
