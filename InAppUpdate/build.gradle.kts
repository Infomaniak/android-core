plugins {
    alias(core.plugins.android.library)
    alias(core.plugins.kotlin.android)
    alias(core.plugins.compose.compiler)
    alias(core.plugins.navigation.safeargs)
    alias(core.plugins.kotlin.serialization)
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
        viewBinding = true
    }
}

dependencies {
    implementation(project(":Common"))
    implementation(project(":AppVersionChecker"))
    implementation(project(":Network"))
    implementation(project(":Sentry"))
    implementation(project(":Ui"))
    implementation(project(":Ui:Compose:BottomStickyButtonScaffolds"))
    implementation(project(":Ui:Compose:Margin"))
    implementation(project(":Ui:View"))

    implementation(core.androidx.concurrent.futures.ktx)
    implementation(core.androidx.datastore.preferences)
    implementation(core.appcompat)
    implementation(core.androidx.work.runtime)
    implementation(core.kotlinx.serialization.json)

    "standardImplementation"(core.play.app.update)
    "standardImplementation"(core.play.app.update.ktx)

    implementation(core.material)

    implementation(core.navigation.fragment.ktx)
    implementation(core.navigation.ui.ktx)

    implementation(core.okhttp)

    // Compose
    implementation(platform(core.compose.bom))
    implementation(core.compose.runtime)
    debugImplementation(core.compose.ui.tooling)
    implementation(core.compose.material3)
    implementation(core.compose.ui)
    implementation(core.compose.ui.tooling.preview)
    implementation(core.androidx.adaptive)
    implementation(core.activity.compose)
}
