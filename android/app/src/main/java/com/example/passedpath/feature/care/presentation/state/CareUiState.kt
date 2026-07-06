package com.example.passedpath.feature.care.presentation.state

import androidx.compose.runtime.Immutable
import com.example.passedpath.feature.care.presentation.model.ProtectedPersonBottomSheetTab
import com.example.passedpath.ui.component.bottomsheet.BaseBottomSheetValue

@Immutable
data class CareUiState(
    val dependents: List<CareDependentUserUiState> = emptyList(),
    val mapMarkers: List<CareDependentMapMarkerUiState> = emptyList(),
    val selectedDependentUserId: Long? = null,
    val selectedDateKey: String = "",
    val route: ProtectedPersonRouteMapUiState = ProtectedPersonRouteMapUiState(),
    val isRouteLoading: Boolean = false,
    val hasRouteLoaded: Boolean = false,
    val isRouteEmpty: Boolean = false,
    val routeErrorMessage: String? = null,
    val placeListUiState: ProtectedPersonPlaceListUiState = ProtectedPersonPlaceListUiState(),
    val summaryUiState: ProtectedPersonSummaryUiState = ProtectedPersonSummaryUiState(),
    val selectedBottomSheetTab: ProtectedPersonBottomSheetTab = ProtectedPersonBottomSheetTab.PLACE,
    val bottomSheetValue: BaseBottomSheetValue = BaseBottomSheetValue.HIDDEN,
    val requestedSheetValue: BaseBottomSheetValue? = null,
    val selectedPlaceId: Long? = null,
    val focusedPlaceId: Long? = null,
    val inviteUiState: CareInviteUiState = CareInviteUiState(),
    val locationStreamErrorMessage: String? = null,
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val errorMessage: String? = null
) {
    val selectedDependent: CareDependentUserUiState?
        get() = selectedDependentUserId?.let { selectedId ->
            dependents.firstOrNull { dependent -> dependent.dependentUserId == selectedId }
        }

    val visitedPlaceMarkers: List<CareVisitedPlaceMarkerUiState>
        get() = placeListUiState.places.mapIndexed { index, place ->
            CareVisitedPlaceMarkerUiState(
                placeId = place.placeId,
                placeName = place.placeName,
                latitude = place.latitude,
                longitude = place.longitude,
                displayOrderIndex = index + 1
            )
        }
}

@Immutable
data class CareDependentUserUiState(
    val dependentUserId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val latestLatitude: Double?,
    val latestLongitude: Double?,
    val latestRecordedAt: String?
) {
    val hasLatestLocation: Boolean
        get() = latestLatitude != null && latestLongitude != null
}

@Immutable
data class CareDependentMapMarkerUiState(
    val dependentUserId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val latitude: Double,
    val longitude: Double,
    val latestRecordedAt: String?
)

@Immutable
data class CareVisitedPlaceMarkerUiState(
    val placeId: Long,
    val placeName: String,
    val latitude: Double,
    val longitude: Double,
    val displayOrderIndex: Int
)

@Immutable
data class CareInviteUiState(
    val isVisible: Boolean = false,
    val isLoading: Boolean = false,
    val inviteLink: String? = null,
    val errorMessage: String? = null,
    val copyFeedbackEventId: Long = 0L
)
