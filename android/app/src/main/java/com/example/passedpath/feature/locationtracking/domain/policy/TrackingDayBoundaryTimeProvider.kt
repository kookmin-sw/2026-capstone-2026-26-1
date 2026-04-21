package com.example.passedpath.feature.locationtracking.domain.policy

import java.time.LocalTime
import java.time.LocalTime.MIDNIGHT

interface TrackingDayBoundaryTimeProvider {
    fun getBoundaryLocalTime(): LocalTime
}

class FixedTrackingDayBoundaryTimeProvider(
    private val boundaryLocalTime: LocalTime = MIDNIGHT
) : TrackingDayBoundaryTimeProvider {
    override fun getBoundaryLocalTime(): LocalTime = boundaryLocalTime
}
