package com.example.passedpath.feature.place.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.passedpath.ui.component.dialog.BaseConfirmDialog
import com.example.passedpath.ui.component.place.PlaceNameMarker
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun PlaceDeleteConfirmDialog(
    placeName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    BaseConfirmDialog(
        title = "이 장소를 삭제할까요?",
        message = "선택한 날짜에서 이 장소만 삭제돼요",
        dismissText = "취소",
        confirmText = "삭제",
        topContent = {
            PlaceNameMarker(placeName = placeName)
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun PlaceDeleteConfirmDialogPreview() {
    PassedPathTheme {
        PlaceDeleteConfirmDialog(
            placeName = "국민대학교 복지관",
            onDismiss = {},
            onConfirm = {}
        )
    }
}
