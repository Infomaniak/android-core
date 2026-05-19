import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(core.plugins.android.library)
    alias(core.plugins.kotlin.android)
    kotlin("plugin.parcelize")
    alias(core.plugins.kotlin.serialization)
    alias(core.plugins.ksp)
    alias(core.plugins.navigation.safeargs)
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

android {
    namespace = "com.infomaniak.core.legacy"
    compileSdk = coreCompileSdk

    defaultConfig {
        minSdk = coreMinSdk

        testOptions.unitTests.isIncludeAndroidResources = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        }
    }
}

dependencies {
    // New Core via composite build: will be substituted to the included build project
    implementation(project(":Network"))

    api(core.kotlinx.coroutines.android)
    api(core.kotlinx.coroutines.test)
    api(core.kotlinx.serialization.json)

    api(core.navigation.fragment.ktx)
    api(core.navigation.ui.ktx)

    api(core.constraintlayout)
    api(core.coordinatorlayout)
    api(core.androidx.core.ktx)
    api(core.androidx.lifecycle.livedata.ktx)
    api(core.androidx.recyclerview)
    api(core.swiperefreshlayout)

    api(core.appcompat)
    api(core.appcompat.resources)

    implementation(core.splitties.appctx)

    api(core.room.ktx)
    api(core.room.runtime)
    ksp(core.room.compiler)

    api(core.material)
    api(core.gson)

    api(core.coil.two)

    api(core.android.login)
    api(core.stetho.okhttp3)
    api(core.okhttp)

    api(core.sentry.android)
    api(core.sentry.okhttp)

    implementation(core.progress.button)

    api(core.matomo)

    testImplementation(core.junit)
    testImplementation(core.androidx.junit)
    testImplementation(core.androidx.test.core)
    testImplementation(core.androidx.test.core.ktx)
    testImplementation(core.robolectric)
}
