package com.example.passedpath.feature.locationtracking.domain.usecase

import android.util.Log
import com.example.passedpath.debug.TrackingDiagnosticsLogger
import com.example.passedpath.feature.locationtracking.data.local.mapper.metersToKilometers
import com.example.passedpath.feature.locationtracking.data.local.mapper.toGpsPointRequestDto
import com.example.passedpath.feature.locationtracking.data.remote.api.DayRouteApi
import com.example.passedpath.feature.locationtracking.data.remote.dto.GpsPointBatchUploadRequestDto
import com.example.passedpath.feature.locationtracking.domain.policy.LocationTrackingPolicy
import com.example.passedpath.feature.locationtracking.domain.repository.DayRouteRepository
import com.example.passedpath.feature.locationtracking.domain.repository.LocationTrackingRepository

class UploadGpsPointsBatchUseCase(
    private val dayRouteApi: DayRouteApi,
    private val locationTrackingRepository: LocationTrackingRepository,
    private val dayRouteRepository: DayRouteRepository,
    private val diagnosticsLogger: TrackingDiagnosticsLogger
) {
    suspend operator fun invoke(
        dateKey: String,
        limit: Int = LocationTrackingPolicy.UPLOAD_BATCH_SIZE
    ): Boolean {
        val pendingLocations = locationTrackingRepository.getPendingUploadLocations(
            dateKey = dateKey,
            limit = limit
        )
        if (pendingLocations.isEmpty()) {
            Log.d(TAG, "Skip upload for dateKey=$dateKey because there are no pending points")
            diagnosticsLogger.log(
                category = TrackingDiagnosticsLogger.CATEGORY_UPLOAD,
                message = "skip_no_pending_points",
                dateKey = dateKey
            )
            return false
        }

        val localDayRoute = dayRouteRepository.getLocalDayRoute(dateKey) ?: return false
        val request = GpsPointBatchUploadRequestDto(
            distance = localDayRoute.totalDistanceMeters.metersToKilometers(),
            gpsPoints = pendingLocations.map { it.toGpsPointRequestDto() }
        )
        Log.i(
            TAG,
            "Uploading ${pendingLocations.size} gps points for dateKey=$dateKey distanceKm=${request.distance}"
        )
        diagnosticsLogger.log(
            category = TrackingDiagnosticsLogger.CATEGORY_UPLOAD,
            message = "attempt count=${pendingLocations.size} distanceKm=${request.distance}",
            dateKey = dateKey
        )

        dayRouteApi.uploadGpsPointsBatch(
            date = dateKey,
            request = request
        )

        locationTrackingRepository.markLocationsUploaded(
            pendingLocations.map { it.recordedAtEpochMillis }
        )
        dayRouteRepository.markLocalDayRouteSynced(
            dateKey = dateKey,
            syncedAtEpochMillis = System.currentTimeMillis()
        )
        Log.i(TAG, "Upload completed for dateKey=$dateKey")
        diagnosticsLogger.log(
            category = TrackingDiagnosticsLogger.CATEGORY_UPLOAD,
            message = "success count=${pendingLocations.size}",
            dateKey = dateKey
        )
        return true
    }

    companion object {
        private const val TAG = "UploadGpsPoints"
    }
}
