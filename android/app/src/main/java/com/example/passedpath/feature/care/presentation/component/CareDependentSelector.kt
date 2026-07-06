package com.example.passedpath.feature.care.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import com.example.passedpath.feature.care.presentation.state.CareDependentUserUiState
import com.example.passedpath.ui.component.loading.BaseSkeletonBlock
import com.example.passedpath.ui.component.loading.rememberBaseSkeletonBrush
import com.example.passedpath.ui.theme.Gray300
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray600
import com.example.passedpath.ui.theme.Green50
import com.example.passedpath.ui.theme.Green500
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White

@Composable
fun CareDependentSelectorRow(
    dependents: List<CareDependentUserUiState>,
    selectedDependentUserId: Long?,
    isLoading: Boolean,
    onSelectAllClick: () -> Unit,
    onDependentClick: (Long) -> Unit,
    onInviteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item(key = "all") {
            CareDependentSelectorCard(
                label = stringResource(R.string.care_all_dependents),
                selected = selectedDependentUserId == null,
                onClick = onSelectAllClick
            ) {
                CareSelectorIconCircle(
                    iconResId = R.drawable.ic_current_location,
                    contentDescription = stringResource(
                        R.string.care_all_dependents_content_description
                    ),
                    selected = selectedDependentUserId == null
                )
            }
        }

        if (isLoading && dependents.isEmpty()) {
            items(2) {
                CareDependentSelectorSkeletonCard()
            }
        } else {
            itemsIndexed(
                items = dependents,
                key = { _, dependent -> dependent.dependentUserId }
            ) { index, dependent ->
                CareDependentSelectorCard(
                    label = dependent.nickname,
                    selected = selectedDependentUserId == dependent.dependentUserId,
                    onClick = { onDependentClick(dependent.dependentUserId) }
                ) {
                    CareSelectorAvatarCircle(
                        selected = selectedDependentUserId == dependent.dependentUserId
                    ) {
                        CareDependentAvatar(
                            nickname = dependent.nickname,
                            profileImageUrl = dependent.profileImageUrl,
                            palette = careDependentAvatarPalette(index),
                            contentDescription = stringResource(
                                R.string.care_dependent_marker_content_description,
                                dependent.nickname
                            ),
                            modifier = Modifier.size(58.dp)
                        )
                    }
                }
            }
        }

        item(key = "invite") {
            CareDependentSelectorCard(
                label = stringResource(R.string.care_invite_dependent),
                selected = false,
                onClick = onInviteClick
            ) {
                CareInviteIconCircle()
            }
        }
    }
}

@Composable
fun CareDependentSelectorCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatarContent: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .width(72.dp)
            .height(92.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(top = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        avatarContent()
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) Green500 else Gray400,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CareSelectorIconCircle(
    iconResId: Int,
    contentDescription: String,
    selected: Boolean
) {
    Box(
        modifier = Modifier
            .size(66.dp)
            .background(
                color = if (selected) Green50 else White,
                shape = CircleShape
            )
            .border(
                border = BorderStroke(2.dp, if (selected) Green500 else White),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = if (selected) Green500 else Gray400,
            modifier = Modifier.size(29.dp)
        )
    }
}

@Composable
private fun CareSelectorAvatarCircle(
    selected: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(66.dp)
            .background(
                color = if (selected) Green50 else White,
                shape = CircleShape
            )
            .border(
                border = BorderStroke(2.dp, if (selected) Green500 else White),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun CareInviteIconCircle() {
    val strokeColor = Gray300
    Box(
        modifier = Modifier.size(66.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(64.dp)) {
            drawCircle(
                color = strokeColor,
                radius = size.minDimension / 2f - 1.dp.toPx(),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(8.dp.toPx(), 6.dp.toPx())
                    )
                )
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_plus),
            contentDescription = stringResource(R.string.care_invite_content_description),
            tint = Gray400,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun CareDependentSelectorSkeletonCard() {
    val skeletonBrush = rememberBaseSkeletonBrush()

    Column(
        modifier = Modifier
            .width(72.dp)
            .height(92.dp)
            .padding(top = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        BaseSkeletonBlock(
            brush = skeletonBrush,
            modifier = Modifier.size(64.dp),
            shape = CircleShape
        )
        BaseSkeletonBlock(
            brush = skeletonBrush,
            modifier = Modifier
                .width(42.dp)
                .height(12.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CareDependentSelectorRowPreview() {
    PassedPathTheme {
        CareDependentSelectorRow(
            dependents = listOf(
                CareDependentUserUiState(
                    dependentUserId = 1L,
                    nickname = "Jiyeon",
                    profileImageUrl = null,
                    latestLatitude = 37.5665,
                    latestLongitude = 126.978,
                    latestRecordedAt = null
                ),
                CareDependentUserUiState(
                    dependentUserId = 2L,
                    nickname = "Seulgi",
                    profileImageUrl = null,
                    latestLatitude = 37.57,
                    latestLongitude = 126.98,
                    latestRecordedAt = null
                ),
                CareDependentUserUiState(
                    dependentUserId = 3L,
                    nickname = "Minji",
                    profileImageUrl = null,
                    latestLatitude = 37.568,
                    latestLongitude = 126.982,
                    latestRecordedAt = null
                ),
                CareDependentUserUiState(
                    dependentUserId = 4L,
                    nickname = "Hana",
                    profileImageUrl = null,
                    latestLatitude = 37.572,
                    latestLongitude = 126.984,
                    latestRecordedAt = null
                ),
                CareDependentUserUiState(
                    dependentUserId = 5L,
                    nickname = "Doyun",
                    profileImageUrl = null,
                    latestLatitude = 37.574,
                    latestLongitude = 126.986,
                    latestRecordedAt = null
                ),
                CareDependentUserUiState(
                    dependentUserId = 6L,
                    nickname = "Yuna",
                    profileImageUrl = null,
                    latestLatitude = 37.576,
                    latestLongitude = 126.988,
                    latestRecordedAt = null
                )
            ),
            selectedDependentUserId = 1L,
            isLoading = false,
            onSelectAllClick = {},
            onDependentClick = {},
            onInviteClick = {}
        )
    }
}
