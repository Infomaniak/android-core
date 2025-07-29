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

    namespace = "com.infomaniak.core.crossloginui"
    compileSdk = coreCompileSdk

    defaultConfig {
        minSdk = coreMinSdk
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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

    implementation(project(":Core"))
    implementation(project(":Core:Compose:Basics"))
    implementation(project(":Core:Compose:Margin"))
    implementation(project(":Core:Compose:MaterialThemeFromXml"))
    implementation(project(":Core:CrossAppLogin"))
    implementation(project(":Core:UserAvatar"))

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
}
