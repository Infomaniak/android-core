plugins {
    id("com.android.library")
    alias(core.plugins.kotlin.android)
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {

    namespace = "com.infomaniak.core.ksuite.ksuitepro"
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
}

dependencies {

}
