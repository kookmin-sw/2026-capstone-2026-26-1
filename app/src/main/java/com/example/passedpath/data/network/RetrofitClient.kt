package com.example.passedpath.data.network

import com.example.passedpath.BuildConfig
import com.example.passedpath.data.datastore.AuthSessionStorage
import com.example.passedpath.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    fun provideOkHttpClient(sessionStorage: AuthSessionStorage): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionStorage))
            .build()
    }

    fun provideRetrofit(sessionStorage: AuthSessionStorage): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(provideOkHttpClient(sessionStorage))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
