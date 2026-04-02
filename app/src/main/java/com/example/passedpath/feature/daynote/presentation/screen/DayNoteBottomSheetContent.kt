package com.example.passedpath.feature.daynote.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.R
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.daynote.presentation.viewmodel.DayNoteViewModel
import com.example.passedpath.feature.daynote.presentation.viewmodel.DayNoteViewModelFactory

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

    val routeStatusMessage = when {
        isRouteLoading -> "선택한 날짜 기록을 불러오는 중입니다."
        routeErrorMessage != null -> "기존 기록을 불러오지 못했습니다. 새 값으로 저장할 수 있습니다."
        isRouteEmpty -> "이 날짜에는 기존 경로 기록이 없어 제목과 메모를 비운 상태로 시작합니다."
        else -> null
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.daynote_sheet_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "선택한 날짜 $selectedDateKey",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "제목과 메모는 PATCH overwrite semantics입니다. 빈값이나 공백도 서버에서 삭제로 간주될 수 있습니다.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        routeStatusMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedTextField(
            value = uiState.title,
            onValueChange = viewModel::updateTitle,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("제목") },
            placeholder = { Text("빈값이면 삭제 처리 가능") },
            singleLine = true
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = viewModel::submitTitle,
                enabled = uiState.isSubmitEnabled,
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("제목 저장")
                }
            }
            Button(
                onClick = viewModel::clearTitle,
                enabled = !uiState.isSubmitting,
                modifier = Modifier.weight(1f)
            ) {
                Text("제목 비우기")
            }
        }
        OutlinedTextField(
            value = uiState.memo,
            onValueChange = viewModel::updateMemo,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("메모") },
            placeholder = { Text("빈값이면 삭제 처리 가능") },
            minLines = 4
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = viewModel::submitMemo,
                enabled = uiState.isSubmitEnabled,
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("메모 저장")
                }
            }
            Button(
                onClick = viewModel::clearMemo,
                enabled = !uiState.isSubmitting,
                modifier = Modifier.weight(1f)
            ) {
                Text("메모 비우기")
            }
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        uiState.successMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
