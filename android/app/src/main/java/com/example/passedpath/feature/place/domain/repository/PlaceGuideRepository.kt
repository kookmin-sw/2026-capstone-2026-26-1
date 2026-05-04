package com.example.passedpath.feature.place.domain.repository

import kotlinx.coroutines.flow.Flow

interface PlaceGuideRepository {
    val isReorderGuideBannerDismissed: Flow<Boolean>

    suspend fun dismissReorderGuideBanner()
}
