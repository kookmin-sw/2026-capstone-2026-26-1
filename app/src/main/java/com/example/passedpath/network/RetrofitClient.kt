package com.example.passedpath.network

import android.content.Context
import com.example.passedpath.interceptor.AuthInterceptor
import com.example.passedpath.network.api.AuthApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Restrofit 생성

object RetrofitClient {

    private const val BASE_URL = "http://15.165.115.7/"

    fun createAuthApi(context: Context): AuthApi {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(AuthApi::class.java)
    }
}