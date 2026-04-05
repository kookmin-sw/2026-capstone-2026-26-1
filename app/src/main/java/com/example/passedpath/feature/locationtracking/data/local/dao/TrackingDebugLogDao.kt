package com.example.passedpath.feature.locationtracking.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.passedpath.feature.locationtracking.data.local.entity.TrackingDebugLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackingDebugLogDao {
    @Insert
    suspend fun insert(log: TrackingDebugLogEntity): Long

    @Query(
        """
        SELECT * FROM tracking_debug_logs
        ORDER BY recordedAtEpochMillis DESC, id DESC
        LIMIT :limit
        """
    )
    fun observeRecent(limit: Int): Flow<List<TrackingDebugLogEntity>>
}
