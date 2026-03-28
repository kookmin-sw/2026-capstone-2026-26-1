package com.example.passedpath.feature.locationtracking.domain.repository

import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import kotlinx.coroutines.flow.Flow

interface LocationTrackingRepository {
    // 수집된 위치 좌표 1건을 로컬 저장 정책에 맞춰 저장한다.
    suspend fun saveRawLocation(location: TrackedLocation)

    // 특정 날짜의 로컬 raw GPS 포인트와 요약 정보를 관찰한다.
    fun observeDailyPath(dateKey: String): Flow<DailyPath>

    // 특정 날짜에 아직 서버로 올리지 않은 로컬 좌표 개수를 조회한다.
    suspend fun getPendingUploadLocationCount(dateKey: String): Int

    // 특정 날짜에 아직 서버로 올리지 않은 로컬 좌표 목록을 조회한다.
    suspend fun getPendingUploadLocations(dateKey: String, limit: Int): List<TrackedLocation>

    // 서버 업로드가 끝난 로컬 좌표들을 업로드 완료 상태로 표시한다.
    suspend fun markLocationsUploaded(recordedAtEpochMillis: List<Long>)
}
