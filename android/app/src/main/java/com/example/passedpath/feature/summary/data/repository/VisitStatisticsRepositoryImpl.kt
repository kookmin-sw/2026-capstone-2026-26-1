package com.example.passedpath.feature.summary.data.repository

import com.example.passedpath.feature.summary.data.remote.api.VisitStatisticsApi
import com.example.passedpath.feature.summary.data.remote.mapper.toVisitStatistics
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod
import com.example.passedpath.feature.summary.domain.model.VisitStatistics
import com.example.passedpath.feature.summary.domain.repository.VisitStatisticsRepository

class VisitStatisticsRepositoryImpl(
    private val visitStatisticsApi: VisitStatisticsApi
) : VisitStatisticsRepository {
    override suspend fun getVisitStatistics(period: StatisticsPeriod): VisitStatistics {
        return visitStatisticsApi.getVisitStatistics(period = period.apiValue).toVisitStatistics()
    }
}
