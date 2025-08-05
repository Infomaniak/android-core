plugins {
    id("com.android.library")
    alias(core.plugins.kotlin.android)
    alias(core.plugins.compose.compiler)
    kotlin("plugin.parcelize")
    kotlin("plugin.serialization")
    id("androidx.navigation.safeargs.kotlin")
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {

    namespace = "com.infomaniak.core.crosslogin"
    compileSdk = coreCompileSdk

    defaultConfig {
        minSdk = coreMinSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures.buildConfig = true

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("standard") { isDefault = true }
        create("fdroid")
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
    implementation(project(":Core"))
    implementation(project(":Core:AppIntegrity"))
    implementation(project(":Core:Avatar"))
    implementation(project(":Core:Coil"))
    implementation(project(":Core:Compose:Basics"))
    implementation(project(":Core:Compose:Margin"))
    implementation(project(":Core:Compose:MaterialThemeFromXml"))
    implementation(project(":Core:Legacy"))

    api(core.kotlinx.coroutines.core)
    api(core.androidx.lifecycle.runtime.ktx)
    api(core.androidx.lifecycle.service)
    api(core.kotlinx.serialization.protobuf)
    api(core.androidx.work.runtime)

    implementation(core.splitties.mainthread)
    implementation(core.ktor.client.core)
    implementation(core.ktor.client.okhttp)
    implementation(core.ktor.client.json)
    implementation(core.ktor.client.content.negociation)

    implementation(core.androidx.core.ktx)
    implementation(core.material)
    implementation(core.navigation.fragment.ktx)
    implementation(core.kotlinx.serialization.json)

    // Compose
    implementation(core.coil.compose)
    implementation(core.coil.network.okhttp)
    implementation(platform(core.compose.bom))
    implementation(core.compose.runtime)
    implementation(core.compose.ui.android)
    debugImplementation(core.compose.ui.tooling)
    implementation(core.compose.material3)
    implementation(core.compose.ui)
    implementation(core.compose.ui.tooling.preview)

    testImplementation(core.junit)
    androidTestImplementation(core.androidx.junit)
}
