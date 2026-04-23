package com.example.passedpath.ui.component.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.ui.theme.Gray300
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun ActionPopupMenu(
    items: List<MenuActionItem>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.widthIn(min = 122.dp, max = 160.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
        ) {
            items.forEachIndexed { index, item ->
                ActionPopupMenuItem(
                    item = item,
                )

                if (index != items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 7.dp),
                        thickness = 0.5.dp,
                        color = Gray300,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionPopupMenuItem(
    item: MenuActionItem,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 20.dp)
            .clickable(onClick = item.onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            painter = painterResource(id = item.iconResId),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Gray500,
        )

        Text(
            text = item.text,
            fontSize = 12.sp,
            color = Gray500,
        )
    }
}

@Preview(showBackground = true, name = "Action Popup Menu")
@Composable
private fun ActionPopupMenuPreview() {
    PassedPathTheme {
        ActionPopupMenu(
            modifier = Modifier.padding(24.dp),
            items = previewMenuItems(),
        )
    }
}

@Preview(showBackground = true, name = "Action Popup Menu Item")
@Composable
private fun ActionPopupMenuItemPreview() {
    PassedPathTheme {
        Surface(color = Color.White) {
            ActionPopupMenuItem(
                item = MenuActionItem(
                    text = "장소 즐겨찾기",
                    iconResId = R.drawable.ic_star_checked,
                    onClick = {},
                ),
            )
        }
    }
}

private fun previewMenuItems(): List<MenuActionItem> {
    return listOf(
        MenuActionItem(
            text = "장소 즐겨찾기",
            iconResId = R.drawable.ic_star_checked,
            onClick = {},
        ),
        MenuActionItem(
            text = "기록 삭제",
            iconResId = R.drawable.ic_trash,
            onClick = {},
        ),
    )
}
