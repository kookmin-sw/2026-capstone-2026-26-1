package com.example.passedpath.data.datastore

interface AuthTokenStore {
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String
    )

    suspend fun getAccessToken(): String?

    suspend fun getRefreshToken(): String?

    fun getCachedAccessToken(): String?

    fun getCachedRefreshToken(): String?

    suspend fun warmTokenCacheIfNeeded()

    suspend fun clear()
}
