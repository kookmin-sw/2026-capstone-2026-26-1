package com.example.passedpath.feature.summary.domain.usecase

import com.example.passedpath.feature.summary.domain.model.WeeklyStatistics
import com.example.passedpath.feature.summary.domain.repository.WeeklyStatisticsRepository

class GetWeeklyStatisticsUseCase(
    private val weeklyStatisticsRepository: WeeklyStatisticsRepository
) {
    suspend operator fun invoke(): WeeklyStatistics {
        return weeklyStatisticsRepository.getWeeklyStatistics()
    }
}
