package com.example.hotel_management_app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hotel_management_app.data.BookingStatus
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.ui.components.EmptyHint
import com.example.hotel_management_app.ui.components.GuestAvatar
import com.example.hotel_management_app.ui.components.PanelCard
import com.example.hotel_management_app.ui.components.PanelSection
import com.example.hotel_management_app.ui.components.SectionHeader
import com.example.hotel_management_app.ui.components.StatTile
import com.example.hotel_management_app.ui.components.StatusPill
import com.example.hotel_management_app.ui.full
import com.example.hotel_management_app.ui.stayRange
import com.example.hotel_management_app.ui.theme.tone
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * A month at a time, each day shaded by how full the property is. Tapping a day shows who
 * is staying, arriving and leaving on it.
 */
@Composable
fun CalendarScreen(
    repo: HotelRepository,
    onOpenBooking: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val today = repo.currentDate()
    var monthOffset by rememberSaveable { mutableStateOf(0) }
    var selectedDay by rememberSaveable { mutableStateOf(today.toString()) }

    val month = YearMonth.from(today).plusMonths(monthOffset.toLong())
    val selected = LocalDate.parse(selectedDay)
    val totalRooms = repo.rooms.size.coerceAtLeast(1)

    val staying = repo.occupancyOn(selected)
    val arrivals = repo.bookings.filter { it.checkIn == selected && it.status != BookingStatus.CANCELLED }
    val departures = repo.bookings.filter { it.checkOut == selected && it.status != BookingStatus.CANCELLED }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PanelSection(
                title = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                trailing = {
                    Row {
                        IconButton(onClick = { monthOffset-- }) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous month"
                            )
                        }
                        IconButton(onClick = { monthOffset++ }) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next month"
                            )
                        }
                    }
                }
            ) {
                MonthGrid(
                    month = month,
                    today = today,
                    selected = selected,
                    occupancyOf = { date ->
                        repo.occupancyOn(date).size.toFloat() / totalRooms
                    },
                    onSelect = { selectedDay = it.toString() }
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    value = staying.size.toString(),
                    label = "Rooms sold",
                    modifier = Modifier.weight(1f),
                    container = MaterialTheme.colorScheme.primaryContainer
                )
                StatTile(
                    value = arrivals.size.toString(),
                    label = "Arrivals",
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = departures.size.toString(),
                    label = "Departures",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { SectionHeader(selected.full()) }

        if (staying.isEmpty() && arrivals.isEmpty() && departures.isEmpty()) {
            item { EmptyHint("Nothing booked on this day") }
        } else {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    (arrivals + staying + departures).distinctBy { it.id }.forEach { booking ->
                        PanelCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onOpenBooking(booking.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GuestAvatar(booking.guestName, size = 38)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = booking.guestName,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "Room ${repo.room(booking.roomId)?.number ?: "—"} · " +
                                            stayRange(booking.checkIn, booking.checkOut),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusPill(
                                    label = when (selected) {
                                        booking.checkIn -> "Arrives"
                                        booking.checkOut -> "Departs"
                                        else -> "In house"
                                    },
                                    tone = booking.status.tone()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    selected: LocalDate,
    occupancyOf: (LocalDate) -> Float,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDay = month.atDay(1)
    // Monday-first grid: DayOfWeek.MONDAY is 1, so an offset of value - 1 lines it up.
    val leadingBlanks = firstDay.dayOfWeek.value - 1
    val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")
    val cells = leadingBlanks + month.lengthOfMonth()
    val rows = (cells + 6) / 7

    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            weekdays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        repeat(rows) { row ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { column ->
                    val dayNumber = row * 7 + column - leadingBlanks + 1
                    if (dayNumber < 1 || dayNumber > month.lengthOfMonth()) {
                        Box(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = month.atDay(dayNumber)
                        DayCell(
                            day = dayNumber,
                            occupancy = occupancyOf(date),
                            isToday = date == today,
                            isSelected = date == selected,
                            onClick = { onSelect(date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    occupancy: Float,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Fuller days sit deeper in the brand colour, so a month reads as a heat map. An
    // empty day stays clear; anything sold starts at a tint that is actually visible.
    val alpha = if (occupancy <= 0f) 0f else 0.18f + occupancy.coerceAtMost(1f) * 0.72f
    val container = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(container, RoundedCornerShape(10.dp))
            .then(
                if (isSelected) {
                    Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.onSurface,
                        RoundedCornerShape(10.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (alpha > 0.55f) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
