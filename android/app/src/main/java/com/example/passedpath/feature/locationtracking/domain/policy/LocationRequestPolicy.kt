package com.example.passedpath.feature.locationtracking.domain.policy

enum class TrackingLocationMode {
    MOVING,
    IDLE
}

object TrackingModePolicy {
    data class RequestConfig(
        val updateIntervalMs: Long,
        val minUpdateIntervalMs: Long,
        val minUpdateDistanceMeters: Float
    )

    private const val IDLE_AFTER_LAST_SAVED_POINT_MS = 5 * 60_000L

    private val movingConfig = RequestConfig(
        updateIntervalMs = 60_000L,
        minUpdateIntervalMs = 30_000L,
        minUpdateDistanceMeters = 35f
    )
    private val idleConfig = RequestConfig(
        updateIntervalMs = 5 * 60_000L,
        minUpdateIntervalMs = 2 * 60_000L,
        minUpdateDistanceMeters = 50f
    )

    fun initialMode(): TrackingLocationMode = TrackingLocationMode.MOVING

    fun requestConfigFor(mode: TrackingLocationMode): RequestConfig {
        return when (mode) {
            TrackingLocationMode.MOVING -> movingConfig
            TrackingLocationMode.IDLE -> idleConfig
        }
    }

    fun callbackSilenceThresholdMs(mode: TrackingLocationMode): Long {
        return requestConfigFor(mode).updateIntervalMs * 2
    }

    fun idleFallbackDelayMillis(): Long = IDLE_AFTER_LAST_SAVED_POINT_MS
}
