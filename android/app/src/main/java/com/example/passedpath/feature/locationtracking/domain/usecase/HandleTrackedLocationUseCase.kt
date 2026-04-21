package com.example.passedpath.feature.locationtracking.domain.usecase

import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.locationtracking.domain.policy.LocationUploadPolicy
import com.example.passedpath.feature.locationtracking.domain.policy.TrackingDateKeyResolver
import com.example.passedpath.feature.locationtracking.domain.repository.LocationTrackingRepository
import com.example.passedpath.feature.locationtracking.domain.repository.SaveRawLocationResult

data class HandleTrackedLocationResult(
    val dateKey: String,
    val saveResult: SaveRawLocationResult,
    val pendingCount: Int,
    val shouldUploadImmediately: Boolean
)

class HandleTrackedLocationUseCase(
    private val locationTrackingRepository: LocationTrackingRepository,
    private val dateKeyResolver: TrackingDateKeyResolver
) {
    suspend operator fun invoke(trackedLocation: TrackedLocation): HandleTrackedLocationResult {
        val dateKey = dateKeyResolver.resolveDateKey(trackedLocation.recordedAtEpochMillis)
        val saveResult = locationTrackingRepository.saveRawLocation(trackedLocation)
        val pendingCount = locationTrackingRepository.getPendingUploadLocationCount(dateKey)

        return HandleTrackedLocationResult(
            dateKey = dateKey,
            saveResult = saveResult,
            pendingCount = pendingCount,
            shouldUploadImmediately = pendingCount >= LocationUploadPolicy.BATCH_SIZE
        )
    }
}
