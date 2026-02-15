package com.example.passedpath.feature.permission

// 위치 권한 플로우의 최종 진입 상태
enum class LocationPermissionState {
    NORMAL,        // 항상 허용 상태 → 정상 메인 진입
    NEED_SETTINGS, // 항상 허용 아님 → 설정 이동 필요
    LIMITED        // 설정 이후에도 항상 허용 아님 → 제한 메인
}

