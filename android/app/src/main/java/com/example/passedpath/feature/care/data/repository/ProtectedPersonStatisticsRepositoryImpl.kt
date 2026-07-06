package com.example.passedpath.feature.care.data.repository

import com.example.passedpath.feature.care.data.remote.api.ProtectedPersonStatisticsApi
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonStatisticsRepository
import com.example.passedpath.feature.summary.data.remote.mapper.toStatisticMetric
import com.example.passedpath.feature.summary.data.remote.mapper.toVisitStatistics
import com.example.passedpath.feature.summary.data.remote.mapper.toWeeklyStatistics
import com.example.passedpath.feature.summary.domain.model.StatisticMetric
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import com.example.passedpath.feature.summary.domain.model.VisitStatistics
import com.example.passedpath.feature.summary.domain.model.WeeklyStatistics

class ProtectedPersonStatisticsRepositoryImpl(
    private val protectedPersonStatisticsApi: ProtectedPersonStatisticsApi
) : ProtectedPersonStatisticsRepository {
    override suspend fun getWeeklyStatistics(dependentUserId: Long): WeeklyStatistics {
        return protectedPersonStatisticsApi.getWeeklyStatistics(
            dependentUserId = dependentUserId
        ).toWeeklyStatistics()
    }

    override suspend fun getVisitStatistics(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): VisitStatistics {
        return protectedPersonStatisticsApi.getVisitStatistics(
            dependentUserId = dependentUserId,
            period = period.apiValue
        ).toVisitStatistics()
    }

    override suspend fun getOutingTime(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): StatisticMetric {
        return protectedPersonStatisticsApi.getOutingTime(
            dependentUserId = dependentUserId,
            period = period.apiValue
        ).toStatisticMetric()
    }

    override suspend fun getEnterHomeTime(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): StatisticMetric {
        return protectedPersonStatisticsApi.getEnterHomeTime(
            dependentUserId = dependentUserId,
            period = period.apiValue
        ).toStatisticMetric()
    }

    override suspend fun getTotalOutingSeconds(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): StatisticMetric {
        return protectedPersonStatisticsApi.getTotalOutingSeconds(
            dependentUserId = dependentUserId,
            period = period.apiValue
        ).toStatisticMetric()
    }

    override suspend fun getTotalOutingCount(
        dependentUserId: Long,
        period: StatisticsPeriod
    ): StatisticMetric {
        return protectedPersonStatisticsApi.getTotalOutingCount(
            dependentUserId = dependentUserId,
            period = period.apiValue
        ).toStatisticMetric()
    }
}
