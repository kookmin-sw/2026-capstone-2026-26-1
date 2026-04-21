package com.example.passedpath.feature.daynote.domain.usecase

import com.example.passedpath.feature.daynote.domain.model.DayRouteTitle
import com.example.passedpath.feature.daynote.domain.repository.DayRouteTitleRepository

class PatchDayRouteTitleUseCase(
    private val dayRouteTitleRepository: DayRouteTitleRepository
) {
    suspend operator fun invoke(dateKey: String, title: String?): DayRouteTitle {
        return dayRouteTitleRepository.patchTitle(
            dateKey = dateKey,
            title = title
        )
    }
}
