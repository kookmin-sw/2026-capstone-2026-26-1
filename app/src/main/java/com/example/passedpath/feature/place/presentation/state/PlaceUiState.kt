package com.example.passedpath.feature.place.presentation.state

data class PlaceUiState(
    val dateKey: String = "",
    val placeId: String = "",
    val placeName: String = "",
    val roadAddress: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val parsedPlaceId: Long?
        get() = placeId.toLongOrNull()

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
}
