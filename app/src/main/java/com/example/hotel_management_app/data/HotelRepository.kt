package com.example.hotel_management_app.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Single source of truth for the property. Screens read the observable lists directly;
 * every mutation writes through to [storage].
 *
 * Operations that a user can get wrong return a message describing what went wrong, and
 * null when they succeeded.
 */
class HotelRepository(
    private val storage: HotelStorage? = null,
    private val today: () -> LocalDate = LocalDate::now,
    private val now: () -> LocalDateTime = LocalDateTime::now
) {
    private val _rooms: SnapshotStateList<Room> = mutableStateListOf()
    private val _bookings: SnapshotStateList<Booking> = mutableStateListOf()
    private val _tasks: SnapshotStateList<HotelTask> = mutableStateListOf()
    private val _inventory: SnapshotStateList<InventoryItem> = mutableStateListOf()

    // Reviews, guest messages and the activity feed are demo content that the app only
    // ever appends to within a session, so they are not written back to storage.
    private val _activity: SnapshotStateList<ActivityEntry> = mutableStateListOf()
    private val _reviews: SnapshotStateList<Review> = mutableStateListOf()
    private val _threads: SnapshotStateList<MessageThread> = mutableStateListOf()

    val rooms: List<Room> get() = _rooms
    val bookings: List<Booking> get() = _bookings
    val tasks: List<HotelTask> get() = _tasks
    val inventory: List<InventoryItem> get() = _inventory
    val activity: List<ActivityEntry> get() = _activity
    val reviews: List<Review> get() = _reviews
    val threads: List<MessageThread> get() = _threads

    init {
        val saved = storage?.load()
        if (saved != null && saved.rooms.isNotEmpty()) {
            _rooms.addAll(saved.rooms)
            _bookings.addAll(saved.bookings)
            _tasks.addAll(saved.tasks.ifEmpty { SampleData.tasks(today()) })
            _inventory.addAll(saved.inventory.ifEmpty { SampleData.inventory() })
            seedSessionContent()
        } else {
            seed()
        }
    }

    // --- Lookups -----------------------------------------------------------------

    fun room(id: String?): Room? = _rooms.firstOrNull { it.id == id }

    fun booking(id: String?): Booking? = _bookings.firstOrNull { it.id == id }

    fun thread(id: String?): MessageThread? = _threads.firstOrNull { it.id == id }

    /** The stay currently occupying a room, if any. */
    fun currentBookingFor(roomId: String): Booking? =
        _bookings.firstOrNull { it.roomId == roomId && it.status == BookingStatus.CHECKED_IN }

    /** The next reservation for a room, ignoring stays already in the past. */
    fun nextBookingFor(roomId: String): Booking? =
        _bookings.filter {
            it.roomId == roomId &&
                it.status == BookingStatus.RESERVED &&
                !it.checkIn.isBefore(today())
        }.minByOrNull { it.checkIn }

    fun bookingsFor(guest: GuestSummary): List<Booking> =
        guest.bookingIds.mapNotNull { id -> booking(id) }

    fun total(booking: Booking): Int = BookingRules.total(booking, room(booking.roomId))

    fun stats(): DashboardStats = BookingRules.stats(_rooms, _bookings, today())

    fun arrivalsToday(): List<Booking> = BookingRules.arrivalsOn(_bookings, today())

    fun departuresToday(): List<Booking> = BookingRules.departuresOn(_bookings, today())

    fun availableRooms(checkIn: LocalDate, checkOut: LocalDate, guests: Int): List<Room> =
        BookingRules.availableRooms(_rooms, _bookings, checkIn, checkOut, guests)

    fun currentDate(): LocalDate = today()

    fun unreadMessages(): Int = _threads.count { it.unread }

    fun openTasks(): List<HotelTask> = _tasks.filter { !it.done }.sortedWith(
        compareBy({ it.due }, { it.priority.ordinal })
    )

    fun lowStock(): List<InventoryItem> = _inventory.filter { it.low }

    // --- Analytics ---------------------------------------------------------------

    fun revenueSeries(months: Int = 6): List<SeriesPoint> =
        BookingRules.revenueByMonth(_bookings, _rooms, today(), months)

    fun reservationSeries(days: Int = 7): Pair<List<SeriesPoint>, List<SeriesPoint>> =
        BookingRules.reservationsByDay(_bookings, today(), days)

    fun channelMix(): List<Slice> = BookingRules.channelMix(_bookings)

    fun revenueByRoomType(): List<Slice> = BookingRules.revenueByRoomType(_bookings, _rooms)

    fun averageDailyRate(): Int = BookingRules.averageDailyRate(_bookings, _rooms)

    fun revPar(): Int = BookingRules.revPar(_bookings, _rooms)

    fun occupancyOn(date: LocalDate): List<Booking> = BookingRules.occupancyOn(_bookings, date)

    fun bookingTrend(): Trend = BookingRules.weekTrend(_bookings, today()) { it.checkIn }

    fun arrivalTrend(): Trend = BookingRules.weekTrend(
        _bookings.filter { it.status != BookingStatus.CANCELLED },
        today()
    ) { it.checkIn }

    fun departureTrend(): Trend = BookingRules.weekTrend(
        _bookings.filter { it.status != BookingStatus.CANCELLED },
        today()
    ) { it.checkOut }

    fun revenueTrend(): Trend = BookingRules.revenueTrend(_bookings, _rooms, today())

    /** The four bands of the room-availability bar, in the order the card lists them. */
    fun availabilityMix(): List<Slice> {
        val stats = stats()
        return listOf(
            Slice("Occupied", stats.occupied.toFloat()),
            Slice("Reserved", stats.reserved.toFloat()),
            Slice("Available", stats.available.toFloat()),
            Slice("Not ready", (stats.cleaning + stats.maintenance).toFloat())
        )
    }

    /** The property's headline rating, blended with any reviews collected in-app. */
    fun rating(): RatingBreakdown = SampleData.rating()

    // --- Bookings ----------------------------------------------------------------

    fun createBooking(
        roomId: String,
        guestName: String,
        guestPhone: String,
        guestEmail: String,
        checkIn: LocalDate,
        checkOut: LocalDate,
        guests: Int,
        notes: String,
        channel: BookingChannel = BookingChannel.DIRECT
    ): String? {
        val room = room(roomId) ?: return "That room no longer exists"
        BookingRules.validate(guestName, room, checkIn, checkOut, guests)?.let { return it }
        BookingRules.conflictFor(_bookings, roomId, checkIn, checkOut)?.let { clash ->
            return "Room ${room.number} is taken by ${clash.guestName} on those dates"
        }
        _bookings.add(
            Booking(
                id = newBookingId(),
                roomId = roomId,
                guestName = guestName.trim(),
                guestPhone = guestPhone.trim(),
                guestEmail = guestEmail.trim(),
                checkIn = checkIn,
                checkOut = checkOut,
                guests = guests,
                status = BookingStatus.RESERVED,
                channel = channel,
                notes = notes.trim()
            )
        )
        log(
            ActivityKind.BOOKING,
            "New reservation",
            "${guestName.trim()} booked room ${room.number} via ${channel.label.lowercase()}."
        )
        persist()
        return null
    }

    fun checkIn(bookingId: String): String? {
        val booking = booking(bookingId) ?: return "Booking not found"
        if (booking.status != BookingStatus.RESERVED) {
            return "Only a reserved booking can be checked in"
        }
        val room = room(booking.roomId) ?: return "That room no longer exists"
        if (room.status == RoomStatus.OCCUPIED) {
            return "Room ${room.number} is still occupied"
        }
        if (room.status == RoomStatus.MAINTENANCE) {
            return "Room ${room.number} is out of service"
        }
        updateBooking(booking.copy(status = BookingStatus.CHECKED_IN))
        updateRoom(room.copy(status = RoomStatus.OCCUPIED))
        log(
            ActivityKind.CHECK_IN,
            "Guest check-in",
            "${booking.guestName} checked into room ${room.number}."
        )
        persist()
        return null
    }

    fun checkOut(bookingId: String): String? {
        val booking = booking(bookingId) ?: return "Booking not found"
        if (booking.status != BookingStatus.CHECKED_IN) {
            return "That guest is not checked in"
        }
        updateBooking(booking.copy(status = BookingStatus.CHECKED_OUT))
        val room = room(booking.roomId)
        room?.let { updateRoom(it.copy(status = RoomStatus.CLEANING)) }
        log(
            ActivityKind.CHECK_OUT,
            "Guest check-out",
            "${booking.guestName} checked out of room ${room?.number ?: "?"}, " +
                "sent to housekeeping."
        )
        persist()
        return null
    }

    fun cancelBooking(bookingId: String): String? {
        val booking = booking(bookingId) ?: return "Booking not found"
        if (booking.status != BookingStatus.RESERVED) {
            return "Only a reserved booking can be cancelled"
        }
        updateBooking(booking.copy(status = BookingStatus.CANCELLED))
        log(
            ActivityKind.BOOKING,
            "Reservation cancelled",
            "${booking.guestName}'s stay was cancelled by the front desk."
        )
        persist()
        return null
    }

    // --- Rooms -------------------------------------------------------------------

    fun setRoomStatus(roomId: String, status: RoomStatus): String? {
        val room = room(roomId) ?: return "That room no longer exists"
        val occupant = currentBookingFor(roomId)
        if (occupant != null && status != RoomStatus.OCCUPIED) {
            return "Check ${occupant.guestName} out before changing the room's status"
        }
        if (occupant == null && status == RoomStatus.OCCUPIED) {
            return "Check a guest in to mark the room occupied"
        }
        updateRoom(room.copy(status = status))
        val kind = if (status == RoomStatus.MAINTENANCE) {
            ActivityKind.MAINTENANCE
        } else {
            ActivityKind.HOUSEKEEPING
        }
        log(kind, "Room ${room.number} updated", "Marked ${status.label.lowercase()}.")
        persist()
        return null
    }

    fun setRoomNotes(roomId: String, notes: String) {
        val room = room(roomId) ?: return
        updateRoom(room.copy(notes = notes.trim()))
        persist()
    }

    // --- Tasks -------------------------------------------------------------------

    fun toggleTask(taskId: String) {
        val index = _tasks.indexOfFirst { it.id == taskId }
        if (index < 0) return
        val task = _tasks[index]
        _tasks[index] = task.copy(done = !task.done)
        if (!task.done) {
            log(ActivityKind.HOUSEKEEPING, "Task completed", task.title)
        }
        persist()
    }

    fun addTask(
        title: String,
        due: LocalDate,
        area: TaskArea,
        priority: TaskPriority
    ): String? {
        if (title.isBlank()) return "Give the task a title"
        val highest = _tasks.mapNotNull { it.id.removePrefix("task-").toIntOrNull() }.maxOrNull()
        _tasks.add(
            HotelTask(
                id = "task-${(highest ?: 0) + 1}",
                title = title.trim(),
                due = due,
                area = area,
                priority = priority
            )
        )
        persist()
        return null
    }

    fun deleteTask(taskId: String) {
        _tasks.removeAll { it.id == taskId }
        persist()
    }

    // --- Inventory ---------------------------------------------------------------

    fun adjustStock(itemId: String, delta: Int) {
        val index = _inventory.indexOfFirst { it.id == itemId }
        if (index < 0) return
        val item = _inventory[index]
        val updated = item.copy(quantity = (item.quantity + delta).coerceAtLeast(0))
        _inventory[index] = updated
        if (!item.low && updated.low) {
            log(
                ActivityKind.INVENTORY,
                "Low stock: ${updated.name}",
                "Down to ${updated.quantity} ${updated.unit}, below the ${updated.threshold} " +
                    "${updated.unit} reorder level."
            )
        }
        persist()
    }

    fun restock(itemId: String) {
        val item = _inventory.firstOrNull { it.id == itemId } ?: return
        adjustStock(itemId, (item.threshold * 3 - item.quantity).coerceAtLeast(1))
        log(ActivityKind.INVENTORY, "Restocked ${item.name}", "Topped back up to par level.")
    }

    // --- Messages ----------------------------------------------------------------

    fun markThreadRead(threadId: String) {
        val index = _threads.indexOfFirst { it.id == threadId }
        if (index < 0) return
        _threads[index] = _threads[index].copy(unread = false)
    }

    fun reply(threadId: String, text: String) {
        if (text.isBlank()) return
        val index = _threads.indexOfFirst { it.id == threadId }
        if (index < 0) return
        val thread = _threads[index]
        _threads[index] = thread.copy(
            unread = false,
            messages = thread.messages + ChatMessage(false, text.trim(), now())
        )
        log(
            ActivityKind.MESSAGE,
            "Replied to ${thread.guestName}",
            "Room ${thread.roomNumber}: ${text.trim()}"
        )
    }

    // --- Housekeeping of state ---------------------------------------------------

    /** Wipes the property back to the seeded demo data. */
    fun resetToSample() {
        _rooms.clear()
        _bookings.clear()
        _tasks.clear()
        _inventory.clear()
        _activity.clear()
        _reviews.clear()
        _threads.clear()
        seed()
    }

    private fun seed() {
        val date = today()
        val rooms = SampleData.rooms()
        val bookings = SampleData.bookings(rooms, date)
        _rooms.addAll(SampleData.applyRoomStates(rooms, bookings))
        _bookings.addAll(bookings)
        _tasks.addAll(SampleData.tasks(date))
        _inventory.addAll(SampleData.inventory())
        seedSessionContent()
        persist()
    }

    private fun seedSessionContent() {
        val date = today()
        _activity.addAll(SampleData.activity(date))
        _reviews.addAll(SampleData.reviews(date))
        _threads.addAll(SampleData.threads(date))
    }

    /** Newest first: the feed is only ever read from the top. */
    private fun log(kind: ActivityKind, title: String, detail: String) {
        _activity.add(
            0,
            ActivityEntry(
                id = "act-${now().toString()}-${_activity.size}",
                kind = kind,
                title = title,
                detail = detail,
                at = now()
            )
        )
    }

    private fun updateBooking(booking: Booking) {
        val index = _bookings.indexOfFirst { it.id == booking.id }
        if (index >= 0) _bookings[index] = booking
    }

    private fun updateRoom(room: Room) {
        val index = _rooms.indexOfFirst { it.id == room.id }
        if (index >= 0) _rooms[index] = room
    }

    private fun newBookingId(): String {
        val highest = _bookings.mapNotNull { it.id.removePrefix("bk-").toIntOrNull() }.maxOrNull()
        return "bk-${(highest ?: 1000) + 1}"
    }

    private fun persist() {
        storage?.save(_rooms.toList(), _bookings.toList(), _tasks.toList(), _inventory.toList())
    }
}
