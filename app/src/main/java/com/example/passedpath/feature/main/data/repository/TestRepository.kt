package com.example.passedpath.feature.main.data.repository

import com.example.passedpath.data.network.api.TestApi

class TestRepository(
    private val testApi: TestApi
) {
    suspend fun test(): String {
        return testApi.test()
    }
}
