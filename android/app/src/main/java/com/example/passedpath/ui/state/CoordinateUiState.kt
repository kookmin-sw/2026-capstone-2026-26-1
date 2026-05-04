package com.example.passedpath.ui.state

data class CoordinateUiState(
    val latitude: Double,
    val longitude: Double,
    val recordedAtEpochMillis: Long? = null
)
