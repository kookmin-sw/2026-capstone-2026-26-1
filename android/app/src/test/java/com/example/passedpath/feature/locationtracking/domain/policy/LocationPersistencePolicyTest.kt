package com.example.passedpath.feature.locationtracking.domain.policy

import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationPersistencePolicyTest {

    @Test
    fun `persists first location when accuracy is acceptable`() {
        val candidate = TrackedLocation(
            latitude = 37.0,
            longitude = 127.0,
            accuracyMeters = 20f,
            recordedAtEpochMillis = 1L
        )

        assertTrue(
            LocationPersistencePolicy.shouldPersistLocation(
                latestSavedLocation = null,
                candidateLocation = candidate
            )
        )
    }

    @Test
    fun `drops location when accuracy exceeds max`() {
        val candidate = TrackedLocation(
            latitude = 37.0,
            longitude = 127.0,
            accuracyMeters = 70f,
            recordedAtEpochMillis = 1L
        )

        assertFalse(
            LocationPersistencePolicy.shouldPersistLocation(
                latestSavedLocation = null,
                candidateLocation = candidate
            )
        )
    }

    @Test
    fun `drops location when moved distance is below min save distance`() {
        val latest = TrackedLocation(
            latitude = 37.5665,
            longitude = 126.9780,
            accuracyMeters = 10f,
            recordedAtEpochMillis = 1L
        )
        val candidate = TrackedLocation(
            latitude = 37.5665,
            longitude = 126.9780,
            accuracyMeters = 10f,
            recordedAtEpochMillis = 2L
        )

        assertFalse(
            LocationPersistencePolicy.shouldPersistLocation(
                latestSavedLocation = latest,
                candidateLocation = candidate
            )
        )
    }
}
