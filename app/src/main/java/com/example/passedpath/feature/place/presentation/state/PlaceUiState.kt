package com.example.passedpath.feature.place.presentation.state

data class PlaceUiState(
    val dateKey: String = "",
    val placeName: String = "",
    val roadAddress: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val isSubmitEnabled: Boolean
        get() = !isSubmitting &&
            dateKey.isNotBlank() &&
            placeName.isNotBlank() &&
            roadAddress.isNotBlank() &&
            latitude.toDoubleOrNull() != null &&
            longitude.toDoubleOrNull() != null
}
