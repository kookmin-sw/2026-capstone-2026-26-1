package com.example.passedpath.feature.locationtracking.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.passedpath.feature.locationtracking.data.local.entity.GpsPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GpsPointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(point: GpsPointEntity): Long

    @Query(
        """
        SELECT * FROM gps_points
        WHERE dateKey = :dateKey
        ORDER BY recordedAtEpochMillis ASC
        """
    )
    fun observePointsByDate(dateKey: String): Flow<List<GpsPointEntity>>

    @Query(
        """
        SELECT * FROM gps_points
        WHERE dateKey = :dateKey
        ORDER BY recordedAtEpochMillis ASC
        """
    )
    suspend fun getPointsByDate(dateKey: String): List<GpsPointEntity>

    @Query(
        """
        SELECT * FROM gps_points
        WHERE dateKey = :dateKey
        ORDER BY recordedAtEpochMillis DESC
        LIMIT 1
        """
    )
    suspend fun getLatestPointByDate(dateKey: String): GpsPointEntity?

    @Query(
        """
        SELECT * FROM gps_points
        WHERE dateKey = :dateKey AND isUploaded = 0
        ORDER BY recordedAtEpochMillis ASC
        LIMIT :limit
        """
    )
    suspend fun getPendingUploadPoints(dateKey: String, limit: Int): List<GpsPointEntity>

    @Query(
        """
        SELECT COUNT(*) FROM gps_points
        WHERE dateKey = :dateKey AND isUploaded = 0
        """
    )
    suspend fun getPendingUploadPointCount(dateKey: String): Int

    @Query(
        """
        UPDATE gps_points
        SET isUploaded = 1
        WHERE recordedAtEpochMillis IN (:recordedAtEpochMillis)
        """
    )
    suspend fun markUploaded(recordedAtEpochMillis: List<Long>)

    @Query(
        """
        DELETE FROM gps_points
        WHERE dateKey = :dateKey
        """
    )
    suspend fun deleteByDate(dateKey: String): Int
}
