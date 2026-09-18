package com.satryo.weatherapp.data.model

/**
 * Kondisi cuaca (mis. hujan ringan) beserta kode ikon OpenWeather.
 *
 * @property id kode kondisi cuaca (2xx badai petir, 3xx gerimis, 5xx hujan, 6xx salju,
 * 7xx atmosfer, 800 cerah, 80x berawan).
 * @property iconCode kode ikon dari API, mis. "10d" (siang) atau "10n" (malam).
 */
data class WeatherCondition(
    val id: Int,
    val main: String,
    val description: String,
    val iconCode: String,
) {
    /**
     * URL ikon resmi OpenWeather sesuai halaman Weather Conditions
     * (https://openweathermap.org/api/weather-conditions), ukuran 100x100 px.
     */
    val iconUrl: String
        get() = "https://openweathermap.org/img/wn/$iconCode@2x.png"

    /** Kode ikon OpenWeather berakhiran "n" untuk malam hari. */
    val isNight: Boolean
        get() = iconCode.endsWith("n")
}

/**
 * Cuaca saat ini (endpoint current weather).
 *
 * Satuan: suhu °C, kecepatan angin m/s, tekanan hPa, jarak pandang meter.
 * Seluruh waktu berupa epoch detik (UTC). Field nullable = tidak dikirim oleh API.
 */
data class CurrentWeather(
    val time: Long,
    val sunrise: Long?,
    val sunset: Long?,
    val temperature: Double,
    val feelsLike: Double?,
    val pressure: Int?,
    val humidity: Int?,
    val cloudiness: Int?,
    val visibility: Int?,
    val windSpeed: Double?,
    val windDegree: Int?,
    val windGust: Double?,
    val condition: WeatherCondition,
)

/**
 * Prakiraan dengan interval 3 jam (endpoint 5 day / 3 hour forecast).
 *
 * @property precipitationProbability peluang presipitasi 0.0 - 1.0.
 */
data class HourlyForecast(
    val time: Long,
    val temperature: Double,
    val precipitationProbability: Double?,
    val condition: WeatherCondition,
)

/**
 * Prakiraan harian (endpoint Daily Forecast 16 Days).
 *
 * @property precipitationProbability peluang presipitasi 0.0 - 1.0.
 */
data class DailyForecast(
    val time: Long,
    val minTemperature: Double,
    val maxTemperature: Double,
    val precipitationProbability: Double?,
    val condition: WeatherCondition,
)

/**
 * Ringkasan cuaca untuk card di Dashboard.
 *
 * @property locationName nama lokasi dari current weather (field "name"), mis. "Depok".
 * @property countryCode kode negara dari current weather (field "sys.country"), mis. "ID".
 * @property todayMinTemperature suhu minimum hari ini dari daily forecast.
 * @property todayMaxTemperature suhu maksimum hari ini dari daily forecast.
 * @property hourly prakiraan per 3 jam; hanya diisi untuk card lokasi GPS (kosong untuk kota tersimpan).
 */
data class WeatherSummary(
    val locationName: String?,
    val countryCode: String?,
    val timezoneOffsetSeconds: Int,
    val current: CurrentWeather,
    val todayMinTemperature: Double?,
    val todayMaxTemperature: Double?,
    val hourly: List<HourlyForecast> = emptyList(),
)

/**
 * Data lengkap untuk halaman Detail: current, prakiraan 3 jam, dan harian.
 */
data class WeatherDetail(
    val timezoneOffsetSeconds: Int,
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
)
