package com.satryo.weatherapp.ui.view.components.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.satryo.weatherapp.data.model.CurrentWeather
import com.satryo.weatherapp.ui.util.WeatherFormatter
import com.satryo.weatherapp.ui.view.components.atoms.SectionTitle
import com.satryo.weatherapp.ui.view.components.molecules.WeatherMetricTile

private data class Metric(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val caption: String? = null,
)

/**
 * Grid dua kolom berisi metrik cuaca saat ini (endpoint current weather).
 * Grid disusun manual (Row per 2 item) agar aman di dalam LazyColumn.
 */
@Composable
fun WeatherMetricsGrid(
    current: CurrentWeather,
    offsetSeconds: Int,
    modifier: Modifier = Modifier,
) {
    val metrics = listOf(
        Metric(
            icon = Icons.Outlined.Thermostat,
            label = "Terasa seperti",
            value = WeatherFormatter.temperature(current.feelsLike),
        ),
        Metric(
            icon = Icons.Outlined.WaterDrop,
            label = "Kelembapan",
            value = WeatherFormatter.percentage(current.humidity),
        ),
        Metric(
            icon = Icons.Outlined.Air,
            label = "Angin",
            value = WeatherFormatter.windSpeed(current.windSpeed),
            caption = windCaption(current),
        ),
        Metric(
            icon = Icons.Outlined.Speed,
            label = "Tekanan",
            value = WeatherFormatter.pressure(current.pressure),
        ),
        Metric(
            icon = Icons.Outlined.Cloud,
            label = "Tutupan awan",
            value = WeatherFormatter.percentage(current.cloudiness),
        ),
        Metric(
            icon = Icons.Outlined.Visibility,
            label = "Jarak pandang",
            value = WeatherFormatter.visibility(current.visibility),
        ),
        Metric(
            icon = Icons.Outlined.WbTwilight,
            label = "Matahari terbit",
            value = WeatherFormatter.time(current.sunrise, offsetSeconds),
        ),
        Metric(
            icon = Icons.Outlined.NightsStay,
            label = "Matahari terbenam",
            value = WeatherFormatter.time(current.sunset, offsetSeconds),
        ),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionTitle(text = "Detail Cuaca Saat Ini")
        metrics.chunked(2).forEach { rowMetrics ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowMetrics.forEach { metric ->
                    WeatherMetricTile(
                        icon = metric.icon,
                        label = metric.label,
                        value = metric.value,
                        caption = metric.caption,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
                if (rowMetrics.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/** Contoh: "Dari Utara · hembusan 20 km/j". Hembusan hanya tampil bila dikirim API. */
private fun windCaption(current: CurrentWeather): String? {
    val direction = WeatherFormatter.windDirection(current.windDegree)?.let { "Dari $it" }
    val gust = current.windGust?.let { "hembusan ${WeatherFormatter.windSpeed(it)}" }
    return listOfNotNull(direction, gust).joinToString(" · ").ifBlank { null }
}
