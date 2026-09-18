package com.satryo.weatherapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satryo.weatherapp.data.model.Country
import com.satryo.weatherapp.data.model.LocationPermissionException
import com.satryo.weatherapp.data.model.Place
import com.satryo.weatherapp.data.model.WeatherSummary
import com.satryo.weatherapp.data.repository.CountryRepository
import com.satryo.weatherapp.data.repository.LocationRepository
import com.satryo.weatherapp.data.repository.SavedLocationRepository
import com.satryo.weatherapp.data.repository.WeatherRepository
import com.satryo.weatherapp.ui.util.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/** Status pemuatan cuaca untuk satu card. */
sealed interface WeatherLoadState {
    data object Loading : WeatherLoadState
    data class Success(val summary: WeatherSummary) : WeatherLoadState
    data class Error(val message: String) : WeatherLoadState
}

/** Status card cuaca lokasi perangkat (GPS). */
sealed interface CurrentLocationUiState {
    data object Loading : CurrentLocationUiState
    data object PermissionRequired : CurrentLocationUiState
    data class Success(val place: Place, val summary: WeatherSummary) : CurrentLocationUiState
    data class Error(val message: String) : CurrentLocationUiState
}

data class SavedLocationUiState(
    val place: Place,
    val weather: WeatherLoadState,
)

/**
 * @property savedLocationsLoaded false sampai daftar kota pertama kali terbaca dari DataStore,
 * agar UI tidak sempat menampilkan status "belum ada kota tersimpan".
 * @property isRefreshing true selama refresh tarik-turun (pull-to-refresh) berlangsung.
 */
data class DashboardUiState(
    val currentLocation: CurrentLocationUiState = CurrentLocationUiState.Loading,
    val savedLocations: List<SavedLocationUiState> = emptyList(),
    val savedLocationsLoaded: Boolean = false,
    val isRefreshing: Boolean = false,
)

/**
 * ViewModel halaman Dashboard.
 *
 * Coroutine di `viewModelScope` berjalan di main thread dan hanya dipakai untuk mengatur
 * alur serta memperbarui state UI. Pemanggilan data tidak memblokir main thread karena
 * fungsi repository bersifat suspend dan main-safe (request Retrofit + mapping dipindahkan
 * ke Dispatchers.IO di data layer). Setiap kota dimuat di coroutine terpisah sehingga
 * card tampil satu per satu tanpa saling menunggu.
 */
