package com.example.passedpath.feature.summary.data.repository

import com.example.passedpath.feature.summary.data.remote.api.WeeklyStatisticsApi
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyCountMetricAverageDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyCountMetricSectionDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyDurationMetricAverageDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyDurationMetricSectionDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyStatisticsResponseDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyTimeMetricAverageDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyTimeMetricSectionDto
import com.example.passedpath.feature.summary.data.remote.dto.WeeklyVisitedRegionsSectionDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyStatisticsRepositoryImplTest {

    @Test
    fun `getWeeklyStatistics fetches weekly statistics and maps response`() = runTest {
        val fakeApi = FakeWeeklyStatisticsApi()
        val repository = WeeklyStatisticsRepositoryImpl(weeklyStatisticsApi = fakeApi)

        val result = repository.getWeeklyStatistics()

        assertEquals(1, fakeApi.requestCount)
        assertEquals("2026-05-07", result.startDate)
        assertEquals("2026-05-13", result.endDate)
        assertEquals(5, result.outingTime.average.sampleSize)
        assertEquals(1.4, result.totalOutingCount.average.value ?: -1.0, 0.0)
    }

    @Test(expected = RuntimeException::class)
    fun `getWeeklyStatistics throws api failures`() = runTest {
        val repository = WeeklyStatisticsRepositoryImpl(
            weeklyStatisticsApi = FakeWeeklyStatisticsApi(
                throwable = RuntimeException("boom")
            )
        )

        repository.getWeeklyStatistics()
    }

    private class FakeWeeklyStatisticsApi(
        private val throwable: Throwable? = null
    ) : WeeklyStatisticsApi {
        var requestCount: Int = 0

        override suspend fun getWeeklyStatistics(): WeeklyStatisticsResponseDto {
            requestCount += 1
            throwable?.let { throw it }
            return WeeklyStatisticsResponseDto(
                startDate = "2026-05-07",
                endDate = "2026-05-13",
                outingTime = WeeklyTimeMetricSectionDto(
                    average = WeeklyTimeMetricAverageDto(
                        value = 552,
                        displayText = "09:12",
                        sampleSize = 5
                    ),
                    sevenDays = emptyList()
                ),
                enterHomeTime = WeeklyTimeMetricSectionDto(
                    average = WeeklyTimeMetricAverageDto(
                        value = 1_120,
                        displayText = "18:40",
                        sampleSize = 5
                    ),
                    sevenDays = emptyList()
                ),
                totalOutingCount = WeeklyCountMetricSectionDto(
                    average = WeeklyCountMetricAverageDto(
                        value = 1.4,
                        displayText = "1.4\uD68C",
                        sampleSize = 5
                    ),
                    sevenDays = emptyList()
                ),
                totalOutingSeconds = WeeklyDurationMetricSectionDto(
                    average = WeeklyDurationMetricAverageDto(
                        value = 19_800.5,
                        displayText = "5\uC2DC\uAC04 30\uBD84",
                        sampleSize = 5
                    ),
                    sevenDays = emptyList()
                ),
                visitedRegions = WeeklyVisitedRegionsSectionDto(topRegions = emptyList())
            )
        }
    }
}
