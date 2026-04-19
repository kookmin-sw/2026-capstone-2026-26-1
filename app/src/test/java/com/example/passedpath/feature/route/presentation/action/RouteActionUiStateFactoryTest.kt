package com.example.passedpath.feature.route.presentation.action

import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.SelectedDayRouteUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteActionUiStateFactoryTest {

    @Test
    fun `today route exposes refresh and tracking actions`() {
        val routeMode = MainRouteModeUiState.Today(
            route = SelectedDayRouteUiState(dateKey = "2026-04-19"),
            canRefreshDistance = true,
            isTrackingToggleVisible = true,
            isTrackingEnabled = true
        )

        val result = buildRouteActionUiState(routeMode)

        assertTrue(result.showRefresh)
        assertTrue(result.showTrackingToggle)
        assertFalse(result.showPlayback)
        assertTrue(result.isTrackingEnabled)
    }

    @Test
    fun `past route exposes playback action only`() {
        val routeMode = MainRouteModeUiState.Past(
            route = SelectedDayRouteUiState(dateKey = "2026-04-18"),
            isPlaybackEntryVisible = true
        )

        val result = buildRouteActionUiState(routeMode)

        assertFalse(result.showRefresh)
        assertFalse(result.showTrackingToggle)
        assertTrue(result.showPlayback)
        assertFalse(result.isTrackingEnabled)
    }
}
