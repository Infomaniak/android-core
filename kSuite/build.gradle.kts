plugins {
    id("com.android.library")
    alias(core.plugins.kotlin.android)
    alias(core.plugins.compose.compiler)
    alias(core.plugins.kotlin.parcelize)
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {

    namespace = "com.infomaniak.core.ksuite"
    compileSdk = coreCompileSdk

    defaultConfig {
        minSdk = coreMinSdk
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("standard") {
            isDefault = true
        }
        create("fdroid")
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose
    implementation(platform(core.compose.bom))
    implementation(core.compose.runtime)
    debugImplementation(core.compose.ui.tooling)
    implementation(core.compose.material3)
    implementation(core.compose.ui)
    implementation(core.compose.ui.tooling.preview)
}
