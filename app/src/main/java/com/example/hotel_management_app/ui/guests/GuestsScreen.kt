package com.example.hotel_management_app.ui.guests

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hotel_management_app.data.BookingRules
import com.example.hotel_management_app.data.BookingStatus
import com.example.hotel_management_app.data.GuestSummary
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.ui.components.EmptyHint
import com.example.hotel_management_app.ui.components.GuestAvatar
import com.example.hotel_management_app.ui.components.StatusPill
import com.example.hotel_management_app.ui.money
import com.example.hotel_management_app.ui.nightsLabel
import com.example.hotel_management_app.ui.stayRange
import com.example.hotel_management_app.ui.theme.tone

/** The guest book: everyone who has stayed, with their history rolled up. */
@Composable
fun GuestsScreen(
    repo: HotelRepository,
    onOpenBooking: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val guests = BookingRules.guestSummaries(repo.bookings, repo.rooms)
    var expandedKey by rememberSaveable { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Guests",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${guests.size} on the books · " +
                        "${guests.count { it.inHouse }} in house tonight",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
            }
        }

        if (guests.isEmpty()) {
            item { EmptyHint("No guests yet — take a booking to start the guest book") }
        } else {
            items(guests, key = { it.name + it.phone }) { guest ->
                val key = guest.name + guest.phone
                GuestCard(
                    guest = guest,
                    repo = repo,
                    expanded = expandedKey == key,
                    onToggle = { expandedKey = if (expandedKey == key) null else key },
                    onOpenBooking = onOpenBooking
                )
            }
        }
    }
}

@Composable
private fun GuestCard(
    guest: GuestSummary,
    repo: HotelRepository,
    expanded: Boolean,
    onToggle: () -> Unit,
    onOpenBooking: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GuestAvatar(guest.name)
                Column(Modifier.weight(1f)) {
                    Text(
                        text = guest.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = guest.phone.ifBlank { guest.email.ifBlank { "No contact on file" } },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (guest.inHouse) {
                    StatusPill(
                        label = "In house",
                        tone = BookingStatus.CHECKED_IN.tone()
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                MiniStat(guest.stays.toString(), if (guest.stays == 1) "stay" else "stays")
                MiniStat(guest.nights.toString(), "nights")
                MiniStat(money(guest.spend), "lifetime")
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    repo.bookingsFor(guest).forEach { booking ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenBooking(booking.id) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "Room ${repo.room(booking.roomId)?.number ?: "—"} · " +
                                        stayRange(booking.checkIn, booking.checkOut),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = nightsLabel(booking.nights),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusPill(
                                label = booking.status.label,
                                tone = booking.status.tone()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
