package com.example.passedpath.feature.care.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
data class ProtectedPersonPlaceListUiState(
    val places: List<ProtectedPersonPlaceUiState> = emptyList(),
    val placeCount: Int = places.size,
    val hasLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPlaceGuideBannerVisible: Boolean = false
)

@Immutable
data class ProtectedPersonPlaceUiState(
    val placeId: Long,
    val placeName: String,
    val roadAddress: String,
    val latitude: Double,
    val longitude: Double,
    val orderIndex: Int,
    val startTimeText: String? = null,
    val endTimeText: String? = null,
    val isFavoritePlace: Boolean = false
)

@Immutable
data class ProtectedPersonSummaryUiState(
    val hasLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val summary: ProtectedPersonSummaryContentUiState = ProtectedPersonSummaryContentUiState()
)

@Immutable
data class ProtectedPersonSummaryContentUiState(
    val outingTimeText: String = EmptyValue,
    val enterHomeTimeText: String = EmptyValue,
    val totalOutingDurationText: String = EmptyValue,
    val totalOutingCountText: String = EmptyValue,
    val visitedDongNames: List<String> = emptyList()
) {
    private companion object {
        const val EmptyValue = "-"
    }
}
