package com.satryo.weatherapp.data.model.dto

import com.google.gson.annotations.SerializedName

/*
 * DTO response OpenWeather API 2.5 sesuai collection Postman:
 * - GET data/2.5/weather         -> CurrentWeatherResponse
 * - GET data/2.5/forecast        -> ForecastResponse (5 day / 3 hour)
 * - GET data/2.5/forecast/daily  -> DailyForecastResponse (Daily Forecast 16 Days)
 *
 * Hanya field yang dipakai aplikasi (plus beberapa field konteks) yang dipetakan;
 * field lain di JSON diabaikan oleh Gson.
 *
 * Semua properti nullable dengan default null karena:
 * 1. Gson tidak menghormati null-safety Kotlin (field yang hilang tetap bisa bernilai null).
 * 2. Sebagian field memang opsional di API (mis. wind.gust).
 * Karena semua parameter punya default, Kotlin membuat constructor tanpa argumen
 * yang dipakai Gson, sehingga nilai default tetap berlaku.
 *
 * Perhatian: bentuk field berbeda antar endpoint. Contoh "clouds" berupa object {"all": 85}
 * di current weather dan forecast, tetapi berupa angka di daily forecast.
 */

// ---------- Bagian bersama ----------

data class CoordDto(
    @SerializedName("lat") val lat: Double? = null,
    @SerializedName("lon") val lon: Double? = null,
)

data class WeatherConditionDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("main") val main: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("icon") val icon: String? = null,
)

/** Blok "main" pada current weather dan item forecast 3 jam. */
data class MainDto(
    @SerializedName("temp") val temp: Double? = null,
    @SerializedName("feels_like") val feelsLike: Double? = null,
    @SerializedName("temp_min") val tempMin: Double? = null,
    @SerializedName("temp_max") val tempMax: Double? = null,
    @SerializedName("pressure") val pressure: Int? = null,
    @SerializedName("humidity") val humidity: Int? = null,
)

data class WindDto(
    @SerializedName("speed") val speed: Double? = null,
    @SerializedName("deg") val deg: Int? = null,
    @SerializedName("gust") val gust: Double? = null,
)

data class CloudsDto(
    @SerializedName("all") val all: Int? = null,
)

/** Informasi kota pada response forecast 3 jam dan daily forecast. */
data class CityDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("coord") val coord: CoordDto? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("timezone") val timezone: Int? = null,
)

// ---------- GET data/2.5/weather ----------

data class CurrentWeatherResponse(
    @SerializedName("coord") val coord: CoordDto? = null,
    @SerializedName("weather") val weather: List<WeatherConditionDto>? = null,
    @SerializedName("main") val main: MainDto? = null,
    @SerializedName("visibility") val visibility: Int? = null,
    @SerializedName("wind") val wind: WindDto? = null,
    @SerializedName("clouds") val clouds: CloudsDto? = null,
    @SerializedName("dt") val dt: Long? = null,
    @SerializedName("sys") val sys: CurrentSysDto? = null,
    /** Selisih zona waktu lokasi terhadap UTC, dalam detik. */
    @SerializedName("timezone") val timezone: Int? = null,
    @SerializedName("id") val id: Long? = null,
    /** Nama lokasi terdekat dari koordinat, mis. "Depok". */
    @SerializedName("name") val name: String? = null,
)

data class CurrentSysDto(
    /** Kode negara ISO 3166-1 alpha-2, mis. "ID". */
    @SerializedName("country") val country: String? = null,
    @SerializedName("sunrise") val sunrise: Long? = null,
    @SerializedName("sunset") val sunset: Long? = null,
)

// ---------- GET data/2.5/forecast (5 day / 3 hour) ----------

data class ForecastResponse(
    @SerializedName("cnt") val count: Int? = null,
    @SerializedName("list") val list: List<ForecastItemDto>? = null,
    @SerializedName("city") val city: CityDto? = null,
)

data class ForecastItemDto(
    @SerializedName("dt") val dt: Long? = null,
    @SerializedName("main") val main: MainDto? = null,
    @SerializedName("weather") val weather: List<WeatherConditionDto>? = null,
    @SerializedName("clouds") val clouds: CloudsDto? = null,
    @SerializedName("wind") val wind: WindDto? = null,
    @SerializedName("visibility") val visibility: Int? = null,
    /** Peluang presipitasi 0.0 - 1.0. */
    @SerializedName("pop") val pop: Double? = null,
    @SerializedName("dt_txt") val dtText: String? = null,
)

// ---------- GET data/2.5/forecast/daily (Daily Forecast 16 Days) ----------

data class DailyForecastResponse(
    @SerializedName("city") val city: CityDto? = null,
    @SerializedName("cnt") val count: Int? = null,
    @SerializedName("list") val list: List<DailyItemDto>? = null,
)

data class DailyItemDto(
    @SerializedName("dt") val dt: Long? = null,
    @SerializedName("sunrise") val sunrise: Long? = null,
    @SerializedName("sunset") val sunset: Long? = null,
    @SerializedName("temp") val temp: DailyTemperatureDto? = null,
    @SerializedName("pressure") val pressure: Int? = null,
    @SerializedName("humidity") val humidity: Int? = null,
    @SerializedName("weather") val weather: List<WeatherConditionDto>? = null,
    /** Kecepatan angin (m/s). Di daily forecast namanya "speed", bukan "wind.speed". */
    @SerializedName("speed") val windSpeed: Double? = null,
    @SerializedName("deg") val windDeg: Int? = null,
    @SerializedName("gust") val windGust: Double? = null,
    /** Tutupan awan (%). Di daily forecast berupa angka, bukan object. */
    @SerializedName("clouds") val clouds: Int? = null,
    /** Peluang presipitasi 0.0 - 1.0. */
    @SerializedName("pop") val pop: Double? = null,
)

data class DailyTemperatureDto(
    @SerializedName("day") val day: Double? = null,
    @SerializedName("min") val min: Double? = null,
    @SerializedName("max") val max: Double? = null,
    @SerializedName("night") val night: Double? = null,
    @SerializedName("eve") val eve: Double? = null,
    @SerializedName("morn") val morn: Double? = null,
)
