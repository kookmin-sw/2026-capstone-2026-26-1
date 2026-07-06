package com.example.passedpath.ui.component.menu

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray200
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray600
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green50
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White

data class BottomSheetMenuItem(
    val text: String,
    val supportingText: String? = null,
    @DrawableRes val iconResId: Int,
    val onClick: () -> Unit,
    val isDestructive: Boolean = false
)

@Composable
fun BaseBottomSheetMenu(
    items: List<BottomSheetMenuItem>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = White,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BottomSheetMenuHandle()
            items.forEachIndexed { index, item ->
                BaseBottomSheetMenuItem(item = item)
                if (index != items.lastIndex) {
                    HorizontalDivider(color = Gray200, thickness = 0.7.dp)
                }
            }
        }
    }
}

@Composable
private fun BottomSheetMenuHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 4.dp)
                .background(color = Gray200, shape = RoundedCornerShape(999.dp))
        )
    }
}

@Composable
private fun BaseBottomSheetMenuItem(
    item: BottomSheetMenuItem,
    modifier: Modifier = Modifier
) {
    val contentColor = if (item.isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        Gray900
    }
    val iconTint = if (item.isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        Green500
    }
    val iconBackground = if (item.isDestructive) {
        Gray100
    } else {
        Green50
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable(onClick = item.onClick)
            .padding(horizontal = 2.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(color = iconBackground, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = item.iconResId),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = item.text,
                color = contentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            item.supportingText?.let { supportingText ->
                Text(
                    text = supportingText,
                    color = if (item.isDestructive) Gray600 else Gray500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Base Bottom Sheet Menu")
@Composable
private fun BaseBottomSheetMenuPreview() {
    PassedPathTheme {
        BaseBottomSheetMenu(
            items = listOf(
                BottomSheetMenuItem(
                    text = "즐겨찾는 장소",
                    supportingText = "저장한 장소를 관리해요",
                    iconResId = R.drawable.ic_bookmarkt_place_heart,
                    onClick = {}
                ),
                BottomSheetMenuItem(
                    text = "1일 위치 기록 삭제",
                    supportingText = "선택한 날짜의 위치 기록을 삭제해요",
                    iconResId = R.drawable.ic_delete,
                    onClick = {},
                    isDestructive = true
                )
            )
        )
    }
}
