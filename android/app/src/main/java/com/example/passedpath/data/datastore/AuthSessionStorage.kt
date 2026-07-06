package com.example.passedpath.data.datastore

import android.content.Context

class AuthSessionStorage(
    private val context: Context
) : AuthTokenStore {
    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var cachedRefreshToken: String? = null

    override suspend fun saveTokens(
        accessToken: String,
        refreshToken: String
    ) {
        TokenDataStore.saveTokens(
            context = context,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
        cachedAccessToken = accessToken
        cachedRefreshToken = refreshToken
    }

    override suspend fun getAccessToken(): String? {
        val accessToken = TokenDataStore.getAccessToken(context)
        cachedAccessToken = accessToken
        return accessToken
    }

    override suspend fun getRefreshToken(): String? {
        val refreshToken = TokenDataStore.getRefreshToken(context)
        cachedRefreshToken = refreshToken
        return refreshToken
    }

    override fun getCachedAccessToken(): String? {
        return cachedAccessToken
    }

    override fun getCachedRefreshToken(): String? {
        return cachedRefreshToken
    }

    override suspend fun warmTokenCacheIfNeeded() {
        if (!cachedAccessToken.isNullOrBlank() && !cachedRefreshToken.isNullOrBlank()) {
            return
        }

        cachedAccessToken = TokenDataStore.getAccessToken(context)
        cachedRefreshToken = TokenDataStore.getRefreshToken(context)
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

    override suspend fun clear() {
        TokenDataStore.clear(context)
        cachedAccessToken = null
        cachedRefreshToken = null
    }
}
