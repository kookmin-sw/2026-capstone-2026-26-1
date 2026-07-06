package com.example.passedpath.feature.care.data.remote.dto

data class CareDependentUserListResponseDto(
    val dependentUserCount: Int?,
    val dependentUsers: List<CareDependentUserItemDto>?
)

data class CareDependentUserItemDto(
    val dependentUserId: Long?,
    val nickname: String?,
    val profileImageUrl: String?,
    val latestGpsPoint: LatestGpsPointItemDto?
)

data class LatestGpsPointItemDto(
    val latitude: Double?,
    val longitude: Double?,
    val recordedAt: String?
)
