plugins {
    id("com.android.library")
    alias(core.plugins.kotlin.android)
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.core.network.ktor"
    compileSdk = coreCompileSdk

    defaultConfig {
        minSdk = coreMinSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
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
    api(project(":Network"))
    implementation(project(":Sentry"))

    api(core.ktor.client.json)
    api(core.ktor.client.content.negociation)
    api(core.ktor.client.core)
    api(core.ktor.client.okhttp)
    api(core.okhttp)

    api(core.kotlinx.serialization.json)
}
