package com.satryo.weatherapp.data.container

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.satryo.weatherapp.BuildConfig
import com.satryo.weatherapp.data.repository.CountryRepository
import com.satryo.weatherapp.data.repository.DataStoreSavedLocationRepository
import com.satryo.weatherapp.data.repository.DefaultLocationRepository
import com.satryo.weatherapp.data.repository.LocationRepository
import com.satryo.weatherapp.data.repository.NetworkCountryRepository
import com.satryo.weatherapp.data.repository.NetworkWeatherRepository
import com.satryo.weatherapp.data.repository.SavedLocationRepository
import com.satryo.weatherapp.data.repository.WeatherRepository
import com.satryo.weatherapp.data.service.ApiKeyInterceptor
import com.satryo.weatherapp.data.service.CountryService
import com.satryo.weatherapp.data.service.FusedLocationService
import com.satryo.weatherapp.data.service.GeocodingService
import com.satryo.weatherapp.data.service.LocationService
import com.satryo.weatherapp.data.service.WeatherService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * DataStore untuk daftar kota tersimpan. Dideklarasikan di top level agar hanya ada
 * satu instance per file (syarat DataStore).
 */
private val Context.savedLocationsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "saved_locations",
)

/**
 * Container dependency injection manual untuk seluruh aplikasi.
 */
interface AppContainer {
    val weatherRepository: WeatherRepository
    val locationRepository: LocationRepository
    val savedLocationRepository: SavedLocationRepository
    val countryRepository: CountryRepository
}

/**
 * Implementasi [AppContainer]: merakit OkHttp, Retrofit, Service, dan Repository.
 * Semua dependency dibuat secara lazy (saat pertama kali dipakai).
 *
 * Base URL mengikuti collection Postman:
 * - OpenWeather (weather, forecast, forecast/daily, geocoding): https://pro.openweathermap.org/
 * - Country Code: https://www.apicountries.com/
 */
class DefaultAppContainer(context: Context) : AppContainer {

    private val appContext = context.applicationContext

    /**
     * Dispatcher untuk pekerjaan data (network, parsing, mapping). Repository memindahkan
     * pekerjaannya ke sini dengan `withContext`, sehingga main thread tetap bebas untuk UI.
     */
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    /** Client dasar tanpa API key, dipakai untuk API pihak ketiga (Country Code). */
    private val baseOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    /** Client khusus OpenWeather: menambahkan `appid` ke setiap request. */
    private val openWeatherOkHttpClient: OkHttpClient by lazy {
        baseOkHttpClient.newBuilder()
            .addInterceptor(ApiKeyInterceptor(BuildConfig.OPENWEATHER_API_KEY))
            .build()
    }

    private val openWeatherRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(OPENWEATHER_BASE_URL)
            .client(openWeatherOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val countryRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(COUNTRY_BASE_URL)
            .client(baseOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val weatherService: WeatherService by lazy {
        openWeatherRetrofit.create(WeatherService::class.java)
    }

    private val geocodingService: GeocodingService by lazy {
        openWeatherRetrofit.create(GeocodingService::class.java)
    }

    private val countryService: CountryService by lazy {
        countryRetrofit.create(CountryService::class.java)
    }

    private val locationService: LocationService by lazy {
        FusedLocationService(appContext)
    }

    override val weatherRepository: WeatherRepository by lazy {
        NetworkWeatherRepository(weatherService, ioDispatcher)
    }

    override val locationRepository: LocationRepository by lazy {
        DefaultLocationRepository(locationService, geocodingService, ioDispatcher)
    }

    override val savedLocationRepository: SavedLocationRepository by lazy {
        DataStoreSavedLocationRepository(
            dataStore = appContext.savedLocationsDataStore,
            ioDispatcher = ioDispatcher,
        )
    }

    override val countryRepository: CountryRepository by lazy {
        NetworkCountryRepository(countryService, ioDispatcher)
    }

    private companion object {
        const val OPENWEATHER_BASE_URL = "https://pro.openweathermap.org/"
        const val COUNTRY_BASE_URL = "https://www.apicountries.com/"
        const val TIMEOUT_SECONDS = 15L
    }
}
