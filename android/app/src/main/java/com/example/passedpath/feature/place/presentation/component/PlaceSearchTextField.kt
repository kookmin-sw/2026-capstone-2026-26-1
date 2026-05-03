package com.example.passedpath.feature.place.presentation.component

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.example.passedpath.R
import com.example.passedpath.ui.component.input.BaseInputField
import com.example.passedpath.ui.theme.Gray300
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
    val iconTint = if (isFocused || value.isNotBlank()) Green500 else Gray300

    BaseInputField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier,
        singleLine = true,
        imeAction = ImeAction.Search,
        onFocusChanged = { isFocused = it },
        leadingContent = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "\uAC80\uC0C9 \uC544\uC774\uCF58",
                tint = iconTint
            )
        }
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
