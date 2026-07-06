package com.example.passedpath.feature.care.domain.usecase

import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDayRouteListResult
import com.example.passedpath.feature.care.domain.repository.ProtectedPersonDayRouteRepository

class GetProtectedPersonDayRouteListUseCase(
    private val repository: ProtectedPersonDayRouteRepository
) {
    suspend operator fun invoke(
        dependentUserId: Long,
        cursorDate: String? = null,
        size: Int? = null
    ): ProtectedPersonDayRouteListResult {
        return repository.fetchDayRoutes(
            dependentUserId = dependentUserId,
            cursorDate = cursorDate,
            size = size
        )
    }
}
