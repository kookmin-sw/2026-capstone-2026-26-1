package com.example.passedpath.feature.locationtracking.domain.policy

object LocationUploadPolicy {
    const val BATCH_SIZE = 20
    const val UPLOAD_INTERVAL_MS = 3 * 60_000L
    const val PRE_BOUNDARY_UPLOAD_LEAD_TIME_MS = 60_000L

    // 보호자가 위치 조회 화면을 나갔다는 FCM 신호(watching=false)가 유실될 경우를 대비한 안전장치.
    // watching=true 수신 후 이 시간 안에 watching=false가 오지 않으면 자동으로 기본 배치 정책으로 복귀한다.
    const val WATCHING_TIMEOUT_MS = 10 * 60_000L
}
