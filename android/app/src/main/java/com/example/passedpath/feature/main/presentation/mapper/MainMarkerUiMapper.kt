package com.example.passedpath.feature.main.presentation.mapper

import com.example.passedpath.feature.place.domain.model.VisitedPlace
import com.example.passedpath.feature.place.presentation.state.PlaceListUiState
import com.example.passedpath.feature.route.presentation.state.PlaceMarkerUiState
import com.example.passedpath.feature.route.presentation.state.SelectedDayRouteUiState

internal fun resolveMainMarkerPlaces(
    placeListUiState: PlaceListUiState,
    route: SelectedDayRouteUiState
): List<PlaceMarkerUiState> {
    return if (placeListUiState.hasLoaded) {
        placeListUiState.places.toPlaceMarkerUiStates()
    } else {
        route.markerPlaces
    }
}

private fun List<VisitedPlace>.toPlaceMarkerUiStates(): List<PlaceMarkerUiState> {
    return sortedBy(VisitedPlace::orderIndex)
        .map { place ->
            PlaceMarkerUiState(
                placeId = place.placeId,
                placeName = place.placeName,
                roadAddress = place.roadAddress,
                latitude = place.latitude,
                longitude = place.longitude,
                orderIndex = place.orderIndex
            )
        }
}
