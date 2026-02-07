package com.example.passedpath.data.network

import android.content.Context
import com.example.passedpath.data.network.api.AuthApi
import com.example.passedpath.data.network.api.TestApi
import com.example.passedpath.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Restrofit 생성

object RetrofitClient {

    private const val BASE_URL = "https://passedpath.site/"

    // 공통 OkHttpClient 생성 (인증 인터셉터 포함)
    fun provideOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()
    }

    // 공통 Retrofit 인스턴스 생성
    fun provideRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(provideOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 인증 관련 API
    fun authApi(context: Context): AuthApi {
        return provideRetrofit(context).create(AuthApi::class.java)
    }

    // 테스트/일반 API
    fun testApi(context: Context): TestApi {
        return provideRetrofit(context).create(TestApi::class.java)
    }
}