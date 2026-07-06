package com.example.passedpath.feature.summary.domain.repository

import com.example.passedpath.feature.summary.domain.model.WeeklyStatistics

interface WeeklyStatisticsRepository {
    suspend fun getWeeklyStatistics(): WeeklyStatistics
}
