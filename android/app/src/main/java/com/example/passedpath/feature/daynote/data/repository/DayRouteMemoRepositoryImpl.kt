package com.example.passedpath.feature.daynote.data.repository

import com.example.passedpath.feature.daynote.data.remote.api.DayRouteMemoApi
import com.example.passedpath.feature.daynote.data.remote.dto.DayRouteMemoRequestDto
import com.example.passedpath.feature.daynote.domain.model.DayRouteMemo
import com.example.passedpath.feature.daynote.domain.repository.DayRouteMemoRepository

class DayRouteMemoRepositoryImpl(
    private val dayRouteMemoApi: DayRouteMemoApi
) : DayRouteMemoRepository {
    override suspend fun patchMemo(dateKey: String, memo: String?): DayRouteMemo {
        val response = dayRouteMemoApi.patchMemo(
            date = dateKey,
            request = DayRouteMemoRequestDto(memo = memo)
        )
        return DayRouteMemo(memo = response.memo)
    }
}
