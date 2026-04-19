package com.example.passedpath.feature.place.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.place.presentation.state.PlaceListUiState
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray700
import com.example.passedpath.ui.theme.Green50
import com.example.passedpath.ui.theme.Green500

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
            PlaceGuideBanner(onClose = onCloseBanner)
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

@Composable
private fun PlaceGuideBanner(
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Green50, shape = RoundedCornerShape(18.dp))
            .padding(start = 16.dp, top = 14.dp, end = 12.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.place_sheet_banner_title),
                style = MaterialTheme.typography.bodyMedium,
                color = Green500,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.place_sheet_banner_body),
                style = MaterialTheme.typography.bodySmall,
                color = Gray400
            )
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.place_sheet_banner_close),
                tint = Gray400,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
