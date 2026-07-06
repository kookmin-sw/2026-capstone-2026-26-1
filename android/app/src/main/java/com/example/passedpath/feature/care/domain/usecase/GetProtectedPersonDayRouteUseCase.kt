package com.example.passedpath.feature.care.domain.usecase

import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDayRouteRepository
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDayRouteResult

class GetProtectedPersonDayRouteUseCase(
    private val repository: ProtectedPersonDayRouteRepository
) {
    suspend operator fun invoke(
        dependentUserId: Long,
        dateKey: String
    ): ProtectedPersonDayRouteResult {
        return repository.fetchDayRoute(
            dependentUserId = dependentUserId,
            dateKey = dateKey
        )
    }
}
