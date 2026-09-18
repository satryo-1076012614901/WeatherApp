package com.satryo.weatherapp.ui.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.satryo.weatherapp.data.model.Place
import com.satryo.weatherapp.ui.theme.WeatherAppTheme
import com.satryo.weatherapp.ui.view.PREVIEW_PHONE_HEIGHT_DP
import com.satryo.weatherapp.ui.view.PREVIEW_PHONE_WIDTH_DP
import com.satryo.weatherapp.ui.view.PreviewData
import com.satryo.weatherapp.ui.view.components.molecules.MessageCard
import com.satryo.weatherapp.ui.view.components.molecules.SearchResultItem
import com.satryo.weatherapp.ui.viewmodel.AppViewModelProvider
import com.satryo.weatherapp.ui.viewmodel.SearchResultState
import com.satryo.weatherapp.ui.viewmodel.SearchUiState
import com.satryo.weatherapp.ui.viewmodel.SearchViewModel

/**
 * Halaman Search (stateful): mencari kota lewat endpoint Direct geocoding.
 * Memilih hasil membuka halaman Detail kota tersebut.
 */
@Composable
fun SearchScreen(
    onPlaceClick: (Place) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    SearchContent(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onSearchAction = {
            keyboardController?.hide()
            viewModel.onSearchAction()
        },
        onClearQuery = viewModel::clearQuery,
        onRetry = viewModel::retry,
        onPlaceClick = { place ->
            keyboardController?.hide()
            onPlaceClick(place)
        },
        onNavigateUp = onNavigateUp,
        modifier = modifier,
    )
}

/**
 * Tampilan Search (stateless) agar mudah di-preview dan diuji.
 *
 * @param autoFocus true untuk langsung memfokuskan kolom input dan membuka keyboard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSearchAction: () -> Unit,
    onClearQuery: () -> Unit,
    onRetry: () -> Unit,
    onPlaceClick: (Place) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = true,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                        )
                    }
                },
                title = {
                    val focusRequester = remember { FocusRequester() }
                    TextField(
                        value = uiState.query,
                        onValueChange = onQueryChange,
                        placeholder = { Text("Cari nama kota…") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearchAction() }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                    )
                    if (autoFocus) {
                        LaunchedEffect(Unit) { focusRequester.requestFocus() }
                    }
                },
                actions = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = onClearQuery) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Hapus kata kunci")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (val result = uiState.result) {
                SearchResultState.Idle -> SearchHint(query = uiState.query)

                SearchResultState.Loading -> CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp),
                )

                is SearchResultState.Empty -> MessageCard(
                    icon = Icons.Outlined.SearchOff,
                    title = "Kota tidak ditemukan",
                    message = "Tidak ada kota dengan nama \"${result.query}\". Periksa ejaan atau coba nama lain.",
                    modifier = Modifier.padding(16.dp),
                )

                is SearchResultState.Error -> MessageCard(
                    icon = Icons.Outlined.CloudOff,
                    title = "Pencarian gagal",
                    message = result.message,
                    actionLabel = "Coba Lagi",
                    onAction = onRetry,
                    modifier = Modifier.padding(16.dp),
                )

                is SearchResultState.Success -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items = result.places, key = { it.id }) { place ->
                        SearchResultItem(place = place, onClick = { onPlaceClick(place) })
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

/** Petunjuk saat kata kunci belum mencapai jumlah huruf minimal. */
@Composable
private fun SearchHint(query: String, modifier: Modifier = Modifier) {
    val remaining = SearchViewModel.MIN_QUERY_LENGTH - query.trim().length
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = if (query.isBlank()) {
                "Ketik nama kota, minimal ${SearchViewModel.MIN_QUERY_LENGTH} huruf."
            } else {
                "Ketik $remaining huruf lagi untuk mulai mencari."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ---------------------------------------------------------------------------
// Preview (SearchContent stateless dengan data contoh; tanpa autofocus/keyboard)
// ---------------------------------------------------------------------------

@Preview(name = "Search - Petunjuk", showBackground = true, widthDp = PREVIEW_PHONE_WIDTH_DP, heightDp = PREVIEW_PHONE_HEIGHT_DP)
@Composable
private fun SearchHintPreview() {
    SearchPreviewContent(SearchUiState(query = "De"))
}

@Preview(name = "Search - Memuat", showBackground = true, widthDp = PREVIEW_PHONE_WIDTH_DP, heightDp = PREVIEW_PHONE_HEIGHT_DP)
@Composable
private fun SearchLoadingPreview() {
    SearchPreviewContent(SearchUiState(query = "Depok", result = SearchResultState.Loading))
}

@Preview(name = "Search - Hasil", showBackground = true, widthDp = PREVIEW_PHONE_WIDTH_DP, heightDp = PREVIEW_PHONE_HEIGHT_DP)
@Composable
private fun SearchResultPreview() {
    SearchPreviewContent(PreviewData.searchResultState)
}

@Preview(name = "Search - Tidak ditemukan", showBackground = true, widthDp = PREVIEW_PHONE_WIDTH_DP, heightDp = PREVIEW_PHONE_HEIGHT_DP)
@Composable
private fun SearchEmptyPreview() {
    SearchPreviewContent(SearchUiState(query = "Xyzabc", result = SearchResultState.Empty("Xyzabc")))
}

@Preview(name = "Search - Gagal", showBackground = true, widthDp = PREVIEW_PHONE_WIDTH_DP, heightDp = PREVIEW_PHONE_HEIGHT_DP)
@Composable
private fun SearchErrorPreview() {
    SearchPreviewContent(PreviewData.searchErrorState)
}

@Composable
private fun SearchPreviewContent(uiState: SearchUiState) {
    WeatherAppTheme {
        SearchContent(
            uiState = uiState,
            onQueryChange = {},
            onSearchAction = {},
            onClearQuery = {},
            onRetry = {},
            onPlaceClick = {},
            onNavigateUp = {},
            autoFocus = false,
        )
    }
}
