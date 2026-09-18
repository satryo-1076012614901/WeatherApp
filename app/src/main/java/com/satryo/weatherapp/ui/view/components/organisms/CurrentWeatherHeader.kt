package com.satryo.weatherapp.ui.view.components.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.satryo.weatherapp.data.model.CurrentWeather
import com.satryo.weatherapp.data.model.DailyForecast
import com.satryo.weatherapp.ui.theme.skyGradient
import com.satryo.weatherapp.ui.util.WeatherFormatter
import com.satryo.weatherapp.ui.view.components.atoms.TemperatureText
import com.satryo.weatherapp.ui.view.components.atoms.WeatherIcon

/**
 * Header halaman Detail: suhu dan kondisi saat ini.
 *
 * @param today prakiraan hari ini, sumber suhu maks/min.
 * @param offsetSeconds offset zona waktu lokasi terhadap UTC.
 */
@Composable
fun CurrentWeatherHeader(
    current: CurrentWeather,
    today: DailyForecast?,
    offsetSeconds: Int,
    modifier: Modifier = Modifier,
) {
    val secondaryText = Color.White.copy(alpha = 0.85f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(skyGradient(current.condition.isNight))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WeatherIcon(
            condition = current.condition,
            tint = Color.White,
            modifier = Modifier.size(120.dp),
        )
        TemperatureText(
            celsius = current.temperature,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
            fontWeight = FontWeight.Light,
            color = Color.White,
        )
        Text(
            text = WeatherFormatter.description(current.condition.description),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = WeatherFormatter.highLow(max = today?.maxTemperature, min = today?.minTemperature),
            style = MaterialTheme.typography.bodyLarge,
            color = secondaryText,
        )
        Text(
            text = "Terasa seperti ${WeatherFormatter.temperature(current.feelsLike)}",
            style = MaterialTheme.typography.bodyMedium,
            color = secondaryText,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Diperbarui ${WeatherFormatter.time(current.time, offsetSeconds)} waktu setempat",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f),
        )
    }
}
