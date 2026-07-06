package com.example.passedpath.feature.care.domain.repository

import kotlinx.coroutines.flow.Flow

interface CareGuideRepository {
    val isProtectedPersonPlaceGuideBannerDismissed: Flow<Boolean>

    suspend fun dismissProtectedPersonPlaceGuideBanner()
}
