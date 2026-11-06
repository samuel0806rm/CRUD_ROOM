package com.samuel.crudroom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = Color(0xFF00658F),
    secondary = Color(0xFF4F616E),
    error = Color(0xFFBA1A1A)
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFF85CFFF),
    secondary = Color(0xFFB7C9D8),
    error = Color(0xFFFFB4AB)
)

/** Tema Material 3 de la app: usa el esquema claro u oscuro según el sistema. */
@Composable
fun CrudRoomTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) EsquemaOscuro else EsquemaClaro,
        content = content
    )
}
