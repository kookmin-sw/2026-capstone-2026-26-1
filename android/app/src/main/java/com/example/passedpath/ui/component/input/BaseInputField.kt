package com.example.passedpath.ui.component.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray300
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray700
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.passedpath.ui.theme.Gray50
import com.example.passedpath.ui.theme.Gray500

@Composable
fun BaseInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1,
    imeAction: ImeAction = ImeAction.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Gray700),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray400
            )
        },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Green500,
            unfocusedBorderColor = Gray100,
            focusedContainerColor = Gray50,
            unfocusedContainerColor = Gray50,
            cursorColor = Green500
        )
    )
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
                value = "한강 저녁 산책",
                onValueChange = {},
                placeholder = "오늘의 지나온 길을 짧게 남겨 보세요",
                singleLine = true,
                imeAction = ImeAction.Next
            )
            BaseInputField(
                value = "바람이 많이 불었고, 강가에 앉아 있다가 집으로 돌아왔다.",
                onValueChange = {},
                placeholder = "기억하고 싶은 장면, 감정, 장소를 적어 보세요",
                minLines = 4
            )
        }
    }
}
