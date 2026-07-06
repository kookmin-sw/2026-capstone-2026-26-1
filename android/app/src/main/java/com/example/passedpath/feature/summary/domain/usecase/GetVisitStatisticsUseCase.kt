package com.example.passedpath.feature.summary.domain.usecase

import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import com.example.passedpath.feature.summary.domain.model.VisitStatistics
import com.example.passedpath.feature.summary.domain.repository.VisitStatisticsRepository

class GetVisitStatisticsUseCase(
    private val visitStatisticsRepository: VisitStatisticsRepository
) {
    suspend operator fun invoke(period: StatisticsPeriod): VisitStatistics {
        return visitStatisticsRepository.getVisitStatistics(period = period)
    }
}
