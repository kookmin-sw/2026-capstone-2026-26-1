package com.example.passedpath.ui.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray50
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun BaseInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1,
    imeAction: ImeAction = ImeAction.Default,
    onFocusChanged: (Boolean) -> Unit = {},
    onImeAction: () -> Unit = {},
    leadingContent: (@Composable () -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    val shape = RoundedCornerShape(10.dp)
    val textStyle = baseInputTextStyle()

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .baseInputFieldSize(singleLine = singleLine)
            .clip(shape)
            .background(if (isFocused) Gray50 else Gray100)
            .border(
                width = 1.5.dp,
                color = if (isFocused) Green500 else Color.Transparent,
                shape = shape
            )
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                onFocusChanged(focusState.isFocused)
            },
        textStyle = textStyle,
        cursorBrush = SolidColor(Green500),
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onDone = { onImeAction() },
            onSearch = { onImeAction() }
        ),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else minLines,
        decorationBox = { innerTextField ->
            BaseInputContentRow(
                singleLine = singleLine,
                leadingContent = leadingContent
            ) {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = Gray400)
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun BaseInputButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingContent: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(10.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val textStyle = baseInputTextStyle()
    val displayText = text.ifBlank { placeholder }
    val displayColor = if (text.isBlank()) Gray400 else Gray900

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(shape)
            .background(Gray100)
            .border(
                width = 1.5.dp,
                color = Color.Transparent,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        BaseInputContentRow(
            singleLine = true,
            leadingContent = leadingContent
        ) {
            Text(
                text = displayText,
                style = textStyle.copy(color = displayColor),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun baseInputTextStyle(): TextStyle {
    return TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Gray900
    )
}

@Composable
private fun BaseInputContentRow(
    singleLine: Boolean,
    leadingContent: (@Composable () -> Unit)?,
    content: @Composable BoxScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .baseInputFieldDecorationSize(singleLine = singleLine)
            .padding(
                horizontal = 16.dp,
                vertical = if (singleLine) 0.dp else 13.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
    ) {
        leadingContent?.invoke()
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
            content = content
        )
    }
}

private fun Modifier.baseInputFieldSize(singleLine: Boolean): Modifier {
    return if (singleLine) {
        height(46.dp)
    } else {
        defaultMinSize(minHeight = 46.dp)
    }
}

private fun Modifier.baseInputFieldDecorationSize(singleLine: Boolean): Modifier {
    return if (singleLine) {
        height(46.dp)
    } else {
        this
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun BaseInputFieldPreview() {
    PassedPathTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BaseInputField(
                value = "",
                onValueChange = {},
                placeholder = "장소 이름 또는 주소를 검색해 보세요",
                singleLine = true,
                imeAction = ImeAction.Search
            )
            BaseInputField(
                value = "국민대학교 복지관",
                onValueChange = {},
                placeholder = "장소 이름 또는 주소를 검색해 보세요",
                singleLine = true,
                imeAction = ImeAction.Next
            )
            BaseInputButton(
                text = "서울특별시 성북구 정릉로 77",
                onClick = {},
                placeholder = "주소를 선택해 주세요"
            )
            BaseInputField(
                value = "오늘은 정릉천을 지나 학교 앞 카페에 들렀다.",
                onValueChange = {},
                placeholder = "기억하고 싶은 장소, 감정, 시간을 적어 보세요",
                minLines = 4
            )
        }
    }
}
