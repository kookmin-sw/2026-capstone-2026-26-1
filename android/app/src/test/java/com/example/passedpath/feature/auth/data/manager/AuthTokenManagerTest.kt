package com.example.passedpath.feature.auth.data.manager

import com.example.passedpath.data.datastore.AuthTokenStore
import com.example.passedpath.feature.auth.data.remote.api.AuthApi
import com.example.passedpath.feature.auth.data.remote.dto.KakaoLoginRequest
import com.example.passedpath.feature.auth.data.remote.dto.KakaoLoginResponse
import com.example.passedpath.feature.auth.data.remote.dto.RefreshTokenResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthTokenManagerTest {

    @Test
    fun `recover uses newer cached token without refresh`() = runTest {
        val authApi = FakeAuthApi()
        val tokenStore = FakeAuthTokenStore(
            storedAccessToken = "new-access",
            cachedAccessToken = "new-access",
            cachedRefreshToken = "refresh"
        )
        val manager = AuthTokenManager(
            authApi = authApi,
            tokenStore = tokenStore
        )

        val result = manager.recoverAccessTokenAfterUnauthorized("old-access")

        assertEquals("new-access", result)
        assertEquals(0, authApi.refreshCalls)
    }

    @Test
    fun `recover refreshes when request token is latest token`() = runTest {
        val authApi = FakeAuthApi(
            refreshResponse = RefreshTokenResponse(
                accessToken = "new-access",
                refreshToken = "new-refresh"
            )
        )
        val tokenStore = FakeAuthTokenStore(
            storedAccessToken = "old-access",
            storedRefreshToken = "old-refresh",
            cachedAccessToken = "old-access",
            cachedRefreshToken = "old-refresh"
        )
        val manager = AuthTokenManager(
            authApi = authApi,
            tokenStore = tokenStore
        )

        val result = manager.recoverAccessTokenAfterUnauthorized("old-access")

        assertEquals("new-access", result)
        assertEquals(listOf("old-refresh"), authApi.refreshTokens)
        assertEquals("new-access", tokenStore.getCachedAccessToken())
        assertEquals("new-refresh", tokenStore.getCachedRefreshToken())
    }

    @Test
    fun `recover falls back to stored refresh token when cache is empty`() = runTest {
        val authApi = FakeAuthApi(
            refreshResponse = RefreshTokenResponse(
                accessToken = "new-access",
                refreshToken = "new-refresh"
            )
        )
        val tokenStore = FakeAuthTokenStore(
            storedAccessToken = "old-access",
            storedRefreshToken = "stored-refresh",
            cachedAccessToken = "old-access",
            cachedRefreshToken = null
        )
        val manager = AuthTokenManager(
            authApi = authApi,
            tokenStore = tokenStore
        )

        val result = manager.recoverAccessTokenAfterUnauthorized("old-access")

        assertEquals("new-access", result)
        assertEquals(listOf("stored-refresh"), authApi.refreshTokens)
        assertEquals(1, tokenStore.getRefreshTokenCalls)
    }

    @Test
    fun `refresh failure clears tokens and returns null`() = runTest {
        val authApi = FakeAuthApi(
            refreshThrowable = IllegalStateException("refresh failed")
        )
        val tokenStore = FakeAuthTokenStore(
            storedAccessToken = "old-access",
            storedRefreshToken = "old-refresh",
            cachedAccessToken = "old-access",
            cachedRefreshToken = "old-refresh"
        )
        val manager = AuthTokenManager(
            authApi = authApi,
            tokenStore = tokenStore
        )

        val result = manager.recoverAccessTokenAfterUnauthorized("old-access")

        assertNull(result)
        assertNull(tokenStore.getCachedAccessToken())
        assertNull(tokenStore.getCachedRefreshToken())
        assertEquals(1, tokenStore.clearCalls)
    }

    @Test
    fun `warm token cache fills cached tokens from stored tokens`() = runTest {
        val tokenStore = FakeAuthTokenStore(
            storedAccessToken = "stored-access",
            storedRefreshToken = "stored-refresh"
        )

        tokenStore.warmTokenCacheIfNeeded()

        assertEquals("stored-access", tokenStore.getCachedAccessToken())
        assertEquals("stored-refresh", tokenStore.getCachedRefreshToken())
        assertEquals(1, tokenStore.warmStoredReadCalls)
    }

    @Test
    fun `warm token cache skips stored read when cache is already warm`() = runTest {
        val tokenStore = FakeAuthTokenStore(
            storedAccessToken = "stored-access",
            storedRefreshToken = "stored-refresh",
            cachedAccessToken = "cached-access",
            cachedRefreshToken = "cached-refresh"
        )

        tokenStore.warmTokenCacheIfNeeded()

        assertEquals("cached-access", tokenStore.getCachedAccessToken())
        assertEquals("cached-refresh", tokenStore.getCachedRefreshToken())
        assertEquals(0, tokenStore.warmStoredReadCalls)
    }

    private class FakeAuthApi(
        private val refreshResponse: RefreshTokenResponse = RefreshTokenResponse(
            accessToken = "access",
            refreshToken = "refresh"
        ),
        private val refreshThrowable: Throwable? = null
    ) : AuthApi {
        val refreshTokens = mutableListOf<String>()

        val refreshCalls: Int
            get() = refreshTokens.size

        override suspend fun loginWithKakao(request: KakaoLoginRequest): KakaoLoginResponse {
            error("loginWithKakao is not used in AuthTokenManagerTest")
        }

        override suspend fun refreshToken(refreshToken: String): RefreshTokenResponse {
            refreshTokens += refreshToken
            refreshThrowable?.let { throwable -> throw throwable }
            return refreshResponse
        }
    }

    private class FakeAuthTokenStore(
        private var storedAccessToken: String? = null,
        private var storedRefreshToken: String? = null,
        private var cachedAccessToken: String? = null,
        private var cachedRefreshToken: String? = null
    ) : AuthTokenStore {
        var getRefreshTokenCalls = 0
            private set

        var clearCalls = 0
            private set

        var warmStoredReadCalls = 0
            private set

        override suspend fun saveTokens(
            accessToken: String,
            refreshToken: String
        ) {
            storedAccessToken = accessToken
            storedRefreshToken = refreshToken
            cachedAccessToken = accessToken
            cachedRefreshToken = refreshToken
        }

        override suspend fun getAccessToken(): String? {
            cachedAccessToken = storedAccessToken
            return storedAccessToken
        }

        override suspend fun getRefreshToken(): String? {
            getRefreshTokenCalls++
            cachedRefreshToken = storedRefreshToken
            return storedRefreshToken
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

            warmStoredReadCalls++
            cachedAccessToken = storedAccessToken
            cachedRefreshToken = storedRefreshToken
        }

        override suspend fun clear() {
            clearCalls++
            storedAccessToken = null
            storedRefreshToken = null
            cachedAccessToken = null
            cachedRefreshToken = null
        }
    }
}
