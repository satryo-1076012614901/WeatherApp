package com.satryo.weatherapp.ui.view.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.satryo.weatherapp.data.model.Place
import com.satryo.weatherapp.ui.theme.WeatherAppTheme
import com.satryo.weatherapp.ui.util.WeatherFormatter
import com.satryo.weatherapp.ui.view.PREVIEW_PHONE_HEIGHT_DP
import com.satryo.weatherapp.ui.view.PREVIEW_PHONE_WIDTH_DP
import com.satryo.weatherapp.ui.view.PreviewData
import com.satryo.weatherapp.ui.view.components.atoms.SectionTitle
import com.satryo.weatherapp.ui.view.components.organisms.CurrentLocationCard
import com.satryo.weatherapp.ui.view.components.organisms.SavedLocationCard
import com.satryo.weatherapp.ui.viewmodel.AppViewModelProvider
import com.satryo.weatherapp.ui.viewmodel.CurrentLocationUiState
import com.satryo.weatherapp.ui.viewmodel.DashboardUiState
import com.satryo.weatherapp.ui.viewmodel.DashboardViewModel

private val LocationPermissions = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
)

/** Jarak tepi horizontal konten Dashboard. */
private val ScreenPadding = 16.dp

/**
 * Halaman Dashboard (stateful): menghubungkan ViewModel, izin lokasi, dan UI.
 */
@Composable
fun DashboardScreen(
    onPlaceClick: (Place) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current

    var hasAutoRequested by rememberSaveable { mutableStateOf(false) }
    var isPermanentlyDenied by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result.values.any { it }
        // Setelah ditolak, bila sistem tidak lagi menampilkan rationale,
        // dialog izin tidak akan muncul lagi: arahkan pengguna ke Pengaturan.
        isPermanentlyDenied = !granted && activity != null &&
            LocationPermissions.none { activity.shouldShowRequestPermissionRationale(it) }
        viewModel.onLocationPermissionResult(granted)
    }

    // Minta izin lokasi otomatis satu kali saat Dashboard pertama kali dibuka.
    LaunchedEffect(uiState.currentLocation) {
        if (uiState.currentLocation is CurrentLocationUiState.PermissionRequired && !hasAutoRequested) {
            hasAutoRequested = true
            permissionLauncher.launch(LocationPermissions)
        }
    }

    // Periksa ulang izin saat pengguna kembali dari Pengaturan.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onScreenResumed()
    }

    DashboardContent(
        uiState = uiState,
        onPlaceClick = onPlaceClick,
        onSearchClick = onSearchClick,
        onRefresh = viewModel::refresh,
        onRequestPermission = {
            if (isPermanentlyDenied) {
                context.openAppSettings()
            } else {
                permissionLauncher.launch(LocationPermissions)
            }
        },
        onRetryCurrentLocation = viewModel::loadCurrentLocation,
        onRetrySavedLocation = viewModel::retrySavedLocation,
        modifier = modifier,
    )
}

