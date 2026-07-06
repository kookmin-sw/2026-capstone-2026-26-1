package com.example.passedpath.feature.care.data.remote.mapper

import com.example.passedpath.feature.care.data.remote.dto.CareDependentUserItemDto
import com.example.passedpath.feature.care.data.remote.dto.CareDependentUserListResponseDto
import com.example.passedpath.feature.care.data.remote.dto.LatestGpsPointItemDto
import com.example.passedpath.feature.care.domain.model.CareDependentUser
import com.example.passedpath.feature.care.domain.model.CareDependentUserList
import com.example.passedpath.feature.care.domain.model.CareLatestGpsPoint
import java.time.OffsetDateTime

internal fun CareDependentUserListResponseDto.toCareDependentUserList(): CareDependentUserList {
    val mappedUsers = dependentUsers.orEmpty()
        .mapNotNull(CareDependentUserItemDto::toCareDependentUserOrNull)

    return CareDependentUserList(
        dependentUserCount = dependentUserCount ?: mappedUsers.size,
        dependentUsers = mappedUsers
    )
}

private fun CareDependentUserItemDto.toCareDependentUserOrNull(): CareDependentUser? {
    val resolvedUserId = dependentUserId ?: return null

    return CareDependentUser(
        dependentUserId = resolvedUserId,
        nickname = nickname.orEmpty(),
        profileImageUrl = profileImageUrl,
        latestGpsPoint = latestGpsPoint?.toCareLatestGpsPointOrNull()
    )
}

private fun LatestGpsPointItemDto.toCareLatestGpsPointOrNull(): CareLatestGpsPoint? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null

    return CareLatestGpsPoint(
        latitude = lat,
        longitude = lng,
        recordedAt = recordedAt,
        recordedAtEpochMillis = recordedAt.toEpochMillisOrNull()
    )
}

private fun String?.toEpochMillisOrNull(): Long? {
    if (isNullOrBlank()) return null

    return runCatching {
        OffsetDateTime.parse(this)
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}
