package com.example.passedpath.feature.summary.data.remote.dto

data class VisitStatisticsResponseDto(
    val period: String?,
    val startDate: String?,
    val endDate: String?,
    val visitedRegions: VisitedRegionStatisticsSectionDto?,
    val places: PlaceStatisticsSectionDto?
)

data class VisitedRegionStatisticsSectionDto(
    val totalVisitCount: Int?,
    val items: List<VisitedRegionStatisticsItemDto>?
)

data class VisitedRegionStatisticsItemDto(
    val rank: Int?,
    val regionName: String?,
    val visitCount: Int?,
    val ratio: Double?,
    val displayRatio: String?
)

data class PlaceStatisticsSectionDto(
    val totalVisitCount: Int?,
    val items: List<PlaceStatisticsItemDto>?
)

data class PlaceStatisticsItemDto(
    val rank: Int?,
    val placeName: String?,
    val roadAddress: String?,
    val visitCount: Int?,
    val displayVisitCount: String?
)
