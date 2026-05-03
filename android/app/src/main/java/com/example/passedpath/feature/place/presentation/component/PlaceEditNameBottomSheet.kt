package com.example.passedpath.feature.place.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.ui.component.button.BaseButton
import com.example.passedpath.ui.theme.Gray100
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray700
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White

@Composable
fun PlaceEditNameBottomSheet(
    placeName: String,
    originalPlaceName: String,
    roadAddress: String,
    onPlaceNameChange: (String) -> Unit,
    onNameFocusChanged: (Boolean) -> Unit,
    onClearInputFocus: () -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    isSubmitting: Boolean = false
) {
    val canSubmit = placeName.trim().isNotBlank() &&
        placeName.trim() != originalPlaceName.trim() &&
        !isSubmitting

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = White,
        tonalElevation = 0.dp,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .clickable(onClick = onClearInputFocus)
                .padding(start = 20.dp, top = 26.dp, end = 20.dp, bottom = 48.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.place_edit_title),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleMedium,
                    color = Gray900,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        tint = Gray400,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            FieldLabel(text = stringResource(R.string.place_edit_name_label))
            Spacer(modifier = Modifier.height(10.dp))
            PlaceNameEditField(
                value = placeName,
                onValueChange = onPlaceNameChange,
                onFocusChanged = onNameFocusChanged,
                onDone = onClearInputFocus
            )
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = stringResource(R.string.place_edit_name_helper),
                style = MaterialTheme.typography.bodySmall,
                color = Green500,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(22.dp))
            FieldLabel(text = stringResource(R.string.place_edit_address_label))
            Spacer(modifier = Modifier.height(10.dp))
            ReadOnlyAddressField(
                roadAddress = roadAddress,
                onClick = onClearInputFocus
            )

            Spacer(modifier = Modifier.height(52.dp))
            BaseButton(
                text = stringResource(R.string.place_edit_submit),
                onClick = onSubmit,
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Gray400,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    )
}

@Composable
private fun PlaceNameEditField(
    value: String,
    onValueChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onDone: () -> Unit
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .border(
                width = 2.dp,
                color = Green500,
                shape = RoundedCornerShape(10.dp)
            )
            .background(White, RoundedCornerShape(10.dp))
            .onFocusChanged { onFocusChanged(it.isFocused) }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = Gray900,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() })
    )
}

@Composable
private fun ReadOnlyAddressField(
    roadAddress: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .background(Gray100, RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = Gray100,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = Green500,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = roadAddress,
            style = MaterialTheme.typography.bodyLarge,
            color = Gray900,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF4B5563)
@Composable
private fun PlaceEditNameBottomSheetPreview() {
    PassedPathTheme {
        PlaceEditNameBottomSheet(
            placeName = "국민대학교 복지관",
            originalPlaceName = "국민대학교",
            roadAddress = "서울 성북구 정릉로 77",
            onPlaceNameChange = {},
            onNameFocusChanged = {},
            onClearInputFocus = {},
            onDismiss = {},
            onSubmit = {}
        )
    }
}
