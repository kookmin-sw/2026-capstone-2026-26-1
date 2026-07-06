package com.example.passedpath.feature.locationtracking.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_routes")
data class DayRouteEntity(
    @PrimaryKey
    val dateKey: String,
    val totalDistanceMeters: Double,
    val pathPointCount: Int,
    val lastRecordedAtEpochMillis: Long?,
    val lastSyncedAtEpochMillis: Long?,
    @ColumnInfo(defaultValue = "'[]'")
    val mapPolylineCacheJson: String = "[]",
    @ColumnInfo(defaultValue = "0")
    val mapPolylineCacheSourcePointCount: Int = 0
)
