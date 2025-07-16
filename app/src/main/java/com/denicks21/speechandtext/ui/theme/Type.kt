package com.denicks21.speechandtext.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import com.denicks21.speechandtext.R

// 1. Definimos la familia Poppins con todas las variantes .ttf
val Poppins = FontFamily(
    // Thin
    Font(R.font.poppins_thin,             weight = FontWeight.Thin),
    Font(R.font.poppins_thinitalic,       weight = FontWeight.Thin,      style = FontStyle.Italic),

    // ExtraLight
    Font(R.font.poppins_extralight,       weight = FontWeight.ExtraLight),
    Font(R.font.poppins_extralightitalic, weight = FontWeight.ExtraLight, style = FontStyle.Italic),

    // Light
    Font(R.font.poppins_light,            weight = FontWeight.Light),
    Font(R.font.poppins_lightitalic,      weight = FontWeight.Light,     style = FontStyle.Italic),

    // Regular
    Font(R.font.poppins_regular,          weight = FontWeight.Normal),
    Font(R.font.poppins_italic,           weight = FontWeight.Normal,    style = FontStyle.Italic),

    // Medium
    Font(R.font.poppins_medium,           weight = FontWeight.Medium),
    Font(R.font.poppins_mediumitalic,     weight = FontWeight.Medium,    style = FontStyle.Italic),

    // SemiBold
    Font(R.font.poppins_semibold,         weight = FontWeight.SemiBold),
    Font(R.font.poppins_semibolditalic,   weight = FontWeight.SemiBold,  style = FontStyle.Italic),

    // Bold
    Font(R.font.poppins_bold,             weight = FontWeight.Bold),
    Font(R.font.poppins_bolditalic,       weight = FontWeight.Bold,      style = FontStyle.Italic),

    // ExtraBold
    Font(R.font.poppins_extrabold,        weight = FontWeight.ExtraBold),
    Font(R.font.poppins_extrabolditalic,  weight = FontWeight.ExtraBold, style = FontStyle.Italic),

    // Black
    Font(R.font.poppins_black,            weight = FontWeight.Black),
    Font(R.font.poppins_blackitalic,      weight = FontWeight.Black,     style = FontStyle.Italic)
)

// 2. Creamos el set de Typography incluyendo h1…h6
val Typography = Typography(
    h1 = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 96.sp
    ),
    h2 = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 60.sp
    ),
    h3 = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
        fontSize   = 48.sp
    ),
    h4 = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
        fontSize   = 34.sp
    ),
    h5 = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 24.sp
    ),
    h6 = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Light,
        fontSize   = 14.sp
    ),
    subtitle1 = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize   = 16.sp
    ),
    subtitle2 = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp
    ),
    body1 = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp
    ),
    body2 = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp
    ),
    button = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp
    ),
    caption = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Light,
        fontSize   = 8.sp
    ),
    // 3. Añadimos estilos adicionales como overline
    overline = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Light,
        fontSize   = 10.sp
    )
)
