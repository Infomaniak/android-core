plugins {
    alias(core.plugins.android.library)
    alias(core.plugins.kotlin.android)
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
    api(project(":Network:Models"))
    implementation(project(":Sentry"))

    implementation(core.androidx.core.ktx)
    implementation(core.kotlinx.serialization.json)
    implementation(core.gson)
    implementation(core.okhttp)
    implementation(core.splitties.toast)
    implementation(core.stetho.okhttp3)
    implementation(core.splitties.systemservices)
}
