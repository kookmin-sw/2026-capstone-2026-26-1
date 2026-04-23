package com.example.passedpath.ui.component.place

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passedpath.R
import com.example.passedpath.ui.theme.Gray200
import com.example.passedpath.ui.theme.Gray300
import com.example.passedpath.ui.theme.Gray500
import com.example.passedpath.ui.theme.Gray700
import com.example.passedpath.ui.theme.Gray900
import com.example.passedpath.ui.theme.Green200
import com.example.passedpath.ui.theme.Green50
import com.example.passedpath.ui.theme.PassedPathTheme

@Composable
fun PlaceSearchCard(
    title: String,
    address: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    category: String = "",
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(20.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = shape,
        color = if (isSelected) Green50 else Color.White,
        border = BorderStroke(
            width = if (isSelected) 1.0.dp else 0.7.dp,
            color = if (isSelected) Green200 else Gray200
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_marker_empty),
                contentDescription = "\uC7A5\uC18C \uC544\uC774\uCF58",
                tint = Gray300,
                modifier = Modifier.size(24.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = address,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Gray500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (category.isNotBlank()) {
                    Text(
                        text = category,
                        modifier = Modifier.width(72.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Gray700,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceSearchCardPreview() {
    PassedPathTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlaceSearchCard(
                title = "Kookmin University Main Hall",
                address = "77 Jeongneung-ro, Seongbuk-gu, Seoul",
                isSelected = false,
                category = "University"
            )

            PlaceSearchCard(
                title = "Kookmin University Business Hall",
                address = "77 Jeongneung-ro, Seongbuk-gu, Seoul",
                isSelected = true,
                category = "Education",
                onClick = {}
            )
        }
    }
}
