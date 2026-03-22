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

    @Upsert
    suspend fun upsert(route: DayRouteEntity)
}
