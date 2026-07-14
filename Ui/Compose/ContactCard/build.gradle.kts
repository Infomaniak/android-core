import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(core.plugins.android.library)
    alias(core.plugins.kotlin.android)
    alias(core.plugins.compose.compiler)
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.core.ui.compose.contactcard"
    compileSdk = coreCompileSdk

    defaultConfig {
        minSdk = coreMinSdk
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    buildFeatures {
        compose = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        }
    }
}

dependencies {
    implementation(project(":Auth"))
    implementation(project(":Network"))
    implementation(project(":Avatar"))
    implementation(project(":Ui:Compose:Margin"))

    implementation(platform(core.compose.bom))
    api(core.compose.runtime)
    api(core.compose.ui)
    implementation(core.compose.ui.android)
    implementation(core.compose.foundation)
    implementation(core.compose.material3)
    implementation(core.compose.material.icons)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(core.compose.ui.tooling.preview)

    implementation(core.androidx.lifecycle.viewmodel.ktx)
    implementation(core.navigation.fragment.ktx)
    implementation(core.activity.compose)
    implementation(core.qrose)
}
