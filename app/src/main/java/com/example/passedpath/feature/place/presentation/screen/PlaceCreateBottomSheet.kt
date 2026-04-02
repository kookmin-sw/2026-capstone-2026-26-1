package com.example.passedpath.feature.place.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passedpath.R
import com.example.passedpath.app.appContainer
import com.example.passedpath.feature.place.presentation.viewmodel.PlaceViewModel
import com.example.passedpath.feature.place.presentation.viewmodel.PlaceViewModelFactory
import com.example.passedpath.ui.component.BaseButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceCreateBottomSheet(
    selectedDateKey: String,
    onDismiss: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaceViewModel = viewModel(
        factory = PlaceViewModelFactory(
            appContainer = LocalContext.current.appContainer,
            initialDateKey = selectedDateKey
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDateKey) {
        viewModel.updateDateKey(selectedDateKey)
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.resetForm()
            onCreated()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.place_sheet_add),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.place_sheet_selected_date, selectedDateKey),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.placeName,
                onValueChange = viewModel::updatePlaceName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.place_field_name)) },
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.roadAddress,
                onValueChange = viewModel::updateRoadAddress,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.place_field_road_address)) }
            )

            OutlinedTextField(
                value = uiState.latitude,
                onValueChange = viewModel::updateLatitude,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.place_field_latitude)) },
                placeholder = { Text("37.5665") },
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.longitude,
                onValueChange = viewModel::updateLongitude,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.place_field_longitude)) },
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

            BaseButton(
                text = stringResource(R.string.place_sheet_add),
                onClick = viewModel::submit,
                enabled = uiState.isSubmitEnabled
            )

            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .height(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
