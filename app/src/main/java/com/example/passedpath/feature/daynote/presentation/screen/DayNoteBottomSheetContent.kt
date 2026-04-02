package com.example.passedpath.feature.daynote.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.passedpath.feature.daynote.presentation.state.DayNoteUiState
import com.example.passedpath.ui.component.BaseInputField
import com.example.passedpath.ui.component.toast.MessageToast
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Green500

@Composable
fun DayNoteBottomSheetContent(
    uiState: DayNoteUiState,
    onTitleChanged: (String) -> Unit,
    onMemoChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        DayNoteFieldSection(
            label = "제목",
            placeholder = "오늘의 지나온 길을 짧게 적어보세요.",
            value = uiState.title,
            onValueChange = onTitleChanged,
            countText = "${uiState.titleCount}/60",
            singleLine = true,
            minLines = 1,
            imeAction = ImeAction.Next
        )

        DayNoteFieldSection(
            label = "메모",
            placeholder = "기억하고 싶은 장면, 감정, 장소를 적어보세요.",
            value = uiState.memo,
            onValueChange = onMemoChanged,
            countText = "${uiState.memoCount}/1000",
            singleLine = false,
            minLines = 6,
            imeAction = ImeAction.Default
        )

        Button(
            onClick = onSaveClick,
            enabled = uiState.isSaveEnabled,
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Green500,
                disabledContainerColor = Gray100,
                contentColor = Color.White,
                disabledContentColor = Gray400
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.height(20.dp)
                )
            } else {
                Text(
                    text = if (uiState.isDirty) "저장하기" else "변경 사항 없음",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        val toastMessage = uiState.errorMessage ?: uiState.successMessage
        if (toastMessage != null) {
            MessageToast(
                message = toastMessage,
                triggerKey = "${uiState.feedbackEventId}:$toastMessage",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun DayNoteFieldSection(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    countText: String,
    singleLine: Boolean,
    minLines: Int,
    imeAction: ImeAction
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = countText,
                style = MaterialTheme.typography.bodySmall,
                color = Gray400
            )
        }
        BaseInputField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            singleLine = singleLine,
            minLines = minLines,
            imeAction = imeAction
        )
    }
}
