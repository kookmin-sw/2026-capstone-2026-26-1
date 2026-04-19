package com.example.passedpath.data.network

import com.example.passedpath.BuildConfig
import com.example.passedpath.data.datastore.AuthSessionStorage
import com.example.passedpath.interceptor.AuthInterceptor
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    fun provideOkHttpClient(
        sessionStorage: AuthSessionStorage,
        attachAuthorizationToRefreshRequest: Boolean = false,
        authenticator: Authenticator? = null
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(
                AuthInterceptor(
                    sessionStorage = sessionStorage,
                    attachAuthorizationToRefreshRequest = attachAuthorizationToRefreshRequest
                )
            )
        authenticator?.let(builder::authenticator)
        return builder.build()
    }

    fun provideRetrofit(
        sessionStorage: AuthSessionStorage,
        attachAuthorizationToRefreshRequest: Boolean = false,
        authenticator: Authenticator? = null
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(
                provideOkHttpClient(
                    sessionStorage = sessionStorage,
                    attachAuthorizationToRefreshRequest = attachAuthorizationToRefreshRequest,
                    authenticator = authenticator
                )
            )
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
