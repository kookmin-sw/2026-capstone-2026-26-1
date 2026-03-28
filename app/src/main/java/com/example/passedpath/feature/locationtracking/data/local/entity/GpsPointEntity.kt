package com.example.passedpath.feature.locationtracking.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gps_points",
    indices = [
        Index(value = ["dateKey", "recordedAtEpochMillis"]),
        Index(value = ["isUploaded", "dateKey"])
    ]
)
data class GpsPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L, // Room 내부 식별자
    val dateKey: String, // 좌표가 속한 날짜 키 (`yyyy-MM-dd`)
    val recordedAtEpochMillis: Long, // 위치가 기록된 시각(epoch millis)
    val latitude: Double, // 기록된 위도
    val longitude: Double, // 기록된 경도
    val accuracyMeters: Float?, // 위치 정확도 반경(미터), 없으면 null
    val isUploaded: Boolean // 서버 batch 업로드 완료 여부
)
