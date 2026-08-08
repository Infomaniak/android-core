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
    api(project(":Network"))
    api(core.androidx.lifecycle.viewmodel.ktx)
    implementation(project(":Auth"))
    implementation(project(":Avatar"))
    implementation(project(":Common"))
    implementation(project(":Ui:View:PrimaryPalette"))
    implementation(project(":Ui:Compose:Margin"))

    implementation(platform(core.compose.bom))
    api(core.compose.runtime)
    api(core.compose.ui)
    implementation(core.compose.foundation)
    implementation(core.compose.material3)
    implementation(core.compose.material.icons)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(core.compose.ui.tooling.preview)

    implementation(core.androidx.lifecycle.viewmodel.compose)
    implementation(core.androidx.lifecycle.runtime.compose)
    implementation(core.qrose)
}
