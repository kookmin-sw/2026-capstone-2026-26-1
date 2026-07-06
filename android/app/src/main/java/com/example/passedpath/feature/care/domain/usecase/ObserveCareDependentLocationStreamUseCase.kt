package com.example.passedpath.feature.care.domain.usecase

import com.example.passedpath.feature.care.domain.repository.CareDependentLocationStreamRepository

class ObserveCareDependentLocationStreamUseCase(
    private val repository: CareDependentLocationStreamRepository
) {
    operator fun invoke() = repository.observeLocationStream()
}
