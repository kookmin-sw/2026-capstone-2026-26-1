package com.example.passedpath.feature.locationtracking.domain.usecase

import com.example.passedpath.feature.locationtracking.domain.repository.TrackingDebugLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

open class ObserveRecentTrackingEventsUseCase(
    private val trackingDebugLogRepository: TrackingDebugLogRepository
) {
    open operator fun invoke(limit: Int): Flow<List<String>> {
        return trackingDebugLogRepository.observeRecent(limit).map { logs ->
            logs.map { log ->
                val datePrefix = log.dateKey?.let { "[$it] " } ?: ""
                "${datePrefix}${log.category}: ${log.message}"
            }
        }
    }
}
