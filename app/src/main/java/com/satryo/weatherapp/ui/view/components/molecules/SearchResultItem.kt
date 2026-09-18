package com.satryo.weatherapp.ui.view.components.molecules

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.satryo.weatherapp.data.model.Place

/**
 * Satu baris hasil pencarian kota: nama kota dan keterangan wilayah/negara.
 */
@Composable
fun SearchResultItem(
    place: Place,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitle = place.subtitle
    ListItem(
        headlineContent = {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = if (subtitle != null) {
            { Text(text = subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        } else {
            null
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        modifier = modifier.clickable(onClick = onClick),
    )
}
