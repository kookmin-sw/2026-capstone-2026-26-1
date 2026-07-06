package com.example.passedpath.feature.summary.data.remote.mapper

import com.example.passedpath.feature.summary.data.remote.dto.WeeklyCountMetricAverageDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyCountMetricDailyItemDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyCountMetricSectionDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyDurationMetricAverageDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyDurationMetricDailyItemDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyDurationMetricSectionDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyStatisticsResponseDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyTimeMetricAverageDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyTimeMetricDailyItemDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyTimeMetricSectionDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyVisitedRegionsSectionDto
import com.example.passedpath.feature.summary.domain.model.WeeklyCountMetricAverage
import com.example.passedpath.feature.summary.domain.model.WeeklyCountMetricDailyItem
import com.example.passedpath.feature.summary.domain.model.WeeklyCountMetricSection
import com.example.passedpath.feature.summary.domain.model.WeeklyDurationMetricAverage
import com.example.passedpath.feature.summary.domain.model.WeeklyDurationMetricDailyItem
import com.example.passedpath.feature.summary.domain.model.WeeklyDurationMetricSection
import com.example.passedpath.feature.summary.domain.model.WeeklyStatistics
import com.example.passedpath.feature.summary.domain.model.WeeklyTimeMetricAverage
import com.example.passedpath.feature.summary.domain.model.WeeklyTimeMetricDailyItem
import com.example.passedpath.feature.summary.domain.model.WeeklyTimeMetricSection
import com.example.passedpath.feature.summary.domain.model.WeeklyVisitedRegionSummaryItem
import com.example.passedpath.feature.summary.domain.model.WeeklyVisitedRegionsSection

internal fun WeeklyStatisticsResponseDto.toWeeklyStatistics(): WeeklyStatistics {
    return WeeklyStatistics(
        startDate = startDate.normalizedOrEmpty(),
        endDate = endDate.normalizedOrEmpty(),
        outingTime = outingTime.toWeeklyTimeMetricSection(),
        enterHomeTime = enterHomeTime.toWeeklyTimeMetricSection(),
        totalOutingCount = totalOutingCount.toWeeklyCountMetricSection(),
        totalOutingSeconds = totalOutingSeconds.toWeeklyDurationMetricSection(),
        visitedRegions = visitedRegions.toWeeklyVisitedRegionsSection()
    )
}

private fun WeeklyTimeMetricSectionDto?.toWeeklyTimeMetricSection(): WeeklyTimeMetricSection {
    return WeeklyTimeMetricSection(
        average = this?.average.toWeeklyTimeMetricAverage(),
        sevenDays = this?.sevenDays.orEmpty().mapNotNull { item ->
            item.toWeeklyTimeMetricDailyItem()
        }
    )
}

private fun WeeklyTimeMetricAverageDto?.toWeeklyTimeMetricAverage(): WeeklyTimeMetricAverage {
    return WeeklyTimeMetricAverage(
        value = this?.value,
        displayText = this?.displayText.normalizedOrNull(),
        sampleSize = this?.sampleSize ?: 0
    )
}

private fun WeeklyTimeMetricDailyItemDto.toWeeklyTimeMetricDailyItem(): WeeklyTimeMetricDailyItem? {
    val dateKey = date.normalizedOrNull() ?: return null
    val routeExists = hasDayRoute == true
    return WeeklyTimeMetricDailyItem(
        date = dateKey,
        hasDayRoute = routeExists,
        value = value.takeIf { routeExists },
        displayText = displayText.normalizedOrNull().takeIf { routeExists }
    )
}

private fun WeeklyCountMetricSectionDto?.toWeeklyCountMetricSection(): WeeklyCountMetricSection {
    return WeeklyCountMetricSection(
        average = this?.average.toWeeklyCountMetricAverage(),
        sevenDays = this?.sevenDays.orEmpty().mapNotNull { item ->
            item.toWeeklyCountMetricDailyItem()
        }
    )
}

private fun WeeklyCountMetricAverageDto?.toWeeklyCountMetricAverage(): WeeklyCountMetricAverage {
    return WeeklyCountMetricAverage(
        value = this?.value,
        displayText = this?.displayText.normalizedOrNull(),
        sampleSize = this?.sampleSize ?: 0
    )
}

private fun WeeklyCountMetricDailyItemDto.toWeeklyCountMetricDailyItem(): WeeklyCountMetricDailyItem? {
    val dateKey = date.normalizedOrNull() ?: return null
    val routeExists = hasDayRoute == true
    return WeeklyCountMetricDailyItem(
        date = dateKey,
        hasDayRoute = routeExists,
        value = value.takeIf { routeExists },
        displayText = displayText.normalizedOrNull().takeIf { routeExists }
    )
}

private fun WeeklyDurationMetricSectionDto?.toWeeklyDurationMetricSection(): WeeklyDurationMetricSection {
    return WeeklyDurationMetricSection(
        average = this?.average.toWeeklyDurationMetricAverage(),
        sevenDays = this?.sevenDays.orEmpty().mapNotNull { item ->
            item.toWeeklyDurationMetricDailyItem()
        }
    )
}

private fun WeeklyDurationMetricAverageDto?.toWeeklyDurationMetricAverage(): WeeklyDurationMetricAverage {
    return WeeklyDurationMetricAverage(
        value = this?.value,
        displayText = this?.displayText.normalizedOrNull(),
        sampleSize = this?.sampleSize ?: 0
    )
}

private fun WeeklyDurationMetricDailyItemDto.toWeeklyDurationMetricDailyItem(): WeeklyDurationMetricDailyItem? {
    val dateKey = date.normalizedOrNull() ?: return null
    val routeExists = hasDayRoute == true
    return WeeklyDurationMetricDailyItem(
        date = dateKey,
        hasDayRoute = routeExists,
        value = value.takeIf { routeExists },
        displayText = displayText.normalizedOrNull().takeIf { routeExists }
    )
}

private fun WeeklyVisitedRegionsSectionDto?.toWeeklyVisitedRegionsSection(): WeeklyVisitedRegionsSection {
    return WeeklyVisitedRegionsSection(
        topRegions = this?.topRegions.orEmpty().mapIndexedNotNull { index, item ->
            val regionName = item.regionName.normalizedOrNull() ?: return@mapIndexedNotNull null
            WeeklyVisitedRegionSummaryItem(
                rank = item.rank ?: index + 1,
                regionName = regionName
            )
        }
    )
}

private fun String?.normalizedOrNull(): String? {
    return this?.trim()?.takeIf(String::isNotEmpty)
}

private fun String?.normalizedOrEmpty(): String {
    return normalizedOrNull().orEmpty()
}
