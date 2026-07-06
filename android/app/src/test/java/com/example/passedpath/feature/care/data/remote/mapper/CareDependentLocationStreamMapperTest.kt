package com.example.passedpath.feature.care.data.remote.mapper

import com.example.passedpath.feature.care.domain.model.CareDependentLocationStreamEvent
import java.time.OffsetDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CareDependentLocationStreamMapperTest {

    @Test
    fun `toCareDependentLocationStreamEventOrNull maps connected event`() {
        val result = """{"message":"connected"}"""
            .toCareDependentLocationStreamEventOrNull(eventType = "connected")

        assertTrue(result is CareDependentLocationStreamEvent.Connected)
        assertEquals(
            "connected",
            (result as CareDependentLocationStreamEvent.Connected).message
        )
    }

    @Test
    fun `toCareDependentLocationStreamEventOrNull ignores heartbeat event`() {
        val result = """{"message":"keep-alive"}"""
            .toCareDependentLocationStreamEventOrNull(eventType = "heartbeat")

        assertNull(result)
    }

    @Test
    fun `toCareDependentLocationStreamEventOrNull maps location updated event`() {
        val recordedAt = "2026-05-11T20:56:45.991+09:00"
        val result = """
            {
              "dependentUserId":2,
              "latestGpsPoint":{
                "latitude":90.0,
                "longitude":180.0,
                "recordedAt":"$recordedAt"
              }
            }
        """.trimIndent()
            .toCareDependentLocationStreamEventOrNull(eventType = "location-updated")

        assertTrue(result is CareDependentLocationStreamEvent.LocationUpdated)
        val event = result as CareDependentLocationStreamEvent.LocationUpdated
        assertEquals(2L, event.dependentUserId)
        assertEquals(90.0, event.latestGpsPoint.latitude, 0.0)
        assertEquals(180.0, event.latestGpsPoint.longitude, 0.0)
        assertEquals(recordedAt, event.latestGpsPoint.recordedAt)
        assertEquals(
            OffsetDateTime.parse(recordedAt).toInstant().toEpochMilli(),
            event.latestGpsPoint.recordedAtEpochMillis
        )
    }

    @Test
    fun `toCareDependentLocationStreamEventOrNull ignores unknown and malformed events`() {
        val missingCoordinate = """
            {
              "dependentUserId":2,
              "latestGpsPoint":{"latitude":90.0}
            }
        """.trimIndent()

        assertNull("""{"message":"ignored"}""".toCareDependentLocationStreamEventOrNull("unknown"))
        assertNull("not-json".toCareDependentLocationStreamEventOrNull("location-updated"))
        assertNull(missingCoordinate.toCareDependentLocationStreamEventOrNull("location-updated"))
    }
}
