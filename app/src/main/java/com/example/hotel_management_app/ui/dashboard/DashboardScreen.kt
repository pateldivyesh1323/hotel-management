package com.example.hotel_management_app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.hotel_management_app.data.ActivityEntry
import com.example.hotel_management_app.data.ActivityKind
import com.example.hotel_management_app.data.Booking
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.data.HotelTask
import com.example.hotel_management_app.data.RoomStatus
import com.example.hotel_management_app.ui.components.AreaChart
import com.example.hotel_management_app.ui.components.ChartLegend
import com.example.hotel_management_app.ui.components.DonutChart
import com.example.hotel_management_app.ui.components.EmptyHint
import com.example.hotel_management_app.ui.components.GroupedBarChart
import com.example.hotel_management_app.ui.components.GuestAvatar
import com.example.hotel_management_app.ui.components.IconBadge
import com.example.hotel_management_app.ui.components.KeyedStatTile
import com.example.hotel_management_app.ui.components.MetricTile
import com.example.hotel_management_app.ui.components.PanelCard
import com.example.hotel_management_app.ui.components.PanelSection
import com.example.hotel_management_app.ui.components.ScoreBar
import com.example.hotel_management_app.ui.components.SegmentedBar
import com.example.hotel_management_app.ui.components.StatusPill
import com.example.hotel_management_app.ui.clockTime
import com.example.hotel_management_app.ui.compactMoney
import com.example.hotel_management_app.ui.dueLabel
import com.example.hotel_management_app.ui.guestsLabel
import com.example.hotel_management_app.ui.money
import com.example.hotel_management_app.ui.nightsLabel
import com.example.hotel_management_app.ui.stayRange
import com.example.hotel_management_app.ui.theme.chartPalette
import com.example.hotel_management_app.ui.theme.tone

/**
 * The property at a glance: how today is trading, how the rooms are selling, what the
 * guests think, and what the team still owes the hotel before the shift ends.
 */
