package com.satryo.weatherapp.data.repository

import com.satryo.weatherapp.data.model.Country
import com.satryo.weatherapp.data.model.InvalidResponseException
import com.satryo.weatherapp.data.model.dto.CountryDto
import com.satryo.weatherapp.data.service.CountryService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Sumber data informasi negara (nama dan bendera) dari endpoint Country Code.
 */
interface CountryRepository {

    /**
     * @param alpha2Code kode negara ISO 3166-1 alpha-2, mis. "ID".
     * @throws Exception bila request gagal; hasil yang berhasil disimpan di cache.
     */
    suspend fun getCountry(alpha2Code: String): Country
}

/**
 * Implementasi dengan cache in-memory per kode negara.
 *
 * - Mutex memastikan satu kode negara hanya di-request sekali, walaupun diminta
 *   oleh banyak card sekaligus (mis. 5 kota default yang semuanya "ID").
 * - Request dan mapping dijalankan di [ioDispatcher] agar tidak membebani main thread.
 */
class NetworkCountryRepository(
    private val countryService: CountryService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CountryRepository {

    private val cache = mutableMapOf<String, Country>()
    private val mutex = Mutex()

    override suspend fun getCountry(alpha2Code: String): Country {
        val code = alpha2Code.trim().uppercase(Locale.ROOT)
        return withContext(ioDispatcher) {
            mutex.withLock {
                cache[code] ?: countryService.getCountry(code).toCountry(code).also { cache[code] = it }
            }
        }
    }
}

internal fun CountryDto.toCountry(requestedCode: String): Country {
    val code = (alpha2Code ?: requestedCode).uppercase(Locale.ROOT)
    val countryName = name?.takeIf { it.isNotBlank() }
        ?: throw InvalidResponseException("Nama negara tidak tersedia")
    return Country(
        code = code,
        name = countryName,
        // Pakai emoji dari API; bila kosong atau berupa URL gambar, bentuk dari kode negara.
        flagEmoji = flag?.takeIf { it.isNotBlank() && !it.startsWith("http") } ?: flagEmojiOf(code),
    )
}

/**
 * Emoji bendera dari kode alpha-2: tiap huruf diubah menjadi Regional Indicator Symbol,
 * mis. "ID" -> 🇮🇩. Null bila kode tidak valid.
 */
internal fun flagEmojiOf(alpha2Code: String): String? {
    val code = alpha2Code.uppercase(Locale.ROOT)
    if (code.length != 2 || code.any { it !in 'A'..'Z' }) return null
    return code.map { letter -> String(Character.toChars(REGIONAL_INDICATOR_A + (letter - 'A'))) }
        .joinToString(separator = "")
}

private const val REGIONAL_INDICATOR_A = 0x1F1E6
