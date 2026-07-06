package com.example.passedpath.feature.care.data.repository

import com.example.passedpath.feature.care.data.remote.api.CareDependentApi
import com.example.passedpath.feature.care.data.remote.dto.CareDependentUserItemDto
import com.example.passedpath.feature.care.data.remote.dto.CareDependentUserListResponseDto
import com.example.passedpath.feature.care.data.remote.dto.LatestGpsPointItemDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CareDependentRepositoryImplTest {

    @Test
    fun `getDependents fetches and maps dependent list`() = runTest {
        val fakeApi = FakeCareDependentApi(
            response = CareDependentUserListResponseDto(
                dependentUserCount = 1,
                dependentUsers = listOf(
                    CareDependentUserItemDto(
                        dependentUserId = 10L,
                        nickname = "Hong",
                        profileImageUrl = "https://example.com/profile.png",
                        latestGpsPoint = LatestGpsPointItemDto(
                            latitude = 37.5665,
                            longitude = 126.978,
                            recordedAt = "2026-05-11T09:00:00+09:00"
                        )
                    )
                )
            )
        )
        val repository = CareDependentRepositoryImpl(careDependentApi = fakeApi)

        val result = repository.getDependents()

        assertEquals(1, fakeApi.requestCount)
        assertEquals(1, result.dependentUserCount)
        assertEquals(1, result.dependentUsers.size)
        assertEquals(10L, result.dependentUsers.first().dependentUserId)
        assertEquals("Hong", result.dependentUsers.first().nickname)
    }

    @Test
    fun `getDependents propagates api exception`() = runTest {
        val expected = IllegalStateException("network failed")
        val repository = CareDependentRepositoryImpl(
            careDependentApi = FakeCareDependentApi(throwable = expected)
        )

        try {
            repository.getDependents()
            fail("Expected exception to be thrown")
        } catch (actual: IllegalStateException) {
            assertEquals(expected, actual)
        }
    }

    private class FakeCareDependentApi(
        private val response: CareDependentUserListResponseDto = CareDependentUserListResponseDto(
            dependentUserCount = 0,
            dependentUsers = emptyList()
        ),
        private val throwable: Throwable? = null
    ) : CareDependentApi {
        var requestCount: Int = 0

        override suspend fun getDependents(): CareDependentUserListResponseDto {
            requestCount++
            throwable?.let { throw it }
            return response
        }
    }
}
