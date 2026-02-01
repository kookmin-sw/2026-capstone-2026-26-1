package com.example.passedpath.data.network

import android.content.Context
import com.example.passedpath.data.network.api.AuthApi
import com.example.passedpath.data.network.api.TestApi
import com.example.passedpath.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

// Restrofit 생성

object RetrofitClient {

    private const val BASE_URL = "http://15.165.115.7/"

    fun createTestApi(context: Context): TestApi {
        val authApi = createAuthApi(context) // refresh용 AuthApi

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context, authApi))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create()) // 먼저
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TestApi::class.java)
    }


    fun createAuthApi(context: Context): AuthApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val authApi = retrofit.create(AuthApi::class.java)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context, authApi))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)

            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}