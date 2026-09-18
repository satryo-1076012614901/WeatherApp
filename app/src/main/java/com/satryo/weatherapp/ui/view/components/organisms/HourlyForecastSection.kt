package com.satryo.weatherapp.ui.view.components.organisms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.satryo.weatherapp.data.model.HourlyForecast
import com.satryo.weatherapp.ui.util.WeatherFormatter
import com.satryo.weatherapp.ui.view.components.atoms.SectionTitle
import com.satryo.weatherapp.ui.view.components.molecules.HourlyForecastItem

/** Interval data forecast adalah 3 jam, jadi 8 item = 24 jam ke depan. */
private const val ITEMS_TO_SHOW = 8
private const val INTERVAL_HOURS = 3

/**
 * Prakiraan per 3 jam untuk 24 jam ke depan (scroll horizontal),
 * dari endpoint 5 day / 3 hour forecast.
 */
@Composable
fun HourlyForecastSection(
    hourly: List<HourlyForecast>,
    offsetSeconds: Int,
    modifier: Modifier = Modifier,
) {
    val items = hourly.take(ITEMS_TO_SHOW)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            SectionTitle(
                text = "Prakiraan Per 3 Jam",
                trailingText = "${items.size * INTERVAL_HOURS} jam ke depan",
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = 8.dp)) {
                // Item pertama adalah slot 3 jam berikutnya, bukan kondisi saat ini,
                // sehingga semua item diberi label jam.
                items(items, key = { it.time }) { item ->
                    HourlyForecastItem(
                        label = WeatherFormatter.time(item.time, offsetSeconds),
                        temperature = item.temperature,
                        condition = item.condition,
                        precipitation = WeatherFormatter.rainChance(item.precipitationProbability),
                    )
                }
            }
        }
    }
}
