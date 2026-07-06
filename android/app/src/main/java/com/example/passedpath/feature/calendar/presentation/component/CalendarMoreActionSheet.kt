package com.example.passedpath.feature.calendar.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.passedpath.R
import com.example.passedpath.ui.component.menu.BaseBottomSheetMenu
import com.example.passedpath.ui.component.menu.BottomSheetMenuItem
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun CalendarMoreActionSheet(
    onFavoriteSettingsClick: () -> Unit,
    onDeleteRecordsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseBottomSheetMenu(
        modifier = modifier,
        items = listOf(
            BottomSheetMenuItem(
                text = stringResource(id = R.string.calendar_more_favorite_settings),
                supportingText = stringResource(
                    id = R.string.calendar_more_favorite_settings_description
                ),
                iconResId = R.drawable.ic_star_checked,
                onClick = onFavoriteSettingsClick
            ),
            BottomSheetMenuItem(
                text = stringResource(id = R.string.calendar_more_delete_records),
                supportingText = stringResource(
                    id = R.string.calendar_more_delete_records_description
                ),
                iconResId = R.drawable.ic_delete,
                onClick = onDeleteRecordsClick,
                isDestructive = true
            )
        )
    )
}

@Preview(showBackground = true, name = "Calendar More Action Sheet")
@Composable
private fun CalendarMoreActionSheetPreview() {
    PassedPathTheme {
        CalendarMoreActionSheet(
            onFavoriteSettingsClick = {},
            onDeleteRecordsClick = {}
        )
    }
}
