package com.example.passedpath.feature.main.presentation.mapper

import com.example.passedpath.feature.place.domain.model.PlaceSourceType
import com.example.passedpath.feature.place.domain.model.VisitedPlace
import com.example.passedpath.feature.place.presentation.state.PlaceListUiState
import com.example.passedpath.feature.route.presentation.state.PlaceMarkerUiState
import com.example.passedpath.feature.route.presentation.state.SelectedDayRouteUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MainMarkerUiMapperTest {

    @Test
    fun `uses route snapshot markers before place list is loaded`() {
        val routeMarkers = listOf(placeMarker(placeId = 10L, orderIndex = 1))

        val result = resolveMainMarkerPlaces(
            placeListUiState = PlaceListUiState(hasLoaded = false),
            route = SelectedDayRouteUiState(
                dateKey = "2026-04-28",
                markerPlaces = routeMarkers
            )
        )

        assertEquals(routeMarkers, result)
    }

    @Test
    fun `uses loaded place list markers when places exist`() {
        val result = resolveMainMarkerPlaces(
            placeListUiState = PlaceListUiState(
                hasLoaded = true,
                places = listOf(
                    visitedPlace(placeId = 2L, orderIndex = 2),
                    visitedPlace(placeId = 1L, orderIndex = 1)
                )
            ),
            route = SelectedDayRouteUiState(
                dateKey = "2026-04-28",
                markerPlaces = listOf(placeMarker(placeId = 10L, orderIndex = 1))
            )
        )

        assertEquals(listOf(1L, 2L), result.map(PlaceMarkerUiState::placeId))
        assertEquals(listOf(1, 2), result.map(PlaceMarkerUiState::orderIndex))
    }

    @Test
    fun `keeps markers empty when loaded place list is empty`() {
        val result = resolveMainMarkerPlaces(
            placeListUiState = PlaceListUiState(hasLoaded = true, places = emptyList()),
            route = SelectedDayRouteUiState(
                dateKey = "2026-04-28",
                markerPlaces = listOf(placeMarker(placeId = 10L, orderIndex = 1))
            )
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `uses stale retained place list markers`() {
        val result = resolveMainMarkerPlaces(
            placeListUiState = PlaceListUiState(
                hasLoaded = true,
                isStale = true,
                places = listOf(visitedPlace(placeId = 3L, orderIndex = 1))
            ),
            route = SelectedDayRouteUiState(
                dateKey = "2026-04-28",
                markerPlaces = listOf(placeMarker(placeId = 10L, orderIndex = 1))
            )
        )

        assertEquals(listOf(3L), result.map(PlaceMarkerUiState::placeId))
    }

    private fun visitedPlace(
        placeId: Long,
        orderIndex: Int
    ): VisitedPlace {
        return VisitedPlace(
            placeId = placeId,
            placeName = "Place $placeId",
            source = PlaceSourceType.MANUAL,
            roadAddress = "Road $placeId",
            latitude = 37.0 + placeId,
            longitude = 127.0 + placeId,
            orderIndex = orderIndex
        )
    }

    private fun placeMarker(
        placeId: Long,
        orderIndex: Int
    ): PlaceMarkerUiState {
        return PlaceMarkerUiState(
            placeId = placeId,
            placeName = "Route $placeId",
            roadAddress = "Route Road $placeId",
            latitude = 37.0 + placeId,
            longitude = 127.0 + placeId,
            orderIndex = orderIndex
        )
    }
}
