plugins {
    id("com.android.library")
    alias(core.plugins.kotlin.android)
    kotlin("plugin.parcelize")
    kotlin("plugin.serialization")
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.core.network"
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
    implementation(project(":Core:Sentry"))

    implementation(core.androidx.core.ktx)
    implementation(core.kotlinx.serialization.json)
    implementation(core.gson)
    implementation(core.okhttp)
    implementation(core.stetho.okhttp3)
}
