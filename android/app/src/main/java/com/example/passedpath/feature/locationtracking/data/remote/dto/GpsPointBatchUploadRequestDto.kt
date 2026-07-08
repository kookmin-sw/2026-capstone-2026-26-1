package com.example.passedpath.feature.locationtracking.data.remote.dto

data class GpsPointBatchUploadRequestDto(
    val distance: Double,
    val gpsPoints: List<GpsPointRequestDto>,
    val uploadTrigger: String
)

data class GpsPointRequestDto(
    val recordedAt: String,
    val latitude: Double,
    val longitude: Double
)
