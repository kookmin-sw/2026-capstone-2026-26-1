package com.example.passedpath.feature.place.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.ui.component.dialog.BaseConfirmDialog
import com.example.passedpath.ui.theme.Green50
import com.example.passedpath.ui.theme.Green500
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
            PlaceNamePill(placeName = placeName)
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Composable
private fun PlaceNamePill(
    placeName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Green50)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = placeName,
            color = Green500,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
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
