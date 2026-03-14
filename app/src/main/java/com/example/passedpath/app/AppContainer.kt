package com.example.passedpath.app

import android.content.Context
import com.example.passedpath.data.datastore.AuthSessionStorage
import com.example.passedpath.data.network.RetrofitClient
import com.example.passedpath.feature.auth.data.manager.AuthTokenManager
import com.example.passedpath.feature.auth.data.remote.api.AuthApi
import com.example.passedpath.feature.auth.data.repository.AuthRepository
import com.example.passedpath.feature.main.data.repository.TestRepository
import com.example.passedpath.feature.permission.data.manager.LocationPermissionChecker

class AppContainer(
    context: Context
) {
    private val appContext = context.applicationContext

    val authSessionStorage: AuthSessionStorage by lazy {
        AuthSessionStorage(appContext)
    }

    val permissionChecker: LocationPermissionChecker by lazy {
        LocationPermissionChecker(appContext)
    }

    private val retrofit by lazy {
        RetrofitClient.provideRetrofit(authSessionStorage)
    }

    private val authApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    private val testApi by lazy {
        retrofit.create(com.example.passedpath.data.network.api.TestApi::class.java)
    }

    private val authTokenManager by lazy {
        AuthTokenManager(
            authApi = authApi,
            sessionStorage = authSessionStorage
        )
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(
            authApi = authApi,
            tokenManager = authTokenManager,
            sessionStorage = authSessionStorage
        )
    }

    val testRepository: TestRepository by lazy {
        TestRepository(testApi)
    }
}
