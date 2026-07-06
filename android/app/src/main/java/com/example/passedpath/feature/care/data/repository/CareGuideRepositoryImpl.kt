package com.example.passedpath.feature.care.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.passedpath.feature.care.domain.repository.CareGuideRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.careGuideDataStore by preferencesDataStore(name = "care_guide")

class CareGuideRepositoryImpl(
    private val context: Context
) : CareGuideRepository {
    override val isProtectedPersonPlaceGuideBannerDismissed: Flow<Boolean> =
        context.careGuideDataStore.data.map { preferences ->
            preferences[ProtectedPersonPlaceGuideBannerDismissedKey] ?: false
        }

    override suspend fun dismissProtectedPersonPlaceGuideBanner() {
        context.careGuideDataStore.edit { preferences ->
            preferences[ProtectedPersonPlaceGuideBannerDismissedKey] = true
        }
    }

    private companion object {
        val ProtectedPersonPlaceGuideBannerDismissedKey =
            booleanPreferencesKey("protected_person_place_guide_banner_dismissed")
    }
}
