package com.satryo.weatherapp.data.service

import com.satryo.weatherapp.data.model.dto.GeoLocationDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Endpoint OpenWeather Geocoding API sesuai collection Postman (folder "Geocoding API").
 * Base URL: https://pro.openweathermap.org/
 * Parameter `appid` ditambahkan otomatis oleh [ApiKeyInterceptor].
 */
interface GeocodingService {

    /** Direct geocoding: mencari kota berdasarkan nama. Disiapkan untuk fitur Add New City. */
    @GET("geo/1.0/direct")
    suspend fun searchCity(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
    ): List<GeoLocationDto>
}
