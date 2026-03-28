package com.example.passedpath.feature.locationtracking.domain.repository

import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import kotlinx.coroutines.flow.Flow

interface LocationTrackingRepository {
    suspend fun saveRawLocation(location: TrackedLocation)
    fun observeDailyPath(dateKey: String): Flow<DailyPath>
    suspend fun getPendingUploadLocations(dateKey: String, limit: Int): List<TrackedLocation>
    suspend fun markLocationsUploaded(recordedAtEpochMillis: List<Long>)
}
