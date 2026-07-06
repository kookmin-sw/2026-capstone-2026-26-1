package com.example.passedpath.feature.care.data.repository

import com.example.passedpath.feature.care.data.remote.api.CareDependentApi
import com.example.passedpath.feature.care.data.remote.mapper.toCareDependentUserList
import com.example.passedpath.feature.care.domain.model.CareDependentUserList
import com.example.passedpath.feature.care.domain.repository.CareDependentRepository

class CareDependentRepositoryImpl(
    private val careDependentApi: CareDependentApi
) : CareDependentRepository {
    override suspend fun getDependents(): CareDependentUserList {
        return careDependentApi.getDependents().toCareDependentUserList()
    }
}
