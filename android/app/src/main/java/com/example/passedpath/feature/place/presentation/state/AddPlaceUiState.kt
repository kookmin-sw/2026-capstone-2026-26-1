package com.example.passedpath.feature.place.presentation.state

import com.example.passedpath.feature.place.domain.model.PlaceSearchResult

data class AddPlaceUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val places: List<PlaceSearchResult> = emptyList(),
    val selectedPlaceId: String? = null,
    val errorMessage: String? = null
) {
    val selectedPlace: PlaceSearchResult?
        get() = places.firstOrNull { it.stableKey == selectedPlaceId }

    val canConfirmPlace: Boolean
        get() = selectedPlace != null && !isSubmitting

    val shouldShowResults: Boolean
        get() = query.isNotBlank() && places.isNotEmpty()
}
