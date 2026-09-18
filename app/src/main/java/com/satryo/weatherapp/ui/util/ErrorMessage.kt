package com.satryo.weatherapp.ui.util

import com.satryo.weatherapp.data.model.InvalidResponseException
import com.satryo.weatherapp.data.model.LocationPermissionException
import com.satryo.weatherapp.data.model.LocationUnavailableException
import com.satryo.weatherapp.data.model.MissingApiKeyException
import retrofit2.HttpException
import java.io.IOException

/**
 * Menerjemahkan exception dari data layer menjadi pesan yang dapat dipahami pengguna.
 */
fun Throwable.toUserMessage(): String = when (this) {
    is MissingApiKeyException ->
        "API key OpenWeather belum diatur. Tambahkan OPENWEATHER_API_KEY di local.properties, lalu build ulang aplikasi."

    is LocationPermissionException ->
        "Izin lokasi dibutuhkan untuk menampilkan cuaca di lokasi Anda."

    is LocationUnavailableException ->
        "Lokasi tidak dapat ditentukan. Pastikan layanan lokasi (GPS) aktif."

    is InvalidResponseException ->
        "Data cuaca dari server tidak lengkap."

    is HttpException -> when (code()) {
        401 -> "API key ditolak (401). Pastikan key valid dan paketnya mendukung pro.openweathermap.org (mis. paket Student)."
        404 -> "Data cuaca untuk lokasi ini tidak ditemukan."
        429 -> "Batas pemanggilan API tercapai. Coba lagi nanti."
        else -> "Server cuaca sedang bermasalah (kode ${code()})."
    }

    is IOException ->
        "Tidak dapat terhubung ke server. Periksa koneksi internet Anda."

    else ->
        "Terjadi kesalahan: ${message ?: javaClass.simpleName}"
}
