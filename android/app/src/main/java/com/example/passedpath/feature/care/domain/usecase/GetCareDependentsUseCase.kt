package com.example.passedpath.feature.care.domain.usecase

import com.example.passedpath.feature.care.domain.model.CareDependentUserList
import com.example.passedpath.feature.care.domain.repository.CareDependentRepository

class GetCareDependentsUseCase(
    private val repository: CareDependentRepository
) {
    suspend operator fun invoke(): CareDependentUserList {
        return repository.getDependents()
    }
}
