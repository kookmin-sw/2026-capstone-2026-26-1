package com.example.passedpath.feature.locationtracking.domain.policy

object LocationUploadPolicy {
    const val BATCH_SIZE = 20
    const val UPLOAD_INTERVAL_MS = 3 * 60_000L
    const val PRE_BOUNDARY_UPLOAD_LEAD_TIME_MS = 60_000L
}
