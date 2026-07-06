package com.example.passedpath.feature.care.data.remote.mapper

import com.example.passedpath.feature.care.data.remote.dto.CareDependentUserItemDto
import com.example.passedpath.feature.care.data.remote.dto.CareDependentUserListResponseDto
import com.example.passedpath.feature.care.data.remote.dto.LatestGpsPointItemDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.OffsetDateTime

class CareDependentRemoteMapperTest {

    @Test
    fun `toCareDependentUserList maps dependent users and latest gps point`() {
        val recordedAt = "2026-05-11T09:00:00+09:00"
        val response = CareDependentUserListResponseDto(
            dependentUserCount = 2,
            dependentUsers = listOf(
                CareDependentUserItemDto(
                    dependentUserId = 10L,
                    nickname = "Hong",
                    profileImageUrl = "https://example.com/profile.png",
                    latestGpsPoint = LatestGpsPointItemDto(
                        latitude = 37.5665,
                        longitude = 126.978,
                        recordedAt = recordedAt
                    )
                ),
                CareDependentUserItemDto(
                    dependentUserId = 11L,
                    nickname = "Kim",
                    profileImageUrl = null,
                    latestGpsPoint = null
                )
            )
        )

        val result = response.toCareDependentUserList()

        assertEquals(2, result.dependentUserCount)
        assertEquals(2, result.dependentUsers.size)
        assertEquals(10L, result.dependentUsers.first().dependentUserId)
        assertEquals("Hong", result.dependentUsers.first().nickname)
        assertEquals("https://example.com/profile.png", result.dependentUsers.first().profileImageUrl)
        assertEquals(37.5665, result.dependentUsers.first().latestGpsPoint?.latitude ?: 0.0, 0.0)
        assertEquals(126.978, result.dependentUsers.first().latestGpsPoint?.longitude ?: 0.0, 0.0)
        assertEquals(recordedAt, result.dependentUsers.first().latestGpsPoint?.recordedAt)
        assertEquals(
            OffsetDateTime.parse(recordedAt).toInstant().toEpochMilli(),
            result.dependentUsers.first().latestGpsPoint?.recordedAtEpochMillis
        )
        assertNull(result.dependentUsers[1].profileImageUrl)
        assertNull(result.dependentUsers[1].latestGpsPoint)
    }

    @Test
    fun `toCareDependentUserList drops invalid user id and invalid latest gps point`() {
        val response = CareDependentUserListResponseDto(
            dependentUserCount = null,
            dependentUsers = listOf(
                CareDependentUserItemDto(
                    dependentUserId = null,
                    nickname = "Broken",
                    profileImageUrl = null,
                    latestGpsPoint = null
                ),
                CareDependentUserItemDto(
                    dependentUserId = 12L,
                    nickname = null,
                    profileImageUrl = null,
                    latestGpsPoint = LatestGpsPointItemDto(
                        latitude = null,
                        longitude = 126.978,
                        recordedAt = "2026-05-11T09:00:00+09:00"
                    )
                ),
                CareDependentUserItemDto(
                    dependentUserId = 13L,
                    nickname = "Lee",
                    profileImageUrl = null,
                    latestGpsPoint = LatestGpsPointItemDto(
                        latitude = 37.5,
                        longitude = 127.5,
                        recordedAt = "invalid-time"
                    )
                )
            )
        )

        val result = response.toCareDependentUserList()

        assertEquals(2, result.dependentUserCount)
        assertEquals(2, result.dependentUsers.size)
        assertEquals(12L, result.dependentUsers.first().dependentUserId)
        assertEquals("", result.dependentUsers.first().nickname)
        assertNull(result.dependentUsers.first().latestGpsPoint)
        assertEquals("invalid-time", result.dependentUsers[1].latestGpsPoint?.recordedAt)
        assertNull(result.dependentUsers[1].latestGpsPoint?.recordedAtEpochMillis)
    }
}
