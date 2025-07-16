package com.denicks21.speechandtext.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de colores para modo oscuro
@SuppressLint("ConflictingOnColor")
private val DarkColorPalette = darkColors(
    primary         = GradientStart,    // color principal (botones, iconos activos…)
    primaryVariant  = GradientEnd,      // variante para estados presionados
    secondary       = LightGrey,        // color secundario (acentos, iconos secundarios)
    background      = GradientStart,    // fondo general
    surface         = DarkSurface,      // fondo de cards, diálogos…
    onPrimary       = TextTest,      // texto/icono sobre primary
    onSecondary     = AlertOff,      // texto/icono sobre secondary
    onBackground    = LightYellow,      // texto sobre background
    onSurface       = LightText,        // texto sobre surface
    error           = ErrorColor        // mensajes de error
    
)

// Paleta de colores para modo claro
private val LightColorPalette = lightColors(
    primary        = DarkGrey,
    primaryVariant = DarkGrey,
    secondary      = DarkYellow,
    background     = DarkGrey,
    surface        = LightSurface,
    onPrimary      = LightYellow,  // mismo onPrimary
    onSecondary    = DarkGrey,
    onBackground   = LightYellow,  // fijarse que onBackground = onPrimary
    onSurface      = DarkGrey,
    error          = ErrorColor // mensajes de error
)

@Composable
fun SpeechAndTextTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content:    @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors     = colors,
        typography = Typography,
        shapes     = Shapes,
        content    = content
    )
}
