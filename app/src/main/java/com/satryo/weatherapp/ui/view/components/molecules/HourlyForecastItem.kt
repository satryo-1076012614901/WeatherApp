package com.satryo.weatherapp.ui.view.components.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.satryo.weatherapp.data.model.WeatherCondition
import com.satryo.weatherapp.ui.view.components.atoms.TemperatureText
import com.satryo.weatherapp.ui.view.components.atoms.WeatherIcon

/**
 * Satu kolom prakiraan: jam, ikon, peluang hujan, dan suhu.
 *
 * @param precipitation teks peluang hujan; null bila tidak perlu ditampilkan.
 * @param labelColor warna teks jam. Di atas latar gradasi (card GPS) gunakan warna terang.
 * @param accentColor warna teks peluang hujan.
 */
@Composable
fun HourlyForecastItem(
    label: String,
    temperature: Double,
    condition: WeatherCondition,
    precipitation: String?,
    modifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Column(
        modifier = modifier
            .width(68.dp)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = labelColor,
        )
        WeatherIcon(condition = condition, modifier = Modifier.size(44.dp))
        // Spasi tetap dipertahankan agar semua kolom sejajar.
        Text(
            text = precipitation ?: " ",
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
        )
        TemperatureText(
            celsius = temperature,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
