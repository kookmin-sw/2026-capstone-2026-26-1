package com.example.passedpath.feature.place.presentation.screen

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.R
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.place.presentation.viewmodel.PlaceViewModel
import com.example.passedpath.feature.place.presentation.viewmodel.PlaceViewModelFactory

@Composable
fun PlaceBottomSheetContent(
    modifier: Modifier = Modifier,
    viewModel: PlaceViewModel = viewModel(
        factory = PlaceViewModelFactory(LocalContext.current.appContainer)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val submitLabel = if (uiState.isCreateMode) "장소 등록" else "장소 수정"

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.place_sheet_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "placeId가 비어 있으면 등록, 값이 있으면 수정입니다. 삭제는 placeId만 있어도 가능합니다.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "main selected date가 아직 연결되지 않아 날짜를 직접 입력합니다.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = uiState.dateKey,
            onValueChange = viewModel::updateDateKey,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("날짜") },
            placeholder = { Text("yyyy-MM-dd") },
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.placeId,
            onValueChange = viewModel::updatePlaceId,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("placeId") },
            placeholder = { Text("비우면 등록") },
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.placeName,
            onValueChange = viewModel::updatePlaceName,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("장소명") },
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.roadAddress,
            onValueChange = viewModel::updateRoadAddress,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("도로명 주소") }
        )
        OutlinedTextField(
            value = uiState.latitude,
            onValueChange = viewModel::updateLatitude,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("위도") },
            placeholder = { Text("37.5665") },
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.longitude,
            onValueChange = viewModel::updateLongitude,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("경도") },
            placeholder = { Text("126.9780") },
            singleLine = true
        )

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = viewModel::submit,
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
                    Text(submitLabel)
                }
            }
            Button(
                onClick = viewModel::deletePlace,
                enabled = uiState.isDeleteEnabled,
                modifier = Modifier.weight(1f)
            ) {
                Text("장소 삭제")
            }
        }

        Button(
            onClick = viewModel::resetForm,
            enabled = !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("입력 초기화")
        }
    }
}
