package com.example.passedpath // 파일의 소속 패키지

// 외부 파일/라이브러리 코드 가져오기
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.passedpath.navigation.AppNavHost
import com.example.passedpath.ui.theme.PassedPathTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // composable 함수 진입점
        // "UI는 compose로 생성한다"
        setContent {

            // 앱 전체를 passedpathTheme으로 감쌈
            PassedPathTheme {

                // Navigation Controller
                val navController = rememberNavController()


                AppNavHost(navController = navController)
            }
        }
    }
}
