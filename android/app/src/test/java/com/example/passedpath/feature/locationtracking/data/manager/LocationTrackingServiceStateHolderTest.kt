package com.example.passedpath.feature.locationtracking.data.manager

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationTrackingServiceStateHolderTest {

    @Test
    fun `holder starts as not tracking and reflects updates`() {
        val holder = InMemoryLocationTrackingServiceStateHolder()

        assertFalse(holder.isTracking.value)

        holder.update(isTracking = true)
        assertTrue(holder.isTracking.value)

        holder.update(isTracking = false)
        assertFalse(holder.isTracking.value)
    }

    @Test
    fun `holder remembers whether tracking is enabled by user`() {
        val holder = InMemoryLocationTrackingServiceStateHolder()

        assertTrue(holder.isTrackingEnabledByUser())

        holder.setTrackingEnabledByUser(false)
        assertFalse(holder.isTrackingEnabledByUser())

        holder.setTrackingEnabledByUser(true)
        assertTrue(holder.isTrackingEnabledByUser())
    }
}
