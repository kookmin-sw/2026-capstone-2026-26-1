package com.example.passedpath.feature.summary.data.repository

import com.example.passedpath.feature.summary.data.remote.api.StatisticMetricApi
import com.example.passedpath.feature.summary.data.remote.mapper.toStatisticMetric
import com.example.passedpath.feature.summary.domain.model.StatisticMetric
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import com.example.passedpath.feature.summary.domain.repository.StatisticMetricRepository

class StatisticMetricRepositoryImpl(
    private val statisticMetricApi: StatisticMetricApi
) : StatisticMetricRepository {
    override suspend fun getOutingTime(period: StatisticsPeriod): StatisticMetric {
        return statisticMetricApi.getOutingTime(period = period.apiValue).toStatisticMetric()
    }

    override suspend fun getEnterHomeTime(period: StatisticsPeriod): StatisticMetric {
        return statisticMetricApi.getEnterHomeTime(period = period.apiValue).toStatisticMetric()
    }

    override suspend fun getTotalOutingSeconds(period: StatisticsPeriod): StatisticMetric {
        return statisticMetricApi.getTotalOutingSeconds(period = period.apiValue).toStatisticMetric()
    }

    override suspend fun getTotalOutingCount(period: StatisticsPeriod): StatisticMetric {
        return statisticMetricApi.getTotalOutingCount(period = period.apiValue).toStatisticMetric()
    }
}
