package com.example.passedpath.feature.locationtracking.domain.repository

import com.example.passedpath.feature.locationtracking.domain.model.DailyPath

interface DayRouteRepository {
    // 오늘처럼 로컬 기준으로 다뤄야 하는 날짜의 경로 요약과 포인트를 조회한다.
    suspend fun getLocalDayRoute(dateKey: String): DailyPath?

    // 로컬에 유지 중인 날짜 경로의 마지막 동기화 성공 시각을 기록한다.
    suspend fun markLocalDayRouteSynced(dateKey: String, syncedAtEpochMillis: Long)

    // 과거 날짜처럼 서버 기준으로 보여줄 경로를 원격에서 새로 불러온다.
    suspend fun refreshRemoteDayRoute(dateKey: String): DailyPath
}
