package com.example.passedpath.feature.locationtracking.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.passedpath.feature.locationtracking.data.local.entity.DayRouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DayRouteDao {
    @Query(
        """
        SELECT * FROM day_routes
        WHERE dateKey = :dateKey
        LIMIT 1
        """
    )
    fun observeByDate(dateKey: String): Flow<DayRouteEntity?>

    @Query(
        """
        SELECT * FROM day_routes
        WHERE dateKey = :dateKey
        LIMIT 1
        """
    )
    suspend fun getByDate(dateKey: String): DayRouteEntity?

    @Query(
        """
        UPDATE day_routes
        SET lastSyncedAtEpochMillis = :syncedAtEpochMillis
        WHERE dateKey = :dateKey
        """
    )
    suspend fun updateLastSyncedAt(dateKey: String, syncedAtEpochMillis: Long)

    @Query(
        """
        SELECT dateKey FROM day_routes
        WHERE lastSyncedAtEpochMillis IS NOT NULL
          AND dateKey < :cutoffDateKey
        ORDER BY dateKey ASC
        """
    )
    suspend fun getSyncedDateKeysOlderThan(cutoffDateKey: String): List<String>

    @Query(
        """
        SELECT dateKey FROM day_routes
        WHERE lastSyncedAtEpochMillis IS NULL
          AND dateKey < :cutoffDateKey
        ORDER BY dateKey ASC
        """
    )
    suspend fun getUnsyncedDateKeysOlderThan(cutoffDateKey: String): List<String>

    @Query(
        """
        DELETE FROM day_routes
        WHERE dateKey = :dateKey
        """
    )
    suspend fun deleteByDate(dateKey: String): Int

    @Upsert
    suspend fun upsert(route: DayRouteEntity)
}
