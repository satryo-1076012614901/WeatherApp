package com.satryo.weatherapp.ui.view.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.satryo.weatherapp.data.model.WeatherDetail
import com.satryo.weatherapp.ui.theme.WeatherAppTheme
import com.satryo.weatherapp.ui.view.PREVIEW_PHONE_HEIGHT_DP
import com.satryo.weatherapp.ui.view.PREVIEW_PHONE_WIDTH_DP
import com.satryo.weatherapp.ui.view.PreviewData
import com.satryo.weatherapp.ui.view.components.molecules.MessageCard
import com.satryo.weatherapp.ui.view.components.organisms.CurrentWeatherHeader
import com.satryo.weatherapp.ui.view.components.organisms.DailyForecastSection
import com.satryo.weatherapp.ui.view.components.organisms.HourlyForecastSection
import com.satryo.weatherapp.ui.view.components.organisms.WeatherMetricsGrid
import com.satryo.weatherapp.ui.viewmodel.AppViewModelProvider
import com.satryo.weatherapp.ui.viewmodel.DetailContentState
import com.satryo.weatherapp.ui.viewmodel.DetailUiState
import com.satryo.weatherapp.ui.viewmodel.DetailViewModel

/**
 * Halaman Detail (stateful): cuaca lengkap untuk lokasi yang dipilih dari Dashboard
 * atau dari hasil pencarian.
 */
@Composable
fun DetailScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DetailContent(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onRetry = viewModel::loadWeather,
        onToggleSaved = viewModel::toggleSaved,
        onMessageShown = viewModel::onMessageShown,
        modifier = modifier,
    )
}

/**
 * Tampilan Detail (stateless) agar mudah di-preview dan diuji.
 * Tombol bintang di app bar menyimpan kota ke Dashboard atau menghapusnya.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailContent(
    uiState: DetailUiState,
    onNavigateUp: () -> Unit,
    onRetry: () -> Unit,
    onToggleSaved: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onMessageShown()
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.placeName.ifBlank { "Detail Cuaca" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        uiState.placeSubtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleSaved, enabled = uiState.canSave) {
                        Icon(
                            imageVector = if (uiState.isSaved) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (uiState.isSaved) {
                                "Hapus dari Dashboard"
                            } else {
                                "Simpan ke Dashboard"
                            },
                            tint = if (uiState.isSaved) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                LocalContentColor.current
                            },
                        )
                    }
                    IconButton(onClick = onRetry) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Muat ulang")
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val content = uiState.content) {
            DetailContentState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            is DetailContentState.Error -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                MessageCard(
                    icon = Icons.Outlined.CloudOff,
                    title = "Gagal memuat cuaca",
                    message = content.message,
                    actionLabel = "Coba Lagi",
                    onAction = onRetry,
                )
            }

            is DetailContentState.Success -> DetailBody(
                detail = content.detail,
                innerPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun DetailBody(
    detail: WeatherDetail,
    innerPadding: PaddingValues,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = innerPadding.calculateTopPadding() + 8.dp,
            bottom = innerPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "header") {
            CurrentWeatherHeader(
                current = detail.current,
                today = detail.daily.firstOrNull(),
                offsetSeconds = detail.timezoneOffsetSeconds,
            )
        }
        if (detail.hourly.isNotEmpty()) {
            item(key = "hourly") {
                HourlyForecastSection(
                    hourly = detail.hourly,
                    offsetSeconds = detail.timezoneOffsetSeconds,
                )
            }
        }
        if (detail.daily.isNotEmpty()) {
            item(key = "daily") {
                DailyForecastSection(
                    daily = detail.daily,
                    offsetSeconds = detail.timezoneOffsetSeconds,
                )
            }
        }
        item(key = "metrics") {
            WeatherMetricsGrid(
                current = detail.current,
                offsetSeconds = detail.timezoneOffsetSeconds,
            )
        }
        item(key = "source") {
            Text(
                text = "Sumber data: OpenWeather. Jam mengikuti zona waktu lokasi.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Preview
// Yang di-preview adalah DetailContent (stateless) dengan data contoh dari PreviewData.
// DetailScreen tidak bisa di-preview langsung karena ViewModel-nya membutuhkan
// WeatherApplication (AppContainer), argumen navigasi, DataStore, dan jaringan.
// ---------------------------------------------------------------------------

/** Tinggi preview halaman penuh: cukup untuk menampilkan semua section hingga grid metrik. */
private const val FULL_PAGE_PREVIEW_HEIGHT_DP = 2000

@Preview(
    name = "Detail - Terang",
    showBackground = true,
    widthDp = PREVIEW_PHONE_WIDTH_DP,
    heightDp = PREVIEW_PHONE_HEIGHT_DP,
)
@Preview(
    name = "Detail - Gelap",
    showBackground = true,
    widthDp = PREVIEW_PHONE_WIDTH_DP,
    heightDp = PREVIEW_PHONE_HEIGHT_DP,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
private fun DetailPreview() {
    DetailPreviewContent(PreviewData.detailState)
}

@Preview(
    name = "Detail - Tersimpan di Dashboard",
    showBackground = true,
    widthDp = PREVIEW_PHONE_WIDTH_DP,
    heightDp = PREVIEW_PHONE_HEIGHT_DP,
)
@Composable
private fun DetailSavedPreview() {
    DetailPreviewContent(PreviewData.detailState.copy(isSaved = true))
}

@Preview(
    name = "Detail - Halaman penuh",
    showBackground = true,
    widthDp = PREVIEW_PHONE_WIDTH_DP,
    heightDp = FULL_PAGE_PREVIEW_HEIGHT_DP,
)
@Composable
private fun DetailFullPagePreview() {
    DetailPreviewContent(PreviewData.detailState)
}

@Preview(
    name = "Detail - Memuat",
    showBackground = true,
    widthDp = PREVIEW_PHONE_WIDTH_DP,
    heightDp = PREVIEW_PHONE_HEIGHT_DP,
)
@Composable
private fun DetailLoadingPreview() {
    DetailPreviewContent(PreviewData.detailLoadingState)
}

@Preview(
    name = "Detail - Gagal memuat",
    showBackground = true,
    widthDp = PREVIEW_PHONE_WIDTH_DP,
    heightDp = PREVIEW_PHONE_HEIGHT_DP,
)
@Composable
private fun DetailErrorPreview() {
    DetailPreviewContent(PreviewData.detailErrorState)
}

/** Pembungkus preview: tema aplikasi dan callback kosong. */
@Composable
private fun DetailPreviewContent(uiState: DetailUiState) {
    WeatherAppTheme {
        DetailContent(
            uiState = uiState,
            onNavigateUp = {},
            onRetry = {},
            onToggleSaved = {},
            onMessageShown = {},
        )
    }
}
