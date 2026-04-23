package com.example.passedpath.feature.auth.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.feature.auth.presentation.component.SocialLoginButton
import com.example.passedpath.ui.theme.Black
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green300
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.kakaoYello

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Green300,
                                Color(0x00E2F8F6)
                            ),
                            center = Offset(0f, constraints.maxHeight.toFloat()),
                            radius = 1400f
                        )
                    )
            )
        }

        LoginHeader(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = 12.dp,
                    top = 120.dp,
                    end = 12.dp
                )
        )

        LoginIllustration(
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        LoginBottomAction(
            onLoginClick = onLoginClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 34.dp
                )
        )
    }
}

@Composable
private fun LoginHeader(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo_background_empty),
            contentDescription = stringResource(R.string.login_logo_content_description),
            modifier = Modifier.size(62.dp)
        )

        LoginHeroCopy(
            brandName = stringResource(R.string.login_hero_brand_name),
            firstLine = stringResource(R.string.login_hero_description_first_line),
            secondLine = stringResource(R.string.login_hero_description_second_line),
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun LoginHeroCopy(
    brandName: String,
    firstLine: String,
    secondLine: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = Green500,
                    fontWeight = FontWeight.ExtraBold
                )
            ) {
                append(brandName)
                append("\n")
            }

            withStyle(
                SpanStyle(
                    color = Gray900,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                append(firstLine)
                append("\n")
                append(secondLine)
            }
        },
        style = TextStyle(
            fontSize = 34.sp,
            lineHeight = 42.sp,
            letterSpacing = 0.sp
        ),
        modifier = modifier
    )
}

@Composable
private fun LoginIllustration(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(520.dp)
    ) {

        Image(
            painter = painterResource(id = R.drawable.pp_onboarding),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(520.dp)
        )
    }
}

@Composable
private fun LoginBottomAction(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SocialLoginButton(
        text = stringResource(R.string.login_with_kakao),
        iconResId = R.drawable.ic_kakao,
        containerColor = kakaoYello,
        contentColor = Black,
        onClick = onLoginClick,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    PassedPathTheme {
        LoginScreen(onLoginClick = {})
    }
}
