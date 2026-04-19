package com.example.passedpath.feature.locationtracking.domain.policy

enum class TrackingLocationMode {
    MOVING,
    IDLE
}

object AdaptiveTrackingModePolicy {
    const val IDLE_AFTER_LAST_SAVED_POINT_MS = 5 * 60_000L

    fun initialMode(): TrackingLocationMode = TrackingLocationMode.MOVING

    fun idleFallbackDelayMillis(): Long = IDLE_AFTER_LAST_SAVED_POINT_MS
}
