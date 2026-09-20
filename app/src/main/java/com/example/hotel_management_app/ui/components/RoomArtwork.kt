package com.example.hotel_management_app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.hotel_management_app.R
import com.example.hotel_management_app.data.Room
import com.example.hotel_management_app.data.RoomType

/**
 * Room photography for the rack: two real photos per room grade, picked by the room number
 * so neighbouring rooms of the same type don't all show the identical picture.
 */
@Composable
fun RoomImage(
    room: Room,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    val photo = remember(room.number, room.type) { photoFor(room.number, room.type) }
    Box(modifier.clip(shape)) {
        Image(
            painter = painterResource(photo),
            contentDescription = "${room.type.label} room",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        overlay()
    }
}

/** The small square picture that fronts a list row. */
@Composable
fun RoomThumbnail(
    room: Room,
    modifier: Modifier = Modifier,
    size: Int = 52
) {
    RoomImage(
        room = room,
        modifier = modifier.size(size.dp),
        shape = MaterialTheme.shapes.small
    )
}

/**
 * Darkens the foot of a picture so white text laid over it stays readable whatever photo
 * happens to be behind it.
 */
fun imageScrim(): Brush = Brush.verticalGradient(
    0.42f to Color.Transparent,
    0.74f to Color(0x4D0A0F08),
    1f to Color(0xA60A0F08)
)

/** Alternates between the two bundled photos for a grade, keyed by the room number. */
private fun photoFor(number: String, type: RoomType): Int {
    val variant = number.sumOf { it.code } % 2
    return when (type) {
        RoomType.STANDARD -> if (variant == 0) R.drawable.room_standard_1 else R.drawable.room_standard_2
        RoomType.DELUXE -> if (variant == 0) R.drawable.room_deluxe_1 else R.drawable.room_deluxe_2
        RoomType.SUITE -> if (variant == 0) R.drawable.room_suite_1 else R.drawable.room_suite_2
    }
}
