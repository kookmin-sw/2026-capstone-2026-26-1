package com.example.passedpath.feature.locationtracking.data.local.mapper

import com.example.passedpath.feature.locationtracking.data.local.entity.DayRouteEntity
import com.example.passedpath.feature.locationtracking.data.local.model.GpsPointRouteProjection
import com.example.passedpath.feature.locationtracking.domain.model.TrackedLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingLocalMapperTest {

    @Test
    fun `route snapshot uses existing day route summary before projected point count`() {
        val points = listOf(
            GpsPointRouteProjection(
                recordedAtEpochMillis = 1L,
                latitude = 37.1,
                longitude = 127.1,
                accuracyMeters = 5f
            ),
            GpsPointRouteProjection(
                recordedAtEpochMillis = 2L,
                latitude = 37.2,
                longitude = 127.2,
                accuracyMeters = 8f
            )
        )
        val route = DayRouteEntity(
            dateKey = "2026-04-01",
            totalDistanceMeters = 1234.0,
            pathPointCount = 99,
            lastRecordedAtEpochMillis = 2L,
            lastSyncedAtEpochMillis = null
        )

        val snapshot = points.toLocalDayRouteSnapshot(
            dateKey = "2026-04-01",
            existingRoute = route
        )

        assertEquals("2026-04-01", snapshot.dateKey)
        assertEquals(2, snapshot.points.size)
        assertEquals(1234.0, snapshot.totalDistanceMeters, 0.0)
        assertEquals(99, snapshot.pathPointCount)
        assertEquals(1L, snapshot.points.first().recordedAtEpochMillis)
        assertEquals(2L, snapshot.points.last().recordedAtEpochMillis)
    }

    @Test
    fun `fresh cache maps directly to route snapshot`() {
        val cachePoints = listOf(
            TrackedLocation(37.1, 127.1, 5f, 1L),
            TrackedLocation(37.2, 127.2, 5f, 2L)
        )
        val route = DayRouteEntity(
            dateKey = "2026-04-01",
            totalDistanceMeters = 1234.0,
            pathPointCount = 2,
            lastRecordedAtEpochMillis = 2L,
            lastSyncedAtEpochMillis = null,
            mapPolylineCacheJson = cachePoints.toMapPolylineCacheJson(),
            mapPolylineCacheSourcePointCount = 2
        )

        val snapshot = route.toLocalDayRouteSnapshotFromCache()

        requireNotNull(snapshot)
        assertEquals("2026-04-01", snapshot.dateKey)
        assertEquals(2, snapshot.points.size)
        assertEquals(1234.0, snapshot.totalDistanceMeters, 0.0)
        assertEquals(2, snapshot.pathPointCount)
        assertEquals(1L, snapshot.points.first().recordedAtEpochMillis)
        assertEquals(2L, snapshot.points.last().recordedAtEpochMillis)
    }

    @Test
    fun `stale cache is not mapped to route snapshot`() {
        val route = DayRouteEntity(
            dateKey = "2026-04-01",
            totalDistanceMeters = 1234.0,
            pathPointCount = 2,
            lastRecordedAtEpochMillis = 2L,
            lastSyncedAtEpochMillis = null,
            mapPolylineCacheJson = listOf(
                TrackedLocation(37.1, 127.1, 5f, 1L)
            ).toMapPolylineCacheJson(),
            mapPolylineCacheSourcePointCount = 1
        )

        assertNull(route.toLocalDayRouteSnapshotFromCache())
    }

    @Test
    fun `invalid cache json is not mapped to route snapshot`() {
        val route = DayRouteEntity(
            dateKey = "2026-04-01",
            totalDistanceMeters = 1234.0,
            pathPointCount = 2,
            lastRecordedAtEpochMillis = 2L,
            lastSyncedAtEpochMillis = null,
            mapPolylineCacheJson = "not-json",
            mapPolylineCacheSourcePointCount = 2
        )

        assertNull(route.toLocalDayRouteSnapshotFromCache())
    }

    @Test
    fun `route snapshot falls back to projected point summary when route row is missing`() {
        val points = listOf(
            GpsPointRouteProjection(
                recordedAtEpochMillis = 10L,
                latitude = 37.1,
                longitude = 127.1,
                accuracyMeters = null
            )
        )

        val snapshot = points.toLocalDayRouteSnapshot(
            dateKey = "2026-04-01",
            existingRoute = null
        )

        assertEquals(1, snapshot.pathPointCount)
        assertEquals(0.0, snapshot.totalDistanceMeters, 0.0)
        assertEquals(10L, snapshot.points.single().recordedAtEpochMillis)
    }

    @Test
    fun `long route snapshot stores display points under render cap while preserving summary`() {
        val points = (0 until 1200).map { index ->
            GpsPointRouteProjection(
                recordedAtEpochMillis = index.toLong(),
                latitude = 37.0 + index * 0.00001,
                longitude = 127.0 + if (index % 2 == 0) 0.0 else 0.0002,
                accuracyMeters = 5f
            )
        }
        val route = DayRouteEntity(
            dateKey = "2026-04-01",
            totalDistanceMeters = 12_000.0,
            pathPointCount = 1200,
            lastRecordedAtEpochMillis = 1199L,
            lastSyncedAtEpochMillis = null
        )

        val snapshot = points.toLocalDayRouteSnapshot(
            dateKey = "2026-04-01",
            existingRoute = route
        )

        assertTrue(snapshot.points.size <= 800)
        assertEquals(0L, snapshot.points.first().recordedAtEpochMillis)
        assertEquals(1199L, snapshot.points.last().recordedAtEpochMillis)
        assertEquals(1200, snapshot.pathPointCount)
        assertEquals(12_000.0, snapshot.totalDistanceMeters, 0.0)
    }

    @Test
    fun `location save appends fresh cache and updates source count`() {
        val previousCache = listOf(
            TrackedLocation(37.1, 127.1, 5f, 1L),
            TrackedLocation(37.2, 127.2, 5f, 2L)
        )
        val previousRoute = DayRouteEntity(
            dateKey = "2026-04-01",
            totalDistanceMeters = 100.0,
            pathPointCount = 2,
            lastRecordedAtEpochMillis = 2L,
            lastSyncedAtEpochMillis = null,
            mapPolylineCacheJson = previousCache.toMapPolylineCacheJson(),
            mapPolylineCacheSourcePointCount = 2
        )

        val updatedRoute = previousRoute.toUpdatedDayRouteEntity(
            dateKey = "2026-04-01",
            newPoint = TrackedLocation(37.3, 127.3, 5f, 3L),
            previousPoint = TrackedLocation(37.2, 127.2, 5f, 2L)
        )

        val updatedCache = updatedRoute.decodeFreshMapPolylineCacheOrNull()
        requireNotNull(updatedCache)
        assertEquals(3, updatedRoute.mapPolylineCacheSourcePointCount)
        assertEquals(3, updatedRoute.pathPointCount)
        assertEquals(1L, updatedCache.first().recordedAtEpochMillis)
        assertEquals(3L, updatedCache.last().recordedAtEpochMillis)
    }

    @Test
    fun `location save does not append stale cache`() {
        val previousRoute = DayRouteEntity(
            dateKey = "2026-04-01",
            totalDistanceMeters = 100.0,
            pathPointCount = 2,
            lastRecordedAtEpochMillis = 2L,
            lastSyncedAtEpochMillis = null,
            mapPolylineCacheJson = listOf(
                TrackedLocation(37.1, 127.1, 5f, 1L)
            ).toMapPolylineCacheJson(),
            mapPolylineCacheSourcePointCount = 1
        )

        val updatedRoute = previousRoute.toUpdatedDayRouteEntity(
            dateKey = "2026-04-01",
            newPoint = TrackedLocation(37.3, 127.3, 5f, 3L),
            previousPoint = TrackedLocation(37.2, 127.2, 5f, 2L)
        )

        assertEquals(3, updatedRoute.pathPointCount)
        assertEquals(1, updatedRoute.mapPolylineCacheSourcePointCount)
        assertNull(updatedRoute.decodeFreshMapPolylineCacheOrNull())
    }
}
