package com.example.passedpath.feature.main.presentation.policy

import com.example.passedpath.feature.route.presentation.state.RouteUiAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MainRouteActionRequestTest {

    @Test
    fun `refresh action resolves to today reload request`() {
        val result = resolveMainRouteActionRequest(
            action = RouteUiAction.RefreshTodayRoute,
            selectedDateKey = "2026-04-19"
        )

        assertTrue(result is MainRouteActionRequest.ReloadRoute)
        result as MainRouteActionRequest.ReloadRoute
        assertEquals("2026-04-19", result.dateKey)
        assertEquals(RouteReloadTrigger.TodayRefresh, result.trigger)
    }

    @Test
    fun `retry action resolves to past retry reload request`() {
        val result = resolveMainRouteActionRequest(
            action = RouteUiAction.RetryPastRoute,
            selectedDateKey = "2026-04-18"
        )

        assertTrue(result is MainRouteActionRequest.ReloadRoute)
        result as MainRouteActionRequest.ReloadRoute
        assertEquals("2026-04-18", result.dateKey)
        assertEquals(RouteReloadTrigger.PastRetry, result.trigger)
    }

    @Test
    fun `toggle tracking action resolves without reload`() {
        val result = resolveMainRouteActionRequest(
            action = RouteUiAction.ToggleTracking,
            selectedDateKey = "2026-04-19"
        )

        assertEquals(MainRouteActionRequest.ToggleTracking, result)
    }
}
