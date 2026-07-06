package com.example.passedpath.feature.summary.data.repository

import com.example.passedpath.feature.summary.data.remote.api.VisitStatisticsApi
import com.example.passedpath.feature.summary.data.remote.dto.PlaceStatisticsSectionDto
import com.example.passedpath.feature.summary.data.remote.dto.VisitStatisticsResponseDto
import com.example.passedpath.feature.summary.data.remote.dto.VisitedRegionStatisticsSectionDto
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VisitStatisticsRepositoryImplTest {

    @Test
    fun `getVisitStatistics passes period query and maps response`() = runTest {
        val fakeApi = FakeVisitStatisticsApi()
        val repository = VisitStatisticsRepositoryImpl(visitStatisticsApi = fakeApi)

        val result = repository.getVisitStatistics(period = StatisticsPeriod.SIX_MONTHS)

        assertEquals("SIX_MONTHS", fakeApi.requestedPeriod)
        assertEquals(StatisticsPeriod.SIX_MONTHS, result.period)
        assertEquals("2025-12-01", result.startDate)
        assertEquals("2026-05-20", result.endDate)
    }

    @Test(expected = RuntimeException::class)
    fun `getVisitStatistics throws api failures`() = runTest {
        val repository = VisitStatisticsRepositoryImpl(
            visitStatisticsApi = FakeVisitStatisticsApi(
                throwable = RuntimeException("boom")
            )
        )

        repository.getVisitStatistics(period = StatisticsPeriod.WEEK)
    }

    private class FakeVisitStatisticsApi(
        private val throwable: Throwable? = null
    ) : VisitStatisticsApi {
        var requestedPeriod: String? = null

        override suspend fun getVisitStatistics(period: String?): VisitStatisticsResponseDto {
            requestedPeriod = period
            throwable?.let { throw it }
            return VisitStatisticsResponseDto(
                period = period,
                startDate = "2025-12-01",
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
    }
}
