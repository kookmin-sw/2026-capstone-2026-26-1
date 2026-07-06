package com.example.passedpath.feature.care.domain.repository

import com.example.passedpath.feature.care.domain.model.CareDependentUserList

interface CareDependentRepository {
    suspend fun getDependents(): CareDependentUserList
}
