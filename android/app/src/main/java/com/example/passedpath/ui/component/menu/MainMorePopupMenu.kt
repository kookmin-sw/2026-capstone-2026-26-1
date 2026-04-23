package com.example.passedpath.ui.component.menu

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.passedpath.R
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun MainMorePopupMenu(
    onPlaceBookmarkClick: () -> Unit,
    onDeleteRecordClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ActionPopupMenu(
        modifier = modifier,
        items = listOf(
            MenuActionItem(
                text = stringResource(id = R.string.main_more_place_bookmark),
                iconResId = R.drawable.ic_star_checked,
                onClick = onPlaceBookmarkClick,
            ),
            MenuActionItem(
                text = stringResource(id = R.string.main_more_delete_record),
                iconResId = R.drawable.ic_trash,
                onClick = onDeleteRecordClick,
            ),
        ),
    )
}

@Preview(showBackground = true, name = "Main More Popup Menu")
@Composable
private fun MainMorePopupMenuPreview() {
    PassedPathTheme {
        MainMorePopupMenu(
            onPlaceBookmarkClick = {},
            onDeleteRecordClick = {},
        )
    }
}
