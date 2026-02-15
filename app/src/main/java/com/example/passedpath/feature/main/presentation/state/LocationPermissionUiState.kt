package com.example.passedpath.feature.main.presentation.state

// 메인 화면 기능 제한용 상태
enum class LocationPermissionUiState {
    FULL,      // 항상 허용 → 모든 기능 사용 가능
    LIMITED    // 항상 허용 아님 → 기능 제한
}
