package com.example.passedpath.feature.route.presentation.action

import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import com.example.passedpath.feature.route.presentation.state.SelectedDayRouteUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteActionUiStateFactoryTest {

    @Test
    fun `today route exposes tracking action only`() {
        val routeMode = MainRouteModeUiState.Today(
            route = SelectedDayRouteUiState(dateKey = "2026-04-19"),
            canRefreshDistance = true,
            isTrackingToggleVisible = true,
            isTrackingEnabled = true
        )

        val result = buildRouteActionUiState(routeMode)

        assertTrue(result.showTrackingToggle)
        assertTrue(result.isTrackingEnabled)
    }

    @Test
    fun `past route exposes no floating route actions`() {
        val routeMode = MainRouteModeUiState.Past(
            route = SelectedDayRouteUiState(dateKey = "2026-04-18")
        )

        val result = buildRouteActionUiState(routeMode)

        assertFalse(result.showTrackingToggle)
        assertFalse(result.isTrackingEnabled)
    }
}
