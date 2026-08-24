package com.example.hotel_management_app.ui.rooms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Balcony
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.data.RoomStatus
import com.example.hotel_management_app.data.RoomType
import com.example.hotel_management_app.ui.components.DetailRow
import com.example.hotel_management_app.ui.components.GuestAvatar
import com.example.hotel_management_app.ui.components.RoomImage
import com.example.hotel_management_app.ui.components.SectionHeader
import com.example.hotel_management_app.ui.components.StatusPill
import com.example.hotel_management_app.ui.components.imageScrim
import com.example.hotel_management_app.ui.guestsLabel
import com.example.hotel_management_app.ui.money
import com.example.hotel_management_app.ui.stayRange
import com.example.hotel_management_app.ui.theme.tone

/** One room: who is in it, what is booked next, and the housekeeping controls. */
@Composable
fun RoomDetailScreen(
    repo: HotelRepository,
    roomId: String,
    onOpenBooking: (String) -> Unit,
    onBookRoom: (String) -> Unit,
    onMessage: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val room = repo.room(roomId)
    if (room == null) {
        Text("This room is no longer on the property.", Modifier.padding(contentPadding))
        return
    }
    val occupant = repo.currentBookingFor(room.id)
    val next = repo.nextBookingFor(room.id)
    var noteDraft by rememberSaveable(room.id) { mutableStateOf(room.notes) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
    ) {
        RoomImage(
            room = room,
            modifier = Modifier
                .fillMaxWidth()
                .height(196.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(imageScrim())
            )
            StatusPill(
                label = room.status.label,
                tone = room.status.tone(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Room ${room.number}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${room.type.label} · floor ${room.floor} · ${money(room.type.nightlyRate)} / night",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            amenitiesFor(room.type).forEach { (icon, label) ->
                AmenityChip(icon = icon, label = label)
            }
        }

        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                DetailRow("Nightly rate", money(room.type.nightlyRate))
                DetailRow("Sleeps", guestsLabel(room.type.maxGuests))
                DetailRow("Floor", room.floor.toString())
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionHeader("Occupancy")
        Spacer(Modifier.height(8.dp))
        if (occupant != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenBooking(occupant.id) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GuestAvatar(
                        name = occupant.guestName,
                        container = MaterialTheme.colorScheme.secondary,
                        content = MaterialTheme.colorScheme.onSecondary
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = occupant.guestName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stayRange(occupant.checkIn, occupant.checkOut),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(onClick = { repo.checkOut(occupant.id)?.let(onMessage) }) {
                        Text("Check out")
                    }
                }
            }
        } else {
            Text(
                text = "The room is empty.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (next != null) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenBooking(next.id) },
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        text = "Next reservation",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = next.guestName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stayRange(next.checkIn, next.checkOut),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionHeader("Housekeeping")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(RoomStatus.AVAILABLE, RoomStatus.CLEANING, RoomStatus.MAINTENANCE)
                .filter { it != room.status }
                .forEach { status ->
                    OutlinedButton(
                        onClick = { repo.setRoomStatus(room.id, status)?.let(onMessage) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(status.label, style = MaterialTheme.typography.labelLarge)
                    }
                }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(Modifier.height(20.dp))
        SectionHeader("Room note")
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = noteDraft,
            onValueChange = { noteDraft = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Maintenance or service note") },
            minLines = 2
        )
        if (noteDraft != room.notes) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { noteDraft = room.notes }) { Text("Discard") }
                Button(onClick = { repo.setRoomNotes(room.id, noteDraft) }) { Text("Save note") }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onBookRoom(room.id) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Book this room")
        }
        Spacer(Modifier.height(32.dp))
    }
}

/** What the grade includes — the line the desk repeats to every caller. */
private fun amenitiesFor(type: RoomType): List<Pair<ImageVector, String>> = buildList {
    add(Icons.Filled.Wifi to "Wi-Fi")
    add(Icons.Filled.AcUnit to "Air con")
    if (type != RoomType.STANDARD) add(Icons.Filled.FreeBreakfast to "Breakfast")
    if (type == RoomType.SUITE) add(Icons.Filled.Balcony to "Balcony")
}

@Composable
private fun AmenityChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
