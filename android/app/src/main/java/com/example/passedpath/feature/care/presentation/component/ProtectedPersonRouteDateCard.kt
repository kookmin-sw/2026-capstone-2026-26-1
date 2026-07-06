package com.example.passedpath.feature.care.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.ui.theme.Gray400
import com.example.passedpath.ui.theme.Gray50
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.PassedPathTheme
import com.example.passedpath.ui.theme.White

@Composable
fun ProtectedPersonRouteDateCard(
    dateText: String,
    outingTimeText: String,
    enterHomeTimeText: String,
    outingCountText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = ProtectedPersonRouteDateCardShape,
        color = Gray50,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 98.dp)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateText,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = Gray900,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = Gray400,
                    modifier = Modifier.size(width = 7.dp, height = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProtectedPersonRouteDateMetric(
                    label = "\uC678\uCD9C",
                    value = outingTimeText,
                    modifier = Modifier.weight(1f)
                )
                ProtectedPersonRouteDateMetric(
                    label = "\uADC0\uAC00",
                    value = enterHomeTimeText,
                    modifier = Modifier.weight(1f)
                )
                ProtectedPersonRouteDateMetric(
                    label = "\uC678\uCD9C \uD69F\uC218",
                    value = outingCountText,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProtectedPersonRouteDateMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Gray400,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private val ProtectedPersonRouteDateCardShape = RoundedCornerShape(18.dp)

@Preview(
    name = "Protected Person Route Date Cards",
    showBackground = true,
    widthDp = 393
)
@Composable
private fun ProtectedPersonRouteDateCardPreview() {
    PassedPathTheme {
        Column(
            modifier = Modifier
                .background(White)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProtectedPersonRouteDateCard(
                dateText = "4\uC6D4 3\uC77C",
                outingTimeText = "09:12",
                enterHomeTimeText = "23:40",
                outingCountText = "3\uD68C",
                onClick = {}
            )
            ProtectedPersonRouteDateCard(
                dateText = "3\uC6D4 20\uC77C",
                outingTimeText = "09:12",
                enterHomeTimeText = "23:40",
                outingCountText = "1\uD68C",
                onClick = {}
            )
            ProtectedPersonRouteDateCard(
                dateText = "3\uC6D4 16\uC77C",
                outingTimeText = "10:05",
                enterHomeTimeText = "21:30",
                outingCountText = "2\uD68C",
                onClick = {}
            )
        }
    }
}
