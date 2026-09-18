package com.satryo.weatherapp.data.model

import java.io.IOException

/**
 * API key OpenWeather kosong. Turunan [IOException] karena dilempar dari interceptor OkHttp.
 */
class MissingApiKeyException : IOException("API key OpenWeather belum diatur")

/** Izin lokasi belum diberikan oleh pengguna. */
class LocationPermissionException : Exception("Izin lokasi belum diberikan")

/** Lokasi perangkat tidak dapat ditentukan (mis. layanan lokasi nonaktif). */
class LocationUnavailableException : Exception("Lokasi perangkat tidak tersedia")

/** Response API tidak memuat data wajib. */
class InvalidResponseException(message: String) : Exception(message)
