package com.satryo.weatherapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Palet utama: biru langit
val SkyBlue10 = Color(0xFF001C3A)
val SkyBlue20 = Color(0xFF00315F)
val SkyBlue30 = Color(0xFF004786)
val SkyBlue40 = Color(0xFF1B5FA8)
val SkyBlue80 = Color(0xFFA5C8FF)
val SkyBlue90 = Color(0xFFD4E3FF)

// Aksen: matahari
val Sun20 = Color(0xFF4A2800)
val Sun30 = Color(0xFF693C00)
val Sun40 = Color(0xFF8A5100)
val Sun80 = Color(0xFFFFB86B)
val Sun90 = Color(0xFFFFDDB9)

// Gradien card cuaca (teks putih di atasnya)
val DaySkyTop = Color(0xFF4F9CF0)
val DaySkyBottom = Color(0xFF1E62C8)
val NightSkyTop = Color(0xFF34457A)
val NightSkyBottom = Color(0xFF151C3B)

/** Latar card cuaca: biru cerah untuk siang, biru gelap untuk malam. */
fun skyGradient(isNight: Boolean): Brush = Brush.verticalGradient(
    colors = if (isNight) {
        listOf(NightSkyTop, NightSkyBottom)
    } else {
        listOf(DaySkyTop, DaySkyBottom)
    },
)
