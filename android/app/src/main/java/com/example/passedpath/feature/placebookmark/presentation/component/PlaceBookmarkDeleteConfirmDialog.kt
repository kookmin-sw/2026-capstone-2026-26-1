package com.example.passedpath.feature.placebookmark.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.passedpath.R
import com.example.passedpath.ui.component.dialog.BaseConfirmDialog
import com.example.passedpath.ui.component.place.PlaceNameMarker

@Composable
internal fun PlaceBookmarkDeleteConfirmDialog(
    placeName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    BaseConfirmDialog(
        title = stringResource(R.string.place_bookmark_delete_title),
        message = stringResource(R.string.place_bookmark_delete_message),
        dismissText = stringResource(R.string.place_bookmark_delete_cancel),
        confirmText = stringResource(R.string.place_bookmark_delete_confirm),
        topContent = {
            PlaceNameMarker(placeName = placeName)
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}
