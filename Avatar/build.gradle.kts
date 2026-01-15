plugins {
    alias(core.plugins.android.library)
    alias(core.plugins.kotlin.android)
    alias(core.plugins.compose.compiler)
}

val coreCompileSdk: Int by rootProject.extra
val coreMinSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.core.avatar"
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
    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
}

dependencies {
    implementation(project(":Ui:Compose:Margin"))

    implementation(platform(core.compose.bom))
    implementation(core.compose.runtime)
    implementation(core.compose.material3)
    implementation(core.compose.ui)
    debugImplementation(core.compose.ui.tooling)
    implementation(core.compose.ui.tooling.preview)

    implementation(core.coil.compose)
}
