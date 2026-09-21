package com.example.hotel_booking_app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.hotel_booking_app.R
import com.example.hotel_booking_app.data.Hotel
import com.example.hotel_booking_app.data.RoomOffer
import com.example.hotel_booking_app.data.RoomType

private val HotelPhotos = listOf(
    R.drawable.hotel_lobby,
    R.drawable.room_suite_1,
    R.drawable.room_deluxe_2,
    R.drawable.room_suite_2,
    R.drawable.room_deluxe_1,
    R.drawable.room_standard_2
)

@Composable
fun HotelImage(
    hotel: Hotel,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    PhotoBox(
        photo = HotelPhotos[hotel.photoIndex % HotelPhotos.size],
        description = hotel.name,
        modifier = modifier,
        shape = shape,
        overlay = overlay
    )
}

@Composable
fun HotelThumbnail(
    hotel: Hotel,
    modifier: Modifier = Modifier,
    size: Int = 64
) {
    HotelImage(
        hotel = hotel,
        modifier = modifier.size(size.dp),
        shape = MaterialTheme.shapes.small
    )
}

@Composable
fun RoomImage(
    offer: RoomOffer,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium
) {
    PhotoBox(
        photo = roomPhoto(offer),
        description = "${offer.type.label} room",
        modifier = modifier,
        shape = shape,
        overlay = {}
    )
}

@Composable
private fun PhotoBox(
    photo: Int,
    description: String,
    modifier: Modifier,
    shape: Shape,
    overlay: @Composable BoxScope.() -> Unit
) {
    Box(modifier.clip(shape)) {
        Image(
            painter = painterResource(photo),
            contentDescription = description,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        overlay()
    }
}

fun imageScrim(): Brush = Brush.verticalGradient(
    0.42f to Color.Transparent,
    0.74f to Color(0x4D0A0F08),
    1f to Color(0xA60A0F08)
)

private fun roomPhoto(offer: RoomOffer): Int {
    val variant = offer.id.sumOf { it.code } % 2
    return when (offer.type) {
        RoomType.STANDARD -> if (variant == 0) R.drawable.room_standard_1 else R.drawable.room_standard_2
        RoomType.DELUXE -> if (variant == 0) R.drawable.room_deluxe_1 else R.drawable.room_deluxe_2
        RoomType.SUITE -> if (variant == 0) R.drawable.room_suite_1 else R.drawable.room_suite_2
    }
}
