package com.example.passedpath.feature.care.data.remote.dto

data class CareDayRouteDetailResponseDto(
    val date: String?,
    val totalDistance: Double?,
    val gpsPoints: List<CareGpsPointItemDto>?
)

data class CareGpsPointItemDto(
    val recordedAt: String?,
    val latitude: Double?,
    val longitude: Double?
)

data class CareDayRouteErrorResponseDto(
    val code: String?,
    val message: String? = null
)
