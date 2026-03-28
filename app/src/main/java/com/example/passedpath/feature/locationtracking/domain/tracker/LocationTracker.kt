package com.example.passedpath.feature.locationtracking.domain.tracker

import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation

interface LocationTrackingSession {
    fun stop()
}

interface LocationTracker {
    suspend fun getCurrentLocation(): TrackedLocation?
    fun startLocationUpdates(onLocationUpdated: (TrackedLocation) -> Unit): LocationTrackingSession
}
