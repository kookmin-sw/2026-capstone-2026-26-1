package com.example.passedpath.feature.care.data.remote.dto

data class CareDependentLocationConnectedEventDto(
    val message: String?
)

data class CareDependentLocationUpdatedEventDto(
    val dependentUserId: Long?,
    val latestGpsPoint: LatestGpsPointItemDto?
)
