package com.example.passedpath.feature.care.presentation.mapper

import com.example.passedpath.feature.care.domain.model.CareDependentUser
import com.example.passedpath.feature.care.presentation.state.CareDependentMapMarkerUiState
import com.example.passedpath.feature.care.presentation.state.CareDependentUserUiState

fun CareDependentUser.toCareDependentUserUiState(): CareDependentUserUiState {
    return CareDependentUserUiState(
        dependentUserId = dependentUserId,
        nickname = nickname.ifBlank { "?" },
        profileImageUrl = profileImageUrl?.trim()?.takeIf(String::isNotEmpty),
        latestLatitude = latestGpsPoint?.latitude,
        latestLongitude = latestGpsPoint?.longitude,
        latestRecordedAt = latestGpsPoint?.recordedAt
    )
}

fun List<CareDependentUserUiState>.toCareDependentMapMarkerUiStates(): List<CareDependentMapMarkerUiState> {
    return mapNotNull(CareDependentUserUiState::toCareDependentMapMarkerUiState)
}

private fun CareDependentUserUiState.toCareDependentMapMarkerUiState(): CareDependentMapMarkerUiState? {
    val latitude = latestLatitude ?: return null
    val longitude = latestLongitude ?: return null

    return CareDependentMapMarkerUiState(
        dependentUserId = dependentUserId,
        nickname = nickname,
        profileImageUrl = profileImageUrl,
        latitude = latitude,
        longitude = longitude,
        latestRecordedAt = latestRecordedAt
    )
}
