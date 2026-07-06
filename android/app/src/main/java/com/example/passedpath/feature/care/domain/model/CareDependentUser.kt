package com.example.passedpath.feature.care.domain.model

data class CareLatestGpsPoint(
    val latitude: Double,
    val longitude: Double,
    val recordedAt: String?,
    val recordedAtEpochMillis: Long?
)

data class CareDependentUser(
    val dependentUserId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val latestGpsPoint: CareLatestGpsPoint?
)

data class CareDependentUserList(
    val dependentUserCount: Int,
    val dependentUsers: List<CareDependentUser>
)
