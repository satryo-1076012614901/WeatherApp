package com.satryo.weatherapp.ui.view.components.organisms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.satryo.weatherapp.data.model.DailyForecast
import com.satryo.weatherapp.ui.util.WeatherFormatter
import com.satryo.weatherapp.ui.view.components.atoms.SectionTitle
import com.satryo.weatherapp.ui.view.components.molecules.DailyForecastRow

/**
 * Prakiraan harian dari Daily Forecast 16 Days (default API: 7 hari, dimulai hari ini).
 */
@Composable
fun DailyForecastSection(
    daily: List<DailyForecast>,
    offsetSeconds: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(
                text = "Prakiraan Harian",
                trailingText = "Maks / Min",
            )
            Spacer(Modifier.height(4.dp))
            daily.forEachIndexed { index, forecast ->
                DailyForecastRow(
                    dayLabel = if (index == 0) {
                        "Hari ini"
                    } else {
                        WeatherFormatter.dayName(forecast.time, offsetSeconds)
                    },
                    forecast = forecast,
                    precipitation = WeatherFormatter.rainChance(forecast.precipitationProbability),
                )
                if (index < daily.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
