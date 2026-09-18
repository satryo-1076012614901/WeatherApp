package com.satryo.weatherapp.ui.view.components.atoms

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.satryo.weatherapp.ui.util.WeatherFormatter

/**
 * Teks suhu dalam format "28°".
 */
@Composable
fun TemperatureText(
    celsius: Double?,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
) {
    Text(
        text = WeatherFormatter.temperature(celsius),
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight,
    )
}
