package com.satryo.weatherapp.data.model

import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Titik koordinat geografis dalam derajat desimal.
 */
data class Coordinates(
    val latitude: Double,
    val longitude: Double,
) {
    /** Jarak lurus ke titik lain dalam kilometer (rumus haversine). */
    fun distanceKmTo(other: Coordinates): Double {
        val dLat = Math.toRadians(other.latitude - latitude)
        val dLon = Math.toRadians(other.longitude - longitude)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(latitude)) * cos(Math.toRadians(other.latitude)) * sin(dLon / 2).pow(2)
        return 2 * EARTH_RADIUS_KM * asin(sqrt(a))
    }

    private companion object {
        const val EARTH_RADIUS_KM = 6371.0
    }
}

/**
 * Lokasi yang ditampilkan aplikasi, baik lokasi perangkat (GPS) maupun kota tersimpan.
 *
 * @property region nama provinsi/state, bila tersedia.
 * @property country kode negara ISO 3166-1 alpha-2 (mis. "ID"), bila tersedia.
 * @property countryName nama negara dari endpoint Country Code (mis. "Indonesia"), bila sudah dimuat.
 * @property countryFlag emoji bendera (mis. "🇮🇩"), bila sudah dimuat.
 */
data class Place(
    val id: String,
    val name: String,
    val region: String? = null,
    val country: String? = null,
    val coordinates: Coordinates,
    val countryName: String? = null,
    val countryFlag: String? = null,
) {
    /**
     * Keterangan wilayah untuk UI, mis. "Jawa Barat, Indonesia 🇮🇩".
     * Sebelum info negara dimuat, yang tampil kode negaranya: "Jawa Barat, ID".
     */
    val subtitle: String?
        get() {
            val area = listOfNotNull(region, countryName ?: country)
                .filter { it.isNotBlank() }
                .joinToString(", ")
            return listOfNotNull(area, countryFlag)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { null }
        }

    /** Melengkapi nama negara dan bendera bila kode negaranya cocok. */
    fun withCountry(info: Country): Place =
        if (country.equals(info.code, ignoreCase = true)) {
            copy(countryName = info.name, countryFlag = info.flagEmoji)
        } else {
            this
        }

    /** Mempertahankan info negara dari versi [other] (mis. saat daftar lokasi diperbarui). */
    fun keepCountryInfoFrom(other: Place): Place =
        if (country == other.country) {
            copy(countryName = other.countryName, countryFlag = other.countryFlag)
        } else {
            this
        }

    /**
     * True bila [other] menunjuk kota yang sama: ID sama, atau nama dan negara sama dengan
     * jarak kurang dari [SAME_CITY_RADIUS_KM]. Aturan kedua membuat hasil pencarian "Jakarta"
     * dikenali sebagai kota default "Jakarta", walau koordinat pusat kotanya sedikit berbeda.
     */
    fun isSameLocationAs(other: Place): Boolean {
        if (id == other.id) return true
        val sameName = name.trim().equals(other.name.trim(), ignoreCase = true)
        val sameCountry = country.equals(other.country, ignoreCase = true)
        return sameName && sameCountry &&
            coordinates.distanceKmTo(other.coordinates) < SAME_CITY_RADIUS_KM
    }

    /**
     * Versi yang disimpan ke daftar Dashboard. Lokasi GPS diberi ID berbasis koordinat,
     * karena [CURRENT_LOCATION_ID] khusus untuk lokasi perangkat yang selalu berubah.
     */
    fun toSavedPlace(): Place = copy(
        id = if (id == CURRENT_LOCATION_ID) idFor(coordinates) else id,
        countryName = null,
        countryFlag = null,
    )

    companion object {
        /** ID khusus untuk lokasi perangkat saat ini. */
        const val CURRENT_LOCATION_ID = "current_location"

        /** Batas jarak untuk menganggap dua lokasi bernama sama sebagai kota yang sama. */
        const val SAME_CITY_RADIUS_KM = 25.0

        /** ID berbasis koordinat (4 desimal ≈ 11 m), dipakai untuk hasil pencarian kota. */
        fun idFor(coordinates: Coordinates): String =
            String.format(Locale.ROOT, "%.4f,%.4f", coordinates.latitude, coordinates.longitude)

        /**
         * Lokasi perangkat. Nama diambil dari response current weather;
         * bila kosong, koordinat dipakai sebagai nama.
         */
        fun currentLocation(coordinates: Coordinates, name: String?, countryCode: String?): Place =
            Place(
                id = CURRENT_LOCATION_ID,
                name = name?.takeIf { it.isNotBlank() } ?: String.format(
                    Locale.ROOT,
                    "%.4f, %.4f",
                    coordinates.latitude,
                    coordinates.longitude,
                ),
                country = countryCode?.takeIf { it.isNotBlank() },
                coordinates = coordinates,
            )
    }
}

/**
 * Informasi negara dari endpoint Country Code.
 *
 * @property code kode ISO 3166-1 alpha-2 dalam huruf besar, mis. "ID".
 */
data class Country(
    val code: String,
    val name: String,
    val flagEmoji: String?,
)
