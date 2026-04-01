package com.example.passedpath.feature.daynote.data.repository

import com.example.passedpath.feature.daynote.data.remote.api.DayRouteTitleApi
import com.example.passedpath.feature.daynote.data.remote.dto.DayRouteTitleRequestDto
import com.example.passedpath.feature.daynote.domain.model.DayRouteTitle
import com.example.passedpath.feature.daynote.domain.repository.DayRouteTitleRepository

class DayRouteTitleRepositoryImpl(
    private val dayRouteTitleApi: DayRouteTitleApi
) : DayRouteTitleRepository {
    override suspend fun patchTitle(dateKey: String, title: String?): DayRouteTitle {
        val response = dayRouteTitleApi.patchTitle(
            date = dateKey,
            request = DayRouteTitleRequestDto(title = title)
        )
        return DayRouteTitle(title = response.title)
    }
}
