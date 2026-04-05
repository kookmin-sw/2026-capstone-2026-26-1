package com.example.passedpath.feature.locationtracking.data.repository

import com.example.passedpath.feature.locationtracking.data.local.dao.TrackingDebugLogDao
import com.example.passedpath.feature.locationtracking.data.local.entity.TrackingDebugLogEntity
import com.example.passedpath.feature.locationtracking.domain.model.TrackingDebugLog
import com.example.passedpath.feature.locationtracking.domain.repository.TrackingDebugLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTrackingDebugLogRepository(
    private val trackingDebugLogDao: TrackingDebugLogDao
) : TrackingDebugLogRepository {

    override suspend fun append(category: String, message: String, dateKey: String?) {
        trackingDebugLogDao.insert(
            TrackingDebugLogEntity(
                recordedAtEpochMillis = System.currentTimeMillis(),
                category = category,
                dateKey = dateKey,
                message = message
            )
        )
    }

    override fun observeRecent(limit: Int): Flow<List<TrackingDebugLog>> {
        return trackingDebugLogDao.observeRecent(limit).map { logs ->
            logs.map { log ->
                TrackingDebugLog(
                    recordedAtEpochMillis = log.recordedAtEpochMillis,
                    category = log.category,
                    dateKey = log.dateKey,
                    message = log.message
                )
            }
        }
    }
}
