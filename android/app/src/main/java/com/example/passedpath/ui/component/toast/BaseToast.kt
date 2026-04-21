package com.example.passedpath.ui.component.toast

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun BaseToast(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.wrapContentWidth(),
            shape = RoundedCornerShape(999.dp), // feat: pill 형태
            color = Gray400,
            tonalElevation = 0.dp,
            shadowElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .wrapContentWidth(align = Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}

@Preview(showBackground = true, name = "Base Toast")
@Composable
private fun BaseToastPreview() {
    PassedPathTheme {
        BaseToast(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Toast preview",
                color = Color.White
            )
        }
    }
}