/**
 * Tampilan Dashboard (stateless) agar mudah di-preview dan diuji.
 * Refresh dilakukan dengan gestur tarik ke bawah (pull-to-refresh).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onPlaceClick: (Place) -> Unit,
    onSearchClick: () -> Unit,
    onRefresh: () -> Unit,
    onRequestPermission: () -> Unit,
    onRetryCurrentLocation: () -> Unit,
    onRetrySavedLocation: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Cuaca",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = WeatherFormatter.today(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = "Cari kota")
                    }
                },
            )
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "section_current_location") {
                    CurrentLocationCard(
                        state = uiState.currentLocation,
                        onPlaceClick = onPlaceClick,
                        onRequestPermission = onRequestPermission,
                        onRetry = onRetryCurrentLocation,
                        modifier = Modifier.padding(horizontal = ScreenPadding),
                    )
                }

                item(key = "section_saved_title") {
                    SectionTitle(
                        text = "Lokasi Tersimpan",
                        trailingText = if (uiState.savedLocationsLoaded) {
                            "${uiState.savedLocations.size} kota"
                        } else {
                            null
                        },
                        modifier = Modifier.padding(start = ScreenPadding, end = ScreenPadding, top = 12.dp),
                    )
                }

                when {
                    // Daftar dari DataStore belum terbaca: jangan tampilkan status kosong dulu.
                    !uiState.savedLocationsLoaded -> Unit

                    uiState.savedLocations.isEmpty() -> item(key = "section_saved_empty") {
                        Text(
                            text = "Belum ada kota tersimpan. Cari kota lewat ikon Cari, " +
                                "lalu tekan ikon bintang di halaman Detail untuk menyimpannya.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = ScreenPadding),
                        )
                    }

                    else -> item(key = "section_saved_list") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = ScreenPadding),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(items = uiState.savedLocations, key = { it.place.id }) { item ->
                                SavedLocationCard(
                                    item = item,
                                    onClick = { onPlaceClick(item.place) },
                                    onRetry = { onRetrySavedLocation(item.place.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

// ---------------------------------------------------------------------------
// Preview
// Yang di-preview adalah DashboardContent (stateless) dengan data contoh dari PreviewData.
// DashboardScreen tidak bisa di-preview langsung karena ViewModel-nya membutuhkan
// WeatherApplication (AppContainer), GPS, DataStore, dan jaringan, yang tidak tersedia di Preview.
// ---------------------------------------------------------------------------

@Preview(
    name = "Dashboard - Terang",
    showBackground = true,
    widthDp = PREVIEW_PHONE_WIDTH_DP,
    heightDp = PREVIEW_PHONE_HEIGHT_DP,
)
@Preview(
    name = "Dashboard - Gelap",
    showBackground = true,
    widthDp = PREVIEW_PHONE_WIDTH_DP,
    heightDp = PREVIEW_PHONE_HEIGHT_DP,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
private fun DashboardPreview() {
    DashboardPreviewContent(PreviewData.dashboardState)
}

@Preview(
    name = "Dashboard - Refresh tarik-turun",
    showBackground = true,
    widthDp = PREVIEW_PHONE_WIDTH_DP,
    heightDp = PREVIEW_PHONE_HEIGHT_DP,
)
@Composable
private fun DashboardRefreshingPreview() {
    DashboardPreviewContent(PreviewData.dashboardState.copy(isRefreshing = true))
}

@Preview(
    name = "Dashboard - Memuat",
    showBackground = true,
    widthDp = PREVIEW_PHONE_WIDTH_DP,
    heightDp = PREVIEW_PHONE_HEIGHT_DP,
)
@Composable
private fun DashboardLoadingPreview() {
    DashboardPreviewContent(PreviewData.dashboardLoadingState)
}

@Preview(
    name = "Dashboard - Izin lokasi",
    showBackground = true,
    widthDp = PREVIEW_PHONE_WIDTH_DP,
    heightDp = PREVIEW_PHONE_HEIGHT_DP,
)
@Composable
private fun DashboardPermissionPreview() {
    DashboardPreviewContent(PreviewData.dashboardPermissionState)
}

@Preview(
    name = "Dashboard - Gagal memuat",
    showBackground = true,
    widthDp = PREVIEW_PHONE_WIDTH_DP,
    heightDp = PREVIEW_PHONE_HEIGHT_DP,
)
@Composable
private fun DashboardErrorPreview() {
    DashboardPreviewContent(PreviewData.dashboardErrorState)
}

@Preview(
    name = "Dashboard - Tanpa kota tersimpan",
    showBackground = true,
    widthDp = PREVIEW_PHONE_WIDTH_DP,
    heightDp = PREVIEW_PHONE_HEIGHT_DP,
)
@Composable
private fun DashboardEmptyPreview() {
    DashboardPreviewContent(PreviewData.dashboardEmptyState)
}

/** Pembungkus preview: tema aplikasi dan callback kosong. */
@Composable
private fun DashboardPreviewContent(uiState: DashboardUiState) {
    WeatherAppTheme {
        DashboardContent(
            uiState = uiState,
            onPlaceClick = {},
            onSearchClick = {},
            onRefresh = {},
            onRequestPermission = {},
            onRetryCurrentLocation = {},
            onRetrySavedLocation = {},
        )
    }
}
