package com.example.passedpath.feature.locationtracking.domain.policy

object LocationTrackingPolicy {
    const val LOCATION_UPDATE_INTERVAL_MS = 25_000L
    const val LOCATION_MIN_UPDATE_INTERVAL_MS = 10_000L
    const val LOCATION_MIN_UPDATE_DISTANCE_METERS = 10f

    const val MIN_SAVE_DISTANCE_METERS = 10.0
    const val MAX_ACCEPTABLE_ACCURACY_METERS = 50f

    const val UPLOAD_BATCH_SIZE = 20
    const val UPLOAD_INTERVAL_MS = 3 * 60_000L
    const val PRE_BOUNDARY_UPLOAD_LEAD_TIME_MS = 60_000L
}
