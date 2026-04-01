package com.example.passedpath.feature.daynote.presentation.state

data class DayNoteUiState(
    val dateKey: String = "",
    val title: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val isSubmitEnabled: Boolean
        get() = !isSubmitting && dateKey.isNotBlank()
}
