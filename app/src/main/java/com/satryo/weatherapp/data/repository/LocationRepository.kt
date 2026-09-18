package com.satryo.weatherapp.data.repository

import com.satryo.weatherapp.data.model.Coordinates
import com.satryo.weatherapp.data.model.LocationPermissionException
import com.satryo.weatherapp.data.model.LocationUnavailableException
import com.satryo.weatherapp.data.model.Place
import com.satryo.weatherapp.data.service.GeocodingService
import com.satryo.weatherapp.data.service.LocationService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Sumber data lokasi: koordinat perangkat dan pencarian kota.
 *
 * Nama lokasi perangkat tidak dicari di sini: nama tersebut ikut dikirim oleh
 * endpoint current weather (field "name"), sehingga tidak perlu reverse geocoding.
 */
interface LocationRepository {

    fun hasLocationPermission(): Boolean

    /**
     * @throws LocationPermissionException bila izin lokasi belum diberikan.
     * @throws LocationUnavailableException bila lokasi tidak dapat ditentukan.
     */
    suspend fun getCurrentCoordinates(): Coordinates

    /** Mencari kota berdasarkan nama (direct geocoding). Disiapkan untuk fitur Add New City. */
    suspend fun searchCity(query: String): List<Place>
}

/**
 * @param ioDispatcher dispatcher untuk request Retrofit dan mapping (default [Dispatchers.IO]).
 */
class DefaultLocationRepository(
    private val locationService: LocationService,
    private val geocodingService: GeocodingService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : LocationRepository {

    override fun hasLocationPermission(): Boolean = locationService.hasLocationPermission()

    /**
     * FusedLocationProviderClient sudah asinkron (hasil dikirim lewat callback), dan callback
     * tersebut hanya melanjutkan coroutine yang sedang menunggu, sehingga tidak memblokir main thread.
     */
    override suspend fun getCurrentCoordinates(): Coordinates =
        locationService.getCurrentCoordinates() ?: throw LocationUnavailableException()

    override suspend fun searchCity(query: String): List<Place> {
        if (query.isBlank()) return emptyList()
        return withContext(ioDispatcher) {
            geocodingService.searchCity(query.trim()).mapNotNull { it.toPlace() }
        }
    }
}
