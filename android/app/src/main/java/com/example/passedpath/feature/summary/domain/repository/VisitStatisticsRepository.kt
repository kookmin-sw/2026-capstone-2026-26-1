package com.example.passedpath.feature.summary.domain.repository

import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import com.example.passedpath.feature.summary.domain.model.VisitStatistics

interface VisitStatisticsRepository {
    suspend fun getVisitStatistics(period: StatisticsPeriod): VisitStatistics
}
