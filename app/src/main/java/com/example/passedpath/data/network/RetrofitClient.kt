package com.example.passedpath.data.network

import com.example.passedpath.data.datastore.AuthSessionStorage
import com.example.passedpath.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://passedpath.site/"

    fun provideOkHttpClient(sessionStorage: AuthSessionStorage): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionStorage))
            .build()
    }

    fun provideRetrofit(sessionStorage: AuthSessionStorage): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(provideOkHttpClient(sessionStorage))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
