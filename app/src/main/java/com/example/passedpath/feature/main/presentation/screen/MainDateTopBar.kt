package com.example.passedpath.feature.main.presentation.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.passedpath.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val TopBarDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. EEE", Locale.KOREAN)

@Composable
internal fun MainDateTopBar(
    selectedDateKey: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val selectedDate = parseDateOrToday(selectedDateKey)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.96f),
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextButton(onClick = { onDateSelected(shiftDate(selectedDateKey, -1)) }) { Text(text = "<") }
            Text(
                text = selectedDate.format(TopBarDateFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            TextButton(onClick = {
                showDatePicker(context, selectedDateKey, onDateSelected)
            }) {
                Text(text = stringResource(R.string.main_pick_date))
            }
            TextButton(onClick = { onDateSelected(shiftDate(selectedDateKey, 1)) }) { Text(text = ">") }
        }
    }
}

private fun showDatePicker(
    context: android.content.Context,
    initialDateKey: String,
    onDateSelected: (String) -> Unit
) {
    val initialDate = runCatching { LocalDate.parse(initialDateKey, DateFormatter) }
        .getOrDefault(LocalDate.now())

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth).format(DateFormatter))
        },
        initialDate.year,
        initialDate.monthValue - 1,
        initialDate.dayOfMonth
    ).show()
}

private fun parseDateOrToday(dateKey: String): LocalDate {
    return runCatching { LocalDate.parse(dateKey, DateFormatter) }.getOrDefault(LocalDate.now())
}

private fun shiftDate(dateKey: String, days: Long): String {
    return parseDateOrToday(dateKey).plusDays(days).format(DateFormatter)
}
