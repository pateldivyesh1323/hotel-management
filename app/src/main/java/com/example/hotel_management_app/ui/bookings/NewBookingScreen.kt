package com.example.hotel_management_app.ui.bookings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.hotel_management_app.data.BookingChannel
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.data.Room
import com.example.hotel_management_app.ui.components.SectionHeader
import com.example.hotel_management_app.ui.full
import com.example.hotel_management_app.ui.money
import com.example.hotel_management_app.ui.nightsLabel
import java.time.LocalDate

/** The booking form: guest details, dates, and whatever rooms are actually free for them. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBookingScreen(
    repo: HotelRepository,
    presetRoomId: String?,
    onSaved: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var guests by rememberSaveable { mutableStateOf(1) }
    var checkInDay by rememberSaveable { mutableStateOf(today.toEpochDay()) }
    var checkOutDay by rememberSaveable { mutableStateOf(today.plusDays(1).toEpochDay()) }
    var selectedRoomId by rememberSaveable { mutableStateOf(presetRoomId) }
    var channel by rememberSaveable { mutableStateOf(BookingChannel.DIRECT.name) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var picking by rememberSaveable { mutableStateOf<DateTarget?>(null) }

    val checkIn = LocalDate.ofEpochDay(checkInDay)
    val checkOut = LocalDate.ofEpochDay(checkOutDay)
    val available = repo.availableRooms(checkIn, checkOut, guests)
    val selectedRoom = available.firstOrNull { it.id == selectedRoomId }
    val nights = (checkOutDay - checkInDay).toInt().coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
    ) {
        SectionHeader("Guest")
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; error = null },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Full name") },
            singleLine = true
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Phone") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(Modifier.height(20.dp))
        SectionHeader("Dates")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DateField(
                label = "Check in",
                value = checkIn.full(),
                onClick = { picking = DateTarget.CHECK_IN },
                modifier = Modifier.weight(1f)
            )
            DateField(
                label = "Check out",
                value = checkOut.full(),
                onClick = { picking = DateTarget.CHECK_OUT },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = nightsLabel(nights),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))
        SectionHeader("Party size")
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { if (guests > 1) guests-- },
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("−", style = MaterialTheme.typography.titleLarge)
            }
            Text(
                text = guests.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(56.dp)
            )
            OutlinedButton(
                onClick = { if (guests < 6) guests++ },
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionHeader("Available rooms")
        Spacer(Modifier.height(8.dp))
        if (available.isEmpty()) {
            Text(
                text = "Nothing free for those dates and party size. Try shorter dates or " +
                    "a smaller party.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            available.forEach { room ->
                RoomChoice(
                    room = room,
                    nights = nights,
                    selected = room.id == selectedRoomId,
                    onClick = { selectedRoomId = room.id; error = null }
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(12.dp))
        SectionHeader("Booking source")
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BookingChannel.entries.forEach { entry ->
                FilterChip(
                    selected = channel == entry.name,
                    onClick = { channel = entry.name },
                    label = { Text(entry.label) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        SectionHeader("Notes")
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Requests, arrival time, billing…") },
            minLines = 2
        )

        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Estimated total", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = selectedRoom?.let {
                            "Room ${it.number} · ${nightsLabel(nights)} × " +
                                money(it.type.nightlyRate)
                        } ?: "Pick a room to price the stay",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = money(nights * (selectedRoom?.type?.nightlyRate ?: 0)),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val room = selectedRoom
                if (room == null) {
                    error = "Pick a room"
                    return@Button
                }
                val failure = repo.createBooking(
                    roomId = room.id,
                    guestName = name,
                    guestPhone = phone,
                    guestEmail = email,
                    checkIn = checkIn,
                    checkOut = checkOut,
                    guests = guests,
                    notes = notes,
                    channel = BookingChannel.valueOf(channel)
                )
                if (failure == null) {
                    onSaved("Booked room ${room.number} for ${name.trim()}")
                } else {
                    error = failure
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm booking")
        }
        Spacer(Modifier.height(32.dp))
    }

    picking?.let { field ->
        val initial = if (field == DateTarget.CHECK_IN) checkInDay else checkOutDay
        val state = rememberDatePickerState(
            initialSelectedDateMillis = initial * MILLIS_PER_DAY
        )
        DatePickerDialog(
            onDismissRequest = { picking = null },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val day = Math.floorDiv(millis, MILLIS_PER_DAY)
                        if (field == DateTarget.CHECK_IN) {
                            checkInDay = day
                            // Keep the stay at least one night long.
                            if (checkOutDay <= day) checkOutDay = day + 1
                        } else {
                            checkOutDay = day
                            if (day <= checkInDay) checkInDay = day - 1
                        }
                        error = null
                    }
                    picking = null
                }) {
                    Text("Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { picking = null }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

private const val MILLIS_PER_DAY = 86_400_000L

private enum class DateTarget { CHECK_IN, CHECK_OUT }

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

@Composable
private fun RoomChoice(
    room: Room,
    nights: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            }
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Room ${room.number}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${room.type.label} · sleeps ${room.type.maxGuests} · floor " +
                        "${room.floor}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = money(room.type.nightlyRate * nights),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${money(room.type.nightlyRate)}/night",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
