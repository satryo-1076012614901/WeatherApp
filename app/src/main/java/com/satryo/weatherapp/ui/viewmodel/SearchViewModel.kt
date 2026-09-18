package com.satryo.weatherapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satryo.weatherapp.data.model.Country
import com.satryo.weatherapp.data.model.Place
import com.satryo.weatherapp.data.repository.CountryRepository
import com.satryo.weatherapp.data.repository.LocationRepository
import com.satryo.weatherapp.ui.util.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/** Status hasil pencarian kota. */
sealed interface SearchResultState {
    /** Kata kunci kurang dari [SearchViewModel.MIN_QUERY_LENGTH] huruf. */
    data object Idle : SearchResultState
    data object Loading : SearchResultState
    data class Success(val places: List<Place>) : SearchResultState
    data class Empty(val query: String) : SearchResultState
    data class Error(val message: String) : SearchResultState
}

data class SearchUiState(
    val query: String = "",
    val result: SearchResultState = SearchResultState.Idle,
)

/**
 * ViewModel halaman Search: mencari kota lewat endpoint Direct geocoding.
 *
 * - Pencarian baru dijalankan bila kata kunci minimal [MIN_QUERY_LENGTH] huruf.
 * - Saat mengetik, pencarian ditunda [DEBOUNCE_MILLIS] ms (debounce) dan pencarian
 *   sebelumnya dibatalkan, sehingga API tidak dipanggil pada setiap huruf.
 */
class SearchViewModel(
    private val locationRepository: LocationRepository,
    private val countryRepository: CountryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        startSearch(query, debounce = true)
    }

    /** Tombol "Cari" pada keyboard: cari langsung tanpa jeda. */
    fun onSearchAction() {
        startSearch(_uiState.value.query, debounce = false)
    }

    fun retry() {
        startSearch(_uiState.value.query, debounce = false)
    }

    fun clearQuery() {
        onQueryChange("")
    }

    private fun startSearch(rawQuery: String, debounce: Boolean) {
        searchJob?.cancel()
        val query = rawQuery.trim()
        if (query.length < MIN_QUERY_LENGTH) {
            _uiState.update { it.copy(result = SearchResultState.Idle) }
            return
        }
        searchJob = viewModelScope.launch {
            if (debounce) delay(DEBOUNCE_MILLIS)
            _uiState.update { it.copy(result = SearchResultState.Loading) }
            val result = try {
                val places = locationRepository.searchCity(query)
                if (places.isEmpty()) {
                    SearchResultState.Empty(query)
                } else {
                    SearchResultState.Success(places)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                SearchResultState.Error(e.toUserMessage())
            }
            _uiState.update { it.copy(result = result) }

            if (result is SearchResultState.Success) addCountryInfo(result.places)
        }
    }

    /** Melengkapi nama negara + bendera pada hasil pencarian (sekali request per kode negara). */
    private suspend fun addCountryInfo(places: List<Place>) {
        val countries = places.mapNotNull { it.country }.distinct().mapNotNull { findCountry(it) }
        if (countries.isEmpty()) return
        _uiState.update { state ->
            val current = state.result as? SearchResultState.Success ?: return@update state
            state.copy(
                result = current.copy(
                    places = current.places.map { place ->
                        countries.fold(place) { enriched, country -> enriched.withCountry(country) }
                    },
                ),
            )
        }
    }

    private suspend fun findCountry(countryCode: String): Country? = try {
        countryRepository.getCountry(countryCode)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    companion object {
        const val MIN_QUERY_LENGTH = 3
        const val DEBOUNCE_MILLIS = 500L
    }
}
