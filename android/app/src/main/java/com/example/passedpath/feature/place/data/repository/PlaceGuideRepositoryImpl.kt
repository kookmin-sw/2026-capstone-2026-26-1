package com.example.passedpath.feature.place.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.passedpath.feature.place.domain.repository.PlaceGuideRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.placeGuideDataStore by preferencesDataStore(name = "place_guide")

class PlaceGuideRepositoryImpl(
    private val context: Context
) : PlaceGuideRepository {
    override val isReorderGuideBannerDismissed: Flow<Boolean> =
        context.placeGuideDataStore.data.map { preferences ->
            preferences[ReorderGuideBannerDismissedKey] ?: false
        }

    override suspend fun dismissReorderGuideBanner() {
        context.placeGuideDataStore.edit { preferences ->
            preferences[ReorderGuideBannerDismissedKey] = true
        }
    }

    private companion object {
        val ReorderGuideBannerDismissedKey = booleanPreferencesKey("reorder_guide_banner_dismissed")
    }
}
