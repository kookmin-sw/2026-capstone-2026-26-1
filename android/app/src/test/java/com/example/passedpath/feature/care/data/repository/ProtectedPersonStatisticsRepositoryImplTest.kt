package com.example.passedpath.feature.care.data.repository

import com.example.passedpath.feature.care.data.remote.api.ProtectedPersonStatisticsApi
import com.example.passedpath.feature.summary.data.remote.dto.PlaceStatisticsSectionDto
import com.example.passedpath.feature.summary.data.remote.dto.StatisticMetricAverageDto
import com.example.passedpath.feature.summary.data.remote.dto.StatisticMetricHighlightDto
import com.example.passedpath.feature.summary.data.remote.dto.StatisticMetricResponseDto
import com.example.passedpath.feature.summary.data.remote.dto.VisitStatisticsResponseDto
import com.example.passedpath.feature.summary.data.remote.dto.VisitedRegionStatisticsSectionDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyCountMetricAverageDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyCountMetricSectionDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyDurationMetricAverageDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyDurationMetricSectionDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyStatisticsResponseDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyTimeMetricAverageDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyTimeMetricSectionDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyVisitedRegionsSectionDto
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProtectedPersonStatisticsRepositoryImplTest {

    @Test
    fun `getWeeklyStatistics passes dependent id and maps response`() = runTest {
        val fakeApi = FakeProtectedPersonStatisticsApi()
        val repository = ProtectedPersonStatisticsRepositoryImpl(
            protectedPersonStatisticsApi = fakeApi
        )

        val result = repository.getWeeklyStatistics(dependentUserId = DependentUserId)

        assertEquals("weekly", fakeApi.requestedEndpoint)
        assertEquals(DependentUserId, fakeApi.requestedDependentUserId)
        assertEquals("2026-05-14", result.startDate)
        assertEquals("2026-05-20", result.endDate)
    }

    @Test
    fun `getVisitStatistics passes dependent id and period query`() = runTest {
        val fakeApi = FakeProtectedPersonStatisticsApi()
        val repository = ProtectedPersonStatisticsRepositoryImpl(
            protectedPersonStatisticsApi = fakeApi
        )

        val result = repository.getVisitStatistics(
            dependentUserId = DependentUserId,
            period = StatisticsPeriod.SIX_MONTHS
        )

        assertEquals("visits", fakeApi.requestedEndpoint)
        assertEquals(DependentUserId, fakeApi.requestedDependentUserId)
        assertEquals("SIX_MONTHS", fakeApi.requestedPeriod)
        assertEquals(StatisticsPeriod.SIX_MONTHS, result.period)
    }

    @Test
    fun `metric APIs pass dependent id and period query`() = runTest {
        val fakeApi = FakeProtectedPersonStatisticsApi()
        val repository = ProtectedPersonStatisticsRepositoryImpl(
            protectedPersonStatisticsApi = fakeApi
        )

        repository.getOutingTime(DependentUserId, StatisticsPeriod.SIX_MONTHS)
        assertEquals("outing-time", fakeApi.requestedEndpoint)
        assertEquals(DependentUserId, fakeApi.requestedDependentUserId)
        assertEquals("SIX_MONTHS", fakeApi.requestedPeriod)

        repository.getEnterHomeTime(DependentUserId, StatisticsPeriod.MONTH)
        assertEquals("enter-home-time", fakeApi.requestedEndpoint)
        assertEquals("MONTH", fakeApi.requestedPeriod)

        repository.getTotalOutingSeconds(DependentUserId, StatisticsPeriod.YEAR)
        assertEquals("total-outing-seconds", fakeApi.requestedEndpoint)
        assertEquals("YEAR", fakeApi.requestedPeriod)

        repository.getTotalOutingCount(DependentUserId, StatisticsPeriod.WEEK)
        assertEquals("total-outing-count", fakeApi.requestedEndpoint)
        assertEquals("WEEK", fakeApi.requestedPeriod)
    }

    @Test(expected = RuntimeException::class)
    fun `api failures are thrown`() = runTest {
        val repository = ProtectedPersonStatisticsRepositoryImpl(
            protectedPersonStatisticsApi = FakeProtectedPersonStatisticsApi(
                throwable = RuntimeException("boom")
            )
        )

        repository.getWeeklyStatistics(dependentUserId = DependentUserId)
    }

    private class FakeProtectedPersonStatisticsApi(
        private val throwable: Throwable? = null
    ) : ProtectedPersonStatisticsApi {
        var requestedEndpoint: String? = null
        var requestedDependentUserId: Long? = null
        var requestedPeriod: String? = null

        override suspend fun getWeeklyStatistics(
            dependentUserId: Long
        ): WeeklyStatisticsResponseDto {
            requestedEndpoint = "weekly"
            requestedDependentUserId = dependentUserId
            throwable?.let { throw it }
            return weeklyResponse()
        }

        override suspend fun getVisitStatistics(
            dependentUserId: Long,
            period: String?
        ): VisitStatisticsResponseDto {
            requestedEndpoint = "visits"
            requestedDependentUserId = dependentUserId
            requestedPeriod = period
            throwable?.let { throw it }
            return VisitStatisticsResponseDto(
                period = period,
                startDate = "2026-05-14",
                endDate = "2026-05-20",
                visitedRegions = VisitedRegionStatisticsSectionDto(
                    totalVisitCount = 0,
                    items = emptyList()
                ),
                places = PlaceStatisticsSectionDto(
                    totalVisitCount = 0,
                    items = emptyList()
                )
            )
        }

        override suspend fun getOutingTime(
            dependentUserId: Long,
            period: String?
        ): StatisticMetricResponseDto {
            return metricResponse(
                endpoint = "outing-time",
                dependentUserId = dependentUserId,
                period = period,
                metricType = "OUTING_TIME"
            )
        }

        override suspend fun getEnterHomeTime(
            dependentUserId: Long,
            period: String?
        ): StatisticMetricResponseDto {
            return metricResponse(
                endpoint = "enter-home-time",
                dependentUserId = dependentUserId,
                period = period,
                metricType = "ENTER_HOME_TIME"
            )
        }

        override suspend fun getTotalOutingSeconds(
            dependentUserId: Long,
            period: String?
        ): StatisticMetricResponseDto {
            return metricResponse(
                endpoint = "total-outing-seconds",
                dependentUserId = dependentUserId,
                period = period,
                metricType = "TOTAL_OUTING_SECONDS"
            )
        }

        override suspend fun getTotalOutingCount(
            dependentUserId: Long,
            period: String?
        ): StatisticMetricResponseDto {
            return metricResponse(
                endpoint = "total-outing-count",
                dependentUserId = dependentUserId,
                period = period,
                metricType = "TOTAL_OUTING_COUNT"
            )
        }

        private fun metricResponse(
            endpoint: String,
            dependentUserId: Long,
            period: String?,
            metricType: String
        ): StatisticMetricResponseDto {
            requestedEndpoint = endpoint
            requestedDependentUserId = dependentUserId
            requestedPeriod = period
            throwable?.let { throw it }
            return StatisticMetricResponseDto(
                metricType = metricType,
                period = period,
                startDate = "2026-05-14",
                endDate = "2026-05-20",
                average = StatisticMetricAverageDto(
                    value = 1.0,
                    displayText = "1",
                    sampleSize = 1
                ),
                bars = emptyList(),
                highlight = StatisticMetricHighlightDto(
                    title = "Title",
                    message = "Message",
                    current = null,
                    previous = null
                )
            )
        }

        private fun weeklyResponse(): WeeklyStatisticsResponseDto {
            return WeeklyStatisticsResponseDto(
                startDate = "2026-05-14",
                endDate = "2026-05-20",
                outingTime = WeeklyTimeMetricSectionDto(
                    average = WeeklyTimeMetricAverageDto(
                        value = 552,
                        displayText = "09:12",
                        sampleSize = 1
                    ),
                    sevenDays = emptyList()
                ),
                enterHomeTime = WeeklyTimeMetricSectionDto(
                    average = WeeklyTimeMetricAverageDto(
                        value = 1_395,
                        displayText = "23:15",
                        sampleSize = 1
                    ),
                    sevenDays = emptyList()
                ),
                totalOutingCount = WeeklyCountMetricSectionDto(
                    average = WeeklyCountMetricAverageDto(
                        value = 1.0,
                        displayText = "1.0\uD68C",
                        sampleSize = 1
                    ),
                    sevenDays = emptyList()
                ),
                totalOutingSeconds = WeeklyDurationMetricSectionDto(
                    average = WeeklyDurationMetricAverageDto(
                        value = 3_600.0,
                        displayText = "1\uC2DC\uAC04",
                        sampleSize = 1
                    ),
                    sevenDays = emptyList()
                ),
                visitedRegions = WeeklyVisitedRegionsSectionDto(topRegions = emptyList())
            )
        }
    }

    private companion object {
        const val DependentUserId = 7L
    }
}
