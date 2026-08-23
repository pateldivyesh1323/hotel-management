package com.example.hotel_management_app.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.data.RoomStatus
import com.example.hotel_management_app.ui.components.IconBadge
import com.example.hotel_management_app.ui.components.PanelCard
import com.example.hotel_management_app.ui.components.SectionHeader

/** Where the app's departmental screens live, with a badge for anything needing attention. */
@Composable
fun OperationsScreen(
    repo: HotelRepository,
    onOpenHousekeeping: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenFinancials: () -> Unit,
    onOpenReviews: () -> Unit,
    onMessage: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val notReady = repo.rooms.count {
        it.status == RoomStatus.CLEANING || it.status == RoomStatus.MAINTENANCE
    }
    val entries = listOf(
        Entry("Housekeeping", "Turnovers and out-of-service rooms", Icons.Filled.CleaningServices, notReady, onOpenHousekeeping),
        Entry("Tasks", "What the shift still owes the hotel", Icons.Filled.TaskAlt, repo.openTasks().size, onOpenTasks),
        Entry("Messages", "Guest conversations", Icons.Filled.ChatBubbleOutline, repo.unreadMessages(), onOpenMessages),
        Entry("Inventory", "Linen, amenities and minibar stock", Icons.Filled.Inventory2, repo.lowStock().size, onOpenInventory),
        Entry("Calendar", "Occupancy month by month", Icons.Filled.CalendarMonth, 0, onOpenCalendar),
        Entry("Financials", "Revenue, ADR and RevPAR", Icons.AutoMirrored.Filled.TrendingUp, 0, onOpenFinancials),
        Entry("Reviews", "Ratings and guest feedback", Icons.Filled.StarRate, 0, onOpenReviews)
    )

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { SectionHeader("Operations") }

        entries.forEach { entry ->
            item(key = entry.title) { EntryRow(entry) }
        }

        item { Spacer(Modifier.width(0.dp)) }

        item {
            OutlinedButton(
                onClick = {
                    repo.resetToSample()
                    onMessage("Property reset to the sample data")
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Reset demo data") }
        }
    }
}

private data class Entry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badge: Int,
    val onClick: () -> Unit
)

@Composable
private fun EntryRow(entry: Entry, modifier: Modifier = Modifier) {
    PanelCard(modifier = modifier.fillMaxWidth(), onClick = entry.onClick) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = entry.icon, size = 40)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(text = entry.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = entry.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (entry.badge > 0) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = entry.badge.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
