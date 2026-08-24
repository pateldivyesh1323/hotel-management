package com.example.hotel_management_app.ui.housekeeping

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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.data.Room
import com.example.hotel_management_app.data.RoomStatus
import com.example.hotel_management_app.ui.components.EmptyHint
import com.example.hotel_management_app.ui.components.PanelCard
import com.example.hotel_management_app.ui.components.RoomThumbnail
import com.example.hotel_management_app.ui.components.SectionHeader
import com.example.hotel_management_app.ui.components.StatTile
import com.example.hotel_management_app.ui.components.StatusPill
import com.example.hotel_management_app.ui.theme.tone

/**
 * The housekeeping board: what has to be turned over now, what is out of service, and
 * what is clean and sellable. Every row can be moved on without leaving the screen.
 */
@Composable
fun HousekeepingScreen(
    repo: HotelRepository,
    onOpenRoom: (String) -> Unit,
    onMessage: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val toClean = repo.rooms.filter { it.status == RoomStatus.CLEANING }.sortedBy { it.number }
    val outOfService = repo.rooms.filter { it.status == RoomStatus.MAINTENANCE }.sortedBy { it.number }
    val occupied = repo.rooms.filter { it.status == RoomStatus.OCCUPIED }.sortedBy { it.number }
    val ready = repo.rooms.filter { it.status == RoomStatus.AVAILABLE }.sortedBy { it.number }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    value = toClean.size.toString(),
                    label = "To clean",
                    modifier = Modifier.weight(1f),
                    container = MaterialTheme.colorScheme.primaryContainer
                )
                StatTile(
                    value = ready.size.toString(),
                    label = "Ready to sell",
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = outOfService.size.toString(),
                    label = "Out of service",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { SectionHeader("Awaiting turnover") }
        if (toClean.isEmpty()) {
            item { EmptyHint("Every room is turned over") }
        } else {
            items(toClean, key = { it.id }) { room ->
                HousekeepingRow(
                    room = room,
                    primaryLabel = "Mark clean",
                    onPrimary = {
                        repo.setRoomStatus(room.id, RoomStatus.AVAILABLE)?.let(onMessage)
                    },
                    secondaryLabel = "Out of service",
                    onSecondary = {
                        repo.setRoomStatus(room.id, RoomStatus.MAINTENANCE)?.let(onMessage)
                    },
                    onClick = { onOpenRoom(room.id) }
                )
            }
        }

        item { SectionHeader("Out of service") }
        if (outOfService.isEmpty()) {
            item { EmptyHint("Nothing is out of service") }
        } else {
            items(outOfService, key = { it.id }) { room ->
                HousekeepingRow(
                    room = room,
                    primaryLabel = "Back in service",
                    onPrimary = {
                        repo.setRoomStatus(room.id, RoomStatus.CLEANING)?.let(onMessage)
                    },
                    onClick = { onOpenRoom(room.id) }
                )
            }
        }

        item { SectionHeader("Occupied (${occupied.size})") }
        if (occupied.isEmpty()) {
            item { EmptyHint("No rooms are occupied") }
        } else {
            items(occupied, key = { it.id }) { room ->
                HousekeepingRow(
                    room = room,
                    occupant = repo.currentBookingFor(room.id)?.guestName,
                    onClick = { onOpenRoom(room.id) }
                )
            }
        }

        item { SectionHeader("Ready to sell (${ready.size})") }
        items(ready, key = { it.id }) { room ->
            HousekeepingRow(room = room, onClick = { onOpenRoom(room.id) })
        }
    }
}

@Composable
private fun HousekeepingRow(
    room: Room,
    modifier: Modifier = Modifier,
    occupant: String? = null,
    primaryLabel: String? = null,
    onPrimary: () -> Unit = {},
    secondaryLabel: String? = null,
    onSecondary: () -> Unit = {},
    onClick: () -> Unit
) {
    PanelCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoomThumbnail(room)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Room ${room.number}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = listOfNotNull(
                            room.type.label,
                            "floor ${room.floor}",
                            occupant
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusPill(label = room.status.label, tone = room.status.tone())
            }
            if (room.notes.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = room.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (primaryLabel != null || secondaryLabel != null) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    primaryLabel?.let { Button(onClick = onPrimary) { Text(it) } }
                    secondaryLabel?.let { OutlinedButton(onClick = onSecondary) { Text(it) } }
                }
            }
        }
    }
}
