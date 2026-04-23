package com.example.passedpath.feature.locationtracking.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_routes")
data class DayRouteEntity(
    @PrimaryKey
    val dateKey: String,
    val totalDistanceMeters: Double,
    val pathPointCount: Int,
    val lastRecordedAtEpochMillis: Long?,
    val lastSyncedAtEpochMillis: Long?
)
