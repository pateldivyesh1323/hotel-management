package com.example.hotel_booking_app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hotel_booking_app.data.StayRequest
import com.example.hotel_booking_app.ui.full
import com.example.hotel_booking_app.ui.guestsLabel
import com.example.hotel_booking_app.ui.nightsLabel
import java.time.LocalDate

private const val MILLIS_PER_DAY = 86_400_000L
private const val MAX_GUESTS = 8

private enum class DateTarget { CHECK_IN, CHECK_OUT }

fun moveCheckIn(stay: StayRequest, checkIn: LocalDate): StayRequest = stay.copy(
    checkIn = checkIn,
    checkOut = if (stay.checkOut.isAfter(checkIn)) stay.checkOut else checkIn.plusDays(1)
)

fun moveCheckOut(stay: StayRequest, checkOut: LocalDate): StayRequest =
    stay.copy(checkOut = checkOut)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StayPicker(
    stay: StayRequest,
    today: LocalDate,
    onChange: (StayRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var picking by remember { mutableStateOf<DateTarget?>(null) }

    Column(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DateField(
                label = "Check in",
                value = stay.checkIn.full(),
                onClick = { picking = DateTarget.CHECK_IN },
                modifier = Modifier
                    .weight(1f)
                    .testTag("stay_check_in")
            )
            DateField(
                label = "Check out",
                value = stay.checkOut.full(),
                onClick = { picking = DateTarget.CHECK_OUT },
                modifier = Modifier
                    .weight(1f)
                    .testTag("stay_check_out")
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = nightsLabel(stay.nights),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onChange(stay.copy(guests = stay.guests - 1)) },
                    enabled = stay.guests > 1,
                    modifier = Modifier.testTag("guests_minus")
                ) { Icon(Icons.Filled.Remove, contentDescription = "Fewer guests") }
                Text(
                    text = guestsLabel(stay.guests),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    onClick = { onChange(stay.copy(guests = stay.guests + 1)) },
                    enabled = stay.guests < MAX_GUESTS,
                    modifier = Modifier.testTag("guests_plus")
                ) { Icon(Icons.Filled.Add, contentDescription = "More guests") }
            }
        }
    }

    picking?.let { target ->
        val selected = if (target == DateTarget.CHECK_IN) stay.checkIn else stay.checkOut
        val earliest = if (target == DateTarget.CHECK_IN) today else stay.checkIn.plusDays(1)
        val state = rememberDatePickerState(
            initialSelectedDateMillis = selected.toEpochDay() * MILLIS_PER_DAY,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis / MILLIS_PER_DAY >= earliest.toEpochDay()
            }
        )
        DatePickerDialog(
            onDismissRequest = { picking = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            val date = LocalDate.ofEpochDay(millis / MILLIS_PER_DAY)
                            onChange(
                                if (target == DateTarget.CHECK_IN) {
                                    moveCheckIn(stay, date)
                                } else {
                                    moveCheckOut(stay, date)
                                }
                            )
                        }
                        picking = null
                    },
                    modifier = Modifier.testTag("date_confirm")
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { picking = null }) { Text("Cancel") } }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun DateField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
