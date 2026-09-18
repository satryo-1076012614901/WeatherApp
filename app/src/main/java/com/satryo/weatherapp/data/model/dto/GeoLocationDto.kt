package com.satryo.weatherapp.data.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Item response OpenWeather Geocoding API (geo/1.0/direct dan geo/1.0/reverse).
 *
 * @property localNames nama lokasi per kode bahasa, mis. localNames["id"] untuk Bahasa Indonesia.
 */
data class GeoLocationDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("local_names") val localNames: Map<String, String>? = null,
    @SerializedName("lat") val lat: Double? = null,
    @SerializedName("lon") val lon: Double? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("state") val state: String? = null,
)
