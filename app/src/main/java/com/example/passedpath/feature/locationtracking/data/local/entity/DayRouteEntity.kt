package com.example.passedpath.feature.locationtracking.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_routes")
data class DayRouteEntity(
    @PrimaryKey
    val dateKey: String, // 하루 경로를 구분하는 날짜 키 (`yyyy-MM-dd`)
    val totalDistanceMeters: Double, // 해당 날짜 경로의 누적 이동 거리
    val pathPointCount: Int, // 해당 날짜 raw gps point 개수
    val lastRecordedAtEpochMillis: Long?, // 마지막 좌표가 기록된 시각(epoch millis)
    val lastSyncedAtEpochMillis: Long?, // 서버와 마지막으로 동기화한 시각(epoch millis)
    val encodedPath: String? // 서버가 계산한 canonical polyline 문자열
)
