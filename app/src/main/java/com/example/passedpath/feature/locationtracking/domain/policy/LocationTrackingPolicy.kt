package com.example.passedpath.feature.locationtracking.domain.policy

object LocationTrackingPolicy {
    // 위치 요청 주기: 이 간격으로 위치 요청
    const val LOCATION_UPDATE_INTERVAL_MS = 5_000L

    // 최소 위치 요청 간격: 알고리즘 시스템 상 더 빠르게 좌표를 주더라도, 이 간격보다 짧으면 받지 않음
    const val LOCATION_MIN_UPDATE_INTERVAL_MS = 2_000L

    // 저장 최소 이동 거리: 이전 좌표와 비교했을 떄, 이 거리 이상일 때만 저장
    const val MIN_SAVE_DISTANCE_METERS = 10.0

    // 저장 최대 정확도: 좌표의 accuracy가 이 값보다 크면 부정확으로 간주하고 저장하지 않음
    const val MAX_ACCEPTABLE_ACCURACY_METERS = 50f

    // 중복 좌표 제거 기준 거리: 이전 좌표 대비 이 거리 이하는 같은 위치로 간주
    const val DUPLICATE_POINT_DISTANCE_METERS = 3.0

    // 중복 좌표 제거 기준 시간: 이전 좌표 대비 이 시간 차는 같은 위치로 간주
    const val DUPLICATE_POINT_WINDOW_MS = 10_000L

    // 업로드 배치 크기: 이 개수 이상의 저장할 좌표가 쌓이면 서버로 전송
    const val UPLOAD_BATCH_SIZE = 20

    // 업로드 주기: 일정 시간마다 저장한 좌표를 서버로 전송
    const val UPLOAD_INTERVAL_MS = 60_000L
}
