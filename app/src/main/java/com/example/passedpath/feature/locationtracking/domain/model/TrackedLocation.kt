package com.example.passedpath.feature.locationtracking.domain.model

data class TrackedLocation(
    val latitude: Double, // 위도
    val longitude: Double, // 경도
    val accuracyMeters: Float?, // 이 좌표는 반역 N미터 내에 있음
    val recordedAtEpochMillis: Long // 좌표 측정 시간(epoch millis 단위)
)
