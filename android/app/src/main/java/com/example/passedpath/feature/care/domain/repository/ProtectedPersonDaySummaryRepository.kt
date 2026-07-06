package com.example.passedpath.feature.care.domain.repository

import com.example.passedpath.feature.care.domain.model.ProtectedPersonDaySummary

sealed interface ProtectedPersonDaySummaryResult {
    data class Success(
        val daySummary: ProtectedPersonDaySummary
    ) : ProtectedPersonDaySummaryResult

    data object Empty : ProtectedPersonDaySummaryResult

    data class Error(
        val throwable: Throwable
    ) : ProtectedPersonDaySummaryResult
}

interface ProtectedPersonDaySummaryRepository {
    suspend fun fetchDaySummary(
        dependentUserId: Long,
        dateKey: String
    ): ProtectedPersonDaySummaryResult
}
