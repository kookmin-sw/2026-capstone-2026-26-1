package com.example.passedpath.feature.locationtracking.domain.tracker

import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.locationtracking.domain.policy.TrackingLocationMode

interface LocationTrackingSession {
    fun stop()

    fun updateMode(mode: TrackingLocationMode)
}

interface LocationTracker {
    suspend fun getCurrentLocation(): TrackedLocation?

    fun startLocationUpdates(onLocationUpdated: (TrackedLocation) -> Unit): LocationTrackingSession
}
