package com.satryo.weatherapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.satryo.weatherapp.data.model.Coordinates
import com.satryo.weatherapp.data.model.Place
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.io.IOException
import java.lang.reflect.Type

/**
 * Daftar lokasi tersimpan yang tampil di Dashboard (diatur lewat tombol bintang di Detail).
 */
interface SavedLocationRepository {

    val savedLocations: Flow<List<Place>>

    /** Menambah kota; diabaikan bila ID-nya sudah ada. */
    suspend fun addLocation(place: Place)

    suspend fun removeLocation(placeId: String)
}

/**
 * Implementasi persisten dengan Preferences DataStore.
 *
 * - Daftar kota disimpan sebagai JSON (Gson) pada satu key.
 * - Selama key belum pernah ditulis, yang dipakai adalah [defaultLocations] (5 kota default).
 *   Setelah pengguna menambah/menghapus kota, isi DataStore yang dipakai, termasuk bila kosong.
 * - DataStore membaca/menulis file di thread latar; decode JSON juga dipindah ke [ioDispatcher].
 */
class DataStoreSavedLocationRepository(
    private val dataStore: DataStore<Preferences>,
    private val defaultLocations: List<Place> = DefaultLocations.cities,
    private val gson: Gson = Gson(),
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SavedLocationRepository {

    override val savedLocations: Flow<List<Place>> = dataStore.data
        .catch { error ->
            // File rusak/tidak terbaca: pakai data kosong agar aplikasi tetap berjalan.
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences -> decode(preferences[SAVED_PLACES_KEY]) ?: defaultLocations }
        .distinctUntilChanged()
        .flowOn(ioDispatcher)

    override suspend fun addLocation(place: Place) {
        dataStore.edit { preferences ->
            val current = decode(preferences[SAVED_PLACES_KEY]) ?: defaultLocations
            if (current.none { it.id == place.id }) {
                preferences[SAVED_PLACES_KEY] = encode(current + place)
            }
        }
    }

    override suspend fun removeLocation(placeId: String) {
        dataStore.edit { preferences ->
            val current = decode(preferences[SAVED_PLACES_KEY]) ?: defaultLocations
            preferences[SAVED_PLACES_KEY] = encode(current.filterNot { it.id == placeId })
        }
    }

    private fun encode(places: List<Place>): String =
        gson.toJson(places.map { it.toEntity() })

    /** Null bila key belum ada atau JSON tidak valid (maka daftar default yang dipakai). */
    private fun decode(json: String?): List<Place>? {
        if (json == null) return null
        return try {
            gson.fromJson<List<SavedPlaceEntity>>(json, ENTITY_LIST_TYPE)?.mapNotNull { it.toPlace() }
        } catch (e: JsonParseException) {
            null
        }
    }

    private companion object {
        val SAVED_PLACES_KEY = stringPreferencesKey("saved_places")
        val ENTITY_LIST_TYPE: Type = object : TypeToken<List<SavedPlaceEntity>>() {}.type
    }
}

/**
 * Bentuk penyimpanan satu kota di DataStore. Nama negara dan bendera tidak disimpan
 * karena selalu dimuat ulang dari endpoint Country Code (dan di-cache).
 */
internal data class SavedPlaceEntity(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("lat") val latitude: Double? = null,
    @SerializedName("lon") val longitude: Double? = null,
)

internal fun Place.toEntity() = SavedPlaceEntity(
    id = id,
    name = name,
    region = region,
    country = country,
    latitude = coordinates.latitude,
    longitude = coordinates.longitude,
)

internal fun SavedPlaceEntity.toPlace(): Place? {
    val placeId = id?.takeIf { it.isNotBlank() } ?: return null
    val placeName = name?.takeIf { it.isNotBlank() } ?: return null
    val lat = latitude ?: return null
    val lon = longitude ?: return null
    return Place(
        id = placeId,
        name = placeName,
        region = region,
        country = country,
        coordinates = Coordinates(lat, lon),
    )
}

/**
 * Implementasi in-memory (dipakai untuk pengujian dan preview):
 * data kembali ke daftar awal setiap aplikasi dijalankan ulang.
 */
class InMemorySavedLocationRepository(
    initialLocations: List<Place> = DefaultLocations.cities,
) : SavedLocationRepository {

    private val locations = MutableStateFlow(initialLocations)

    override val savedLocations: Flow<List<Place>> = locations.asStateFlow()

    override suspend fun addLocation(place: Place) {
        locations.update { current ->
            if (current.any { it.id == place.id }) current else current + place
        }
    }

    override suspend fun removeLocation(placeId: String) {
        locations.update { current -> current.filterNot { it.id == placeId } }
    }
}

/**
 * Lima kota default Dashboard (sesuai README). Koordinat = pusat kota.
 */
object DefaultLocations {
    val cities: List<Place> = listOf(
        Place(
            id = "jakarta",
            name = "Jakarta",
            region = "DKI Jakarta",
            country = "ID",
            coordinates = Coordinates(-6.2088, 106.8456),
        ),
        Place(
            id = "bandung",
            name = "Bandung",
            region = "Jawa Barat",
            country = "ID",
            coordinates = Coordinates(-6.9175, 107.6191),
        ),
        Place(
            id = "surabaya",
            name = "Surabaya",
            region = "Jawa Timur",
            country = "ID",
            coordinates = Coordinates(-7.2575, 112.7521),
        ),
        Place(
            id = "yogyakarta",
            name = "Yogyakarta",
            region = "DI Yogyakarta",
            country = "ID",
            coordinates = Coordinates(-7.7956, 110.3695),
        ),
        Place(
            id = "denpasar",
            name = "Denpasar",
            region = "Bali",
            country = "ID",
            coordinates = Coordinates(-8.6705, 115.2126),
        ),
    )
}
