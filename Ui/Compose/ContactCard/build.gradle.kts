import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(core.plugins.infomaniak.android.library)
    alias(core.plugins.infomaniak.android.library.flavor.aware)
    alias(core.plugins.compose.compiler)
}

android {
    namespace = "com.infomaniak.core.ui.compose.contactcard"

    buildFeatures {
        compose = true
    }

}

dependencies {
    implementation(project(":Auth"))
    implementation(project(":Network"))
    implementation(project(":Avatar"))
    implementation(project(":Ui:Compose:Margin"))
    implementation(project(":Ui:Compose:MaterialThemeFromXml"))

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
    implementation(core.androidx.lifecycle.viewmodel.compose)
    implementation(core.androidx.lifecycle.runtime.compose)
    implementation(core.navigation.fragment.ktx)
    implementation(core.activity.compose)
    implementation(core.qrose)
}
