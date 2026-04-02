package com.example.passedpath.feature.route.presentation.mapper

import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.DayRouteDetail
import com.example.passedpath.feature.locationtracking.domain.model.DayRoutePlace
import com.example.passedpath.feature.locationtracking.domain.model.RoutePoint
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.route.presentation.state.MainRouteModeUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteUiMapperTest {

    @Test
    fun `daily path maps to selected route ui state`() {
        val dailyPath = DailyPath(
            dateKey = "2026-04-01",
            points = listOf(
                TrackedLocation(37.1, 127.1, 5f, 1L),
                TrackedLocation(37.2, 127.2, 5f, 2L)
            ),
            totalDistanceMeters = 2450.0,
            pathPointCount = 2
        )

        val uiState = dailyPath.toSelectedDayRouteUiState()

        assertEquals("2026-04-01", uiState.dateKey)
        assertEquals("", uiState.title)
        assertEquals("", uiState.memo)
        assertEquals(2, uiState.polylinePoints.size)
        assertEquals(2.45, uiState.totalDistanceKm, 0.0)
        assertTrue(uiState.places.isEmpty())
    }

    @Test
    fun `day route detail maps to selected route ui state with title memo and places`() {
        val routeDetail = DayRouteDetail(
            dateKey = "2026-03-31",
            totalDistanceKm = 7.8,
            title = "Spring walk",
            memo = "Warm and clear",
            pathPointCount = 3,
            polylinePoints = listOf(
                RoutePoint(37.1, 127.1),
                RoutePoint(37.2, 127.2),
                RoutePoint(37.3, 127.3)
            ),
            places = listOf(
                DayRoutePlace(10L, "Seoul Forest", "Ttukseom-ro", 37.4, 127.4, 1),
                DayRoutePlace(11L, "Cafe", "Seoul Forest 2-gil", 37.5, 127.5, 2)
            )
        )

        val uiState = routeDetail.toSelectedDayRouteUiState()

        assertEquals("2026-03-31", uiState.dateKey)
        assertEquals("Spring walk", uiState.title)
        assertEquals("Warm and clear", uiState.memo)
        assertEquals(3, uiState.polylinePoints.size)
        assertEquals(7.8, uiState.totalDistanceKm, 0.0)
        assertEquals(2, uiState.places.size)
        assertEquals("Seoul Forest", uiState.places.first().placeName)
        assertEquals(2, uiState.places.last().orderIndex)
    }

    @Test
    fun `createLoadingRouteMode returns today loading state`() {
        val state = createLoadingRouteMode(
            dateKey = "2026-04-01",
            isToday = true
        ) as MainRouteModeUiState.Today

        assertTrue(state.isRouteLoading)
        assertFalse(state.isRouteEmpty)
        assertEquals("2026-04-01", state.route.dateKey)
    }

    @Test
    fun `createLoadingRouteMode returns past loading state`() {
        val state = createLoadingRouteMode(
            dateKey = "2026-03-31",
            isToday = false
        ) as MainRouteModeUiState.Past

        assertTrue(state.isRouteLoading)
        assertFalse(state.isRouteEmpty)
        assertEquals("2026-03-31", state.route.dateKey)
    }

    @Test
    fun `createPastErrorRouteMode returns retryable past state`() {
        val state = createPastErrorRouteMode("2026-03-31")

        assertEquals("2026-03-31", state.route.dateKey)
        assertEquals("선택한 날짜의 경로를 불러오지 못했습니다.", state.routeErrorMessage)
        assertFalse(state.isRouteLoading)
    }
}
