package com.example.passedpath.feature.summary.data.remote.dto

data class StatisticMetricResponseDto(
    val metricType: String?,
    val period: String?,
    val startDate: String?,
    val endDate: String?,
    val average: StatisticMetricAverageDto?,
    val bars: List<StatisticMetricBarItemDto>?,
    val highlight: StatisticMetricHighlightDto?
)

data class StatisticMetricAverageDto(
    val value: Double?,
    val displayText: String?,
    val sampleSize: Int?
)

data class StatisticMetricBarItemDto(
    val label: String?,
    val startDate: String?,
    val endDate: String?,
    val value: Double?,
    val displayText: String?,
    val hasValue: Boolean?,
    val sampleSize: Int?
)

data class StatisticMetricHighlightDto(
    val title: String?,
    val message: String?,
    val current: HighlightMetricValueDto?,
    val previous: HighlightMetricValueDto?
)

data class HighlightMetricValueDto(
    val label: String?,
    val value: Double?,
    val displayText: String?,
    val sampleSize: Int?
)
