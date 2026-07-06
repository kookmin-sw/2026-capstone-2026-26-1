package com.example.passedpath.feature.main.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.passedpath.R
import com.example.passedpath.ui.component.menu.BaseBottomSheetMenu
import com.example.passedpath.ui.component.menu.BottomSheetMenuItem
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun MainMoreActionSheet(
    onPlaceBookmarkClick: () -> Unit,
    onDeleteRecordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BaseBottomSheetMenu(
        modifier = modifier,
        items = listOf(
            BottomSheetMenuItem(
                text = stringResource(id = R.string.main_more_place_bookmark),
                supportingText = stringResource(id = R.string.main_more_place_bookmark_description),
                iconResId = R.drawable.ic_bookmarkt_place_heart,
                onClick = onPlaceBookmarkClick
            ),
            BottomSheetMenuItem(
                text = stringResource(id = R.string.main_more_delete_record),
                supportingText = stringResource(id = R.string.main_more_delete_record_description),
                iconResId = R.drawable.ic_delete,
                onClick = onDeleteRecordClick,
                isDestructive = true
            )
        )
    )
}

@Preview(showBackground = true, name = "Main More Action Sheet")
@Composable
private fun MainMoreActionSheetPreview() {
    PassedPathTheme {
        MainMoreActionSheet(
            onPlaceBookmarkClick = {},
            onDeleteRecordClick = {}
        )
    }
}
