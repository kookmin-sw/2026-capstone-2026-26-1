package com.example.passedpath.feature.care.presentation.component

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
import com.example.passedpath.ui.component.banner.InfoBottomBanner
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun CareEmptyDependentsBanner(
    modifier: Modifier = Modifier
) {
    InfoBottomBanner(
        message = stringResource(R.string.care_dependents_empty_title),
        modifier = modifier,
        borderColor = null,
        shadowElevation = 8.dp
    )
}

@Preview(showBackground = true, name = "Care Empty Dependents Banner")
@Composable
private fun CareEmptyDependentsBannerPreview() {
    PassedPathTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F4F6))
                .padding(16.dp)
        ) {
            CareEmptyDependentsBanner()
        }
    }
}
