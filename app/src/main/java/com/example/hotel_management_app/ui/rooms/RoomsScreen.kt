package com.example.hotel_management_app.ui.rooms

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.data.Room
import com.example.hotel_management_app.data.RoomStatus
import com.example.hotel_management_app.ui.components.PanelCard
import com.example.hotel_management_app.ui.components.StatusPill
import com.example.hotel_management_app.ui.money
import com.example.hotel_management_app.ui.theme.tone

/** The room rack: every room on the property, filterable by what state it is in. */
@Composable
fun RoomsScreen(
    repo: HotelRepository,
    onOpenRoom: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    var filter by rememberSaveable { mutableStateOf<RoomStatus?>(null) }
    val rooms = repo.rooms.sortedBy { it.number }
    val shown = rooms.filter { filter == null || it.status == filter }
    val counts = rooms.groupingBy { it.status }.eachCount()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Text(
                    text = "Rooms",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
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
                        label = { Text("All ${rooms.size}") }
                    )
                    RoomStatus.entries.forEach { status ->
                        FilterChip(
                            selected = filter == status,
                            onClick = { filter = if (filter == status) null else status },
                            label = { Text("${status.label} ${counts[status] ?: 0}") }
                        )
                    }
                }
            }
        }

        items(shown, key = { it.id }) { room ->
            RoomCard(
                room = room,
                occupantName = repo.currentBookingFor(room.id)?.guestName,
                onClick = { onOpenRoom(room.id) }
            )
        }

        if (shown.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "No rooms in that state right now.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun RoomCard(
    room: Room,
    occupantName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PanelCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = room.number,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${room.type.label} · floor ${room.floor}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            StatusPill(label = room.status.label, tone = room.status.tone())
            Spacer(Modifier.height(10.dp))
            Text(
                text = occupantName ?: "${money(room.type.nightlyRate)} / night",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (occupantName != null) FontWeight.Medium else FontWeight.Normal,
                color = if (occupantName != null) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
