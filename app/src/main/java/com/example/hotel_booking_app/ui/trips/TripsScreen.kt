package com.example.hotel_booking_app.ui.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.hotel_booking_app.data.Booking
import com.example.hotel_booking_app.data.BookingRules
import com.example.hotel_booking_app.data.HotelRepository
import com.example.hotel_booking_app.data.TripStage
import com.example.hotel_booking_app.ui.components.EmptyHint
import com.example.hotel_booking_app.ui.components.HotelThumbnail
import com.example.hotel_booking_app.ui.components.PanelCard
import com.example.hotel_booking_app.ui.components.SectionHeader
import com.example.hotel_booking_app.ui.components.StatusPill
import com.example.hotel_booking_app.ui.money
import com.example.hotel_booking_app.ui.nightsLabel
import com.example.hotel_booking_app.ui.stayRange
import com.example.hotel_booking_app.ui.theme.tone

@Composable
fun TripsScreen(
    repo: HotelRepository,
    onOpenTrip: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    var stageName by rememberSaveable { mutableStateOf(TripStage.UPCOMING.name) }
    val stage = TripStage.valueOf(stageName)
    val trips = repo.trips(stage)

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { SectionHeader("My trips") }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TripStage.entries.forEach { entry ->
                    FilterChip(
                        selected = stage == entry,
                        onClick = { stageName = entry.name },
                        label = { Text("${entry.label} (${repo.trips(entry).size})") },
                        modifier = Modifier.testTag("stage_${entry.name}")
                    )
                }
            }
        }

        if (trips.isEmpty()) {
            item {
                EmptyHint(
                    when (stage) {
                        TripStage.UPCOMING -> "No upcoming trips. Find a hotel in the Explore tab."
                        TripStage.PAST -> "Your completed stays will show up here."
                        TripStage.CANCELLED -> "You haven't cancelled any bookings."
                    }
                )
            }
        }

        items(trips, key = { it.id }) { booking ->
            TripRow(repo = repo, booking = booking, onClick = { onOpenTrip(booking.id) })
        }
    }
}

@Composable
private fun TripRow(
    repo: HotelRepository,
    booking: Booking,
    onClick: () -> Unit
) {
    val hotel = repo.hotel(booking.hotelId) ?: return
    val stage = repo.stage(booking)
    PanelCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trip_${booking.id}"),
        onClick = onClick
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            HotelThumbnail(hotel = hotel, size = 72)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = hotel.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = hotel.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${stayRange(booking.checkIn, booking.checkOut)} · ${nightsLabel(booking.nights)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                StatusPill(label = stage.label, tone = stage.tone())
                Spacer(Modifier.height(6.dp))
                Text(
                    text = money(BookingRules.price(booking).total),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
