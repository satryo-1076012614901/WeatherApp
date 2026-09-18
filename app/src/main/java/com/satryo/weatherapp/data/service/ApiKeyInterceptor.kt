package com.satryo.weatherapp.data.service

import com.satryo.weatherapp.data.model.MissingApiKeyException
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Menambahkan query `appid` ke request yang menuju server OpenWeather.
 *
 * - Request ke host lain diteruskan tanpa API key, agar key tidak bocor ke pihak ketiga.
 * - Bila API key kosong, request dihentikan dengan [MissingApiKeyException]
 *   agar UI dapat menampilkan pesan yang jelas, bukan error 401.
 */
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!isOpenWeatherHost(request.url.host)) return chain.proceed(request)
        if (apiKey.isBlank()) throw MissingApiKeyException()

        val url = request.url.newBuilder()
            .addQueryParameter("appid", apiKey)
            .build()
        return chain.proceed(request.newBuilder().url(url).build())
    }

    private fun isOpenWeatherHost(host: String): Boolean =
        host == OPENWEATHER_DOMAIN || host.endsWith(".$OPENWEATHER_DOMAIN")

    private companion object {
        const val OPENWEATHER_DOMAIN = "openweathermap.org"
    }
}
