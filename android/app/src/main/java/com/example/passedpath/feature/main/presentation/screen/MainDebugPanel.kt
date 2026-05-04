package com.example.passedpath.feature.main.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.main.presentation.state.MainDebugUiState

@Composable
internal fun MainDebugPanel(
    debugUiState: MainDebugUiState,
    onRefreshSystemState: () -> Unit,
    onReloadRoute: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.debug_panel_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onClose) {
                Text(text = stringResource(R.string.debug_panel_close))
            }
        }
        Text(
            text = stringResource(R.string.debug_panel_date, debugUiState.selectedDateKey),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = stringResource(
                R.string.debug_panel_route,
                debugUiState.routeMode,
                debugUiState.routeSource,
                debugUiState.routeStatus
            ),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = stringResource(
                R.string.debug_panel_permission,
                debugUiState.permissionState.name,
                debugUiState.isLocationServiceEnabled.toDebugFlag()
            ),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = stringResource(
                R.string.debug_panel_tracking,
                debugUiState.isTrackingActive.toDebugFlag(),
                debugUiState.isTrackingEnabledByUser.toDebugFlag()
            ),
            style = MaterialTheme.typography.bodySmall
        )
        debugUiState.lastRouteMessage?.let { message ->
            Text(
                text = stringResource(R.string.debug_panel_last_event, message),
                style = MaterialTheme.typography.bodySmall
            )
        }
        debugUiState.recentTrackingEvents.forEach { event ->
            Text(
                text = event,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onRefreshSystemState) {
                Text(text = stringResource(R.string.debug_panel_refresh_system))
            }
            TextButton(onClick = onReloadRoute) {
                Text(text = stringResource(R.string.debug_panel_reload_route))
            }
        }
    }
}

private fun Boolean.toDebugFlag(): String = if (this) "ON" else "OFF"
