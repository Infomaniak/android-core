plugins {
    id("com.android.library")
    alias(core.plugins.kotlin.android)
    alias(core.plugins.compose.compiler)
    alias(core.plugins.ksp)
    kotlin("plugin.parcelize")
    kotlin("plugin.serialization") version core.versions.kotlin
    id("androidx.navigation.safeargs.kotlin")
}

val coreCompileSdk: Int by rootProject.extra
val legacyMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {

    namespace = "com.infomaniak.core.myksuite"
    compileSdk = coreCompileSdk

    defaultConfig {
        minSdk = legacyMinSdk

        consumerProguardFiles("consumer-rules.pro")

        ksp { arg("room.schemaLocation", "$projectDir/schemas") }
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

    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(core.androidx.core.ktx)
    implementation(core.material)
    implementation(core.navigation.fragment.ktx)
    implementation(core.kotlinx.serialization.json)

    // Room
    implementation(core.room.runtime)
    implementation(core.room.ktx)
    ksp(core.room.compiler)

    // Compose
    implementation(core.coil.compose)
    implementation(core.coil.network.okhttp)
    implementation(platform(core.compose.bom))
    implementation(core.compose.runtime)
    debugImplementation(core.compose.ui.tooling)
    implementation(core.compose.material3)
    api(core.compose.ui)
    implementation(core.compose.ui.tooling.preview)
}
