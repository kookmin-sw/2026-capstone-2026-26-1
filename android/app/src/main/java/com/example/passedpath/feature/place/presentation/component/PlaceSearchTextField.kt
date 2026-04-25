package com.example.passedpath.feature.place.presentation.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray300
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun PlaceSearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "\uC7A5\uC18C \uC774\uB984 \uB610\uB294 \uC8FC\uC18C\uB97C \uAC80\uC0C9\uD574 \uBCF4\uC138\uC694"
) {
    var isFocused by remember { mutableStateOf(false) }

    val shape = RoundedCornerShape(16.dp)
    val borderColor = if (isFocused) Green500 else Color.Transparent
    val backgroundColor = if (isFocused) Color.White else Gray100
    val iconTint = if (isFocused || value.isNotBlank()) Green500 else Gray300
    val textColor = Gray900
    val placeholderColor = Gray400

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = shape
            )
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            },
        singleLine = true,
        shape = shape,
        textStyle = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "\uAC80\uC0C9 \uC544\uC774\uCF58",
                tint = iconTint
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 14.sp,
                color = placeholderColor
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = backgroundColor,
            unfocusedContainerColor = backgroundColor,
            disabledContainerColor = backgroundColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedLeadingIconColor = iconTint,
            unfocusedLeadingIconColor = iconTint,
            focusedPlaceholderColor = placeholderColor,
            unfocusedPlaceholderColor = placeholderColor,
            cursorColor = Green500
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun PlaceSearchTextFieldEmptyPreview() {
    PassedPathTheme {
        PlaceSearchTextField(
            value = "",
            onValueChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceSearchTextFieldFilledPreview() {
    PassedPathTheme {
        PlaceSearchTextField(
            value = "Kookmin",
            onValueChange = {}
        )
    }
}
