package com.example.hotel_management_app.ui.bookings

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.hotel_management_app.data.Booking
import com.example.hotel_management_app.data.BookingStatus
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.data.Room
import com.example.hotel_management_app.ui.components.EmptyHint
import com.example.hotel_management_app.ui.components.GuestAvatar
import com.example.hotel_management_app.ui.components.PanelCard
import com.example.hotel_management_app.ui.components.StatusPill
import com.example.hotel_management_app.ui.money
import com.example.hotel_management_app.ui.nightsLabel
import com.example.hotel_management_app.ui.stayRange
import com.example.hotel_management_app.ui.theme.tone

/** Every reservation on the books, searchable by guest and filterable by state. */
@Composable
fun BookingsScreen(
    repo: HotelRepository,
    onOpenBooking: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf<BookingStatus?>(null) }

    val counts = repo.bookings.groupingBy { it.status }.eachCount()
    val shown = repo.bookings
        .filter { filter == null || it.status == filter }
        .filter { booking ->
            query.isBlank() ||
                booking.guestName.contains(query, ignoreCase = true) ||
                repo.room(booking.roomId)?.number?.contains(query) == true
        }
        .sortedWith(compareBy({ it.status.ordinal }, { it.checkIn }))

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Bookings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search guest or room") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filter == null,
                        onClick = { filter = null },
                        label = { Text("All ${repo.bookings.size}") }
                    )
                    BookingStatus.entries.forEach { status ->
                        FilterChip(
                            selected = filter == status,
                            onClick = { filter = if (filter == status) null else status },
                            label = { Text("${status.label} ${counts[status] ?: 0}") }
                        )
                    }
                }
            }
        }

        if (shown.isEmpty()) {
            item { EmptyHint("Nothing matches that search") }
        } else {
            items(shown, key = { it.id }) { booking ->
                BookingRow(
                    booking = booking,
                    room = repo.room(booking.roomId),
                    total = repo.total(booking),
                    onClick = { onOpenBooking(booking.id) }
                )
            }
        }
    }
}

@Composable
fun BookingRow(
    booking: Booking,
    room: Room?,
    total: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PanelCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GuestAvatar(booking.guestName)
            Column(Modifier.weight(1f)) {
                Text(
                    text = booking.guestName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Room ${room?.number ?: "—"} · ${room?.type?.label ?: "Unassigned"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${stayRange(booking.checkIn, booking.checkOut)} · " +
                        nightsLabel(booking.nights),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = booking.channel.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusPill(label = booking.status.label, tone = booking.status.tone())
                Spacer(Modifier.height(6.dp))
                Text(
                    text = money(total),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
