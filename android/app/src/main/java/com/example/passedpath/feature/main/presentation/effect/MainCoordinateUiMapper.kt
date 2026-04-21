package com.example.passedpath.feature.main.presentation.effect

import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.main.presentation.state.MainCoordinateUiState

internal fun TrackedLocation.toMainCoordinateUiState(): MainCoordinateUiState {
    return MainCoordinateUiState(
        latitude = latitude,
        longitude = longitude
    )
}
