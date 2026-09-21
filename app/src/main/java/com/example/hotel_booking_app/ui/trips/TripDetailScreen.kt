package com.example.hotel_booking_app.ui.trips

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hotel_booking_app.data.BookingRules
import com.example.hotel_booking_app.data.HotelRepository
import com.example.hotel_booking_app.data.TripStage
import com.example.hotel_booking_app.ui.booking.PriceCard
import com.example.hotel_booking_app.ui.components.DetailRow
import com.example.hotel_booking_app.ui.components.EmptyHint
import com.example.hotel_booking_app.ui.components.HotelImage
import com.example.hotel_booking_app.ui.components.PanelSection
import com.example.hotel_booking_app.ui.components.RatingBadge
import com.example.hotel_booking_app.ui.components.StatusPill
import com.example.hotel_booking_app.ui.full
import com.example.hotel_booking_app.ui.guestsLabel
import com.example.hotel_booking_app.ui.nightsLabel
import com.example.hotel_booking_app.ui.theme.tone

@Composable
fun TripDetailScreen(
    repo: HotelRepository,
    bookingId: String,
    onOpenHotel: (String) -> Unit,
    onMessage: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val booking = repo.booking(bookingId)
    val hotel = repo.hotel(booking?.hotelId)
    val offer = repo.offer(booking?.roomId)
    if (booking == null || hotel == null || offer == null) {
        EmptyHint("Booking not found", modifier.padding(contentPadding))
        return
    }
    var confirmingCancel by rememberSaveable { mutableStateOf(false) }
    val stage = repo.stage(booking)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HotelImage(
            hotel = hotel,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = hotel.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${hotel.address}, ${hotel.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(
                label = stage.label,
                tone = stage.tone(),
                modifier = Modifier.testTag("trip_stage")
            )
        }

        PanelSection(title = "Your stay") {
            Column {
                DetailRow("Confirmation", booking.id.uppercase())
                DetailRow("Check in", booking.checkIn.full())
                DetailRow("Check out", booking.checkOut.full())
                DetailRow("Length", nightsLabel(booking.nights))
                DetailRow("Room", "${offer.type.label} · ${offer.beds}")
                DetailRow("Guests", guestsLabel(booking.guests))
                DetailRow("Booked on", booking.bookedOn.full())
                DetailRow("Breakfast", if (booking.breakfastIncluded) "Included" else "Not included")
                DetailRow("Payment", if (booking.payAtHotel) "Pay at the hotel" else "Paid by card")
            }
        }

        PanelSection(title = "Guest") {
            Column {
                DetailRow("Name", booking.guestName)
                DetailRow("Email", booking.guestEmail)
                if (booking.guestPhone.isNotBlank()) DetailRow("Phone", booking.guestPhone)
                if (booking.specialRequests.isNotBlank()) DetailRow("Requests", booking.specialRequests)
            }
        }

        PriceCard(BookingRules.price(booking))

        if (stage == TripStage.PAST) {
            ReviewSection(repo = repo, bookingId = booking.id, onMessage = onMessage)
        }

        OutlinedButton(
            onClick = { onOpenHotel(hotel.id) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("View hotel") }

        if (repo.canCancel(booking)) {
            Button(
                onClick = { confirmingCancel = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cancel_booking"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) { Text("Cancel booking") }
        }
    }

    if (confirmingCancel) {
        AlertDialog(
            onDismissRequest = { confirmingCancel = false },
            title = { Text("Cancel this booking?") },
            text = { Text("${hotel.name}, ${booking.checkIn.full()} – ${booking.checkOut.full()}. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingCancel = false
                        onMessage(repo.cancelBooking(booking.id) ?: "Booking cancelled")
                    },
                    modifier = Modifier.testTag("confirm_cancel")
                ) { Text("Cancel booking") }
            },
            dismissButton = {
                TextButton(onClick = { confirmingCancel = false }) { Text("Keep booking") }
            }
        )
    }
}

@Composable
private fun ReviewSection(
    repo: HotelRepository,
    bookingId: String,
    onMessage: (String) -> Unit
) {
    val existing = repo.reviewFor(bookingId)
    var rating by rememberSaveable { mutableStateOf(5) }
    var comment by rememberSaveable { mutableStateOf("") }

    PanelSection(title = if (existing == null) "Rate your stay" else "Your review") {
        if (existing != null) {
            Column {
                RatingBadge(existing.rating)
                Spacer(Modifier.height(8.dp))
                Text(text = existing.comment, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { value ->
                        FilterChip(
                            selected = rating == value,
                            onClick = { rating = value },
                            label = { Text("$value ★") },
                            modifier = Modifier.testTag("review_rating_$value")
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_comment"),
                    label = { Text("How was your stay?") },
                    minLines = 2
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        onMessage(repo.addReview(bookingId, rating.toFloat(), comment) ?: "Thanks for your review")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_review")
                ) { Text("Submit review") }
            }
        }
    }
}
