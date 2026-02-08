package com.example.passedpath.feature.permission

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.ui.component.AppButton
import com.example.passedpath.ui.theme.PassedPathTheme

// "위치 권한이 필요해요"


@Composable
fun LocationPermissionIntroScreen(
    onStartClick: () -> Unit // 시작하기 버튼 클릭 이벤트 던지기
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 상단 설명 영역
        Column(
            modifier = Modifier.padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "위치 권한이 필요해요")
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "앱이 종료되어도 지나온 길을 기록할 수 있도록\n위치 권한을 항상 허용으로 설정해 주세요")
        }

        // Image
        Image(
            painter = painterResource(id = R.drawable.onboarding_image),
            contentDescription = "Onboarding illustration for location permission",
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 48.dp)
        )

        // 하단 CTA 버튼
        AppButton(
            text = "시작하기",
            onClick = onStartClick,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}


/* ---------- Preview ---------- */
@Preview(showBackground = true, name = "Light")
@Composable
private fun LocationPermissionIntroScreenPreview() {
    PassedPathTheme {
        LocationPermissionIntroScreen(
            onStartClick = {}
        )
    }
}
