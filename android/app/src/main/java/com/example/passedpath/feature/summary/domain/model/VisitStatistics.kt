package com.example.passedpath.feature.summary.domain.model

data class VisitStatistics(
    val period: StatisticsPeriod,
    val startDate: String,
    val endDate: String,
    val visitedRegions: VisitedRegionStatisticsSection,
    val places: PlaceStatisticsSection
)

data class VisitedRegionStatisticsSection(
    val totalVisitCount: Int,
    val items: List<VisitedRegionStatisticsItem>
)

data class VisitedRegionStatisticsItem(
    val rank: Int,
    val regionName: String,
    val visitCount: Int,
    val ratio: Double,
    val displayRatio: String
)

data class PlaceStatisticsSection(
    val totalVisitCount: Int,
    val items: List<PlaceStatisticsItem>
)

data class PlaceStatisticsItem(
    val rank: Int,
    val placeName: String,
    val roadAddress: String,
    val visitCount: Int,
    val displayVisitCount: String
)
