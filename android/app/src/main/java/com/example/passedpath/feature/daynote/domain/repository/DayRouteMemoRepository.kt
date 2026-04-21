package com.example.passedpath.feature.daynote.domain.repository

import com.example.passedpath.feature.daynote.domain.model.DayRouteMemo

interface DayRouteMemoRepository {
    suspend fun patchMemo(dateKey: String, memo: String?): DayRouteMemo
}
