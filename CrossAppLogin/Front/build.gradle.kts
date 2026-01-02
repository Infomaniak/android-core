plugins {
    alias(core.plugins.android.library)
    alias(core.plugins.kotlin.android)
    alias(core.plugins.compose.compiler)
    kotlin("plugin.parcelize")
    alias(core.plugins.navigation.safeargs)
    alias(core.plugins.kotlin.serialization)
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {

    namespace = "com.infomaniak.core.crossapplogin.front"
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

    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(project(":"))
    implementation(project(":Avatar"))
    implementation(project(":Coil"))
    implementation(project(":Ui:Compose:BasicButton"))
    implementation(project(":Ui:Compose:Basics"))
    implementation(project(":Ui:Compose:Margin"))
    implementation(project(":Ui:Compose:MaterialThemeFromXml"))
    api(project(":CrossAppLogin:Back"))
    api(project(":Onboarding"))

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
