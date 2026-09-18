package com.satryo.weatherapp.ui.view.components.molecules

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.satryo.weatherapp.data.model.DailyForecast
import com.satryo.weatherapp.ui.util.WeatherFormatter
import com.satryo.weatherapp.ui.view.components.atoms.WeatherIcon

/**
 * Satu baris prakiraan harian: hari, peluang hujan, ikon, suhu maks dan min.
 *
 * @param precipitation teks peluang hujan; null bila tidak perlu ditampilkan.
 */
@Composable
fun DailyForecastRow(
    dayLabel: String,
    forecast: DailyForecast,
    precipitation: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = precipitation.orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End,
            modifier = Modifier.width(44.dp),
        )
        WeatherIcon(
            condition = forecast.condition,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(40.dp),
        )
        Text(
            text = WeatherFormatter.temperature(forecast.maxTemperature),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp),
        )
        Text(
            text = WeatherFormatter.temperature(forecast.minTemperature),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp),
        )
    }
}
