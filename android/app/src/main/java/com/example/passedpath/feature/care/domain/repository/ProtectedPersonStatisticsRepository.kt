package com.example.passedpath.feature.care.domain.repository

import com.example.passedpath.feature.summary.domain.model.StatisticMetric
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import com.example.passedpath.feature.summary.domain.model.VisitStatistics
import com.example.passedpath.feature.summary.domain.model.WeeklyStatistics

interface ProtectedPersonStatisticsRepository {
    suspend fun getWeeklyStatistics(dependentUserId: Long): WeeklyStatistics

    suspend fun getVisitStatistics(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): VisitStatistics

    suspend fun getOutingTime(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): StatisticMetric

    suspend fun getEnterHomeTime(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): StatisticMetric

    suspend fun getTotalOutingSeconds(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): StatisticMetric

    suspend fun getTotalOutingCount(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): StatisticMetric
}
