package com.example.passedpath.feature.locationtracking.domain.usecase

import android.content.Context
import com.example.passedpath.feature.locationtracking.service.LocationTrackingService

class StartLocationTrackingUseCase(
    private val context: Context
) {
    operator fun invoke() {
        LocationTrackingService.start(context)
    }
}
