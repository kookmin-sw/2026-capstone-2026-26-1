package com.example.passedpath.feature.summary.data.remote.mapper

import com.example.passedpath.feature.summary.data.remote.dto.HighlightMetricValueDto
import com.example.passedpath.feature.summary.data.remote.dto.StatisticMetricAverageDto
import com.example.passedpath.feature.summary.data.remote.dto.StatisticMetricBarItemDto
import com.example.passedpath.feature.summary.data.remote.dto.StatisticMetricHighlightDto
import com.example.passedpath.feature.summary.data.remote.dto.StatisticMetricResponseDto
import com.example.passedpath.feature.summary.domain.model.HighlightMetricValue
import com.example.passedpath.feature.summary.domain.model.StatisticMetric
import com.example.passedpath.feature.summary.domain.model.StatisticMetricAverage
import com.example.passedpath.feature.summary.domain.model.StatisticMetricBarItem
import com.example.passedpath.feature.summary.domain.model.StatisticMetricHighlight
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod

internal fun StatisticMetricResponseDto.toStatisticMetric(): StatisticMetric {
    return StatisticMetric(
        metricType = metricType.normalizedOrEmpty(),
        period = StatisticsPeriod.fromApiValue(period),
        startDate = startDate.normalizedOrEmpty(),
        endDate = endDate.normalizedOrEmpty(),
        average = average.toStatisticMetricAverage(),
        bars = bars.orEmpty().map { item -> item.toStatisticMetricBarItem() },
        highlight = highlight.toStatisticMetricHighlight()
    )
}

private fun StatisticMetricAverageDto?.toStatisticMetricAverage(): StatisticMetricAverage {
    return StatisticMetricAverage(
        value = this?.value?.coerceAtLeast(0.0),
        displayText = this?.displayText.normalizedOrNull(),
        sampleSize = this?.sampleSize.nonNegativeOrZero()
    )
}

private fun StatisticMetricBarItemDto.toStatisticMetricBarItem(): StatisticMetricBarItem {
    val valueExists = hasValue == true

    return StatisticMetricBarItem(
        label = label.normalizedOrEmpty(),
        startDate = startDate.normalizedOrEmpty(),
        endDate = endDate.normalizedOrEmpty(),
        value = value?.coerceAtLeast(0.0).takeIf { valueExists },
        displayText = displayText.normalizedOrNull().takeIf { valueExists },
        hasValue = valueExists,
        sampleSize = sampleSize.nonNegativeOrZero()
    )
}

private fun StatisticMetricHighlightDto?.toStatisticMetricHighlight(): StatisticMetricHighlight {
    return StatisticMetricHighlight(
        title = this?.title.normalizedOrEmpty(),
        message = this?.message.normalizedOrEmpty(),
        current = this?.current.toHighlightMetricValue(),
        previous = this?.previous.toHighlightMetricValue()
    )
}

private fun HighlightMetricValueDto?.toHighlightMetricValue(): HighlightMetricValue {
    return HighlightMetricValue(
        label = this?.label.normalizedOrEmpty(),
        value = this?.value?.coerceAtLeast(0.0),
        displayText = this?.displayText.normalizedOrNull(),
        sampleSize = this?.sampleSize.nonNegativeOrZero()
    )
}

private fun Int?.nonNegativeOrZero(): Int {
    return this?.coerceAtLeast(0) ?: 0
}

private fun String?.normalizedOrNull(): String? {
    return this?.trim()?.takeIf(String::isNotEmpty)
}

private fun String?.normalizedOrEmpty(): String {
    return normalizedOrNull().orEmpty()
}
