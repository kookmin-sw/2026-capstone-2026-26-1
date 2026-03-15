package com.example.passedpath.data.datastore


import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "auth")

object TokenDataStore {

    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val USER_ID = longPreferencesKey("user_id")
    private val NICKNAME = stringPreferencesKey("nickname")
    private val PROFILE_IMAGE_URL = stringPreferencesKey("profile_image_url")

    // accessToken + refreshToken 저장
    suspend fun saveTokens(
        context: Context,
        accessToken: String,
        refreshToken: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveUserProfile(
        context: Context,
        userId: Long,
        nickname: String,
        profileImageUrl: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = userId
            prefs[NICKNAME] = nickname
            prefs[PROFILE_IMAGE_URL] = profileImageUrl
        }
    }

    // accessToken 조회
    suspend fun getAccessToken(context: Context): String? {
        return context.dataStore.data.first()[ACCESS_TOKEN]
    }

    // refreshToken 조회
    suspend fun getRefreshToken(context: Context): String? {
        return context.dataStore.data.first()[REFRESH_TOKEN]
    }

    suspend fun getUserProfile(context: Context): UserProfile? {
        val preferences = context.dataStore.data.first()
        val userId = preferences[USER_ID] ?: return null
        val nickname = preferences[NICKNAME] ?: return null
        val profileImageUrl = preferences[PROFILE_IMAGE_URL] ?: return null

        return UserProfile(
            userId = userId,
            nickname = nickname,
            profileImageUrl = profileImageUrl
        )
    }


    // accessToken 삭제
    suspend fun clear(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN)
            prefs.remove(REFRESH_TOKEN)
            prefs.remove(USER_ID)
            prefs.remove(NICKNAME)
            prefs.remove(PROFILE_IMAGE_URL)
        }
    }
}
