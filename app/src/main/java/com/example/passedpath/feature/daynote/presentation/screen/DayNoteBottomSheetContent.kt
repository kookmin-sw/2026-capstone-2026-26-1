package com.example.passedpath.feature.daynote.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.daynote.presentation.viewmodel.DayNoteViewModel
import com.example.passedpath.feature.daynote.presentation.viewmodel.DayNoteViewModelFactory
import com.example.passedpath.ui.component.BaseInputField
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Green100
import com.example.passedpath.ui.theme.Green50
import com.example.passedpath.ui.theme.Green500

@Composable
fun DayNoteBottomSheetContent(
    selectedDateKey: String,
    initialTitle: String,
    initialMemo: String,
    isRouteLoading: Boolean,
    isRouteEmpty: Boolean,
    routeErrorMessage: String?,
    modifier: Modifier = Modifier,
    viewModel: DayNoteViewModel = viewModel(
        factory = DayNoteViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDateKey, initialTitle, initialMemo) {
        viewModel.syncSelectedDay(
            dateKey = selectedDateKey,
            title = initialTitle,
            memo = initialMemo
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        DayNoteFieldSection(
            label = "제목",
            placeholder = "오늘의 지나온 길을 짧게 남겨 보세요",
            value = uiState.title,
            onValueChange = viewModel::updateTitle,
            countText = "${uiState.titleCount}/${DayNoteViewModel.MAX_TITLE_LENGTH}",
            singleLine = true,
            minLines = 1,
            imeAction = ImeAction.Next
        )

        DayNoteFieldSection(
            label = "메모",
            placeholder = "기억하고 싶은 장면, 감정, 장소를 적어 보세요",
            value = uiState.memo,
            onValueChange = viewModel::updateMemo,
            countText = "${uiState.memoCount}/${DayNoteViewModel.MAX_MEMO_LENGTH}",
            singleLine = false,
            minLines = 6,
            imeAction = ImeAction.Default
        )

        uiState.errorMessage?.let { message ->
            InlineMessageCard(
                message = message,
                backgroundColor = Color(0xFFFFF1F2),
                borderColor = Color(0xFFFECDD3),
                textColor = Color(0xFFBE123C)
            )
        }

        uiState.successMessage?.let { message ->
            InlineMessageCard(
                message = message,
                backgroundColor = Green50,
                borderColor = Green100,
                textColor = Green500
            )
        }

        Button(
            onClick = viewModel::submitDayNote,
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

@Composable
private fun InlineMessageCard(
    message: String,
    backgroundColor: Color,
    borderColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, shape = RoundedCornerShape(18.dp))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
    }
}
