plugins {
    id("com.android.library")
    alias(core.plugins.kotlin.android)
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.core.login.crossapp"
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

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
}

dependencies {
    api(core.kotlinx.coroutines.core)
    api(core.androidx.lifecycle.runtime.ktx)
    api(core.androidx.lifecycle.service)
    api(core.kotlinx.serialization.protobuf)

    implementation(project(":Core"))
    implementation(project(":Core:Legacy"))
    implementation(core.splitties.mainthread)

    testImplementation(core.junit)
    androidTestImplementation(core.androidx.junit)
}
