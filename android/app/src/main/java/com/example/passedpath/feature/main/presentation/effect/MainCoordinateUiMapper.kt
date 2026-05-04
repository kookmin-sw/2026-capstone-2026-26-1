package com.example.passedpath.feature.main.presentation.effect

import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.ui.state.CoordinateUiState

internal fun TrackedLocation.toCoordinateUiState(): CoordinateUiState {
    return CoordinateUiState(
        latitude = latitude,
        longitude = longitude
    )
}
