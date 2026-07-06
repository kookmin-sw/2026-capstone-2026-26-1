package com.example.passedpath.feature.summary.data.remote.mapper

import com.example.passedpath.feature.summary.data.remote.dto.PlaceStatisticsItemDto
import com.example.passedpath.feature.summary.data.remote.dto.PlaceStatisticsSectionDto
import com.example.passedpath.feature.summary.data.remote.dto.VisitStatisticsResponseDto
import com.example.passedpath.feature.summary.data.remote.dto.VisitedRegionStatisticsItemDto
import com.example.passedpath.feature.summary.data.remote.dto.VisitedRegionStatisticsSectionDto
import com.example.passedpath.feature.summary.domain.model.PlaceStatisticsItem
import com.example.passedpath.feature.summary.domain.model.PlaceStatisticsSection
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import com.example.passedpath.feature.summary.domain.model.VisitStatistics
import com.example.passedpath.feature.summary.domain.model.VisitedRegionStatisticsItem
import com.example.passedpath.feature.summary.domain.model.VisitedRegionStatisticsSection
import kotlin.math.roundToInt

internal fun VisitStatisticsResponseDto.toVisitStatistics(): VisitStatistics {
    return VisitStatistics(
        period = StatisticsPeriod.fromApiValue(period),
        startDate = startDate.normalizedOrEmpty(),
        endDate = endDate.normalizedOrEmpty(),
        visitedRegions = visitedRegions.toVisitedRegionStatisticsSection(),
        places = places.toPlaceStatisticsSection()
    )
}

private fun VisitedRegionStatisticsSectionDto?.toVisitedRegionStatisticsSection(): VisitedRegionStatisticsSection {
    return VisitedRegionStatisticsSection(
        totalVisitCount = this?.totalVisitCount.nonNegativeOrZero(),
        items = this?.items.orEmpty().mapIndexedNotNull { index, item ->
            item.toVisitedRegionStatisticsItem(defaultRank = index + 1)
        }
    )
}

private fun VisitedRegionStatisticsItemDto.toVisitedRegionStatisticsItem(
    defaultRank: Int
): VisitedRegionStatisticsItem? {
    val name = regionName.normalizedOrNull() ?: return null
    val safeRatio = ratio?.coerceAtLeast(0.0) ?: 0.0

    return VisitedRegionStatisticsItem(
        rank = rank ?: defaultRank,
        regionName = name,
        visitCount = visitCount.nonNegativeOrZero(),
        ratio = safeRatio,
        displayRatio = displayRatio.normalizedOrNull() ?: "${safeRatio.roundToInt()}%"
    )
}

private fun PlaceStatisticsSectionDto?.toPlaceStatisticsSection(): PlaceStatisticsSection {
    return PlaceStatisticsSection(
        totalVisitCount = this?.totalVisitCount.nonNegativeOrZero(),
        items = this?.items.orEmpty().mapIndexedNotNull { index, item ->
            item.toPlaceStatisticsItem(defaultRank = index + 1)
        }
    )
}

private fun PlaceStatisticsItemDto.toPlaceStatisticsItem(
    defaultRank: Int
): PlaceStatisticsItem? {
    val name = placeName.normalizedOrNull() ?: return null
    val safeVisitCount = visitCount.nonNegativeOrZero()

    return PlaceStatisticsItem(
        rank = rank ?: defaultRank,
        placeName = name,
        roadAddress = roadAddress.normalizedOrEmpty(),
        visitCount = safeVisitCount,
        displayVisitCount = displayVisitCount.normalizedOrNull() ?: "${safeVisitCount}회"
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
