package com.example.passedpath.feature.care.domain.model

sealed interface CareDependentLocationStreamEvent {
    data class Connected(
        val message: String?
    ) : CareDependentLocationStreamEvent

    data class LocationUpdated(
        val dependentUserId: Long,
        val latestGpsPoint: CareLatestGpsPoint
    ) : CareDependentLocationStreamEvent

    data class Error(
        val throwable: Throwable
    ) : CareDependentLocationStreamEvent
}