@Composable
fun DashboardScreen(
    repo: HotelRepository,
    onOpenBooking: (String) -> Unit,
    onOpenRoom: (String) -> Unit,
    onOpenTasks: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenReviews: () -> Unit,
    onOpenBookings: () -> Unit,
    onMessage: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val today = repo.currentDate()
    val stats = repo.stats()
    val palette = chartPalette()
    var query by rememberSaveable { mutableStateOf("") }
    var revenueMonths by rememberSaveable { mutableIntStateOf(6) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }

    val revenue = repo.revenueSeries(revenueMonths)
    val (booked, cancelled) = repo.reservationSeries()
    val channels = repo.channelMix()
    val rating = repo.rating()
    val tasks = repo.openTasks().take(4)

    val bookings = remember(query, repo.bookings.toList()) {
        val needle = query.trim().lowercase()
        repo.bookings
            .filter { booking ->
                needle.isBlank() ||
                    booking.guestName.lowercase().contains(needle) ||
                    booking.id.lowercase().contains(needle) ||
                    repo.room(booking.roomId)?.number.orEmpty().contains(needle) ||
                    booking.status.label.lowercase().contains(needle)
            }
            .sortedByDescending { it.checkIn }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DashboardHeader(
                unread = repo.unreadMessages(),
                onOpenMessages = onOpenMessages
            )
        }

        item {
            SearchField(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Search room, guest, booking"
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(
                    label = "New bookings",
                    value = stats.newBookings.toString(),
                    icon = Icons.Filled.CalendarMonth,
                    trend = repo.bookingTrend(),
                    highlighted = true,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenBookings
                )
                MetricTile(
                    label = "Check-in",
                    value = stats.arrivalsToday.toString(),
                    icon = Icons.AutoMirrored.Filled.Login,
                    trend = repo.arrivalTrend(),
                    modifier = Modifier.weight(1f),
                    onClick = onOpenBookings
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(
                    label = "Check-out",
                    value = stats.departuresToday.toString(),
                    icon = Icons.AutoMirrored.Filled.Logout,
                    trend = repo.departureTrend(),
                    modifier = Modifier.weight(1f),
                    onClick = onOpenBookings
                )
                MetricTile(
                    label = "Total revenue",
                    value = compactMoney(stats.totalRevenue.toFloat()),
                    icon = Icons.Filled.Payments,
                    trend = repo.revenueTrend(),
                    caption = "from last month",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            PanelSection(title = "Room availability") {
                Column {
                    SegmentedBar(slices = repo.availabilityMix(), colors = palette)
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth()) {
                        KeyedStatTile(
                            value = stats.occupied.toString(),
                            label = "Occupied",
                            keyColor = palette[0],
                            modifier = Modifier.weight(1f)
                        )
                        KeyedStatTile(
                            value = stats.reserved.toString(),
                            label = "Reserved",
                            keyColor = palette[1],
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth()) {
                        KeyedStatTile(
                            value = stats.available.toString(),
                            label = "Available",
                            keyColor = palette[2],
                            modifier = Modifier.weight(1f)
                        )
                        KeyedStatTile(
                            value = (stats.cleaning + stats.maintenance).toString(),
                            label = "Not ready",
                            keyColor = palette[3],
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            PanelSection(
                title = "Revenue",
                trailing = {
                    PeriodPicker(
                        months = revenueMonths,
                        onSelect = {
                            revenueMonths = it
                            selectedMonth = null
                        }
                    )
                }
            ) {
                Column {
                    val index = selectedMonth?.coerceIn(revenue.indices)
                    Text(
                        text = if (index != null) {
                            "${revenue[index].label}: ${money(revenue[index].value.toInt())}"
                        } else {
                            "Tap the chart for a month"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    AreaChart(
                        points = revenue,
                        selectedIndex = index,
                        onSelect = { selectedMonth = it },
                        valueLabel = { compactMoney(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            PanelSection(
                title = "Reservations",
                trailing = {
                    Text(
                        text = "Last 7 days",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            ) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        LegendDot("Booked", palette[0])
                        LegendDot("Cancelled", palette[2])
                    }
                    Spacer(Modifier.height(12.dp))
                    GroupedBarChart(
                        primary = booked,
                        secondary = cancelled,
                        primaryColor = palette[0],
                        secondaryColor = palette[2],
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            PanelSection(title = "Booking by platform") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DonutChart(
                        slices = channels,
                        colors = palette,
                        size = 132,
                        centerLabel = channels.sumOf { it.value.toInt() }.toString(),
                        centerCaption = "bookings"
                    )
                    Spacer(Modifier.width(14.dp))
                    ChartLegend(
                        slices = channels,
                        colors = palette,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            PanelSection(
                title = "Overall rating",
                trailing = {
                    TextButton(onClick = onOpenReviews) { Text("All reviews") }
                }
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.shapes.medium
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "%.1f".format(rating.overall),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "/5",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Impressive",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "from ${"%,d".format(rating.reviews)} reviews",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    rating.facets.forEach { (label, score) ->
                        ScoreBar(
                            label = label,
                            score = score,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            PanelSection(
                title = "Tasks",
                trailing = {
                    IconButton(onClick = onOpenTasks) {
                        Icon(Icons.Filled.Add, contentDescription = "Add task")
                    }
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (tasks.isEmpty()) {
                        EmptyHint("Nothing outstanding")
                    } else {
                        tasks.forEach { task ->
                            TaskRow(
                                task = task,
                                dueText = dueLabel(task.due, today),
                                onToggle = { repo.toggleTask(task.id) }
                            )
                        }
                    }
                }
            }
        }

        item {
            PanelSection(title = "Recent activities") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    repo.activity.take(5).forEach { entry -> ActivityRow(entry) }
                }
            }
        }

        item {
            PanelSection(
                title = "Booking list",
                trailing = {
                    TextButton(onClick = onOpenBookings) { Text("See all") }
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (bookings.isEmpty()) {
                        EmptyHint("No bookings match \"$query\"")
                    } else {
                        bookings.take(6).forEach { booking ->
                            BookingRow(
                                booking = booking,
                                roomNumber = repo.room(booking.roomId)?.number,
                                roomType = repo.room(booking.roomId)?.type?.label,
                                onClick = { onOpenBooking(booking.id) }
                            )
                        }
                    }
                }
            }
        }

        item {
            PanelSection(title = "Housekeeping") {
                val toClean = repo.rooms.filter { it.status == RoomStatus.CLEANING }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (toClean.isEmpty()) {
                        EmptyHint("Every room is turned over")
                    } else {
                        toClean.forEach { room ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenRoom(room.id) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "Room ${room.number}",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "${room.type.label} · floor ${room.floor}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        repo.setRoomStatus(room.id, RoomStatus.AVAILABLE)
                                            ?.let(onMessage)
                                    }
                                ) { Text("Mark clean") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    unread: Int,
    onOpenMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Lodgify Grand · front desk",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box {
            IconButton(onClick = onOpenMessages) {
                Icon(Icons.Filled.Notifications, contentDescription = "Messages")
            }
            if (unread > 0) {
                Box(
                    modifier = Modifier
                        .padding(start = 26.dp, top = 6.dp)
                        .size(16.dp)
                        .background(MaterialTheme.colorScheme.error, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unread.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
        Spacer(Modifier.width(4.dp))
        GuestAvatar(name = "Jaylon Dorwart", size = 38)
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

/** "Last 6 months" dropdown on the revenue card. */
@Composable
private fun PeriodPicker(
    months: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var open by remember { mutableStateOf(false) }
    val options = listOf(3, 6, 12)
    Box(modifier) {
        Row(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(50)
                )
                .clickable { open = true }
                .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Last $months months",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text("Last $option months") },
                    onClick = {
                        onSelect(option)
                        open = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(9.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TaskRow(
    task: HotelTask,
    dueText: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    PanelCard(
        modifier = modifier.fillMaxWidth(),
        container = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        if (task.done) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        RoundedCornerShape(6.dp)
                    )
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                if (task.done) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Done",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = dueText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ActivityRow(entry: ActivityEntry, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        IconBadge(icon = iconFor(entry.kind))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = entry.at.clockTime(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = entry.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BookingRow(
    booking: Booking,
    roomNumber: String?,
    roomType: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GuestAvatar(booking.guestName, size = 38)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = booking.guestName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listOfNotNull(
                    roomType,
                    roomNumber?.let { "Room $it" },
                    nightsLabel(booking.nights)
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = "${stayRange(booking.checkIn, booking.checkOut)} · ${guestsLabel(booking.guests)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(8.dp))
        StatusPill(label = booking.status.label, tone = booking.status.tone())
    }
}

private fun iconFor(kind: ActivityKind) = when (kind) {
    ActivityKind.CHECK_IN -> Icons.AutoMirrored.Filled.Login
    ActivityKind.CHECK_OUT -> Icons.AutoMirrored.Filled.Logout
    ActivityKind.BOOKING -> Icons.Filled.EventAvailable
    ActivityKind.HOUSEKEEPING -> Icons.Filled.CleaningServices
    ActivityKind.MAINTENANCE -> Icons.Filled.Handyman
    ActivityKind.INVENTORY -> Icons.Filled.Inventory2
    ActivityKind.MESSAGE -> Icons.Filled.ChatBubbleOutline
    ActivityKind.EVENTS -> Icons.Filled.Celebration
}
