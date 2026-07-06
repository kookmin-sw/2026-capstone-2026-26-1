package com.example.passedpath.feature.care.presentation.state

import androidx.compose.runtime.Immutable
import com.example.passedpath.feature.care.presentation.model.ProtectedPersonBottomSheetTab
import com.example.passedpath.feature.route.presentation.state.PlaceMarkerUiState
import com.example.passedpath.ui.component.bottomsheet.BaseBottomSheetValue
import com.example.passedpath.ui.state.CoordinateUiState

@Immutable
data class ProtectedPersonRouteDetailUiState(
    val dependentNickname: String = "",
    val dateKey: String = "",
    val route: ProtectedPersonRouteMapUiState = ProtectedPersonRouteMapUiState(),
    val isRouteLoading: Boolean = false,
    val isRouteEmpty: Boolean = false,
    val routeErrorMessage: String? = null,
    val placeListUiState: ProtectedPersonPlaceListUiState = ProtectedPersonPlaceListUiState(),
    val summaryUiState: ProtectedPersonSummaryUiState = ProtectedPersonSummaryUiState(),
    val selectedBottomSheetTab: ProtectedPersonBottomSheetTab =
        ProtectedPersonBottomSheetTab.PLACE,
    val bottomSheetValue: BaseBottomSheetValue = BaseBottomSheetValue.MIDDLE,
    val requestedSheetValue: BaseBottomSheetValue? = null,
    val selectedPlaceId: Long? = null,
    val focusedPlaceId: Long? = null
) {
    val markerPlaces: List<PlaceMarkerUiState>
        get() = placeListUiState.places
            .sortedBy(ProtectedPersonPlaceUiState::orderIndex)
            .mapIndexed { index, place ->
                PlaceMarkerUiState(
                    placeId = place.placeId,
                    placeName = place.placeName,
                    roadAddress = place.roadAddress,
                    latitude = place.latitude,
                    longitude = place.longitude,
                    orderIndex = index + 1
                )
            }
}

@Immutable
data class ProtectedPersonRouteMapUiState(
    val dateKey: String = "",
    val dateText: String = "",
    val totalDistanceText: String = "",
    val totalDistanceKm: Double = 0.0,
    val pathPointCount: Int = 0,
    val mapPolylinePoints: List<CoordinateUiState> = emptyList()
) {
    val hasLocationData: Boolean
        get() = mapPolylinePoints.isNotEmpty()
}
