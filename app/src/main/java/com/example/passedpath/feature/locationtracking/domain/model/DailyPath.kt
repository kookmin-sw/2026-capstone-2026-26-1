package com.example.passedpath.feature.locationtracking.domain.model

// 하루 단위의 경로 집계 모델
data class DailyPath(
    val dateKey: String, // 경로가 속한 날짜
    val points: List<TrackedLocation> = emptyList(), // 하루 동안 수집된 gps 좌표의 시간순 목록
    val totalDistanceMeters: Double = 0.0, // 위치 좌표 기반으로 계산된 총 이동 거리
    val pathPointCount: Int = points.size // 경로를 구성하는 포인트의 수
)
