package com.example.passedpath.feature.care.data.repository

import com.example.passedpath.feature.care.data.remote.datasource.CareDependentLocationStreamConnection
import com.example.passedpath.feature.care.data.remote.datasource.CareDependentLocationStreamListener
import com.example.passedpath.feature.care.data.remote.datasource.CareDependentLocationStreamRemoteDataSource
import com.example.passedpath.feature.care.domain.model.CareDependentLocationStreamEvent
import com.example.passedpath.feature.care.domain.model.CareLatestGpsPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CareDependentLocationStreamRepositoryImplTest {

    @Test
    fun `observeLocationStream forwards remote events and cancels connection on completion`() = runTest {
        val remoteDataSource = FakeCareDependentLocationStreamRemoteDataSource()
        val repository = CareDependentLocationStreamRepositoryImpl(
            remoteDataSource = remoteDataSource
        )
        val receivedEvents = mutableListOf<CareDependentLocationStreamEvent>()

        val job = launch {
            repository.observeLocationStream()
                .take(2)
                .toList(receivedEvents)
        }
        runCurrent()

        remoteDataSource.listener?.onEvent(CareDependentLocationStreamEvent.Connected("connected"))
        remoteDataSource.listener?.onEvent(locationUpdatedEvent())
        job.join()

        assertEquals(2, receivedEvents.size)
        assertTrue(receivedEvents[0] is CareDependentLocationStreamEvent.Connected)
        assertTrue(receivedEvents[1] is CareDependentLocationStreamEvent.LocationUpdated)
        assertEquals(1, remoteDataSource.cancelCount)
    }

    @Test
    fun `observeLocationStream emits Error and closes when remote fails`() = runTest {
        val remoteDataSource = FakeCareDependentLocationStreamRemoteDataSource()
        val repository = CareDependentLocationStreamRepositoryImpl(
            remoteDataSource = remoteDataSource
        )
        val receivedEvents = mutableListOf<CareDependentLocationStreamEvent>()
        val expected = IllegalStateException("stream failed")

        val job = launch {
            repository.observeLocationStream().toList(receivedEvents)
        }
        runCurrent()

        remoteDataSource.listener?.onFailure(expected)
        job.join()

        assertEquals(1, receivedEvents.size)
        val errorEvent = receivedEvents.single()
        assertTrue(errorEvent is CareDependentLocationStreamEvent.Error)
        assertEquals(expected, (errorEvent as CareDependentLocationStreamEvent.Error).throwable)
        assertEquals(1, remoteDataSource.cancelCount)
    }

    @Test
    fun `observeLocationStream cancels connection when collector is cancelled`() = runTest {
        val remoteDataSource = FakeCareDependentLocationStreamRemoteDataSource()
        val repository = CareDependentLocationStreamRepositoryImpl(
            remoteDataSource = remoteDataSource
        )

        val job = launch {
            repository.observeLocationStream().collect()
        }
        runCurrent()

        job.cancelAndJoin()

        assertEquals(1, remoteDataSource.cancelCount)
    }

    private class FakeCareDependentLocationStreamRemoteDataSource :
        CareDependentLocationStreamRemoteDataSource {
        var listener: CareDependentLocationStreamListener? = null
        var cancelCount: Int = 0

        override fun connect(
            listener: CareDependentLocationStreamListener
        ): CareDependentLocationStreamConnection {
            this.listener = listener
            return object : CareDependentLocationStreamConnection {
                override fun cancel() {
                    cancelCount += 1
                }
            }
        }
    }

    private companion object {
        fun locationUpdatedEvent(): CareDependentLocationStreamEvent.LocationUpdated {
            return CareDependentLocationStreamEvent.LocationUpdated(
                dependentUserId = 2L,
                latestGpsPoint = CareLatestGpsPoint(
                    latitude = 90.0,
                    longitude = 180.0,
                    recordedAt = "2026-05-11T20:56:45.991+09:00",
                    recordedAtEpochMillis = 1_747_050_000_000L
                )
            )
        }
    }
}
