package com.example.passedpath.feature.summary.domain.repository

import com.example.passedpath.feature.summary.domain.model.StatisticMetric
import com.example.passedpath.feature.summary.domain.model.StatisticsPeriod

interface StatisticMetricRepository {
    suspend fun getOutingTime(period: StatisticsPeriod): StatisticMetric

    suspend fun getEnterHomeTime(period: StatisticsPeriod): StatisticMetric

    suspend fun getTotalOutingSeconds(period: StatisticsPeriod): StatisticMetric

    suspend fun getTotalOutingCount(period: StatisticsPeriod): StatisticMetric
}
