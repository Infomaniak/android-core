plugins {
    id("com.android.library")
    alias(core.plugins.kotlin.android)
    kotlin("plugin.parcelize")
    kotlin("plugin.serialization")
    alias(core.plugins.ksp)
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

android {
    namespace = "com.infomaniak.core.auth"
    compileSdk = coreCompileSdk

    defaultConfig {
        namespace = "com.infomaniak.core.auth"
        minSdk = coreMinSdk

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

    buildFeatures {
        buildConfig = true
    }

    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
}

dependencies {
    implementation(project(":Core:Network"))
    implementation(project(":Core:Sentry"))

    implementation(core.androidx.core.ktx)
    implementation(core.kotlinx.serialization.json)
    implementation(core.gson)
    implementation(core.splitties.appctx)
    implementation(core.okhttp)
    implementation(core.stetho.okhttp3)
    api(core.android.login)
    api(core.gson)

    // Room
    implementation(core.room.ktx)
    api(core.room.runtime)
    ksp(core.room.compiler)
}
