package com.example.passedpath.data.network.api

import retrofit2.http.GET

interface TestApi {
    @GET("/test")
    suspend fun test(): String
}