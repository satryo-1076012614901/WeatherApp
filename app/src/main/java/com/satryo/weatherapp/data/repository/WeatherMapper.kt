package com.satryo.weatherapp.data.repository

import com.satryo.weatherapp.data.model.Coordinates
import com.satryo.weatherapp.data.model.CurrentWeather
import com.satryo.weatherapp.data.model.DailyForecast
import com.satryo.weatherapp.data.model.HourlyForecast
import com.satryo.weatherapp.data.model.InvalidResponseException
import com.satryo.weatherapp.data.model.Place
import com.satryo.weatherapp.data.model.WeatherCondition
import com.satryo.weatherapp.data.model.WeatherDetail
import com.satryo.weatherapp.data.model.WeatherSummary
import com.satryo.weatherapp.data.model.dto.CurrentWeatherResponse
import com.satryo.weatherapp.data.model.dto.DailyForecastResponse
import com.satryo.weatherapp.data.model.dto.DailyItemDto
import com.satryo.weatherapp.data.model.dto.ForecastItemDto
import com.satryo.weatherapp.data.model.dto.ForecastResponse
import com.satryo.weatherapp.data.model.dto.GeoLocationDto
import com.satryo.weatherapp.data.model.dto.WeatherConditionDto

/*
 * Pemetaan DTO (bentuk response API) ke domain model (bentuk yang dipakai UI).
 * Item yang tidak memiliki data wajib (waktu, suhu, kondisi) dibuang,
 * bukan diisi nilai palsu.
 */

/**
 * Ringkasan untuk card Dashboard.
 *
 * Suhu maks/min diambil dari daily forecast hari ini, karena `main.temp_min/temp_max`
 * pada current weather adalah variasi suhu saat ini di area kota, bukan min/maks harian.
 */
internal fun CurrentWeatherResponse.toWeatherSummary(
    daily: DailyForecastResponse,
    forecast: ForecastResponse? = null,
): WeatherSummary {
    val today = daily.list?.firstOrNull()?.temp
    return WeatherSummary(
        locationName = name?.takeIf { it.isNotBlank() },
        countryCode = sys?.country?.takeIf { it.isNotBlank() },
        timezoneOffsetSeconds = timezone ?: daily.city?.timezone ?: 0,
        current = toCurrentWeather(),
        todayMinTemperature = today?.min,
        todayMaxTemperature = today?.max,
        hourly = forecast?.list.orEmpty().mapNotNull { it.toHourlyForecast() },
    )
}

/** Data lengkap halaman Detail dari tiga endpoint: current, forecast 3 jam, dan daily. */
internal fun CurrentWeatherResponse.toWeatherDetail(
    forecast: ForecastResponse,
    daily: DailyForecastResponse,
): WeatherDetail = WeatherDetail(
    timezoneOffsetSeconds = timezone ?: forecast.city?.timezone ?: daily.city?.timezone ?: 0,
    current = toCurrentWeather(),
    hourly = forecast.list.orEmpty().mapNotNull { it.toHourlyForecast() },
    daily = daily.list.orEmpty().mapNotNull { it.toDailyForecast() },
)

internal fun CurrentWeatherResponse.toCurrentWeather(): CurrentWeather {
    val mainData = main ?: throw incompleteCurrentWeather()
    val time = dt ?: throw incompleteCurrentWeather()
    val temperature = mainData.temp ?: throw incompleteCurrentWeather()
    val condition = weather?.firstOrNull()?.toDomain() ?: throw incompleteCurrentWeather()
    return CurrentWeather(
        time = time,
        sunrise = sys?.sunrise,
        sunset = sys?.sunset,
        temperature = temperature,
        feelsLike = mainData.feelsLike,
        pressure = mainData.pressure,
        humidity = mainData.humidity,
        cloudiness = clouds?.all,
        visibility = visibility,
        windSpeed = wind?.speed,
        windDegree = wind?.deg,
        windGust = wind?.gust,
        condition = condition,
    )
}

/** Nama lokasi dalam Bahasa Indonesia bila tersedia, selain itu nama default dari API. */
internal fun GeoLocationDto.localizedName(language: String = "id"): String? =
    localNames?.get(language)?.takeIf { it.isNotBlank() } ?: name?.takeIf { it.isNotBlank() }

/** Hasil direct geocoding menjadi [Place]. ID dibentuk dari koordinat agar unik. */
internal fun GeoLocationDto.toPlace(): Place? {
    val latitude = lat ?: return null
    val longitude = lon ?: return null
    val placeName = localizedName() ?: return null
    val coordinates = Coordinates(latitude, longitude)
    return Place(
        id = Place.idFor(coordinates),
        name = placeName,
        region = state,
        country = country,
        coordinates = coordinates,
    )
}

private fun incompleteCurrentWeather() =
    InvalidResponseException("Data cuaca saat ini tidak lengkap")

private fun ForecastItemDto.toHourlyForecast(): HourlyForecast? {
    val time = dt ?: return null
    val temperature = main?.temp ?: return null
    val condition = weather?.firstOrNull()?.toDomain() ?: return null
    return HourlyForecast(
        time = time,
        temperature = temperature,
        precipitationProbability = pop,
        condition = condition,
    )
}

private fun DailyItemDto.toDailyForecast(): DailyForecast? {
    val time = dt ?: return null
    val temperature = temp ?: return null
    val min = temperature.min ?: return null
    val max = temperature.max ?: return null
    val condition = weather?.firstOrNull()?.toDomain() ?: return null
    return DailyForecast(
        time = time,
        minTemperature = min,
        maxTemperature = max,
        precipitationProbability = pop,
        condition = condition,
    )
}

private fun WeatherConditionDto.toDomain(): WeatherCondition? {
    val iconCode = icon?.takeIf { it.isNotBlank() } ?: return null
    return WeatherCondition(
        id = id ?: 0,
        main = main.orEmpty(),
        description = description?.takeIf { it.isNotBlank() } ?: main.orEmpty(),
        iconCode = iconCode,
    )
}
