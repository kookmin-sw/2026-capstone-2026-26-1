package com.example.passedpath.feature.placebookmark.presentation.state

import com.example.passedpath.feature.placebookmark.domain.model.PlaceBookmarkSummary

data class PlaceBookmarkMapMarkerUiState(
    val bookmarkPlaces: List<PlaceBookmarkSummary> = emptyList(),
    val hasLoaded: Boolean = false,
    val isLoading: Boolean = false
)
