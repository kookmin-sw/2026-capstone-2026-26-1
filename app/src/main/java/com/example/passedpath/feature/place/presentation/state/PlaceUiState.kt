package com.example.passedpath.feature.place.presentation.state

import com.example.passedpath.feature.place.domain.model.VisitedPlace

data class PlaceUiState(
    val dateKey: String = "",
    val placeId: String = "",
    val reorderPlaceIdsInput: String = "",
    val placeName: String = "",
    val roadAddress: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val isSubmitting: Boolean = false,
    val placeList: PlaceListUiState = PlaceListUiState(),
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val parsedPlaceId: Long?
        get() = placeId.toLongOrNull()

    val parsedReorderPlaceIds: List<Long>
        get() = reorderPlaceIdsInput
            .split(',')
            .map(String::trim)
            .filter(String::isNotBlank)
            .mapNotNull(String::toLongOrNull)

    val isDateValid: Boolean
        get() = dateKey.isNotBlank()

    val hasCompleteForm: Boolean
        get() = placeName.isNotBlank() &&
            roadAddress.isNotBlank() &&
            latitude.toDoubleOrNull() != null &&
            longitude.toDoubleOrNull() != null

    val isCreateMode: Boolean
        get() = parsedPlaceId == null

    val isSubmitEnabled: Boolean
        get() = !isSubmitting && isDateValid && hasCompleteForm

    val isDeleteEnabled: Boolean
        get() = !isSubmitting && isDateValid && parsedPlaceId != null

    val isReorderEnabled: Boolean
        get() = !isSubmitting && isDateValid && parsedReorderPlaceIds.isNotEmpty()
}

data class PlaceListUiState(
    val dateKey: String = "",
    val places: List<VisitedPlace> = emptyList(),
    val placeCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
