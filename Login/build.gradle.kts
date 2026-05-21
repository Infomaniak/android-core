import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(core.plugins.android.library)
    alias(core.plugins.kotlin.android)
    alias(core.plugins.kotlin.serialization)
    kotlin("plugin.parcelize")
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.lib.login"
    compileSdk = coreCompileSdk

    defaultConfig {
        minSdk = coreMinSdk

        consumerProguardFiles("proguard-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":Common"))

    implementation(core.androidx.browser)
    implementation(core.appcompat)
    implementation(core.kotlinx.coroutines.android)
    implementation(core.kotlinx.serialization.json)
    implementation(core.material)
    implementation(core.okhttp)
}
