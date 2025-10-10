plugins {
    alias(core.plugins.android.library)
    alias(core.plugins.kotlin.android)
    kotlin("plugin.parcelize")
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.core.network.models"
    compileSdk = coreCompileSdk

    defaultConfig {
        minSdk = coreMinSdk
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
}

dependencies {
    implementation(core.androidx.core.ktx)
    implementation(core.gson)
    implementation(core.kotlinx.serialization.json)
    implementation(core.okhttp)
}
