package com.example.passedpath.feature.summary.domain.model

data class StatisticMetric(
    val metricType: String,
    val period: StatisticsPeriod,
    val startDate: String,
    val endDate: String,
    val average: StatisticMetricAverage,
    val bars: List<StatisticMetricBarItem>,
    val highlight: StatisticMetricHighlight
)

data class StatisticMetricAverage(
    val value: Double?,
    val displayText: String?,
    val sampleSize: Int
)

data class StatisticMetricBarItem(
    val label: String,
    val startDate: String,
    val endDate: String,
    val value: Double?,
    val displayText: String?,
    val hasValue: Boolean,
    val sampleSize: Int
)

data class StatisticMetricHighlight(
    val title: String,
    val message: String,
    val current: HighlightMetricValue,
    val previous: HighlightMetricValue
)

data class HighlightMetricValue(
    val label: String,
    val value: Double?,
    val displayText: String?,
    val sampleSize: Int
)
