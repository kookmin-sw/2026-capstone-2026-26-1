package com.example.passedpath.feature.locationtracking.domain.usecase

import com.example.passedpath.feature.locationtracking.data.local.mapper.metersToKilometers
import com.example.passedpath.feature.locationtracking.data.local.mapper.toGpsPointRequestDto
import com.example.passedpath.feature.locationtracking.data.remote.api.DayRouteApi
import com.example.passedpath.feature.locationtracking.data.remote.dto.GpsPointBatchUploadRequestDto
import com.example.passedpath.feature.locationtracking.domain.policy.LocationTrackingPolicy
import com.example.passedpath.feature.locationtracking.domain.repository.DayRouteRepository
import com.example.passedpath.feature.locationtracking.domain.repository.LocationTrackingRepository

// UploadGpsPointsBatchUseCase : 로컬 Room에 쌓인 GPS 데이터들을 서버로 batch 업로드하는 UseCase(기능)
class UploadGpsPointsBatchUseCase(
    private val dayRouteApi: DayRouteApi,
    private val locationTrackingRepository: LocationTrackingRepository,
    private val dayRouteRepository: DayRouteRepository
) {

    suspend operator fun invoke(
        dateKey: String,
        limit: Int = LocationTrackingPolicy.UPLOAD_BATCH_SIZE
    ): Boolean {

        // 그 날의 업로드할 pending 좌표 가져오기
        val pendingLocations = locationTrackingRepository.getPendingUploadLocations(
            dateKey = dateKey,
            limit = limit
        )
        if (pendingLocations.isEmpty()) return false

        // DayRoute 조회
        val localDayRoute = dayRouteRepository.getLocalDayRoute(dateKey) ?: return false

        // uploadRequestDto에 따라 Request 생성
        val request = GpsPointBatchUploadRequestDto(
            distance = localDayRoute.totalDistanceMeters.metersToKilometers(),
            gpsPoints = pendingLocations.map { it.toGpsPointRequestDto() }
        )

        // API 호출
        dayRouteApi.uploadGpsPointsBatch(
            date = dateKey,
            request = request
        )

        // 계류 좌표들을 업로드표시
        locationTrackingRepository.markLocationsUploaded(
            pendingLocations.map { it.recordedAtEpochMillis }
        )


        dayRouteRepository.markLocalDayRouteSynced(
            dateKey = dateKey,
            syncedAtEpochMillis = System.currentTimeMillis()
        )
        return true
    }
}
