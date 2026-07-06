package com.example.passedpath.feature.care.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
data class ProtectedPersonRouteHistoryUiState(
    val dependentNickname: String = "",
    val dayRoutes: List<ProtectedPersonRouteDateUiState> = emptyList(),
    val hasNext: Boolean = false,
    val nextCursorDate: String? = null,
    val hasLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null
)

@Immutable
data class ProtectedPersonRouteDateUiState(
    val dateKey: String,
    val dateText: String,
    val outingTimeText: String,
    val enterHomeTimeText: String,
    val outingCountText: String
)
