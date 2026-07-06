package com.example.passedpath.feature.summary.domain.usecase

import com.example.passedpath.feature.summary.domain.model.StatisticMetric
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import com.example.passedpath.feature.summary.domain.repository.StatisticMetricRepository

class GetOutingTimeStatisticsUseCase(
    private val statisticMetricRepository: StatisticMetricRepository
) {
    suspend operator fun invoke(period: StatisticsPeriod): StatisticMetric {
        return statisticMetricRepository.getOutingTime(period = period)
    }
}
