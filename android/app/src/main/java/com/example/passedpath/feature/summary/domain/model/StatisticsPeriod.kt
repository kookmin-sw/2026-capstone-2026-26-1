package com.example.passedpath.feature.summary.domain.model

enum class StatisticsPeriod(
    val apiValue: String
) {
    WEEK("WEEK"),
    MONTH("MONTH"),
    SIX_MONTHS("SIX_MONTHS"),
    YEAR("YEAR");

    companion object {
        fun fromApiValue(value: String?): StatisticsPeriod {
            return entries.firstOrNull { period -> period.apiValue == value } ?: WEEK
        }
    }
}
