package com.example.passedpath.feature.care.domain.usecase

import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDaySummaryRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDaySummaryResult

class GetProtectedPersonDaySummaryUseCase(
    private val repository: ProtectedPersonDaySummaryRepository
) {
    suspend operator fun invoke(
        dependentUserId: Long,
        dateKey: String
    ): ProtectedPersonDaySummaryResult {
        return repository.fetchDaySummary(
            dependentUserId = dependentUserId,
            dateKey = dateKey
        )
    }
}
