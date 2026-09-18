package com.satryo.weatherapp.data.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Response endpoint Country Code (GET alpha/{kode}) dari collection Postman.
 * Hanya field yang dipakai aplikasi yang dipetakan.
 *
 * @property flag emoji bendera, mis. "🇮🇩".
 */
data class CountryDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("nativeName") val nativeName: String? = null,
    @SerializedName("alpha2Code") val alpha2Code: String? = null,
    @SerializedName("capital") val capital: String? = null,
    @SerializedName("flag") val flag: String? = null,
    @SerializedName("flags") val flags: CountryFlagsDto? = null,
)

/** URL gambar bendera. */
data class CountryFlagsDto(
    @SerializedName("png") val png: String? = null,
    @SerializedName("svg") val svg: String? = null,
)
