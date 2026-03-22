package com.example.passedpath.feature.main.data.manager

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.locationtracking.domain.policy.LocationTrackingPolicy
import com.example.passedpath.feature.locationtracking.domain.tracker.LocationTracker
import com.example.passedpath.feature.locationtracking.domain.tracker.LocationTrackingSession
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
) : LocationTracker {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        LocationTrackingPolicy.LOCATION_UPDATE_INTERVAL_MS
    )
        .setMinUpdateIntervalMillis(LocationTrackingPolicy.LOCATION_MIN_UPDATE_INTERVAL_MS)
        .setMinUpdateDistanceMeters(LocationTrackingPolicy.LOCATION_MIN_UPDATE_DISTANCE_METERS)
        .build()

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): TrackedLocation? = suspendCoroutine { continuation ->
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient
            .getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            )
            .addOnSuccessListener { location ->
                continuation.resume(location?.toTrackedLocation())
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }

    @SuppressLint("MissingPermission")
    override fun startLocationUpdates(
        onLocationUpdated: (TrackedLocation) -> Unit
    ): LocationTrackingSession {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.toTrackedLocation()?.let(onLocationUpdated)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        return GoogleLocationTrackingSession(
            fusedLocationClient = fusedLocationClient,
            locationCallback = locationCallback
        )
    }

    private fun android.location.Location.toTrackedLocation(): TrackedLocation {
        return TrackedLocation(
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = if (hasAccuracy()) accuracy else null,
            recordedAtEpochMillis = time
        )
    }
}

private class GoogleLocationTrackingSession(
    private val fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    private val locationCallback: LocationCallback
) : LocationTrackingSession {
    override fun stop() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
