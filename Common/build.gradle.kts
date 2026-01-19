import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(core.plugins.android.library)
    alias(core.plugins.kotlin.android)
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.core.common"
    compileSdk = coreCompileSdk
    compileSdk {
        version = release(coreCompileSdk)
    }

    defaultConfig {
        minSdk = coreMinSdk
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(javaVersion.toString())
    }
}

dependencies {
    api(core.kotlinx.coroutines.core)
    api(core.splitties.appctx)
    api(core.splitties.systemservices)
    api(core.splitties.coroutines)
    api(core.androidx.lifecycle.service)
    api(core.splitties.intents)
    implementation(core.androidx.collection)
    implementation(core.splitties.bitflags)
    implementation(core.splitties.toast)
    implementation(core.splitties.bundle)
    implementation(core.splitties.mainhandler)
    implementation(core.splitties.mainthread)
    implementation(core.androidx.core)
    implementation(core.androidx.core.ktx)
    implementation(core.androidx.lifecycle.runtime.ktx)
    implementation(core.androidx.lifecycle.process)

    testImplementation(kotlin("test"))
    testImplementation(core.kotest.assertions)
    testImplementation(core.kotlinx.coroutines.test)
    testImplementation(core.androidx.junit)
    testImplementation(core.androidx.test.core.ktx)
    testImplementation(core.androidx.test.core)
    testImplementation(core.junit)
    testImplementation(core.robolectric)
}
