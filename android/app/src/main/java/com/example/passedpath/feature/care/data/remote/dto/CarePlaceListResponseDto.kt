package com.example.passedpath.feature.care.data.remote.dto

data class CarePlaceListResponseDto(
    val placeCount: Int?,
    val places: List<CarePlaceItemDto>?
)

data class CarePlaceItemDto(
    val placeId: Long?,
    val placeName: String?,
    val source: String?,
    val type: String?,
    val roadAddress: String?,
    val latitude: Double?,
    val longitude: Double?,
    val orderIndex: Int?,
    val startTime: String?,
    val endTime: String?
)
