package com.example.hotel_booking_app.ui.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hotel_booking_app.data.HotelRepository
import com.example.hotel_booking_app.data.StayRequest
import com.example.hotel_booking_app.ui.components.EmptyHint
import com.example.hotel_booking_app.ui.components.HotelCard
import com.example.hotel_booking_app.ui.components.SectionHeader

@Composable
fun SavedScreen(
    repo: HotelRepository,
    stay: StayRequest,
    onOpenHotel: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val saved = repo.favoriteHotels()

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { SectionHeader("Saved hotels") }

        if (saved.isEmpty()) {
            item { EmptyHint("Tap the heart on a hotel to save it here.") }
        }

        items(saved, key = { it.id }) { hotel ->
            HotelCard(
                hotel = hotel,
                fromRate = repo.availableOffers(hotel.id, stay).minOfOrNull { it.nightlyRate },
                favorite = true,
                onToggleFavorite = { repo.toggleFavorite(hotel.id) },
                onClick = { onOpenHotel(hotel.id) }
            )
        }
    }
}
