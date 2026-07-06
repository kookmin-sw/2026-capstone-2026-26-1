package com.example.passedpath.feature.summary.data.repository

import com.example.passedpath.feature.summary.data.remote.api.WeeklyStatisticsApi
import com.example.passedpath.feature.summary.data.remote.mapper.toWeeklyStatistics
import com.example.passedpath.feature.summary.domain.model.WeeklyStatistics
import com.example.passedpath.feature.summary.domain.repository.WeeklyStatisticsRepository

class WeeklyStatisticsRepositoryImpl(
    private val weeklyStatisticsApi: WeeklyStatisticsApi
) : WeeklyStatisticsRepository {
    override suspend fun getWeeklyStatistics(): WeeklyStatistics {
        return weeklyStatisticsApi.getWeeklyStatistics().toWeeklyStatistics()
    }
}
