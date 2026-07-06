package com.example.passedpath.feature.care.domain.usecase

import com.example.passedpath.feature.care.domain.repository.ProtectedPersonStatisticsRepository
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import com.example.passedpath.feature.summary.domain.model.VisitStatistics

class GetProtectedPersonVisitStatisticsUseCase(
    private val repository: ProtectedPersonStatisticsRepository
) {
    suspend operator fun invoke(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): VisitStatistics {
        return repository.getVisitStatistics(
            dependentUserId = dependentUserId,
            period = period
        )
    }
}
