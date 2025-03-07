plugins {
    id("com.android.library")
    alias(core.plugins.kotlin.android)
    alias(core.plugins.compose.compiler)

    alias(core.plugins.ksp)
    kotlin("plugin.parcelize")
    kotlin("plugin.serialization") version core.versions.kotlin
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.core.inappstore"
    compileSdk = coreCompileSdk

    defaultConfig {
        minSdk = coreMinSdk

        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "INFOMANIAK_API_V1", "\"https://api.infomaniak.com/1\"")
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
        flavorDimensions += "distribution"
        compose = true
        buildConfig = true
    }

    productFlavors {
        create("standard") {
            dimension = "distribution"
            isDefault = true
        }

        // standard { getIsDefault().set(true) }
        // fdroid
    }

    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
}

dependencies {
    implementation(project(":Core"))
    implementation(project(":Core:Sentry"))
    implementation(project(":Core:Ui"))
    // implementation(project(":Core:WebView")) // TODO: Uncomment this line when the WebView module is merged.
    implementation(core.play.review)
    implementation(core.play.review.ktx)
    implementation(core.appUpdate)
    implementation(core.appUpdate.ktx)
    implementation(core.work.runtime.ktx)
    implementation(core.concurrent.futures.ktx)
    implementation(core.datastore.preferences)
    implementation(core.gson)

    implementation(core.ktor.client.core)
    implementation(core.ktor.client.okhttp)

    // TODO: Delete?
    // implementation(core.material)

    // Compose
    // TODO: Delete unused implementations.
    implementation(core.coil.compose)
    implementation(core.coil.network.okhttp)
    implementation(platform(core.compose.bom))
    implementation(core.compose.runtime)
    debugImplementation(core.compose.ui.tooling)
    implementation(core.compose.material3)
    implementation(core.compose.ui)
    implementation(core.compose.ui.tooling.preview)
    implementation(core.activity.compose)

    // TODO: Added automaticaly, check if this is ok.
    // TODO: REPLACE libs WITH core!
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
}
