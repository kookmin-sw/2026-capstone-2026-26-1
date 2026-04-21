package com.example.passedpath.feature.locationtracking.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracking_debug_logs",
    indices = [
        Index(value = ["recordedAtEpochMillis"]),
        Index(value = ["dateKey", "recordedAtEpochMillis"])
    ]
)
data class TrackingDebugLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val recordedAtEpochMillis: Long,
    val category: String,
    val dateKey: String?,
    val message: String
)
