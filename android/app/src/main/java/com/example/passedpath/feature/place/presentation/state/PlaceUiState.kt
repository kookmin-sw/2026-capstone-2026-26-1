package com.example.passedpath.feature.place.presentation.state

import com.example.passedpath.feature.place.domain.model.VisitedPlace

data class PlaceUiState(
    val dateKey: String = "",
    val isSubmitting: Boolean = false,
    val placeList: PlaceListUiState = PlaceListUiState(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val feedbackEventId: Long = 0L
) {
    val isDateValid: Boolean
        get() = dateKey.isNotBlank()
}

data class PlaceListUiState(
    val dateKey: String = "",
    val places: List<VisitedPlace> = emptyList(),
    val placeCount: Int = 0,
    val hasLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isStale: Boolean = false,
    val isReorderGuideBannerVisible: Boolean = false
) {
    val hasRetainedContent: Boolean
        get() = hasLoaded && places.isNotEmpty()
}
