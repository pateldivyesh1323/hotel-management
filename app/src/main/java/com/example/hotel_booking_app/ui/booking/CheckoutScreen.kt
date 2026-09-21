package com.example.hotel_booking_app.ui.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hotel_booking_app.data.BookingOutcome
import com.example.hotel_booking_app.data.BookingRules
import com.example.hotel_booking_app.data.HotelRepository
import com.example.hotel_booking_app.data.PriceBreakdown
import com.example.hotel_booking_app.data.StayRequest
import com.example.hotel_booking_app.ui.components.DetailRow
import com.example.hotel_booking_app.ui.components.EmptyHint
import com.example.hotel_booking_app.ui.components.HotelThumbnail
import com.example.hotel_booking_app.ui.components.PanelCard
import com.example.hotel_booking_app.ui.components.SectionHeader
import com.example.hotel_booking_app.ui.guestsLabel
import com.example.hotel_booking_app.ui.money
import com.example.hotel_booking_app.ui.nightsLabel
import com.example.hotel_booking_app.ui.stayRange

@Composable
fun CheckoutScreen(
    repo: HotelRepository,
    offerId: String,
    stay: StayRequest,
    onBooked: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val offer = repo.offer(offerId)
    val hotel = repo.hotel(offer?.hotelId)
    if (offer == null || hotel == null) {
        EmptyHint("This room is no longer offered", modifier.padding(contentPadding))
        return
    }

    var name by rememberSaveable { mutableStateOf(repo.profile.name) }
    var email by rememberSaveable { mutableStateOf(repo.profile.email) }
    var phone by rememberSaveable { mutableStateOf(repo.profile.phone) }
    var requests by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var breakfast by rememberSaveable { mutableStateOf(false) }
    var promoInput by rememberSaveable { mutableStateOf("") }
    var appliedPromo by rememberSaveable { mutableStateOf("") }
    var promoNote by rememberSaveable { mutableStateOf<String?>(null) }
    var payAtHotel by rememberSaveable { mutableStateOf(false) }
    val promoPercent = BookingRules.promoPercent(appliedPromo) ?: 0
    val price = BookingRules.price(offer.nightlyRate, stay.nights, stay.guests, breakfast, promoPercent)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PanelCard(modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                HotelThumbnail(hotel = hotel, size = 72)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = hotel.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${offer.type.label} room · ${offer.beds}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stayRange(stay.checkIn, stay.checkOut)} · ${nightsLabel(stay.nights)} · ${guestsLabel(stay.guests)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column {
            SectionHeader("Guest details")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; error = null },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("guest_name"),
                label = { Text("Full name") },
                singleLine = true
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; error = null },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("guest_email"),
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("guest_phone"),
                label = { Text("Phone (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = requests,
                onValueChange = { requests = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("guest_requests"),
                label = { Text("Special requests (optional)") },
                minLines = 2
            )
        }

        Column {
            SectionHeader("Extras")
            Spacer(Modifier.height(8.dp))
            PanelCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Add breakfast", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${money(BookingRules.BREAKFAST_RATE)} per guest per night",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = breakfast,
                            onCheckedChange = { breakfast = it },
                            modifier = Modifier.testTag("breakfast_switch")
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = promoInput,
                            onValueChange = { promoInput = it; promoNote = null },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("promo_field"),
                            label = { Text("Promo code") },
                            singleLine = true
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                val percent = BookingRules.promoPercent(promoInput)
                                if (percent == null) {
                                    appliedPromo = ""
                                    promoNote = "That code isn't valid"
                                } else {
                                    appliedPromo = promoInput
                                    promoNote = "$percent% off applied"
                                }
                            },
                            modifier = Modifier.testTag("promo_apply")
                        ) { Text("Apply") }
                    }
                    promoNote?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (promoPercent > 0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .testTag("promo_note")
                        )
                    }
                }
            }
        }

        Column {
            SectionHeader("Payment")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !payAtHotel,
                    onClick = { payAtHotel = false },
                    label = { Text("Pay now by card") }
                )
                FilterChip(
                    selected = payAtHotel,
                    onClick = { payAtHotel = true },
                    label = { Text("Pay at the hotel") },
                    modifier = Modifier.testTag("pay_at_hotel")
                )
            }
        }

        Column {
            SectionHeader("Price details")
            Spacer(Modifier.height(8.dp))
            PriceCard(price)
        }

        error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("checkout_error")
            )
        }

        Button(
            onClick = {
                when (val outcome = repo.book(offer.id, name, phone, email, stay, requests, breakfast, appliedPromo, payAtHotel)) {
                    is BookingOutcome.Confirmed -> onBooked(outcome.bookingId)
                    is BookingOutcome.Rejected -> error = outcome.message
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("confirm_booking")
        ) { Text("Confirm booking · ${money(price.total)}") }
    }
}

@Composable
fun PriceCard(price: PriceBreakdown, modifier: Modifier = Modifier) {
    PanelCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            DetailRow(
                label = "${money(price.nightlyRate)} × ${nightsLabel(price.nights)}",
                value = money(price.subtotal)
            )
            if (price.breakfast > 0) DetailRow(label = "Breakfast", value = money(price.breakfast))
            if (price.discount > 0) DetailRow(label = "Promo discount", value = "-" + money(price.discount))
            DetailRow(label = "Taxes & fees (${BookingRules.TAX_PERCENT}%)", value = money(price.taxes))
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = money(price.total),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
