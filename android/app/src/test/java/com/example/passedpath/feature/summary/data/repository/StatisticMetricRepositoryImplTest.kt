package com.example.passedpath.feature.summary.data.repository

import com.example.passedpath.feature.summary.data.remote.api.StatisticMetricApi
import com.example.passedpath.feature.summary.data.remote.dto.StatisticMetricAverageDto
import com.example.passedpath.feature.summary.data.remote.dto.StatisticMetricHighlightDto
import com.example.passedpath.feature.summary.data.remote.dto.StatisticMetricResponseDto
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticMetricRepositoryImplTest {

    @Test
    fun `getOutingTime passes period query and maps response`() = runTest {
        val fakeApi = FakeStatisticMetricApi()
        val repository = StatisticMetricRepositoryImpl(statisticMetricApi = fakeApi)

        val result = repository.getOutingTime(period = StatisticsPeriod.SIX_MONTHS)

        assertEquals("outing-time", fakeApi.requestedEndpoint)
        assertEquals("SIX_MONTHS", fakeApi.requestedPeriod)
        assertEquals(StatisticsPeriod.SIX_MONTHS, result.period)
        assertEquals("2025-12-01", result.startDate)
        assertEquals("2026-05-20", result.endDate)
    }

    @Test
    fun `getEnterHomeTime passes period query and maps response`() = runTest {
        val fakeApi = FakeStatisticMetricApi()
        val repository = StatisticMetricRepositoryImpl(statisticMetricApi = fakeApi)

        val result = repository.getEnterHomeTime(period = StatisticsPeriod.SIX_MONTHS)

        assertEquals("enter-home-time", fakeApi.requestedEndpoint)
        assertEquals("SIX_MONTHS", fakeApi.requestedPeriod)
        assertEquals(StatisticsPeriod.SIX_MONTHS, result.period)
        assertEquals("2025-12-01", result.startDate)
        assertEquals("2026-05-20", result.endDate)
    }

    @Test
    fun `getTotalOutingSeconds passes period query and maps response`() = runTest {
        val fakeApi = FakeStatisticMetricApi()
        val repository = StatisticMetricRepositoryImpl(statisticMetricApi = fakeApi)

        val result = repository.getTotalOutingSeconds(period = StatisticsPeriod.SIX_MONTHS)

        assertEquals("SIX_MONTHS", fakeApi.requestedPeriod)
        assertEquals(StatisticsPeriod.SIX_MONTHS, result.period)
        assertEquals("2025-12-01", result.startDate)
        assertEquals("2026-05-20", result.endDate)
    }

    @Test
    fun `getTotalOutingCount passes period query and maps response`() = runTest {
        val fakeApi = FakeStatisticMetricApi()
        val repository = StatisticMetricRepositoryImpl(statisticMetricApi = fakeApi)

        val result = repository.getTotalOutingCount(period = StatisticsPeriod.SIX_MONTHS)

        assertEquals("total-outing-count", fakeApi.requestedEndpoint)
        assertEquals("SIX_MONTHS", fakeApi.requestedPeriod)
        assertEquals(StatisticsPeriod.SIX_MONTHS, result.period)
        assertEquals("2025-12-01", result.startDate)
        assertEquals("2026-05-20", result.endDate)
    }

    @Test(expected = RuntimeException::class)
    fun `getOutingTime throws api failures`() = runTest {
        val repository = StatisticMetricRepositoryImpl(
            statisticMetricApi = FakeStatisticMetricApi(
                throwable = RuntimeException("boom")
            )
        )

        repository.getOutingTime(period = StatisticsPeriod.WEEK)
    }

    @Test(expected = RuntimeException::class)
    fun `getEnterHomeTime throws api failures`() = runTest {
        val repository = StatisticMetricRepositoryImpl(
            statisticMetricApi = FakeStatisticMetricApi(
                throwable = RuntimeException("boom")
            )
        )

        repository.getEnterHomeTime(period = StatisticsPeriod.WEEK)
    }

    @Test(expected = RuntimeException::class)
    fun `getTotalOutingSeconds throws api failures`() = runTest {
        val repository = StatisticMetricRepositoryImpl(
            statisticMetricApi = FakeStatisticMetricApi(
                throwable = RuntimeException("boom")
            )
        )

        repository.getTotalOutingSeconds(period = StatisticsPeriod.WEEK)
    }

    @Test(expected = RuntimeException::class)
    fun `getTotalOutingCount throws api failures`() = runTest {
        val repository = StatisticMetricRepositoryImpl(
            statisticMetricApi = FakeStatisticMetricApi(
                throwable = RuntimeException("boom")
            )
        )

        repository.getTotalOutingCount(period = StatisticsPeriod.WEEK)
    }

    private class FakeStatisticMetricApi(
        private val throwable: Throwable? = null
    ) : StatisticMetricApi {
        var requestedEndpoint: String? = null
        var requestedPeriod: String? = null

        override suspend fun getOutingTime(period: String?): StatisticMetricResponseDto {
            requestedEndpoint = "outing-time"
            return response(period = period, metricType = "OUTING_TIME")
        }

        override suspend fun getEnterHomeTime(period: String?): StatisticMetricResponseDto {
            requestedEndpoint = "enter-home-time"
            return response(period = period, metricType = "ENTER_HOME_TIME")
        }

        override suspend fun getTotalOutingSeconds(period: String?): StatisticMetricResponseDto {
            requestedEndpoint = "total-outing-seconds"
            return response(period = period, metricType = "TOTAL_OUTING_SECONDS")
        }

        override suspend fun getTotalOutingCount(period: String?): StatisticMetricResponseDto {
            requestedEndpoint = "total-outing-count"
            return response(period = period, metricType = "TOTAL_OUTING_COUNT")
        }

        private fun response(
            period: String?,
            metricType: String
        ): StatisticMetricResponseDto {
            requestedPeriod = period
            throwable?.let { throw it }
            return StatisticMetricResponseDto(
                metricType = metricType,
                period = period,
                startDate = "2025-12-01",
                endDate = "2026-05-20",
                average = StatisticMetricAverageDto(
                    value = 0.0,
                    displayText = "0m",
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
    }
}
