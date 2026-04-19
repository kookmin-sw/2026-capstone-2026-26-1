package com.example.passedpath.feature.locationtracking.domain.policy

object LocationRequestPolicy {
    data class RequestConfig(
        val updateIntervalMs: Long,
        val minUpdateIntervalMs: Long,
        val minUpdateDistanceMeters: Float
    )

    private val movingConfig = RequestConfig(
        updateIntervalMs = 60_000L,
        minUpdateIntervalMs = 30_000L,
        minUpdateDistanceMeters = 20f
    )
    private val idleConfig = RequestConfig(
        updateIntervalMs = 5 * 60_000L,
        minUpdateIntervalMs = 2 * 60_000L,
        minUpdateDistanceMeters = 50f
    )

    fun configFor(mode: TrackingLocationMode): RequestConfig {
        return when (mode) {
            TrackingLocationMode.MOVING -> movingConfig
            TrackingLocationMode.IDLE -> idleConfig
        }
    }

    fun callbackSilenceThresholdMs(mode: TrackingLocationMode): Long {
        return configFor(mode).updateIntervalMs * 2
    }
}
