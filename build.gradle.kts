// Archivo de compilación raíz: declara los plugins sin aplicarlos.
// Cada módulo (aquí solo :app) decide cuáles aplica.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
