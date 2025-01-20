plugins {
    alias(libs.plugins.android.library)
    alias(core.plugins.kotlin.android)
    kotlin("plugin.serialization") version libs.versions.kotlin
}

val sharedCompileSdk: Int by rootProject.extra
val sharedMinSdk: Int by rootProject.extra
val sharedJavaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.core.appintegrity"
    compileSdk = sharedCompileSdk

    defaultConfig {
        minSdk = sharedMinSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "APP_INTEGRITY_BASE_URL", "\"https://api.infomaniak.com\"")
        }

        debug {
            buildConfigField("String", "APP_INTEGRITY_BASE_URL", "\"https://api.preprod.dev.infomaniak.ch\"")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = sharedJavaVersion
        targetCompatibility = sharedJavaVersion
    }

    kotlinOptions {
        jvmTarget = sharedJavaVersion.toString()
    }
}

dependencies {

    implementation(project(":Core:Sentry"))

    implementation(core.integrity)
    implementation(core.ktor.client.core)
    implementation(core.ktor.client.content.negociation)
    implementation(core.ktor.client.json)
    implementation(core.ktor.client.encoding)
    implementation(core.ktor.client.okhttp)
    implementation(core.kotlinx.serialization.json)
    testImplementation(core.junit)
    testImplementation(core.ktor.client.mock)
    androidTestImplementation(core.androidx.junit)
}
