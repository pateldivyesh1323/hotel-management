package com.example.hotel_management_app.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/** Room categories, each with its own nightly rate and occupancy limit. */
enum class RoomType(val label: String, val nightlyRate: Int, val maxGuests: Int) {
    STANDARD("Standard", 120, 2),
    DELUXE("Deluxe", 190, 3),
    SUITE("Suite", 320, 4)
}

/** Housekeeping / front-desk state of a physical room. */
enum class RoomStatus(val label: String) {
    AVAILABLE("Available"),
    OCCUPIED("Occupied"),
    CLEANING("Cleaning"),
    MAINTENANCE("Maintenance")
}

enum class BookingStatus(val label: String) {
    RESERVED("Reserved"),
    CHECKED_IN("Checked in"),
    CHECKED_OUT("Checked out"),
    CANCELLED("Cancelled")
}

/** Where the reservation came from — drives the booking-mix breakdown. */
enum class BookingChannel(val label: String) {
    DIRECT("Direct booking"),
    BOOKING_COM("Booking.com"),
    AGODA("Agoda"),
    AIRBNB("Airbnb"),
    HOTELS_COM("Hotels.com"),
    OTHER("Others")
}

data class Room(
    val id: String,
    val number: String,
    val floor: Int,
    val type: RoomType,
    val status: RoomStatus = RoomStatus.AVAILABLE,
    val notes: String = ""
)

data class Booking(
    val id: String,
    val roomId: String,
    val guestName: String,
    val guestPhone: String = "",
    val guestEmail: String = "",
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val guests: Int = 1,
    val status: BookingStatus = BookingStatus.RESERVED,
    val channel: BookingChannel = BookingChannel.DIRECT,
    val notes: String = ""
) {
    /** A same-day booking still bills one night. */
    val nights: Int
        get() = ChronoUnit.DAYS.between(checkIn, checkOut).toInt().coerceAtLeast(1)

    /** True while the booking still holds the room against other reservations. */
    val holdsRoom: Boolean
        get() = status == BookingStatus.RESERVED || status == BookingStatus.CHECKED_IN
}

/** A guest identity derived from their booking history. */
data class GuestSummary(
    val name: String,
    val phone: String,
    val email: String,
    val stays: Int,
    val nights: Int,
    val spend: Int,
    val inHouse: Boolean,
    val bookingIds: List<String>
)

data class DashboardStats(
    val totalRooms: Int,
    val occupied: Int,
    val available: Int,
    val cleaning: Int,
    val maintenance: Int,
    val reserved: Int,
    val arrivalsToday: Int,
    val departuresToday: Int,
    val inHouseGuests: Int,
    val revenueToday: Int,
    val newBookings: Int,
    val totalRevenue: Int
) {
    val occupancyRate: Float
        get() = if (totalRooms == 0) 0f else occupied.toFloat() / totalRooms
}

// --- Operations ------------------------------------------------------------------

enum class TaskPriority(val label: String) {
    HIGH("High"),
    NORMAL("Normal"),
    LOW("Low")
}

enum class TaskArea(val label: String) {
    HOUSEKEEPING("Housekeeping"),
    FRONT_DESK("Front desk"),
    MAINTENANCE("Maintenance"),
    EVENTS("Events")
}

data class HotelTask(
    val id: String,
    val title: String,
    val due: LocalDate,
    val area: TaskArea = TaskArea.FRONT_DESK,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val done: Boolean = false
)

enum class ActivityKind(val label: String) {
    CHECK_IN("Check-in"),
    CHECK_OUT("Check-out"),
    BOOKING("Booking"),
    HOUSEKEEPING("Housekeeping"),
    MAINTENANCE("Maintenance"),
    INVENTORY("Inventory"),
    MESSAGE("Message"),
    EVENTS("Events")
}

/** One line in the recent-activity feed. */
data class ActivityEntry(
    val id: String,
    val kind: ActivityKind,
    val title: String,
    val detail: String,
    val at: LocalDateTime
)

/** A guest review, and the per-facet scores the rating card breaks down. */
data class Review(
    val id: String,
    val guestName: String,
    val roomNumber: String,
    val rating: Float,
    val comment: String,
    val date: LocalDate
)

data class RatingBreakdown(
    val overall: Float,
    val reviews: Int,
    val facilities: Float,
    val cleanliness: Float,
    val services: Float,
    val comfort: Float,
    val location: Float
) {
    /** Label/score pairs in the order the rating card lists them. */
    val facets: List<Pair<String, Float>>
        get() = listOf(
            "Facilities" to facilities,
            "Cleanliness" to cleanliness,
            "Services" to services,
            "Comfort" to comfort,
            "Location" to location
        )
}

data class InventoryItem(
    val id: String,
    val name: String,
    val category: String,
    val quantity: Int,
    val threshold: Int,
    val unit: String
) {
    val low: Boolean get() = quantity <= threshold
}

data class ChatMessage(
    val fromGuest: Boolean,
    val text: String,
    val at: LocalDateTime
)

data class MessageThread(
    val id: String,
    val guestName: String,
    val roomNumber: String,
    val unread: Boolean,
    val messages: List<ChatMessage>
) {
    val preview: String get() = messages.lastOrNull()?.text.orEmpty()
    val lastAt: LocalDateTime? get() = messages.lastOrNull()?.at
}

/** One point on a time series — a month of revenue, or a day of reservations. */
data class SeriesPoint(val label: String, val value: Float)

/** A share of some total, used by the donut and the rating bars. */
data class Slice(val label: String, val value: Float)

/** A period-over-period change, rendered as the little up/down pill on a stat tile. */
data class Trend(val percent: Float) {
    val up: Boolean get() = percent >= 0f
}
