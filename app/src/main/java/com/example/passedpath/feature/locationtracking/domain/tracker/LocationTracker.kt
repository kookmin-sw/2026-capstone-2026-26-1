package com.example.passedpath.feature.locationtracking.domain.tracker

import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation

interface LocationTrackingSession {
    // 진행 중인 위치 업데이트 구독을 중지한다.
    fun stop()
}

// 인터페이스: 정책에 따라 위치를 받는다
interface LocationTracker {
    // 현재 시점 기준 위치 1건을 조회한다.
    suspend fun getCurrentLocation(): TrackedLocation?

    // 연속 위치 업데이트를 시작하고 중지 가능한 세션을 반환한다.
    fun startLocationUpdates(onLocationUpdated: (TrackedLocation) -> Unit): LocationTrackingSession
}
