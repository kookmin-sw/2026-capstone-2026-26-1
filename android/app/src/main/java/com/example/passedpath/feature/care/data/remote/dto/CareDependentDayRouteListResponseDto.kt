package com.example.passedpath.feature.care.data.remote.dto

data class CareDependentDayRouteListResponseDto(
    val dayRouteCount: Int?,
    val hasNext: Boolean?,
    val nextCursorDate: String?,
    val dayRoutes: List<CareDependentDayRouteItemDto>?
)

data class CareDependentDayRouteItemDto(
    val date: String?,
    val outingTime: String?,
    val enterHomeTime: String?,
    val totalOutingCount: Int?
)
