package com.example.hotel_management_app.ui.bookings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hotel_management_app.data.BookingStatus
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.ui.components.DetailRow
import com.example.hotel_management_app.ui.components.GuestAvatar
import com.example.hotel_management_app.ui.components.SectionHeader
import com.example.hotel_management_app.ui.components.StatusPill
import com.example.hotel_management_app.ui.full
import com.example.hotel_management_app.ui.guestsLabel
import com.example.hotel_management_app.ui.money
import com.example.hotel_management_app.ui.nightsLabel
import com.example.hotel_management_app.ui.theme.tone

/** One reservation, with the front-desk actions that apply to its current state. */
@Composable
fun BookingDetailScreen(
    repo: HotelRepository,
    bookingId: String,
    onOpenRoom: (String) -> Unit,
    onMessage: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val booking = repo.booking(bookingId)
    if (booking == null) {
        Text("This booking no longer exists.", Modifier.padding(contentPadding))
        return
    }
    val room = repo.room(booking.roomId)
    val total = repo.total(booking)
    var confirmCancel by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GuestAvatar(booking.guestName, size = 56)
            Column(Modifier.weight(1f)) {
                Text(
                    text = booking.guestName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                StatusPill(label = booking.status.label, tone = booking.status.tone())
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionHeader("Stay")
        Spacer(Modifier.height(8.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                DetailRow("Check in", booking.checkIn.full())
                DetailRow("Check out", booking.checkOut.full())
                DetailRow("Length", nightsLabel(booking.nights))
                DetailRow("Party", guestsLabel(booking.guests))
                DetailRow("Reference", booking.id.uppercase())
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionHeader("Room")
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = room != null) { room?.let { onOpenRoom(it.id) } }
        ) {
            Column(Modifier.padding(16.dp)) {
                if (room == null) {
                    Text("The room for this booking was removed.")
                } else {
                    DetailRow("Room", room.number)
                    DetailRow("Type", room.type.label)
                    DetailRow("Nightly rate", money(room.type.nightlyRate))
                }
            }
        }

        if (booking.guestPhone.isNotBlank() || booking.guestEmail.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            SectionHeader("Contact")
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    if (booking.guestPhone.isNotBlank()) DetailRow("Phone", booking.guestPhone)
                    if (booking.guestEmail.isNotBlank()) DetailRow("Email", booking.guestEmail)
                }
            }
        }

        if (booking.notes.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            SectionHeader("Notes")
            Spacer(Modifier.height(8.dp))
            Text(
                text = booking.notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Folio total", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "${nightsLabel(booking.nights)} × " +
                            money(room?.type?.nightlyRate ?: 0),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = money(total),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        when (booking.status) {
            BookingStatus.RESERVED -> {
                Button(
                    onClick = { repo.checkIn(booking.id)?.let(onMessage) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Check in")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { confirmCancel = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel booking")
                }
            }

            BookingStatus.CHECKED_IN -> {
                Button(
                    onClick = { repo.checkOut(booking.id)?.let(onMessage) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Check out")
                }
            }

            BookingStatus.CHECKED_OUT -> Text(
                text = "This stay is closed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            BookingStatus.CANCELLED -> Text(
                text = "This booking was cancelled.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(32.dp))
    }

    if (confirmCancel) {
        AlertDialog(
            onDismissRequest = { confirmCancel = false },
            title = { Text("Cancel this booking?") },
            text = {
                Text("${booking.guestName}'s reservation will be released and room " +
                    "${room?.number ?: ""} freed for those dates.")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmCancel = false
                    repo.cancelBooking(booking.id)?.let(onMessage)
                }) {
                    Text("Cancel booking")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmCancel = false }) { Text("Keep it") }
            }
        )
    }
}
