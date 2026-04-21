package com.example.passedpath.feature.locationtracking.data.remote.dto

data class GpsPointBatchUploadRequestDto(
    val distance: Double,
    val gpsPoints: List<GpsPointRequestDto>
)

data class GpsPointRequestDto(
    val recordedAt: String,
    val latitude: Double,
    val longitude: Double
)
