package com.example.passedpath.feature.daynote.domain.repository

import com.example.passedpath.feature.daynote.domain.model.DayRouteTitle

interface DayRouteTitleRepository {
    suspend fun patchTitle(dateKey: String, title: String?): DayRouteTitle
}
