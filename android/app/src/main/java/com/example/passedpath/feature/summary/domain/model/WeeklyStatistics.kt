package com.example.passedpath.feature.summary.domain.model

data class WeeklyStatistics(
    val startDate: String,
    val endDate: String,
    val outingTime: WeeklyTimeMetricSection,
    val enterHomeTime: WeeklyTimeMetricSection,
    val totalOutingCount: WeeklyCountMetricSection,
    val totalOutingSeconds: WeeklyDurationMetricSection,
    val visitedRegions: WeeklyVisitedRegionsSection
)

data class WeeklyTimeMetricSection(
    val average: WeeklyTimeMetricAverage,
    val sevenDays: List<WeeklyTimeMetricDailyItem>
)

data class WeeklyTimeMetricAverage(
    val value: Int?,
    val displayText: String?,
    val sampleSize: Int
)

data class WeeklyTimeMetricDailyItem(
    val date: String,
    val hasDayRoute: Boolean,
    val value: Int?,
    val displayText: String?
)

data class WeeklyCountMetricSection(
    val average: WeeklyCountMetricAverage,
    val sevenDays: List<WeeklyCountMetricDailyItem>
)

data class WeeklyCountMetricAverage(
    val value: Double?,
    val displayText: String?,
    val sampleSize: Int
)

data class WeeklyCountMetricDailyItem(
    val date: String,
    val hasDayRoute: Boolean,
    val value: Double?,
    val displayText: String?
)

data class WeeklyDurationMetricSection(
    val average: WeeklyDurationMetricAverage,
    val sevenDays: List<WeeklyDurationMetricDailyItem>
)

data class WeeklyDurationMetricAverage(
    val value: Double?,
    val displayText: String?,
    val sampleSize: Int
)

data class WeeklyDurationMetricDailyItem(
    val date: String,
    val hasDayRoute: Boolean,
    val value: Long?,
    val displayText: String?
)

data class WeeklyVisitedRegionsSection(
    val topRegions: List<WeeklyVisitedRegionSummaryItem>
)

data class WeeklyVisitedRegionSummaryItem(
    val rank: Int,
    val regionName: String
)
