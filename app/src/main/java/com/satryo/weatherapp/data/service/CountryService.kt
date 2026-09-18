package com.satryo.weatherapp.data.service

import com.satryo.weatherapp.data.model.dto.CountryDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Endpoint "Country Code" dari collection Postman.
 * Base URL: https://www.apicountries.com/ (server mengalihkan ke countries.dev;
 * OkHttp mengikuti pengalihan ini secara otomatis).
 *
 * API pihak ketiga ini TIDAK menerima API key OpenWeather: Retrofit-nya memakai
 * OkHttpClient tanpa [ApiKeyInterceptor] (lihat AppContainer).
 */
interface CountryService {

    /** @param alpha2Code kode negara ISO 3166-1 alpha-2, mis. "ID". */
    @GET("alpha/{code}")
    suspend fun getCountry(@Path("code") alpha2Code: String): CountryDto
}
