package com.example.passedpath.feature.route.presentation.policy

import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteGapRenderPolicyTest {

    @Test
    fun `segment is dashed when time gap and distance gap both exceed threshold`() {
        val start = MainCoordinateUiState(
            latitude = 37.5665,
            longitude = 126.9780,
            recordedAtEpochMillis = 0L
        )
        val end = MainCoordinateUiState(
            latitude = 37.5758,
            longitude = 126.9900,
            recordedAtEpochMillis = 10 * 60 * 1000L
        )

        assertTrue(shouldRenderGapAsDashed(start = start, end = end))
    }

    @Test
    fun `segment stays solid when timestamps are missing`() {
        val start = MainCoordinateUiState(latitude = 37.5665, longitude = 126.9780)
        val end = MainCoordinateUiState(latitude = 37.5758, longitude = 126.9900)

        assertFalse(shouldRenderGapAsDashed(start = start, end = end))
    }

    @Test
    fun `segment stays solid when only time gap exceeds threshold`() {
        val start = MainCoordinateUiState(
            latitude = 37.5665,
            longitude = 126.9780,
            recordedAtEpochMillis = 0L
        )
        val end = MainCoordinateUiState(
            latitude = 37.5670,
            longitude = 126.9785,
            recordedAtEpochMillis = 10 * 60 * 1000L
        )

        assertFalse(shouldRenderGapAsDashed(start = start, end = end))
    }
}
