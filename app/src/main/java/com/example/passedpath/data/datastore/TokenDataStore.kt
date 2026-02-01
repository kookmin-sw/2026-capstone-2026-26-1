package com.example.passedpath.data.datastore


import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "auth")

object TokenDataStore {

    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")

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

    // accessToken 조회
    suspend fun getAccessToken(context: Context): String? {
        return context.dataStore.data.first()[ACCESS_TOKEN]
    }

    // refreshToken 조회
    suspend fun getRefreshToken(context: Context): String? {
        return context.dataStore.data.first()[REFRESH_TOKEN]
    }


    // accessToken 삭제
    suspend fun clear(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN)
        }
    }


}
