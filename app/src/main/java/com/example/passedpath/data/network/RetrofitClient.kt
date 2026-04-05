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
        authenticator: Authenticator? = null
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionStorage))
        authenticator?.let(builder::authenticator)
        return builder.build()
    }

    fun provideRetrofit(
        sessionStorage: AuthSessionStorage,
        authenticator: Authenticator? = null
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(provideOkHttpClient(sessionStorage, authenticator))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun providePlainRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
