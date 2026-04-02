package com.example.passedpath.feature.daynote.presentation.state

data class DayNoteUiState(
    val dateKey: String = "",
    val originalTitle: String = "",
    val originalMemo: String = "",
    val title: String = "",
    val memo: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val normalizedOriginalTitle: String
        get() = originalTitle.trim()

    val normalizedOriginalMemo: String
        get() = originalMemo.trim()

    val normalizedTitle: String
        get() = title.trim()

    val normalizedMemo: String
        get() = memo.trim()

    val titleCount: Int
        get() = title.length

    val memoCount: Int
        get() = memo.length

    val isDirty: Boolean
        get() = normalizedTitle != normalizedOriginalTitle || normalizedMemo != normalizedOriginalMemo

    val isSaveEnabled: Boolean
        get() = !isSubmitting && dateKey.isNotBlank() && isDirty
}
