package com.example.hotel_management_app.data

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pure booking logic, kept free of Android and Compose types so it can be unit tested
 * directly. [HotelRepository] holds the state; this object decides what is legal.
 */
object BookingRules {

    /**
     * Half-open interval comparison: a stay ending on the day another begins does not
     * clash, because the departing guest leaves before the arriving one checks in.
     */
    fun overlaps(
        firstIn: LocalDate,
        firstOut: LocalDate,
        secondIn: LocalDate,
        secondOut: LocalDate
    ): Boolean = firstIn < secondOut && secondIn < firstOut

    /** The existing booking that would clash with this stay, or null when the room is free. */
    fun conflictFor(
        bookings: List<Booking>,
        roomId: String,
        checkIn: LocalDate,
        checkOut: LocalDate,
        ignoreBookingId: String? = null
    ): Booking? = bookings.firstOrNull { existing ->
        existing.roomId == roomId &&
            existing.id != ignoreBookingId &&
            existing.holdsRoom &&
            overlaps(existing.checkIn, existing.checkOut, checkIn, checkOut)
    }

    /** Rooms that can be sold for the given dates, cheapest type first. */
    fun availableRooms(
        rooms: List<Room>,
        bookings: List<Booking>,
        checkIn: LocalDate,
        checkOut: LocalDate,
        guests: Int = 1,
        ignoreBookingId: String? = null
    ): List<Room> = rooms
        .filter { room ->
            room.status != RoomStatus.MAINTENANCE &&
                room.type.maxGuests >= guests &&
                conflictFor(bookings, room.id, checkIn, checkOut, ignoreBookingId) == null
        }
        .sortedWith(compareBy({ it.type.nightlyRate }, { it.number }))

    /** Null when the draft is bookable, otherwise a message to show the user. */
    fun validate(
        guestName: String,
        room: Room?,
        checkIn: LocalDate,
        checkOut: LocalDate,
        guests: Int
    ): String? = when {
        guestName.isBlank() -> "Enter the guest's name"
        !checkOut.isAfter(checkIn) -> "Check-out must be after check-in"
        room == null -> "Pick a room"
        guests < 1 -> "A booking needs at least one guest"
        guests > room.type.maxGuests ->
            "${room.type.label} rooms sleep up to ${room.type.maxGuests} guests"
        else -> null
    }

    fun arrivalsOn(bookings: List<Booking>, date: LocalDate): List<Booking> =
        bookings.filter { it.status == BookingStatus.RESERVED && it.checkIn == date }
            .sortedBy { it.guestName }

    fun departuresOn(bookings: List<Booking>, date: LocalDate): List<Booking> =
        bookings.filter { it.status == BookingStatus.CHECKED_IN && it.checkOut == date }
            .sortedBy { it.guestName }

    fun inHouse(bookings: List<Booking>): List<Booking> =
        bookings.filter { it.status == BookingStatus.CHECKED_IN }

    fun total(booking: Booking, room: Room?): Int =
        booking.nights * (room?.type?.nightlyRate ?: 0)

    fun stats(rooms: List<Room>, bookings: List<Booking>, today: LocalDate): DashboardStats {
        val byStatus = rooms.groupingBy { it.status }.eachCount()
        val staying = inHouse(bookings)
        val roomsById = rooms.associateBy { it.id }
        val live = bookings.filter { it.status != BookingStatus.CANCELLED }
        return DashboardStats(
            totalRooms = rooms.size,
            occupied = byStatus[RoomStatus.OCCUPIED] ?: 0,
            available = byStatus[RoomStatus.AVAILABLE] ?: 0,
            cleaning = byStatus[RoomStatus.CLEANING] ?: 0,
            maintenance = byStatus[RoomStatus.MAINTENANCE] ?: 0,
            reserved = bookings.count { it.status == BookingStatus.RESERVED },
            arrivalsToday = arrivalsOn(bookings, today).size,
            departuresToday = departuresOn(bookings, today).size,
            inHouseGuests = staying.sumOf { it.guests },
            revenueToday = staying.sumOf { roomsById[it.roomId]?.type?.nightlyRate ?: 0 },
            newBookings = bookings.count { !it.checkIn.isBefore(today.minusDays(30)) },
            totalRevenue = live.sumOf { total(it, roomsById[it.roomId]) }
        )
    }

    // --- Analytics ---------------------------------------------------------------

    /**
     * Revenue for each of the last [months] calendar months, oldest first. A stay is
     * booked to the month it starts in, which is how the property reports it.
     */
    fun revenueByMonth(
        bookings: List<Booking>,
        rooms: List<Room>,
        today: LocalDate,
        months: Int = 6
    ): List<SeriesPoint> {
        val roomsById = rooms.associateBy { it.id }
        val formatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
        return (months - 1 downTo 0).map { back ->
            val month = YearMonth.from(today).minusMonths(back.toLong())
            val amount = bookings
                .filter { it.status != BookingStatus.CANCELLED && YearMonth.from(it.checkIn) == month }
                .sumOf { total(it, roomsById[it.roomId]) }
            SeriesPoint(month.atDay(1).format(formatter), amount.toFloat())
        }
    }

