package com.example.passedpath.feature.locationtracking.data.manager

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.locationtracking.domain.policy.AdaptiveTrackingModePolicy
import com.example.passedpath.feature.locationtracking.domain.policy.LocationRequestPolicy
import com.example.passedpath.feature.locationtracking.domain.policy.TrackingLocationMode
import com.example.passedpath.feature.locationtracking.domain.tracker.LocationTracker
import com.example.passedpath.feature.locationtracking.domain.tracker.LocationTrackingSession
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TrackingLocationProvider(
    context: Context
) : LocationTracker {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): TrackedLocation? = suspendCoroutine { continuation ->
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient
            .getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
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

        return GoogleTrackingLocationSession(
            fusedLocationClient = fusedLocationClient,
            locationCallback = locationCallback,
            currentMode = AdaptiveTrackingModePolicy.initialMode()
        ).also { session ->
            session.start()
        }
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

private class GoogleTrackingLocationSession(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val locationCallback: LocationCallback,
    private var currentMode: TrackingLocationMode
) : LocationTrackingSession {

    @SuppressLint("MissingPermission")
    fun start() {
        fusedLocationClient.requestLocationUpdates(
            buildLocationRequest(currentMode),
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun stop() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    override fun updateMode(mode: TrackingLocationMode) {
        if (currentMode == mode) return
        currentMode = mode
        fusedLocationClient.removeLocationUpdates(locationCallback)
        fusedLocationClient.requestLocationUpdates(
            buildLocationRequest(currentMode),
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun buildLocationRequest(mode: TrackingLocationMode): LocationRequest {
        val config = LocationRequestPolicy.configFor(mode)
        return LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            config.updateIntervalMs
        )
            .setMinUpdateIntervalMillis(config.minUpdateIntervalMs)
            .setMinUpdateDistanceMeters(config.minUpdateDistanceMeters)
            .setWaitForAccurateLocation(false)
            .build()
    }
}
