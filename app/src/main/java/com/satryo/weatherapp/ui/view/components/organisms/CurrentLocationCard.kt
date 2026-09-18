package com.satryo.weatherapp.ui.view.components.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.satryo.weatherapp.data.model.Place
import com.satryo.weatherapp.data.model.WeatherSummary
import com.satryo.weatherapp.ui.theme.skyGradient
import com.satryo.weatherapp.ui.util.WeatherFormatter
import com.satryo.weatherapp.ui.view.components.atoms.TemperatureText
import com.satryo.weatherapp.ui.view.components.atoms.WeatherIcon
import com.satryo.weatherapp.ui.view.components.molecules.HourlyForecastItem
import com.satryo.weatherapp.ui.view.components.molecules.MessageCard
import com.satryo.weatherapp.ui.viewmodel.CurrentLocationUiState

private val HeroShape = RoundedCornerShape(28.dp)

/**
 * Card utama Dashboard: cuaca di lokasi perangkat beserta prakiraan per 3 jam,
 * lengkap dengan status izin, loading, dan error.
 */
@Composable
fun CurrentLocationCard(
    state: CurrentLocationUiState,
    onPlaceClick: (Place) -> Unit,
    onRequestPermission: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        CurrentLocationUiState.Loading -> LoadingHeroCard(modifier)

        CurrentLocationUiState.PermissionRequired -> MessageCard(
            icon = Icons.Outlined.LocationOff,
            title = "Izin lokasi dibutuhkan",
            message = "Izinkan akses lokasi agar aplikasi dapat menampilkan cuaca di sekitar Anda.",
            actionLabel = "Izinkan Lokasi",
            onAction = onRequestPermission,
            modifier = modifier,
        )

        is CurrentLocationUiState.Error -> MessageCard(
            icon = Icons.Outlined.CloudOff,
            title = "Cuaca lokasi Anda gagal dimuat",
            message = state.message,
            actionLabel = "Coba Lagi",
            onAction = onRetry,
            modifier = modifier,
        )

        is CurrentLocationUiState.Success -> WeatherHeroCard(
            place = state.place,
            summary = state.summary,
            onClick = { onPlaceClick(state.place) },
            modifier = modifier,
        )
    }
}

@Composable
private fun LoadingHeroCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(HeroShape)
            .background(skyGradient(isNight = false)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color.White)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Mencari lokasi Anda…",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}

@Composable
private fun WeatherHeroCard(
    place: Place,
    summary: WeatherSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val current = summary.current
    val secondaryText = Color.White.copy(alpha = 0.85f)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = HeroShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(skyGradient(current.condition.isNight))
                .padding(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.NearMe,
                    contentDescription = null,
                    tint = secondaryText,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Lokasi Anda",
                    style = MaterialTheme.typography.labelLarge,
                    color = secondaryText,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = place.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            place.subtitle?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = secondaryText)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TemperatureText(
                    celsius = current.temperature,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.weight(1f),
                )
                WeatherIcon(
                    condition = current.condition,
                    modifier = Modifier.size(96.dp),
                )
            }
            Text(
                text = WeatherFormatter.description(current.condition.description),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = WeatherFormatter.highLow(
                    max = summary.todayMaxTemperature,
                    min = summary.todayMinTemperature,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryText,
            )
            if (summary.hourly.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Prakiraan per 3 jam",
                    style = MaterialTheme.typography.labelLarge,
                    color = secondaryText,
                )
                LazyRow(modifier = Modifier.padding(top = 4.dp)) {
                    items(summary.hourly, key = { it.time }) { item ->
                        HourlyForecastItem(
                            label = WeatherFormatter.time(item.time, summary.timezoneOffsetSeconds),
                            temperature = item.temperature,
                            condition = item.condition,
                            precipitation = WeatherFormatter.rainChance(item.precipitationProbability),
                            labelColor = secondaryText,
                            accentColor = Color.White,
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Ketuk untuk melihat detail",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}
