package com.example.passedpath.feature.main.data.manager

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CurrentLocationProvider(
    context: Context
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        LOCATION_UPDATE_INTERVAL_MS
    )
        .setMinUpdateIntervalMillis(LOCATION_MIN_UPDATE_INTERVAL_MS)
        .build()

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): MainCoordinateUiState? = suspendCoroutine { continuation ->
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient
            .getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )
            .addOnSuccessListener { location ->
                continuation.resume(location?.toMainCoordinate())
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(
        onLocationUpdated: (MainCoordinateUiState) -> Unit
    ): LocationCallback {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.toMainCoordinate()?.let(onLocationUpdated)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        return locationCallback
    }

    fun stopLocationUpdates(locationCallback: LocationCallback) {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun android.location.Location.toMainCoordinate(): MainCoordinateUiState {
        return MainCoordinateUiState(
            latitude = latitude,
            longitude = longitude
        )
    }

    companion object {
        private const val LOCATION_UPDATE_INTERVAL_MS = 5_000L
        private const val LOCATION_MIN_UPDATE_INTERVAL_MS = 2_000L
    }
}
