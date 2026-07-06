package com.example.passedpath.feature.care.presentation.mapper

import com.example.passedpath.feature.care.domain.model.ProtectedPersonDayRouteDetail
import com.example.passedpath.feature.care.domain.model.ProtectedPersonRoutePoint
import com.example.passedpath.feature.locationtracking.domain.policy.MapPolylineSimplificationPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtectedPersonRouteDetailUiMapperTest {

    @Test
    fun `route detail maps date distance and route points`() {
        val routeDetail = ProtectedPersonDayRouteDetail(
            dateKey = "2026-01-20",
            totalDistanceKm = 3.42,
            pathPointCount = 2,
            routePoints = listOf(
                routePoint(latitude = 37.1, longitude = 127.1),
                routePoint(latitude = 37.2, longitude = 127.2)
            )
        )

        val uiState = routeDetail.toProtectedPersonRouteMapUiState()

        assertEquals("2026-01-20", uiState.dateKey)
        assertEquals("2026.01.20. \uD654", uiState.dateText)
        assertEquals("3.4km", uiState.totalDistanceText)
        assertEquals(2, uiState.pathPointCount)
        assertEquals(2, uiState.mapPolylinePoints.size)
        assertEquals(37.1, uiState.mapPolylinePoints.first().latitude, CoordinateDelta)
        assertEquals(127.2, uiState.mapPolylinePoints.last().longitude, CoordinateDelta)
    }

    @Test
    fun `route detail caps long polyline for map rendering`() {
        val points = List(900) { index ->
            routePoint(
                latitude = 37.0 + index * 0.0001,
                longitude = 127.0 + index * 0.0001
            )
        }
        val routeDetail = ProtectedPersonDayRouteDetail(
            dateKey = "2026-01-20",
            totalDistanceKm = 3.4,
            pathPointCount = points.size,
            routePoints = points
        )

        val uiState = routeDetail.toProtectedPersonRouteMapUiState()

        assertTrue(
            uiState.mapPolylinePoints.size <=
                MapPolylineSimplificationPolicy.MAX_MAP_POLYLINE_POINTS
        )
        assertEquals(points.first().latitude, uiState.mapPolylinePoints.first().latitude, CoordinateDelta)
        assertEquals(points.last().longitude, uiState.mapPolylinePoints.last().longitude, CoordinateDelta)
    }

    @Test
    fun `invalid date keeps original date key text`() {
        val routeDetail = ProtectedPersonDayRouteDetail(
            dateKey = "bad-date",
            totalDistanceKm = -1.0,
            pathPointCount = 0,
            routePoints = emptyList()
        )

        val uiState = routeDetail.toProtectedPersonRouteMapUiState()

        assertEquals("bad-date", uiState.dateText)
        assertEquals("0.0km", uiState.totalDistanceText)
    }

    private fun routePoint(
        latitude: Double,
        longitude: Double
    ): ProtectedPersonRoutePoint {
        return ProtectedPersonRoutePoint(
            recordedAt = "2026-01-20T09:00:00+09:00",
            recordedAtEpochMillis = null,
            latitude = latitude,
            longitude = longitude
        )
    }

    private companion object {
        const val CoordinateDelta = 0.000001
    }
}
