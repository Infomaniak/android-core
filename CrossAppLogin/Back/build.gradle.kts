plugins {
    alias(core.plugins.android.library)
    alias(core.plugins.kotlin.android)
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.core.crossapplogin.back"
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
    api(core.kotlinx.coroutines.core)
    api(core.androidx.lifecycle.runtime.ktx)
    api(core.androidx.lifecycle.service)
    api(core.kotlinx.serialization.protobuf)
    api(core.androidx.work.runtime)

    implementation(project(":"))
    implementation(project(":AppIntegrity"))
    implementation(project(":Auth"))
    implementation(project(":Network:Ktor"))
    implementation(project(":Sentry"))
    implementation(core.splitties.mainthread)
    implementation(core.ktor.client.core)
    implementation(core.ktor.client.okhttp)
    implementation(core.ktor.client.json)
    implementation(core.ktor.client.content.negociation)

    implementation(core.androidx.lifecycle.viewmodel.ktx)
    implementation(core.activity.compose) // To access ComponentActivity

    testImplementation(core.junit)
    androidTestImplementation(core.androidx.junit)
}