    /** Reservations created for each of the last [days] days, oldest first. */
    fun reservationsByDay(
        bookings: List<Booking>,
        today: LocalDate,
        days: Int = 7
    ): Pair<List<SeriesPoint>, List<SeriesPoint>> {
        val formatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
        val booked = mutableListOf<SeriesPoint>()
        val cancelled = mutableListOf<SeriesPoint>()
        (days - 1 downTo 0).forEach { back ->
            val day = today.minusDays(back.toLong())
            val label = day.format(formatter)
            val onDay = bookings.filter { it.checkIn == day }
            booked += SeriesPoint(label, onDay.count { it.status != BookingStatus.CANCELLED }.toFloat())
            cancelled += SeriesPoint(label, onDay.count { it.status == BookingStatus.CANCELLED }.toFloat())
        }
        return booked to cancelled
    }

    /** Share of live bookings per channel, largest first. */
    fun channelMix(bookings: List<Booking>): List<Slice> = bookings
        .filter { it.status != BookingStatus.CANCELLED }
        .groupingBy { it.channel }
        .eachCount()
        .map { (channel, count) -> Slice(channel.label, count.toFloat()) }
        .sortedByDescending { it.value }

    /** Average nightly rate actually achieved across live bookings. */
    fun averageDailyRate(bookings: List<Booking>, rooms: List<Room>): Int {
        val roomsById = rooms.associateBy { it.id }
        val live = bookings.filter { it.status != BookingStatus.CANCELLED }
        val nights = live.sumOf { it.nights }
        if (nights == 0) return 0
        return live.sumOf { total(it, roomsById[it.roomId]) } / nights
    }

    /** Revenue per available room: the rate the property actually earns on its whole stock. */
    fun revPar(bookings: List<Booking>, rooms: List<Room>): Int {
        if (rooms.isEmpty()) return 0
        val roomsById = rooms.associateBy { it.id }
        return inHouse(bookings).sumOf { roomsById[it.roomId]?.type?.nightlyRate ?: 0 } / rooms.size
    }

    /** Revenue split by room category, for the financials breakdown. */
    fun revenueByRoomType(bookings: List<Booking>, rooms: List<Room>): List<Slice> {
        val roomsById = rooms.associateBy { it.id }
        return bookings
            .filter { it.status != BookingStatus.CANCELLED }
            .groupBy { roomsById[it.roomId]?.type }
            .mapNotNull { (type, stays) ->
                type?.let { Slice(it.label, stays.sumOf { stay -> total(stay, roomsById[stay.roomId]) }.toFloat()) }
            }
            .sortedByDescending { it.value }
    }

    /** Week-on-week change for arrivals, departures and new reservations. */
    fun weekTrend(
        bookings: List<Booking>,
        today: LocalDate,
        select: (Booking) -> LocalDate
    ): Trend {
        val thisWeek = bookings.count { !select(it).isBefore(today.minusDays(6)) && !select(it).isAfter(today) }
        val lastWeek = bookings.count {
            !select(it).isBefore(today.minusDays(13)) && select(it).isBefore(today.minusDays(6))
        }
        return percentChange(thisWeek.toFloat(), lastWeek.toFloat())
    }

    /** Month-on-month revenue change, used by the total-revenue tile. */
    fun revenueTrend(bookings: List<Booking>, rooms: List<Room>, today: LocalDate): Trend {
        val series = revenueByMonth(bookings, rooms, today, months = 2)
        return percentChange(series.last().value, series.first().value)
    }

    /**
     * A growth of nothing into something is reported as +100% rather than infinity, which
     * is what the tile has room to show.
     */
    fun percentChange(current: Float, previous: Float): Trend = when {
        previous > 0f -> Trend(((current - previous) / previous) * 100f)
        current > 0f -> Trend(100f)
        else -> Trend(0f)
    }

    /** Bookings that hold a room on [date] — the calendar's per-day occupancy. */
    fun occupancyOn(bookings: List<Booking>, date: LocalDate): List<Booking> =
        bookings.filter { it.holdsRoom && !date.isBefore(it.checkIn) && date.isBefore(it.checkOut) }

    /** Collapses the booking history into one entry per guest (matched on name + phone). */
    fun guestSummaries(bookings: List<Booking>, rooms: List<Room>): List<GuestSummary> {
        val roomsById = rooms.associateBy { it.id }
        return bookings
            .filter { it.status != BookingStatus.CANCELLED }
            .groupBy { it.guestName.trim().lowercase() to it.guestPhone.trim() }
            .map { (_, stays) ->
                val latest = stays.maxBy { it.checkIn }
                GuestSummary(
                    name = latest.guestName,
                    phone = latest.guestPhone,
                    email = stays.firstOrNull { it.guestEmail.isNotBlank() }?.guestEmail.orEmpty(),
                    stays = stays.size,
                    nights = stays.sumOf { it.nights },
                    spend = stays.sumOf { total(it, roomsById[it.roomId]) },
                    inHouse = stays.any { it.status == BookingStatus.CHECKED_IN },
                    bookingIds = stays.sortedByDescending { it.checkIn }.map { it.id }
                )
            }
            .sortedWith(compareByDescending<GuestSummary> { it.inHouse }.thenBy { it.name })
    }
}
