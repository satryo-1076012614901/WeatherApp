package com.satryo.weatherapp.data.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.satryo.weatherapp.data.model.Coordinates
import com.satryo.weatherapp.data.model.LocationPermissionException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Sumber data lokasi perangkat.
 * Catatan: ini bukan komponen Android `Service`, melainkan data source di data layer.
 */
interface LocationService {

    /** True bila izin lokasi (coarse atau fine) sudah diberikan. */
    fun hasLocationPermission(): Boolean

    /**
     * @return koordinat perangkat, atau null bila lokasi tidak tersedia
     * (mis. layanan lokasi dimatikan).
     * @throws LocationPermissionException bila izin lokasi belum diberikan.
     */
    suspend fun getCurrentCoordinates(): Coordinates?
}

/**
 * Implementasi [LocationService] berbasis FusedLocationProviderClient (Google Play Services).
 */
class FusedLocationService(context: Context) : LocationService {

    private val appContext = context.applicationContext
    private val client: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)

    override fun hasLocationPermission(): Boolean =
        LOCATION_PERMISSIONS.any { permission ->
            ContextCompat.checkSelfPermission(appContext, permission) ==
                PackageManager.PERMISSION_GRANTED
        }

    @SuppressLint("MissingPermission") // Izin sudah diperiksa melalui hasLocationPermission().
    override suspend fun getCurrentCoordinates(): Coordinates? {
        if (!hasLocationPermission()) throw LocationPermissionException()

        val cancellationSource = CancellationTokenSource()
        val location: Location? = client
            .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationSource.token)
            .await(cancellationSource)
            ?: client.lastLocation.await()

        return location?.let { Coordinates(it.latitude, it.longitude) }
    }

    companion object {
        /** Izin yang diminta ke pengguna. Cukup salah satu diberikan. */
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }
}

/**
 * Mengubah [Task] Play Services menjadi suspend function tanpa dependency tambahan.
 */
private suspend fun <T> Task<T>.await(
    cancellationSource: CancellationTokenSource? = null,
): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { error -> continuation.resumeWithException(error) }
    addOnCanceledListener { continuation.cancel() }
    continuation.invokeOnCancellation { cancellationSource?.cancel() }
}
