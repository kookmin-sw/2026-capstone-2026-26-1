package com.example.passedpath.feature.summary.data.remote.dto

data class WeeklyStatisticsResponseDto(
    val startDate: String?,
    val endDate: String?,
    val outingTime: WeeklyTimeMetricSectionDto?,
    val enterHomeTime: WeeklyTimeMetricSectionDto?,
    val totalOutingCount: WeeklyCountMetricSectionDto?,
    val totalOutingSeconds: WeeklyDurationMetricSectionDto?,
    val visitedRegions: WeeklyVisitedRegionsSectionDto?
)

data class WeeklyTimeMetricSectionDto(
    val average: WeeklyTimeMetricAverageDto?,
    val sevenDays: List<WeeklyTimeMetricDailyItemDto>?
)

data class WeeklyTimeMetricAverageDto(
    val value: Int?,
    val displayText: String?,
    val sampleSize: Int?
)

data class WeeklyTimeMetricDailyItemDto(
    val date: String?,
    val hasDayRoute: Boolean?,
    val value: Int?,
    val displayText: String?
)

data class WeeklyCountMetricSectionDto(
    val average: WeeklyCountMetricAverageDto?,
    val sevenDays: List<WeeklyCountMetricDailyItemDto>?
)

data class WeeklyCountMetricAverageDto(
    val value: Double?,
    val displayText: String?,
    val sampleSize: Int?
)

data class WeeklyCountMetricDailyItemDto(
    val date: String?,
    val hasDayRoute: Boolean?,
    val value: Double?,
    val displayText: String?
)

data class WeeklyDurationMetricSectionDto(
    val average: WeeklyDurationMetricAverageDto?,
    val sevenDays: List<WeeklyDurationMetricDailyItemDto>?
)

data class WeeklyDurationMetricAverageDto(
    val value: Double?,
    val displayText: String?,
    val sampleSize: Int?
)

data class WeeklyDurationMetricDailyItemDto(
    val date: String?,
    val hasDayRoute: Boolean?,
    val value: Long?,
    val displayText: String?
)

data class WeeklyVisitedRegionsSectionDto(
    val topRegions: List<WeeklyVisitedRegionSummaryItemDto>?
)

data class WeeklyVisitedRegionSummaryItemDto(
    val rank: Int?,
    val regionName: String?
)
