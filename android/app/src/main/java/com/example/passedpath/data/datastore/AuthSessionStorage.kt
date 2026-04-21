package com.example.passedpath.data.datastore

import android.content.Context

class AuthSessionStorage(
    private val context: Context
) {
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String
    ) {
        TokenDataStore.saveTokens(
            context = context,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    suspend fun getAccessToken(): String? {
        return TokenDataStore.getAccessToken(context)
    }

    suspend fun getRefreshToken(): String? {
        return TokenDataStore.getRefreshToken(context)
    }

    suspend fun saveUserProfile(
        userId: Long,
        nickname: String,
        profileImageUrl: String?
    ) {
        TokenDataStore.saveUserProfile(
            context = context,
            userId = userId,
            nickname = nickname,
            profileImageUrl = profileImageUrl
        )
    }

    suspend fun getUserProfile(): UserProfile? {
        return TokenDataStore.getUserProfile(context)
    }

    suspend fun clear() {
        TokenDataStore.clear(context)
    }
}
