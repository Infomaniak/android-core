plugins {
    id("com.android.library")
    alias(core.plugins.kotlin.android)
    alias(core.plugins.compose.compiler)
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.core.onboarding"
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
    buildFeatures {
        compose = true
    }
    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
}

dependencies {
    implementation(project(":Core:Compose:Margin"))

    implementation(platform(core.compose.bom))
    implementation(core.activity.compose)
    implementation(core.androidx.core.ktx)
    implementation(core.compose.foundation)
    implementation(core.compose.material3)
    implementation(core.compose.runtime)
    implementation(core.compose.ui.tooling.preview)

    implementation(core.lottie)

    debugImplementation(core.compose.ui.tooling)
}
