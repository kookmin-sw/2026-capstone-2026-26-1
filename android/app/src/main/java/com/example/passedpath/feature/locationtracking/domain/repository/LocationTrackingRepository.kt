package com.example.passedpath.feature.locationtracking.domain.repository

import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import kotlinx.coroutines.flow.Flow

enum class SaveRawLocationResult {
    SAVED,
    DROPPED_ACCURACY,
    DROPPED_DISTANCE,
    DROPPED_OUT_OF_ORDER
}

interface LocationTrackingRepository {
    suspend fun saveRawLocation(location: TrackedLocation): SaveRawLocationResult

    fun observeDailyPath(dateKey: String): Flow<DailyPath>

    suspend fun getPendingUploadLocationCount(dateKey: String): Int

    suspend fun getPendingUploadLocations(dateKey: String, limit: Int): List<TrackedLocation>

    suspend fun markLocationsUploaded(recordedAtEpochMillis: List<Long>)
}
