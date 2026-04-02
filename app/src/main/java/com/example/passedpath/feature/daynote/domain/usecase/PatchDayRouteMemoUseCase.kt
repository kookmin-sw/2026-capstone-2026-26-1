package com.example.passedpath.feature.daynote.domain.usecase

import com.example.passedpath.feature.daynote.domain.model.DayRouteMemo
import com.example.passedpath.feature.daynote.domain.repository.DayRouteMemoRepository

class PatchDayRouteMemoUseCase(
    private val dayRouteMemoRepository: DayRouteMemoRepository
) {
    suspend operator fun invoke(dateKey: String, memo: String?): DayRouteMemo {
        return dayRouteMemoRepository.patchMemo(
            dateKey = dateKey,
            memo = memo
        )
    }
}
