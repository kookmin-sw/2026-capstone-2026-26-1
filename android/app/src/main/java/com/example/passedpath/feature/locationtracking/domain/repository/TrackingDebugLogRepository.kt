package com.example.passedpath.feature.locationtracking.domain.repository

import com.example.passedpath.feature.locationtracking.domain.model.TrackingDebugLog
import kotlinx.coroutines.flow.Flow

interface TrackingDebugLogRepository {
    suspend fun append(category: String, message: String, dateKey: String? = null)

    fun observeRecent(limit: Int): Flow<List<TrackingDebugLog>>
}