class DashboardViewModel(
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository,
    private val savedLocationRepository: SavedLocationRepository,
    private val countryRepository: CountryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var currentLocationJob: Job? = null
    private val savedLocationJobs = mutableMapOf<String, Job>()

    init {
        observeSavedLocations()
        loadCurrentLocation()
    }

    /** Memuat ulang cuaca lokasi perangkat. Bila izin belum ada, status menjadi PermissionRequired. */
    fun loadCurrentLocation() {
        startCurrentLocationLoad(showLoading = true)
    }

    /**
     * @param showLoading false saat pull-to-refresh: data lama tetap tampil sampai data baru siap.
     * @return job pemuatan, atau null bila izin lokasi belum ada.
     */
    private fun startCurrentLocationLoad(showLoading: Boolean): Job? {
        currentLocationJob?.cancel()
        if (!locationRepository.hasLocationPermission()) {
            _uiState.update { it.copy(currentLocation = CurrentLocationUiState.PermissionRequired) }
            return null
        }
        val job = viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(currentLocation = CurrentLocationUiState.Loading) }
            }
            val newState = try {
                val coordinates = locationRepository.getCurrentCoordinates()
                // Card GPS juga menampilkan prakiraan per 3 jam (8 slot = 24 jam).
                val summary = weatherRepository.getWeatherSummary(coordinates, hourlyCount = HOURLY_SLOTS)
                // Nama lokasi dan kode negara diambil dari response current weather.
                val place = Place.currentLocation(
                    coordinates = coordinates,
                    name = summary.locationName,
                    countryCode = summary.countryCode,
                )
                CurrentLocationUiState.Success(place, summary)
            } catch (e: CancellationException) {
                throw e
            } catch (e: LocationPermissionException) {
                CurrentLocationUiState.PermissionRequired
            } catch (e: SecurityException) {
                CurrentLocationUiState.PermissionRequired
            } catch (e: Exception) {
                CurrentLocationUiState.Error(e.toUserMessage())
            }
            _uiState.update { it.copy(currentLocation = newState) }

            if (newState is CurrentLocationUiState.Success) {
                val country = findCountry(newState.place.country) ?: return@launch
                _uiState.update { state ->
                    val current = state.currentLocation
                    if (current is CurrentLocationUiState.Success) {
                        state.copy(currentLocation = current.copy(place = current.place.withCountry(country)))
                    } else {
                        state
                    }
                }
            }
        }
        currentLocationJob = job
        return job
    }

    /** Hasil dialog izin lokasi dari UI. */
    fun onLocationPermissionResult(granted: Boolean) {
        if (granted) {
            loadCurrentLocation()
        } else {
            _uiState.update { it.copy(currentLocation = CurrentLocationUiState.PermissionRequired) }
        }
    }

    /** Dipanggil saat layar aktif kembali, mis. setelah pengguna memberi izin lewat Pengaturan. */
    fun onScreenResumed() {
        val waitingPermission =
            _uiState.value.currentLocation is CurrentLocationUiState.PermissionRequired
        if (waitingPermission && locationRepository.hasLocationPermission()) {
            loadCurrentLocation()
        }
    }

    /**
     * Pull-to-refresh: memuat ulang semua card secara paralel. Indikator refresh
     * ditampilkan sampai semua pemuatan selesai; data lama tetap tampil selama proses.
     */
    fun refresh() {
        if (_uiState.value.isRefreshing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val jobs = buildList {
                startCurrentLocationLoad(showLoading = false)?.let { add(it) }
                _uiState.value.savedLocations.forEach { item ->
                    add(loadSavedLocationWeather(item.place, showLoading = false))
                }
            }
            jobs.joinAll()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun retrySavedLocation(placeId: String) {
        _uiState.value.savedLocations
            .firstOrNull { it.place.id == placeId }
            ?.let { loadSavedLocationWeather(it.place) }
    }

    private fun observeSavedLocations() {
        viewModelScope.launch {
            savedLocationRepository.savedLocations.collect { places ->
                val previous = _uiState.value.savedLocations.associateBy { it.place.id }

                _uiState.update { state ->
                    state.copy(
                        savedLocations = places.map { place ->
                            val old = previous[place.id]
                            // Pertahankan status cuaca dan info negara yang sudah dimuat.
                            old?.copy(place = place.keepCountryInfoFrom(old.place))
                                ?: SavedLocationUiState(place, WeatherLoadState.Loading)
                        },
                        savedLocationsLoaded = true,
                    )
                }

                // Hentikan request untuk lokasi yang sudah dihapus, lalu muat lokasi baru.
                val activeIds = places.map { it.id }.toSet()
                (savedLocationJobs.keys - activeIds).forEach { id ->
                    savedLocationJobs.remove(id)?.cancel()
                }
                places.filter { it.id !in previous }.forEach { loadSavedLocationWeather(it) }
            }
        }
    }

    private fun loadSavedLocationWeather(place: Place, showLoading: Boolean = true): Job {
        savedLocationJobs[place.id]?.cancel()
        val job = viewModelScope.launch {
            if (showLoading) {
                updateSavedLocation(place.id) { it.copy(weather = WeatherLoadState.Loading) }
            }
            val result = try {
                WeatherLoadState.Success(weatherRepository.getWeatherSummary(place.coordinates))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                WeatherLoadState.Error(e.toUserMessage())
            }
            updateSavedLocation(place.id) { it.copy(weather = result) }

            // Info negara dimuat setelah cuaca agar tidak menunda tampilnya data cuaca.
            val country = findCountry(place.country) ?: return@launch
            updateSavedLocation(place.id) { it.copy(place = it.place.withCountry(country)) }
        }
        savedLocationJobs[place.id] = job
        return job
    }

    private fun updateSavedLocation(
        placeId: String,
        transform: (SavedLocationUiState) -> SavedLocationUiState,
    ) {
        _uiState.update { state ->
            state.copy(
                savedLocations = state.savedLocations.map { item ->
                    if (item.place.id == placeId) transform(item) else item
                },
            )
        }
    }

    /**
     * Nama negara dan bendera bersifat pelengkap: bila gagal dimuat,
     * UI tetap menampilkan kode negara (mis. "ID") tanpa pesan error.
     */
    private suspend fun findCountry(countryCode: String?): Country? {
        if (countryCode.isNullOrBlank()) return null
        return try {
            countryRepository.getCountry(countryCode)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    private companion object {
        /** Jumlah slot prakiraan 3 jam pada card GPS (8 x 3 jam = 24 jam). */
        const val HOURLY_SLOTS = 8
    }
}
