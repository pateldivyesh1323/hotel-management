package com.example.hotel_booking_app.ui.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LocalAirport
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hotel_booking_app.data.Amenity
import com.example.hotel_booking_app.data.BookingRules
import com.example.hotel_booking_app.data.HotelRepository
import com.example.hotel_booking_app.data.Review
import com.example.hotel_booking_app.data.RoomOffer
import com.example.hotel_booking_app.data.StayRequest
import com.example.hotel_booking_app.ui.components.DetailRow
import com.example.hotel_booking_app.ui.components.EmptyHint
import com.example.hotel_booking_app.ui.components.PanelSection
import com.example.hotel_booking_app.ui.components.FavoriteButton
import com.example.hotel_booking_app.ui.components.GuestAvatar
import com.example.hotel_booking_app.ui.components.HotelImage
import com.example.hotel_booking_app.ui.components.PanelCard
import com.example.hotel_booking_app.ui.components.RatingBadge
import com.example.hotel_booking_app.ui.components.RoomImage
import com.example.hotel_booking_app.ui.components.SectionHeader
import com.example.hotel_booking_app.ui.components.StayPicker
import com.example.hotel_booking_app.ui.full
import com.example.hotel_booking_app.ui.guestsLabel
import com.example.hotel_booking_app.ui.money

private fun Amenity.icon(): ImageVector = when (this) {
    Amenity.WIFI -> Icons.Filled.Wifi
    Amenity.POOL -> Icons.Filled.Pool
    Amenity.BREAKFAST -> Icons.Filled.FreeBreakfast
    Amenity.PARKING -> Icons.Filled.DirectionsCar
    Amenity.SPA -> Icons.Filled.Spa
    Amenity.GYM -> Icons.Filled.FitnessCenter
    Amenity.AIRPORT_SHUTTLE -> Icons.Filled.LocalAirport
    Amenity.PET_FRIENDLY -> Icons.Filled.Pets
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HotelDetailScreen(
    repo: HotelRepository,
    hotelId: String,
    stay: StayRequest,
    onStayChange: (StayRequest) -> Unit,
    onBookRoom: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val hotel = repo.hotel(hotelId)
    if (hotel == null) {
        EmptyHint("This hotel is no longer listed", modifier.padding(contentPadding))
        return
    }
    val offers = repo.offersFor(hotelId).sortedBy { it.nightlyRate }
    val reviews = repo.reviewsFor(hotelId)

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HotelImage(
                hotel = hotel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                FavoriteButton(
                    favorite = repo.isFavorite(hotelId),
                    onToggle = { repo.toggleFavorite(hotelId) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .testTag("detail_favorite")
                )
            }
        }

        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = hotel.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    RatingBadge(hotel.rating)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${"★".repeat(hotel.stars)} · ${hotel.address}, ${hotel.location}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${hotel.reviewCount} guest reviews",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Text(text = hotel.description, style = MaterialTheme.typography.bodyMedium)
            }
        }

        item {
            Column {
                SectionHeader("Highlights")
                Spacer(Modifier.height(8.dp))
                hotel.highlights.forEach { line ->
                    Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = line, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        item {
            Column {
                SectionHeader("Amenities")
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    hotel.amenities.forEach { amenity ->
                        AssistChip(
                            onClick = {},
                            label = { Text(amenity.label) },
                            leadingIcon = {
                                Icon(amenity.icon(), contentDescription = null)
                            }
                        )
                    }
                }
            }
        }

        item {
            Column {
                SectionHeader("Your stay")
                Spacer(Modifier.height(8.dp))
                PanelCard(modifier = Modifier.fillMaxWidth()) {
                    StayPicker(
                        stay = stay,
                        today = repo.currentDate(),
                        onChange = onStayChange,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        item { SectionHeader("Choose your room") }

        items(offers, key = { it.id }) { offer ->
            RoomOfferCard(
                offer = offer,
                stay = stay,
                unitsLeft = repo.unitsLeft(offer, stay),
                onBook = { onBookRoom(offer.id) }
            )
        }

        item {
            PanelSection(title = "Good to know") {
                Column {
                    DetailRow("Check-in", "From 2:00 PM")
                    DetailRow("Check-out", "Until 11:00 AM")
                    DetailRow("Cancellation", "Free until the day before check-in")
                    DetailRow("Breakfast", "Add for ${money(BookingRules.BREAKFAST_RATE)} per guest per night")
                }
            }
        }

        item { SectionHeader("Guest reviews") }

        if (reviews.isEmpty()) {
            item { EmptyHint("No reviews yet") }
        }

        items(reviews, key = { it.id }) { review -> ReviewRow(review) }
    }
}

@Composable
private fun RoomOfferCard(
    offer: RoomOffer,
    stay: StayRequest,
    unitsLeft: Int,
    onBook: () -> Unit
) {
    val fits = offer.type.maxGuests >= stay.guests
    val bookable = fits && unitsLeft > 0
    PanelCard(modifier = Modifier
        .fillMaxWidth()
        .testTag("offer_${offer.id}")) {
        Row(Modifier.padding(12.dp)) {
            RoomImage(offer = offer, modifier = Modifier.width(96.dp).height(112.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "${offer.type.label} room",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${offer.beds} · ${offer.sizeSqm} m² · sleeps ${offer.type.maxGuests}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when {
                        !fits -> "Too small for ${guestsLabel(stay.guests)}"
                        unitsLeft == 0 -> "Sold out for these dates"
                        unitsLeft <= 2 -> "Only $unitsLeft left"
                        else -> "Free cancellation before check-in"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (bookable && unitsLeft > 2) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = money(offer.nightlyRate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "per night",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onBook,
                        enabled = bookable,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("book_${offer.id}")
                    ) { Text("Book") }
                }
            }
        }
    }
}

@Composable
private fun ReviewRow(review: Review) {
    PanelCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GuestAvatar(name = review.guestName, size = 36)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = review.guestName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = review.date.full(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RatingBadge(review.rating)
            }
            Spacer(Modifier.height(8.dp))
            Text(text = review.comment, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
