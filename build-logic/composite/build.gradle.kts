plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        create("coreComposite") {
            id = "com.infomaniak.core.composite"
            implementationClass = "com.infomaniak.core.composite.CoreCompositePlugin"
        }
    }
}
