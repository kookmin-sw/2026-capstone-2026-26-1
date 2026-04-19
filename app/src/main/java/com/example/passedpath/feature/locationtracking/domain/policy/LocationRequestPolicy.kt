package com.example.passedpath.feature.locationtracking.domain.policy

object LocationRequestPolicy {
    const val UPDATE_INTERVAL_MS = 60_000L
    const val MIN_UPDATE_INTERVAL_MS = 30_000L
    const val MIN_UPDATE_DISTANCE_METERS = 20f

    const val CALLBACK_SILENCE_THRESHOLD_MS = UPDATE_INTERVAL_MS * 2
}
