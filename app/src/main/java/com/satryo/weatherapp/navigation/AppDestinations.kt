package com.satryo.weatherapp.navigation

import android.net.Uri
import com.satryo.weatherapp.data.model.Place

/**
 * Kontrak destinasi navigasi.
 */
interface NavigationDestination {
    val route: String
}

object DashboardDestination : NavigationDestination {
    override val route = "dashboard"
}

/** Halaman pencarian kota (direct geocoding). */
object SearchDestination : NavigationDestination {
    override val route = "search"
}

/**
 * Halaman Detail menerima koordinat (path) serta identitas lokasi (query): ID, nama,
 * wilayah, dan kode negara. Data ini dipakai untuk judul halaman dan untuk tombol bintang
 * (menyimpan/menghapus kota dari Dashboard), sehingga Detail dapat dibuka dari card GPS,
 * card kota tersimpan, maupun hasil pencarian.
 */
object DetailDestination : NavigationDestination {
    const val ARG_LAT = "lat"
    const val ARG_LON = "lon"
    const val ARG_ID = "id"
    const val ARG_NAME = "name"
    const val ARG_REGION = "region"
    const val ARG_COUNTRY = "country"

    override val route = "detail"

    val routeWithArgs =
        "$route/{$ARG_LAT}/{$ARG_LON}" +
            "?$ARG_ID={$ARG_ID}&$ARG_NAME={$ARG_NAME}&$ARG_REGION={$ARG_REGION}&$ARG_COUNTRY={$ARG_COUNTRY}"

    /** Argumen opsional yang bernilai null tidak dimasukkan ke route. */
    fun createRoute(place: Place): String {
        val query = listOfNotNull(
            "$ARG_ID=${Uri.encode(place.id)}",
            "$ARG_NAME=${Uri.encode(place.name)}",
            place.region?.let { "$ARG_REGION=${Uri.encode(it)}" },
            place.country?.let { "$ARG_COUNTRY=${Uri.encode(it)}" },
        ).joinToString("&")
        return "$route/${place.coordinates.latitude}/${place.coordinates.longitude}?$query"
    }
}
