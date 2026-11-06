// AGP 9 trae Kotlin integrado, por eso no se aplica el plugin org.jetbrains.kotlin.android.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp) // Procesador que genera el código de Room (reemplaza a kapt)
}

android {
    namespace = "com.samuel.crudroom"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.samuel.crudroom"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // AndroidX base
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Jetpack Compose (Material 3)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Room: runtime para la app y compilador ejecutado con KSP
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
}
