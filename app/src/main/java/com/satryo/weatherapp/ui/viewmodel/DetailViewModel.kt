package com.satryo.weatherapp.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satryo.weatherapp.data.model.Coordinates
import com.satryo.weatherapp.data.model.Place
import com.satryo.weatherapp.data.model.WeatherDetail
import com.satryo.weatherapp.data.repository.CountryRepository
import com.satryo.weatherapp.data.repository.SavedLocationRepository
import com.satryo.weatherapp.data.repository.WeatherRepository
import com.satryo.weatherapp.navigation.DetailDestination
import com.satryo.weatherapp.ui.util.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

sealed interface DetailContentState {
    data object Loading : DetailContentState
    data class Success(val detail: WeatherDetail) : DetailContentState
    data class Error(val message: String) : DetailContentState
}

/**
 * @property isSaved true bila kota ini ada di daftar Dashboard (bintang terisi).
 * @property canSave false bila argumen lokasi tidak valid, sehingga bintang dinonaktifkan.
 * @property message pesan singkat untuk snackbar (mis. setelah menekan bintang); null = tidak ada.
 */
data class DetailUiState(
    val placeName: String,
    val placeSubtitle: String?,
    val isSaved: Boolean = false,
    val canSave: Boolean = true,
    val message: String? = null,
    val content: DetailContentState = DetailContentState.Loading,
)

/**
 * ViewModel halaman Detail.
 *
 * - Argumen navigasi (koordinat, ID, nama, wilayah, kode negara) dibaca dari [SavedStateHandle]
 *   yang diisi otomatis oleh Navigation Component.
 * - Data dimuat di `viewModelScope`; repository menjalankan request Retrofit di Dispatchers.IO
 *   sehingga main thread tidak terbebani.
 * - Tombol bintang menambah/menghapus kota dari daftar Dashboard (DataStore).
 */
class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val weatherRepository: WeatherRepository,
    private val savedLocationRepository: SavedLocationRepository,
    private val countryRepository: CountryRepository,
) : ViewModel() {

    /** Lokasi halaman ini; null bila koordinat pada argumen navigasi tidak valid. */
    private val place: Place? = savedStateHandle.toPlace()

    private val _uiState = MutableStateFlow(
        DetailUiState(
            placeName = place?.name.orEmpty(),
            placeSubtitle = place?.subtitle,
            canSave = place != null,
        ),
    )
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    /** Kota di daftar Dashboard yang cocok dengan lokasi ini (null = belum disimpan). */
    private var matchedSavedPlace: Place? = null
    private var loadJob: Job? = null
    private var toggleJob: Job? = null

    init {
        loadWeather()
        observeSavedState()
        loadCountryInfo()
    }

    fun loadWeather() {
        val target = place
        if (target == null) {
            _uiState.update {
                it.copy(content = DetailContentState.Error("Koordinat lokasi tidak valid."))
            }
            return
        }

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(content = DetailContentState.Loading) }
            val content = try {
                DetailContentState.Success(weatherRepository.getWeatherDetail(target.coordinates))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DetailContentState.Error(e.toUserMessage())
            }
            _uiState.update { it.copy(content = content) }
        }
    }

    /** Bintang ditekan: simpan kota ke Dashboard, atau hapus bila sudah tersimpan. */
    fun toggleSaved() {
        val target = place ?: return
        if (toggleJob?.isActive == true) return // abaikan ketukan ganda selama proses simpan
        toggleJob = viewModelScope.launch {
            val existing = matchedSavedPlace
            val message = try {
                if (existing != null) {
                    savedLocationRepository.removeLocation(existing.id)
                    "${target.name} dihapus dari Dashboard"
                } else {
                    savedLocationRepository.addLocation(target.toSavedPlace())
                    "${target.name} disimpan ke Dashboard"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                "Gagal memperbarui daftar kota: ${e.message ?: e.javaClass.simpleName}"
            }
            _uiState.update { it.copy(message = message) }
        }
    }

    /** Dipanggil UI setelah snackbar ditampilkan. */
    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    /** Status bintang mengikuti isi DataStore, termasuk perubahan dari halaman lain. */
    private fun observeSavedState() {
        val target = place ?: return
        viewModelScope.launch {
            savedLocationRepository.savedLocations.collect { saved ->
                matchedSavedPlace = saved.firstOrNull { it.isSameLocationAs(target) }
                _uiState.update { it.copy(isSaved = matchedSavedPlace != null) }
            }
        }
    }

    /** Melengkapi keterangan lokasi dengan nama negara + bendera (hasilnya di-cache repository). */
    private fun loadCountryInfo() {
        val target = place ?: return
        val countryCode = target.country ?: return
        viewModelScope.launch {
            val country = try {
                countryRepository.getCountry(countryCode)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null // info negara bersifat pelengkap
            } ?: return@launch
            _uiState.update { it.copy(placeSubtitle = target.withCountry(country).subtitle) }
        }
    }
}

/** Membentuk [Place] dari argumen route Detail; null bila koordinat tidak valid. */
private fun SavedStateHandle.toPlace(): Place? {
    val latitude = get<String>(DetailDestination.ARG_LAT)?.toDoubleOrNull() ?: return null
    val longitude = get<String>(DetailDestination.ARG_LON)?.toDoubleOrNull() ?: return null
    val coordinates = Coordinates(latitude, longitude)
    return Place(
        id = get<String>(DetailDestination.ARG_ID)?.takeIf { it.isNotBlank() } ?: Place.idFor(coordinates),
        name = get<String>(DetailDestination.ARG_NAME)?.takeIf { it.isNotBlank() }
            ?: Place.idFor(coordinates),
        region = get<String>(DetailDestination.ARG_REGION)?.takeIf { it.isNotBlank() },
        country = get<String>(DetailDestination.ARG_COUNTRY)?.takeIf { it.isNotBlank() },
        coordinates = coordinates,
    )
}
