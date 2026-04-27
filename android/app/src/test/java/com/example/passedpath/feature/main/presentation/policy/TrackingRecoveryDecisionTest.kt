package com.example.passedpath.feature.main.presentation.policy

import com.example.passedpath.feature.permission.presentation.state.LocationPermissionUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingRecoveryDecisionTest {

    @Test
    fun `user enabled inactive tracking with access resolves to restart`() {
        val result = decideTrackingRecovery(
            permissionState = LocationPermissionUiState.ALWAYS,
            isLocationServiceEnabled = true,
            isTrackingActive = false,
            isTrackingEnabledByUser = true
        )

        assertEquals(TrackingRecoveryDecision.RestartTracking, result)
    }

    @Test
    fun `user disabled active tracking with access resolves to stop`() {
        val result = decideTrackingRecovery(
            permissionState = LocationPermissionUiState.ALWAYS,
            isLocationServiceEnabled = true,
            isTrackingActive = true,
            isTrackingEnabledByUser = false
        )

        assertEquals(TrackingRecoveryDecision.StopTracking, result)
    }

    @Test
    fun `active tracking without access resolves to stop`() {
        val result = decideTrackingRecovery(
            permissionState = LocationPermissionUiState.DENIED,
            isLocationServiceEnabled = true,
            isTrackingActive = true,
            isTrackingEnabledByUser = true
        )

        assertEquals(TrackingRecoveryDecision.StopTracking, result)
    }

    @Test
    fun `inactive tracking without access resolves to no op`() {
        val result = decideTrackingRecovery(
            permissionState = LocationPermissionUiState.DENIED,
            isLocationServiceEnabled = true,
            isTrackingActive = false,
            isTrackingEnabledByUser = true
        )

        assertEquals(TrackingRecoveryDecision.NoOp, result)
    }

    @Test
    fun `user enabled active tracking with access resolves to no op`() {
        val result = decideTrackingRecovery(
            permissionState = LocationPermissionUiState.ALWAYS,
            isLocationServiceEnabled = true,
            isTrackingActive = true,
            isTrackingEnabledByUser = true
        )

        assertEquals(TrackingRecoveryDecision.NoOp, result)
    }
}
