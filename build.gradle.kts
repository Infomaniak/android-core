plugins {
    alias(libs.plugins.android.library)
    alias(core.plugins.kotlin.android)
}

val sharedCompileSdk: Int by rootProject.extra
val sharedMinSdk: Int by rootProject.extra
val sharedJavaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.core"
    compileSdk = sharedCompileSdk

    defaultConfig {
        minSdk = sharedMinSdk

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
        sourceCompatibility = sharedJavaVersion
        targetCompatibility = sharedJavaVersion
    }
    kotlinOptions {
        jvmTarget = sharedJavaVersion.toString()
    }
}

dependencies {
    api(core.kotlinx.coroutines.core)
    api(core.splitties.appctx)
    api(core.splitties.systemservices)
    api(core.splitties.coroutines)
    implementation(core.splitties.toast)
    implementation(core.splitties.mainhandler)
    implementation(core.splitties.mainthread)
    implementation(core.androidx.core)
    implementation(core.androidx.lifecycle.runtime.ktx)
}
