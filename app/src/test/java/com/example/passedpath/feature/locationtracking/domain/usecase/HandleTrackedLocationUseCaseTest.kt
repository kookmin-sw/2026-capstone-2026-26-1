package com.example.passedpath.feature.locationtracking.domain.usecase

import com.example.passedpath.feature.locationtracking.domain.model.DailyPath
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import com.example.passedpath.feature.locationtracking.domain.policy.FixedTrackingDayBoundaryTimeProvider
import com.example.passedpath.feature.locationtracking.domain.policy.TrackingDateKeyResolver
import com.example.passedpath.feature.locationtracking.domain.repository.LocationTrackingRepository
import com.example.passedpath.feature.locationtracking.domain.repository.SaveRawLocationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime
import java.time.ZoneId

class HandleTrackedLocationUseCaseTest {

    @Test
    fun `returns upload decision when pending count reaches batch size`() = runTest {
        val repository = FakeLocationTrackingRepository(
            saveResult = SaveRawLocationResult.SAVED,
            pendingCount = 20
        )
        val useCase = HandleTrackedLocationUseCase(
            locationTrackingRepository = repository,
            dateKeyResolver = testDateKeyResolver()
        )

        val result = useCase(
            TrackedLocation(
                latitude = 37.0,
                longitude = 127.0,
                accuracyMeters = 10f,
                recordedAtEpochMillis = 0L
            )
        )

        assertEquals("1970-01-01", result.dateKey)
        assertEquals(SaveRawLocationResult.SAVED, result.saveResult)
        assertEquals(20, result.pendingCount)
        assertTrue(result.shouldUploadImmediately)
    }

    @Test
    fun `returns no immediate upload when pending count is below batch size`() = runTest {
        val repository = FakeLocationTrackingRepository(
            saveResult = SaveRawLocationResult.DROPPED_DISTANCE,
            pendingCount = 3
        )
        val useCase = HandleTrackedLocationUseCase(
            locationTrackingRepository = repository,
            dateKeyResolver = testDateKeyResolver()
        )

        val result = useCase(
            TrackedLocation(
                latitude = 37.0,
                longitude = 127.0,
                accuracyMeters = 10f,
                recordedAtEpochMillis = 0L
            )
        )

        assertEquals(SaveRawLocationResult.DROPPED_DISTANCE, result.saveResult)
        assertEquals(3, result.pendingCount)
        assertFalse(result.shouldUploadImmediately)
    }

    private fun testDateKeyResolver(): TrackingDateKeyResolver {
        return TrackingDateKeyResolver(
            boundaryTimeProvider = FixedTrackingDayBoundaryTimeProvider(LocalTime.MIDNIGHT),
            zoneId = ZoneId.of("UTC")
        )
    }
}

private class FakeLocationTrackingRepository(
    private val saveResult: SaveRawLocationResult,
    private val pendingCount: Int
) : LocationTrackingRepository {
    override suspend fun saveRawLocation(location: TrackedLocation): SaveRawLocationResult = saveResult

    override fun observeDailyPath(dateKey: String): Flow<DailyPath> = emptyFlow()

    override suspend fun getPendingUploadLocationCount(dateKey: String): Int = pendingCount

    override suspend fun getPendingUploadLocations(dateKey: String, limit: Int): List<TrackedLocation> = emptyList()

    override suspend fun markLocationsUploaded(recordedAtEpochMillis: List<Long>) = Unit
}
