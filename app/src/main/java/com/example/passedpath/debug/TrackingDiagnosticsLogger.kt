package com.example.passedpath.debug

import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.locationtracking.domain.repository.TrackingDebugLogRepository

class TrackingDiagnosticsLogger(
    private val repository: TrackingDebugLogRepository
) {
    suspend fun log(category: String, message: String, dateKey: String? = null) {
        AppDebugLogger.debug(DebugLogTag.TRACKING_DIAGNOSTICS, "[$category][$dateKey] $message")
        repository.append(category = category, message = message, dateKey = dateKey)
    }

    suspend fun logLocationCallback(dateKey: String, location: TrackedLocation) {
        log(
            category = CATEGORY_CALLBACK,
            dateKey = dateKey,
            message = "lat=${location.latitude}, lng=${location.longitude}, accuracy=${location.accuracyMeters}, at=${location.recordedAtEpochMillis}"
        )
    }

    companion object {
        const val CATEGORY_SERVICE = "service"
        const val CATEGORY_CALLBACK = "callback"
        const val CATEGORY_SAVE = "save"
        const val CATEGORY_UPLOAD = "upload"
    }
}
