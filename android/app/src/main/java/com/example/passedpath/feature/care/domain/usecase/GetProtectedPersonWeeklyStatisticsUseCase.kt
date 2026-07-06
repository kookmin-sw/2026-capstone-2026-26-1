package com.example.passedpath.feature.care.domain.usecase

import com.example.passedpath.feature.care.domain.repository.ProtectedPersonStatisticsRepository
import com.example.passedpath.feature.summary.domain.model.WeeklyStatistics

class GetProtectedPersonWeeklyStatisticsUseCase(
    private val repository: ProtectedPersonStatisticsRepository
) {
    suspend operator fun invoke(dependentUserId: Long): WeeklyStatistics {
        return repository.getWeeklyStatistics(dependentUserId = dependentUserId)
    }
}
