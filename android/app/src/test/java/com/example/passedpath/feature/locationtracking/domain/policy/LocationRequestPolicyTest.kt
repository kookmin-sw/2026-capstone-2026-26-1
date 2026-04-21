package com.example.passedpath.feature.locationtracking.domain.policy

import org.junit.Assert.assertEquals
import org.junit.Test

class LocationRequestPolicyTest {

    @Test
    fun `moving mode uses tighter request config`() {
        val config = TrackingModePolicy.requestConfigFor(TrackingLocationMode.MOVING)

        assertEquals(60_000L, config.updateIntervalMs)
        assertEquals(30_000L, config.minUpdateIntervalMs)
        assertEquals(30f, config.minUpdateDistanceMeters)
    }

    @Test
    fun `idle mode uses relaxed request config`() {
        val config = TrackingModePolicy.requestConfigFor(TrackingLocationMode.IDLE)

        assertEquals(5 * 60_000L, config.updateIntervalMs)
        assertEquals(2 * 60_000L, config.minUpdateIntervalMs)
        assertEquals(50f, config.minUpdateDistanceMeters)
    }
}
