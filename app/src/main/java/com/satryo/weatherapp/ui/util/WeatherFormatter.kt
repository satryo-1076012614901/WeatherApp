package com.satryo.weatherapp.ui.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Format tampilan data cuaca (Bahasa Indonesia, satuan metrik).
 * Nilai null ditampilkan sebagai "-".
 */
object WeatherFormatter {

    private val locale: Locale = Locale.forLanguageTag("id-ID")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", locale)
    private val dayFormatter = DateTimeFormatter.ofPattern("EEEE", locale)
    private val fullDateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", locale)
    private val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(locale))

    private val windDirections = listOf(
        "Utara", "Timur Laut", "Timur", "Tenggara",
        "Selatan", "Barat Daya", "Barat", "Barat Laut",
    )

    const val EMPTY = "-"

    /** Contoh: 27.6 -> "28°". */
    fun temperature(celsius: Double?): String =
        celsius?.let { "${it.roundToInt()}°" } ?: EMPTY

    /** Contoh: "Maks 31° · Min 24°". */
    fun highLow(max: Double?, min: Double?): String =
        "Maks ${temperature(max)} · Min ${temperature(min)}"

    /** Kecepatan angin dari m/s ke km/jam. */
    fun windSpeed(metersPerSecond: Double?): String =
        metersPerSecond?.let { "${(it * 3.6).roundToInt()} km/j" } ?: EMPTY

    /** Arah asal angin dari derajat meteorologis (0 = Utara). */
    fun windDirection(degree: Int?): String? {
        if (degree == null) return null
        val normalized = ((degree % 360) + 360) % 360
        val index = ((normalized + 22.5) / 45).toInt() % windDirections.size
        return windDirections[index]
    }

    fun percentage(value: Int?): String = value?.let { "$it%" } ?: EMPTY

    /** Peluang presipitasi 0.0 - 1.0 menjadi persen, mis. 0.35 -> "35%". */
    fun probability(pop: Double?): String? = pop?.let { "${(it * 100).roundToInt()}%" }

    /** Peluang hujan untuk ditampilkan; di bawah 10% disembunyikan agar tidak ramai. */
    fun rainChance(pop: Double?): String? = pop?.takeIf { it >= 0.1 }?.let { probability(it) }

    fun pressure(hectopascal: Int?): String = hectopascal?.let { "$it hPa" } ?: EMPTY

    /** Jarak pandang dari meter ke km, mis. 8500 -> "8,5 km". */
    fun visibility(meters: Int?): String =
        meters?.let { "${decimalFormat.format(it / 1000.0)} km" } ?: EMPTY

    /** Jam lokal lokasi cuaca (bukan jam perangkat), mis. "14:00". */
    fun time(epochSeconds: Long?, offsetSeconds: Int): String {
        if (epochSeconds == null) return EMPTY
        return Instant.ofEpochSecond(epochSeconds)
            .atOffset(ZoneOffset.ofTotalSeconds(offsetSeconds))
            .format(timeFormatter)
    }

    /** Nama hari lokal lokasi cuaca, mis. "Sabtu". */
    fun dayName(epochSeconds: Long, offsetSeconds: Int): String =
        Instant.ofEpochSecond(epochSeconds)
            .atOffset(ZoneOffset.ofTotalSeconds(offsetSeconds))
            .format(dayFormatter)

    /** Tanggal hari ini menurut perangkat, mis. "Jumat, 18 September 2026". */
    fun today(): String = LocalDate.now().format(fullDateFormatter)

    /** Huruf pertama kapital, mis. "hujan ringan" -> "Hujan ringan". */
    fun description(text: String): String =
        text.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}
