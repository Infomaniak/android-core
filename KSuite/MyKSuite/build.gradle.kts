plugins {
    alias(core.plugins.android.library)
    alias(core.plugins.kotlin.android)
    alias(core.plugins.compose.compiler)
    alias(core.plugins.ksp)
    kotlin("plugin.parcelize")
    id("androidx.navigation.safeargs.kotlin")
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {

    namespace = "com.infomaniak.core.ksuite.myksuite"
    compileSdk = coreCompileSdk

    defaultConfig {
        minSdk = coreMinSdk

        consumerProguardFiles("consumer-rules.pro")

        ksp { arg("room.schemaLocation", "$projectDir/schemas") }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation(project(":Ui:Compose:Basics"))
    implementation(project(":Ui:Compose:Margin"))
    implementation(project(":Ui:Compose:MaterialThemeFromXml"))
    implementation(project(":KSuite"))
    implementation(project(":Network")) // To access API URL

    implementation(core.androidx.core.ktx)
    implementation(core.material)
    implementation(core.navigation.fragment.ktx)
    implementation(core.kotlinx.serialization.json)

    implementation(core.coil)
    implementation(core.coil.compose)

    // Room
    implementation(core.room.runtime)
    implementation(core.room.ktx)
    ksp(core.room.compiler)

    // Compose
    implementation(platform(core.compose.bom))
    implementation(core.compose.runtime)
    debugImplementation(core.compose.ui.tooling)
    implementation(core.compose.material3)
    implementation(core.compose.ui)
    implementation(core.compose.ui.tooling.preview)
}
