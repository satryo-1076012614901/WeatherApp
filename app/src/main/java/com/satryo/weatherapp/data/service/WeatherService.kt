package com.satryo.weatherapp.data.service

import com.satryo.weatherapp.data.model.dto.CurrentWeatherResponse
import com.satryo.weatherapp.data.model.dto.DailyForecastResponse
import com.satryo.weatherapp.data.model.dto.ForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Endpoint OpenWeather API 2.5 sesuai collection Postman (folder "Current & Forecast").
 * Base URL: https://pro.openweathermap.org/ (variabel {{pro_api2.5}} = .../data/2.5).
 * Parameter `appid` ditambahkan otomatis oleh [ApiKeyInterceptor].
 *
 * Parameter bersama:
 * - `units = metric`: suhu dalam °C dan angin dalam m/s (sama dengan collection).
 * - `lang = id`: deskripsi cuaca dalam Bahasa Indonesia (tambahan, tidak ada di collection).
 */
interface WeatherService {

    /** Current weather data. */
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") units: String = "metric",
        @Query("lang") language: String = "id",
    ): CurrentWeatherResponse

    /**
     * Call 5 day / 3 hour forecast data: maksimal 40 item dengan interval 3 jam.
     *
     * @param count jumlah item; null = semua (sesuai collection).
     */
    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("cnt") count: Int? = null,
        @Query("units") units: String = "metric",
        @Query("lang") language: String = "id",
    ): ForecastResponse

    /**
     * Daily Forecast 16 Days.
     *
     * @param count jumlah hari (1 - 16); null = default API, yaitu 7 hari
     * (sesuai request di collection dan contoh response-nya).
     */
    @GET("data/2.5/forecast/daily")
    suspend fun getDailyForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("cnt") count: Int? = null,
        @Query("units") units: String = "metric",
        @Query("lang") language: String = "id",
    ): DailyForecastResponse
}
