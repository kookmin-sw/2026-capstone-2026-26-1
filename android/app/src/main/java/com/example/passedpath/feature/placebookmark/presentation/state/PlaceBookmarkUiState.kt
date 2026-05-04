package com.example.passedpath.feature.placebookmark.presentation.state

import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmarkSummary

data class PlaceBookmarkUiState(
    val placeCount: Int = 0,
    val bookmarkPlaces: List<PlaceBookmarkSummary> = emptyList(),
    val hasLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val feedbackEventId: Long = 0L
)
