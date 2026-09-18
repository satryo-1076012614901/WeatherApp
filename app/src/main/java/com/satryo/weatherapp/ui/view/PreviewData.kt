package com.satryo.weatherapp.ui.view

import com.satryo.weatherapp.data.model.Coordinates
import com.satryo.weatherapp.data.model.Country
import com.satryo.weatherapp.data.model.CurrentWeather
import com.satryo.weatherapp.data.model.DailyForecast
import com.satryo.weatherapp.data.model.HourlyForecast
import com.satryo.weatherapp.data.model.LocationUnavailableException
import com.satryo.weatherapp.data.model.Place
import com.satryo.weatherapp.data.model.WeatherCondition
import com.satryo.weatherapp.data.model.WeatherDetail
import com.satryo.weatherapp.data.model.WeatherSummary
import com.satryo.weatherapp.data.repository.DefaultLocations
import com.satryo.weatherapp.ui.util.toUserMessage
import com.satryo.weatherapp.ui.viewmodel.CurrentLocationUiState
import com.satryo.weatherapp.ui.viewmodel.DashboardUiState
import com.satryo.weatherapp.ui.viewmodel.DetailContentState
import com.satryo.weatherapp.ui.viewmodel.DetailUiState
import com.satryo.weatherapp.ui.viewmodel.SavedLocationUiState
import com.satryo.weatherapp.ui.viewmodel.SearchResultState
import com.satryo.weatherapp.ui.viewmodel.SearchUiState
import com.satryo.weatherapp.ui.viewmodel.WeatherLoadState
import java.io.IOException
import kotlin.math.PI
import kotlin.math.cos

/** Lebar layar ponsel untuk preview screen (dp), mengikuti ukuran referensi ponsel Android Studio. */
internal const val PREVIEW_PHONE_WIDTH_DP = 411

/** Tinggi layar ponsel untuk preview screen (dp). */
internal const val PREVIEW_PHONE_HEIGHT_DP = 891

/**
 * Data contoh KHUSUS untuk @Preview di Android Studio.
 * Bukan data cuaca asli dan tidak pernah dipakai saat aplikasi berjalan.
 * Lokasi GPS contoh memakai kota dan koordinat dari contoh request di collection Postman (Depok).
 */
internal object PreviewData {

    private const val BASE_TIME = 1_789_700_400L // Jumat, 18 Sep 2026 10:00 WIB
    private const val OFFSET_WIB = 7 * 3_600
    private const val HOUR = 3_600L
    private const val DAY = 86_400L
    private const val TODAY_MIN = 24.8
    private const val TODAY_MAX = 32.1

    /** Hasil endpoint Country Code untuk "ID" (nama + emoji bendera). */
    private val indonesia = Country(code = "ID", name = "Indonesia", flagEmoji = "🇮🇩")

    private val clear = WeatherCondition(800, "Clear", "cerah", "01d")
    private val clearNight = WeatherCondition(800, "Clear", "cerah", "01n")
    private val fewClouds = WeatherCondition(801, "Clouds", "sedikit berawan", "02d")
    private val brokenClouds = WeatherCondition(803, "Clouds", "awan pecah", "04d")
    private val brokenCloudsNight = WeatherCondition(803, "Clouds", "awan pecah", "04n")
    private val lightRain = WeatherCondition(500, "Rain", "hujan ringan", "10d")
    private val thunderstorm = WeatherCondition(211, "Thunderstorm", "badai petir", "11d")

    private fun currentWeather(temperature: Double, condition: WeatherCondition) = CurrentWeather(
        time = BASE_TIME,
        sunrise = BASE_TIME - 15_600, // 05:40 WIB
        sunset = BASE_TIME + 28_200, // 17:50 WIB
        temperature = temperature,
        feelsLike = temperature + 3.7,
        pressure = 1009,
        humidity = 70,
        cloudiness = 75,
        visibility = 9_000,
        windSpeed = 3.6,
        windDegree = 135,
        windGust = 5.1,
        condition = condition,
    )

    private fun summary(
        locationName: String,
        temperature: Double,
        min: Double,
        max: Double,
        condition: WeatherCondition,
    ) = WeatherSummary(
        locationName = locationName,
        countryCode = "ID",
        timezoneOffsetSeconds = OFFSET_WIB,
        current = currentWeather(temperature, condition),
        todayMinTemperature = min,
        todayMaxTemperature = max,
    )

    /** Suhu mengikuti pola harian: tertinggi ±32° pukul 14:00, terendah ±25° pukul 02:00. */
    private fun temperatureAt(hourOfDay: Int): Double =
        28.5 + 3.6 * cos((hourOfDay - 14) / 24.0 * 2 * PI)

    /** 8 slot prakiraan 3 jam. Slot pertama setelah 10:00 WIB adalah 13:00 WIB (06:00 UTC). */
    private val hourlySamples = List(8) { index ->
        val hoursAhead = 3 + index * 3
        val hourOfDay = (10 + hoursAhead) % 24
        val isNight = hourOfDay !in 6..17
        val isRaining = hourOfDay in 15..17
        HourlyForecast(
            time = BASE_TIME + hoursAhead * HOUR,
            temperature = temperatureAt(hourOfDay),
            precipitationProbability = if (isRaining) 0.65 else 0.05,
            condition = when {
                isRaining -> lightRain
                isNight && hourOfDay in 0..4 -> clearNight
                isNight -> brokenCloudsNight
                else -> brokenClouds
            },
        )
    }

    /**
     * Dipakai bersama oleh card GPS (Dashboard) dan halaman Detail agar angkanya konsisten.
     * Card GPS ikut menampilkan prakiraan per 3 jam.
     */
    private val currentLocationSummary =
        summary("Depok", 30.3, TODAY_MIN, TODAY_MAX, brokenClouds).copy(hourly = hourlySamples)

