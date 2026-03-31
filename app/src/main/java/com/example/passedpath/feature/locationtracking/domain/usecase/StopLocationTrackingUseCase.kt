package com.example.passedpath.feature.locationtracking.domain.usecase

import android.content.Context
import com.example.passedpath.feature.locationtracking.data.manager.LocationTrackingServiceStateWriter
import com.example.passedpath.feature.locationtracking.service.LocationTrackingService

class StopLocationTrackingUseCase(
    private val context: Context,
    private val trackingServiceStateWriter: LocationTrackingServiceStateWriter
) {
    operator fun invoke(persistUserPreference: Boolean = true) {
        if (persistUserPreference) {
            trackingServiceStateWriter.setTrackingEnabledByUser(false)
        }
        LocationTrackingService.stop(context)
    }
}
