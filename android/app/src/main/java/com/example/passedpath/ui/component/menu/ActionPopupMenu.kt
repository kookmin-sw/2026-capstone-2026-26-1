package com.example.passedpath.ui.component.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.StarBorder
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun ActionPopupMenu(
    items: List<MenuActionItem>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp),
        ) {
            items.forEachIndexed { index, item ->
                ActionPopupMenuItem(item = item)

                if (index != items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
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
            .heightIn(min = 48.dp)
            .clickable(onClick = item.onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = item.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}

@Preview(showBackground = true, name = "Action Popup Menu")
@Composable
private fun ActionPopupMenuPreview() {
    PassedPathTheme {
        ActionPopupMenu(
            items = previewMenuItems(),
        )
    }
}

@Preview(showBackground = true, name = "Action Popup Menu Item")
@Composable
private fun ActionPopupMenuItemPreview() {
    PassedPathTheme {
        Surface {
            ActionPopupMenuItem(
                item = MenuActionItem(
                    text = "장소 즐겨찾기",
                    icon = Icons.Outlined.StarBorder,
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
            icon = Icons.Outlined.StarBorder,
            onClick = {},
        ),
        MenuActionItem(
            text = "기록 삭제",
            icon = Icons.Outlined.Delete,
            onClick = {},
        ),
    )
}
