package com.example.passedpath.feature.care.domain.usecase

import com.example.passedpath.feature.care.domain.repository.ProtectedPersonStatisticsRepository
import com.example.passedpath.feature.summary.domain.model.StatisticMetric
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod

class GetProtectedPersonStatisticMetricUseCase(
    private val repository: ProtectedPersonStatisticsRepository
) {
    suspend fun getOutingTime(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): StatisticMetric {
        return repository.getOutingTime(
            dependentUserId = dependentUserId,
            period = period
        )
    }

    suspend fun getEnterHomeTime(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): StatisticMetric {
        return repository.getEnterHomeTime(
            dependentUserId = dependentUserId,
            period = period
        )
    }

    suspend fun getTotalOutingSeconds(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): StatisticMetric {
        return repository.getTotalOutingSeconds(
            dependentUserId = dependentUserId,
            period = period
        )
    }

    suspend fun getTotalOutingCount(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): StatisticMetric {
        return repository.getTotalOutingCount(
            dependentUserId = dependentUserId,
            period = period
        )
    }
}
