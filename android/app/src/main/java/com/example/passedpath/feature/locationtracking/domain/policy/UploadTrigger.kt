package com.example.passedpath.feature.locationtracking.domain.policy

// GPS 배치 업로드가 어떤 이유로 트리거됐는지 나타내는 관측용 사유.
// 서버로 그대로 전달되어 디버깅/관측 로그에 남는다.
enum class UploadTrigger {
    WATCHING_IMMEDIATE, // 보호자가 보고 있어(watching) 즉시 업로드 모드일 때
    BATCH_SIZE,         // 대기 좌표가 배치 크기에 도달해 즉시 업로드될 때
    PERIODIC,           // 3분 주기 루프
    PRE_BOUNDARY,       // 날짜 경계 직전 flush
    NETWORK_RECOVERY,   // 네트워크 복구 시 flush
    STOP_FLUSH          // 서비스 종료 시 flush
}
