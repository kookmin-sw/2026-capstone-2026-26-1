package com.example.passedpath.feature.care.data.repository

import com.example.passedpath.feature.care.data.remote.datasource.CareDependentLocationStreamListener
import com.example.passedpath.feature.care.data.remote.datasource.CareDependentLocationStreamRemoteDataSource
import com.example.passedpath.feature.care.domain.model.CareDependentLocationStreamEvent
import com.example.passedpath.feature.care.domain.repository.CareDependentLocationStreamRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class CareDependentLocationStreamRepositoryImpl(
    private val remoteDataSource: CareDependentLocationStreamRemoteDataSource
) : CareDependentLocationStreamRepository {
    override fun observeLocationStream(): Flow<CareDependentLocationStreamEvent> {
        return callbackFlow {
            val connection = remoteDataSource.connect(
                listener = object : CareDependentLocationStreamListener {
                    override fun onEvent(event: CareDependentLocationStreamEvent) {
                        trySend(event)
                    }

                    override fun onFailure(throwable: Throwable) {
                        trySend(CareDependentLocationStreamEvent.Error(throwable))
                        close()
                    }
                }
            )

            awaitClose {
                connection.cancel()
            }
        }
    }
}
