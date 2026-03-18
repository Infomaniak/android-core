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
    namespace = "com.infomaniak.core.privacymanagement"
    compileSdk = coreCompileSdk

    defaultConfig {
        minSdk = coreMinSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }
}

dependencies {
    implementation(project(":Common"))
    implementation(project(":Ui:Compose:BottomStickyButtonScaffolds"))
    implementation(project(":Ui:Compose:Margin"))
    implementation(project(":Ui:Compose:Preview"))
    implementation(project(":Ui:Compose:Theme"))

    implementation(platform(core.compose.bom))
    implementation(core.compose.foundation)
    implementation(core.compose.material3)
    implementation(core.compose.runtime)
    implementation(core.compose.ui.tooling.preview)
    debugImplementation(core.compose.ui.tooling)

    implementation(core.kotlinx.collections.immutable)
}
