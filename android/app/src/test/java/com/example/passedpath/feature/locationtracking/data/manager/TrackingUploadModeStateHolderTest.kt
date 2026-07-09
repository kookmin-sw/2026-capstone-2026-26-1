package com.example.passedpath.feature.locationtracking.data.manager

import com.example.passedpath.feature.locationtracking.domain.policy.TrackingUploadMode
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingUploadModeStateHolderTest {

    @Test
    fun `update reflects the latest mode on the state flow`() = runTest {
        val holder = InMemoryTrackingUploadModeStateHolder()

        holder.update(TrackingUploadMode.IMMEDIATE)

        assertEquals(TrackingUploadMode.IMMEDIATE, holder.uploadMode.value)
    }

    @Test
    fun `update emits a watching signal event even when the mode value is unchanged`() = runTest {
        val holder = InMemoryTrackingUploadModeStateHolder()
        val events = mutableListOf<TrackingUploadMode>()
        val job = launch { holder.watchingSignalEvents.collect { events += it } }
        runCurrent()

        holder.update(TrackingUploadMode.IMMEDIATE)
        runCurrent()
        holder.update(TrackingUploadMode.IMMEDIATE)
        runCurrent()

        assertEquals(
            listOf(TrackingUploadMode.IMMEDIATE, TrackingUploadMode.IMMEDIATE),
            events
        )
        job.cancel()
    }

    @Test
    fun `update emits a watching signal event when the mode changes`() = runTest {
        val holder = InMemoryTrackingUploadModeStateHolder()
        val events = mutableListOf<TrackingUploadMode>()
        val job = launch { holder.watchingSignalEvents.collect { events += it } }
        runCurrent()

        holder.update(TrackingUploadMode.IMMEDIATE)
        runCurrent()
        holder.update(TrackingUploadMode.NORMAL)
        runCurrent()

        assertEquals(
            listOf(TrackingUploadMode.IMMEDIATE, TrackingUploadMode.NORMAL),
            events
        )
        job.cancel()
    }
}
