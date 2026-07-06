package com.example.passedpath.feature.place.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.place.presentation.component.ReorderGuideBanner
import com.example.passedpath.feature.place.presentation.state.PlaceListUiState
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray700

@Composable
internal fun PlaceBottomSheetHeader(
    selectedDateKey: String,
    placeListUiState: PlaceListUiState,
    isBannerVisible: Boolean,
    onCloseBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (isBannerVisible) {
            ReorderGuideBanner(onClickClose = onCloseBanner)
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.place_sheet_selected_date, selectedDateKey),
                style = MaterialTheme.typography.bodySmall,
                color = Gray400
            )
            Text(
                text = stringResource(R.string.place_sheet_visit_count, placeListUiState.placeCount),
                style = MaterialTheme.typography.bodyLarge,
                color = Gray700,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
