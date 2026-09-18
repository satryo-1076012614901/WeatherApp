package com.satryo.weatherapp.data.repository

import com.satryo.weatherapp.data.model.Coordinates
import com.satryo.weatherapp.data.model.WeatherDetail
import com.satryo.weatherapp.data.model.WeatherSummary
import com.satryo.weatherapp.data.service.WeatherService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

/**
 * Sumber data cuaca untuk ViewModel.
 */
interface WeatherRepository {

    /**
     * Ringkasan cuaca untuk card Dashboard:
     * current weather + daily forecast hari ini (untuk suhu maks/min).
     *
     * @param hourlyCount jumlah slot prakiraan 3 jam yang ikut diambil dari endpoint
     * 5 day / 3 hour forecast; 0 = tidak diambil (card kota tersimpan).
     */
    suspend fun getWeatherSummary(coordinates: Coordinates, hourlyCount: Int = 0): WeatherSummary

    /**
     * Cuaca lengkap untuk halaman Detail:
     * current weather + 5 day / 3 hour forecast + daily forecast.
     */
    suspend fun getWeatherDetail(coordinates: Coordinates): WeatherDetail
}

/**
 * Implementasi [WeatherRepository] yang memanggil endpoint API 2.5 dari collection Postman.
 *
 * - Main-safe: seluruh pekerjaan (request Retrofit + mapping DTO) dipindahkan ke [ioDispatcher]
 *   dengan `withContext`, sehingga aman dipanggil dari `viewModelScope` (main thread)
 *   tanpa membebani UI.
 * - Beberapa endpoint dipanggil paralel dengan `async`; bila salah satu gagal, pemanggilan
 *   lain dibatalkan (structured concurrency) dan error diteruskan ke ViewModel.
 */
class NetworkWeatherRepository(
    private val weatherService: WeatherService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : WeatherRepository {

    override suspend fun getWeatherSummary(coordinates: Coordinates, hourlyCount: Int): WeatherSummary =
        withContext(ioDispatcher) {
            val current = async {
                weatherService.getCurrentWeather(coordinates.latitude, coordinates.longitude)
            }
            // Cukup data hari ini (cnt = 1) untuk suhu maks/min.
            val today = async {
                weatherService.getDailyForecast(coordinates.latitude, coordinates.longitude, count = 1)
            }
            val forecast = if (hourlyCount > 0) {
                async {
                    weatherService.getForecast(coordinates.latitude, coordinates.longitude, count = hourlyCount)
                }
            } else {
                null
            }
            current.await().toWeatherSummary(daily = today.await(), forecast = forecast?.await())
        }

    override suspend fun getWeatherDetail(coordinates: Coordinates): WeatherDetail =
        withContext(ioDispatcher) {
            val current = async {
                weatherService.getCurrentWeather(coordinates.latitude, coordinates.longitude)
            }
            val forecast = async {
                weatherService.getForecast(coordinates.latitude, coordinates.longitude)
            }
            val daily = async {
                weatherService.getDailyForecast(coordinates.latitude, coordinates.longitude)
            }
            current.await().toWeatherDetail(forecast = forecast.await(), daily = daily.await())
        }
}
