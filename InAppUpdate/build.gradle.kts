plugins {
    alias(core.plugins.android.library)
    alias(core.plugins.kotlin.android)
    alias(core.plugins.compose.compiler)

}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.core.inappupdate"
    compileSdk = coreCompileSdk

    defaultConfig {
        minSdk = coreMinSdk

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

    flavorDimensions += "distribution"
    productFlavors {
        create("standard") {
            isDefault = true
        }
        create("fdroid")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":"))
    implementation(project(":AppVersionChecker"))
    implementation(project(":Ui:Compose:Margin"))
    implementation(project(":Network"))
    implementation(project(":Sentry"))

    implementation(core.androidx.datastore.preferences)
    implementation(core.appcompat)
    implementation(core.androidx.work.runtime)
    implementation(core.kotlinx.serialization.json)

    implementation(core.app.update)
    implementation(core.app.update.ktx)

    implementation(core.androidx.concurrent.futures.ktx)

    implementation(core.okhttp)

    // Compose
    implementation(platform(core.compose.bom))
    implementation(core.compose.runtime)
    debugImplementation(core.compose.ui.tooling)
    implementation(core.compose.material3)
    implementation(core.compose.ui)
    implementation(core.compose.ui.tooling.preview)
    implementation(core.androidx.adaptive)
}
