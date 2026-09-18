package com.satryo.weatherapp.ui.view.components.atoms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Dehaze
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import coil.compose.AsyncImage
import com.satryo.weatherapp.data.model.WeatherCondition

/**
 * Ikon kondisi cuaca resmi OpenWeather (halaman Weather Conditions), dimuat dari
 * https://openweathermap.org/img/wn/{kode ikon}@2x.png sesuai field "icon" dari API.
 *
 * Selama gambar belum berhasil dimuat, ditampilkan ikon vektor pengganti yang mewakili
 * kode ikon yang sama. Ikon pengganti ini juga yang terlihat di @Preview, karena Preview
 * Android Studio tidak mengunduh gambar dari internet.
 *
 * @param tint warna ikon pengganti; default mengikuti warna konten di sekitarnya.
 */
@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    val url = condition.iconUrl
    var showFallback by remember(url) { mutableStateOf(true) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (showFallback) {
            Icon(
                imageVector = condition.fallbackIcon(),
                contentDescription = null,
                tint = tint.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxSize(0.7f),
            )
        }
        AsyncImage(
            model = url,
            contentDescription = condition.description,
            onSuccess = { showFallback = false },
            onError = { showFallback = true },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Ikon vektor pengganti per kode ikon OpenWeather (daftar "Icon list" di halaman
 * Weather Conditions): 01 cerah, 02 sedikit berawan, 03/04 berawan, 09/10 hujan,
 * 11 badai petir, 13 salju, 50 kabut.
 */
private fun WeatherCondition.fallbackIcon(): ImageVector = when (iconCode.take(2)) {
    "01" -> if (isNight) Icons.Outlined.NightsStay else Icons.Outlined.WbSunny
    "02" -> if (isNight) Icons.Outlined.Cloud else Icons.Outlined.WbCloudy
    "03", "04" -> Icons.Outlined.Cloud
    "09", "10" -> Icons.Outlined.Umbrella
    "11" -> Icons.Outlined.Thunderstorm
    "13" -> Icons.Outlined.AcUnit
    "50" -> Icons.Outlined.Dehaze
    else -> Icons.Outlined.Cloud
}
