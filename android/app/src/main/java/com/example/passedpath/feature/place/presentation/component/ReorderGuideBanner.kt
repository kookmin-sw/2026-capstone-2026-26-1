package com.example.passedpath.feature.place.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.ui.component.banner.InfoActionBottomBanner

@Composable
fun ReorderGuideBanner(
    modifier: Modifier = Modifier,
    onClickClose: () -> Unit
) {
    InfoActionBottomBanner(
        message = stringResource(id = R.string.reorder_guide_banner_message),
        actionText = stringResource(id = R.string.reorder_guide_banner_close),
        onClickAction = onClickClose,
        modifier = modifier
    )
}

@Preview(showBackground = true, name = "Reorder Guide Banner")
@Composable
private fun ReorderGuideBannerPreview() {
    com.example.passedpath.ui.theme.PassedPathTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F4F6))
                .padding(16.dp)
        ) {
            ReorderGuideBanner(onClickClose = {})
        }
    }
}
