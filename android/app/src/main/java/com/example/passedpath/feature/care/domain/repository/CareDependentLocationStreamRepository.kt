package com.example.passedpath.feature.care.domain.repository

import com.example.passedpath.feature.care.domain.model.CareDependentLocationStreamEvent
import kotlinx.coroutines.flow.Flow

interface CareDependentLocationStreamRepository {
    fun observeLocationStream(): Flow<CareDependentLocationStreamEvent>
}
