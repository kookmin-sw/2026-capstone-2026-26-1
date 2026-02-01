package com.example.passedpath.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // 메인 화면 타이틀
        Text(text = "메인 화면")

        Spacer(modifier = Modifier.height(24.dp))

        // 로그아웃 버튼
        Button(
            onClick = {
                // 로그아웃 트리거
                onLogout()
            }
        ) {
            Text(text = "로그아웃")
        }

        // TODO: 사용자 정보 표시
        // TODO: 위치 기록 리스트 UI
        // TODO: 지도 화면 연결
    }
}



@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    // Preview 전용: 더미 로그아웃 함수
    MainScreen(
        onLogout = {}
    )
}
