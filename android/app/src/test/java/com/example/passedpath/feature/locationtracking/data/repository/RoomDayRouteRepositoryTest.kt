package com.example.passedpath.feature.locationtracking.data.repository

import com.example.passedpath.feature.locationtracking.data.local.dao.DayRouteDao
import com.example.passedpath.feature.locationtracking.data.local.dao.GpsPointDao
import com.example.passedpath.feature.locationtracking.data.local.entity.DayRouteEntity
import com.example.passedpath.feature.locationtracking.data.local.entity.GpsPointEntity
import com.example.passedpath.feature.locationtracking.data.local.mapper.decodeFreshMapPolylineCacheOrNull
import com.example.passedpath.feature.locationtracking.data.local.mapper.toMapPolylineCacheJson
import com.example.passedpath.feature.locationtracking.data.local.model.GpsPointRouteProjection
import com.example.passedpath.feature.locationtracking.data.remote.api.DayRouteApi
import com.example.passedpath.feature.locationtracking.data.remote.dto.DayRouteDetailResponseDto
import com.example.passedpath.feature.locationtracking.data.remote.dto.GpsPointBatchUploadRequestDto
import com.example.passedpath.feature.locationtracking.data.remote.dto.GpsPointBatchUploadResponseDto
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomDayRouteRepositoryTest {

    @Test
    fun `observeLocalRouteSnapshot uses fresh cache without reading projected points`() = runTest {
        val cachePoints = listOf(
            TrackedLocation(37.1, 127.1, 5f, 1L),
            TrackedLocation(37.2, 127.2, 5f, 2L)
        )
        val route = DayRouteEntity(
            dateKey = TestDateKey,
            totalDistanceMeters = 120.0,
            pathPointCount = 2,
            lastRecordedAtEpochMillis = 2L,
            lastSyncedAtEpochMillis = null,
            mapPolylineCacheJson = cachePoints.toMapPolylineCacheJson(),
            mapPolylineCacheSourcePointCount = 2
        )
        val dayRouteDao = FakeDayRouteDao(route)
        val gpsPointDao = FakeGpsPointDao(projections = emptyList())
        val repository = createRepository(
            dayRouteDao = dayRouteDao,
            gpsPointDao = gpsPointDao,
            dispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        val snapshot = repository.observeLocalRouteSnapshot(TestDateKey).first()

        assertNotNull(snapshot)
        requireNotNull(snapshot)
        assertEquals(0, gpsPointDao.projectionReadCount)
        assertNull(dayRouteDao.upsertedRoute)
        assertEquals(2, snapshot.points.size)
        assertEquals(120.0, snapshot.totalDistanceMeters, 0.0)
        assertEquals(2, snapshot.pathPointCount)
        assertEquals(1L, snapshot.points.first().recordedAtEpochMillis)
        assertEquals(2L, snapshot.points.last().recordedAtEpochMillis)
    }

    @Test
    fun `observeLocalRouteSnapshot rebuilds stale cache and preserves route summary`() = runTest {
        val staleRoute = DayRouteEntity(
            dateKey = TestDateKey,
            totalDistanceMeters = 321.0,
            pathPointCount = 3,
            lastRecordedAtEpochMillis = 3L,
            lastSyncedAtEpochMillis = null,
            mapPolylineCacheJson = "[]",
            mapPolylineCacheSourcePointCount = 0
        )
        val projections = listOf(
            GpsPointRouteProjection(1L, 37.1, 127.1, 5f),
            GpsPointRouteProjection(2L, 37.2, 127.2, 5f),
            GpsPointRouteProjection(3L, 37.3, 127.3, 5f)
        )
        val dayRouteDao = FakeDayRouteDao(staleRoute)
        val gpsPointDao = FakeGpsPointDao(projections = projections)
        val repository = createRepository(
            dayRouteDao = dayRouteDao,
            gpsPointDao = gpsPointDao,
            dispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        val snapshot = repository.observeLocalRouteSnapshot(TestDateKey).first()

        assertNotNull(snapshot)
        requireNotNull(snapshot)
        assertEquals(1, gpsPointDao.projectionReadCount)
        assertNotNull(dayRouteDao.upsertedRoute)
        val rebuiltRoute = requireNotNull(dayRouteDao.upsertedRoute)
        assertEquals(3, rebuiltRoute.mapPolylineCacheSourcePointCount)
        assertNotNull(rebuiltRoute.decodeFreshMapPolylineCacheOrNull())
        assertEquals(3, snapshot.points.size)
        assertEquals(321.0, snapshot.totalDistanceMeters, 0.0)
        assertEquals(3, snapshot.pathPointCount)
        assertEquals(1L, snapshot.points.first().recordedAtEpochMillis)
        assertEquals(3L, snapshot.points.last().recordedAtEpochMillis)
    }

    private fun createRepository(
        dayRouteDao: FakeDayRouteDao,
        gpsPointDao: FakeGpsPointDao,
        dispatcher: CoroutineDispatcher
    ): RoomDayRouteRepository {
        return RoomDayRouteRepository(
            dayRouteDao = dayRouteDao,
            gpsPointDao = gpsPointDao,
            dayRouteApi = FakeDayRouteApi,
            ioDispatcher = dispatcher,
            cpuDispatcher = dispatcher
        )
    }

    private class FakeDayRouteDao(initialRoute: DayRouteEntity?) : DayRouteDao {
        private val routeFlow = MutableStateFlow(initialRoute)
        var upsertedRoute: DayRouteEntity? = null
            private set

        override fun observeByDate(dateKey: String): Flow<DayRouteEntity?> = routeFlow

        override suspend fun getByDate(dateKey: String): DayRouteEntity? = routeFlow.value

        override suspend fun updateLastSyncedAt(dateKey: String, syncedAtEpochMillis: Long) = Unit

        override suspend fun getSyncedDateKeysOlderThan(cutoffDateKey: String): List<String> = emptyList()

        override suspend fun getUnsyncedDateKeysOlderThan(cutoffDateKey: String): List<String> = emptyList()

        override suspend fun deleteByDate(dateKey: String): Int {
            routeFlow.value = null
            return 1
        }

        override suspend fun upsert(route: DayRouteEntity) {
            upsertedRoute = route
            routeFlow.value = route
        }
    }

    private class FakeGpsPointDao(
        private val projections: List<GpsPointRouteProjection>
    ) : GpsPointDao {
        var projectionReadCount: Int = 0
            private set

        override suspend fun insert(point: GpsPointEntity): Long = point.recordedAtEpochMillis

        override fun observePointsByDate(dateKey: String): Flow<List<GpsPointEntity>> {
            return MutableStateFlow(emptyList())
        }

        override suspend fun getRoutePointProjectionsByDate(
            dateKey: String
        ): List<GpsPointRouteProjection> {
            projectionReadCount += 1
            return projections
        }

        override suspend fun getPointsByDate(dateKey: String): List<GpsPointEntity> = emptyList()

        override suspend fun getLatestPointByDate(dateKey: String): GpsPointEntity? = null

        override suspend fun getPendingUploadPoints(dateKey: String, limit: Int): List<GpsPointEntity> {
            return emptyList()
        }

        override suspend fun getPendingUploadPointCount(dateKey: String): Int = 0

        override suspend fun markUploaded(recordedAtEpochMillis: List<Long>) = Unit

        override suspend fun deleteByDate(dateKey: String): Int = 0
    }

    private object FakeDayRouteApi : DayRouteApi {
        override suspend fun uploadGpsPointsBatch(
            date: String,
            request: GpsPointBatchUploadRequestDto
        ): GpsPointBatchUploadResponseDto {
            error("Unexpected upload call")
        }

        override suspend fun getDayRoute(date: String): DayRouteDetailResponseDto {
            error("Unexpected route fetch call")
        }
    }

    private companion object {
        const val TestDateKey = "2026-04-01"
    }
}