    /** Lokasi GPS contoh, dibentuk dengan cara yang sama seperti di DashboardViewModel. */
    private val currentPlace = Place.currentLocation(
        coordinates = Coordinates(-6.40719, 106.8158371),
        name = currentLocationSummary.locationName,
        countryCode = currentLocationSummary.countryCode,
    )

    /** Cuaca contoh untuk 5 kota default, urut sesuai DefaultLocations.cities. */
    private val savedSummaries = listOf(
        summary("Jakarta", 31.2, 25.0, 32.4, brokenClouds),
        summary("Bandung", 23.6, 18.9, 27.0, lightRain),
        summary("Surabaya", 32.8, 26.1, 34.0, clear),
        summary("Yogyakarta", 28.4, 23.2, 31.5, fewClouds),
        summary("Denpasar", 29.1, 24.6, 30.8, thunderstorm),
    )

    // ---------- Dashboard ----------

    /** Semua data berhasil dimuat, termasuk nama negara dan bendera. */
    val dashboardState = DashboardUiState(
        currentLocation = CurrentLocationUiState.Success(
            place = currentPlace.withCountry(indonesia),
            summary = currentLocationSummary,
        ),
        savedLocations = DefaultLocations.cities.mapIndexed { index, city ->
            SavedLocationUiState(
                place = city.withCountry(indonesia),
                weather = WeatherLoadState.Success(savedSummaries[index % savedSummaries.size]),
            )
        },
        savedLocationsLoaded = true,
    )

    /** Kondisi awal saat aplikasi baru dibuka (info negara belum dimuat). */
    val dashboardLoadingState = DashboardUiState(
        currentLocation = CurrentLocationUiState.Loading,
        savedLocations = DefaultLocations.cities.map { city ->
            SavedLocationUiState(place = city, weather = WeatherLoadState.Loading)
        },
        savedLocationsLoaded = true,
    )

    /** Izin lokasi belum diberikan; kota tersimpan tetap tampil. */
    val dashboardPermissionState = dashboardState.copy(
        currentLocation = CurrentLocationUiState.PermissionRequired,
    )

    /**
     * GPS mati dan perangkat offline, sehingga info negara juga tidak termuat.
     * Pesan memakai pemetaan error yang sama dengan aplikasi.
     */
    val dashboardErrorState = DashboardUiState(
        currentLocation = CurrentLocationUiState.Error(LocationUnavailableException().toUserMessage()),
        savedLocations = DefaultLocations.cities.map { city ->
            SavedLocationUiState(
                place = city,
                weather = WeatherLoadState.Error(IOException().toUserMessage()),
            )
        },
        savedLocationsLoaded = true,
    )

    /** Semua kota sudah dihapus lewat tombol bintang. */
    val dashboardEmptyState = dashboardState.copy(savedLocations = emptyList())

    // ---------- Detail ----------

    private val detail = WeatherDetail(
        timezoneOffsetSeconds = OFFSET_WIB,
        current = currentLocationSummary.current,
        hourly = hourlySamples,
        // Tanpa parameter cnt, Daily Forecast mengembalikan 7 hari.
        daily = List(7) { index ->
            val isToday = index == 0
            val isRainy = index % 3 == 1
            DailyForecast(
                time = BASE_TIME + index * DAY,
                minTemperature = if (isToday) TODAY_MIN else 24.0 + index % 2,
                maxTemperature = when {
                    isToday -> TODAY_MAX
                    isRainy -> 29.5
                    else -> 31.0 + index % 3
                },
                precipitationProbability = if (isRainy) 0.7 else 0.1,
                condition = when {
                    isRainy -> lightRain
                    index % 3 == 2 -> fewClouds
                    else -> brokenClouds
                },
            )
        },
    )

    /** Semua data Detail berhasil dimuat (dibuka dari card GPS). */
    val detailState = DetailUiState(
        placeName = currentPlace.name,
        placeSubtitle = currentPlace.withCountry(indonesia).subtitle,
        content = DetailContentState.Success(detail),
    )

    val detailLoadingState = detailState.copy(content = DetailContentState.Loading)

    /** Perangkat offline. */
    val detailErrorState = detailState.copy(
        content = DetailContentState.Error(IOException().toUserMessage()),
    )

    // ---------- Search ----------

    /**
     * Hasil pencarian "Depok" (nama, provinsi, dan koordinat dari contoh response
     * Direct geocoding di collection Postman), sudah dilengkapi nama negara + bendera.
     */
    val searchResultState = SearchUiState(
        query = "Depok",
        result = SearchResultState.Success(
            listOf(
                searchPlace("Depok", "West Java", -6.40719, 106.8158371),
                searchPlace("Depok", "Special Region of Yogyakarta", -7.7689657, 110.4053572),
                searchPlace("Depok", "West Java", -7.4618286, 107.6606434),
                searchPlace("Depok", "Special Region of Yogyakarta", -7.9141717, 110.1633116),
                searchPlace("Depok", "Central Java", -7.0082622, 109.1590275),
            ),
        ),
    )

    val searchErrorState = SearchUiState(
        query = "Depok",
        result = SearchResultState.Error(IOException().toUserMessage()),
    )

    private fun searchPlace(name: String, region: String, latitude: Double, longitude: Double): Place {
        val coordinates = Coordinates(latitude, longitude)
        return Place(
            id = Place.idFor(coordinates),
            name = name,
            region = region,
            country = "ID",
            coordinates = coordinates,
        ).withCountry(indonesia)
    }
}
