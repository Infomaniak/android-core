plugins {
    alias(core.plugins.android.library)
    alias(core.plugins.kotlin.android)
    alias(core.plugins.compose.compiler)
    kotlin("plugin.parcelize")
    alias(core.plugins.ksp)
    alias(core.plugins.kotlin.serialization)
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
        compose = true
    }

    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
}

dependencies {
    implementation(project(":"))
    implementation(project(":Network"))
    implementation(project(":Sentry"))

    implementation(platform(core.compose.bom))
    implementation(core.compose.runtime)
    implementation(core.activity.compose)

    implementation(platform(core.compose.bom))
    implementation(core.compose.runtime)
    implementation(core.activity.compose)

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
