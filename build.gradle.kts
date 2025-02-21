plugins {
    id("com.android.library")
    alias(core.plugins.kotlin.android)
}

rootProject.extra.apply {
    set("coreCompileSdk", 35)
    set("coreMinSdk", 21)
    set("javaVersion", JavaVersion.VERSION_17)
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.core"
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
    kotlinOptions {
        jvmTarget = javaVersion.toString()
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
