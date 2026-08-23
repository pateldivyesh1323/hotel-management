package com.example.hotel_management_app.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * The property as it looks on a first launch: three floors of rooms and a plausible six
 * months of bookings around [today], so every screen — including the charts — has
 * something real to show.
 */
object SampleData {

    fun rooms(): List<Room> = buildList {
        val layout = listOf(
            1 to listOf(RoomType.STANDARD, RoomType.STANDARD, RoomType.STANDARD, RoomType.DELUXE),
            2 to listOf(RoomType.STANDARD, RoomType.DELUXE, RoomType.DELUXE, RoomType.SUITE),
            3 to listOf(RoomType.DELUXE, RoomType.SUITE, RoomType.SUITE, RoomType.SUITE)
        )
        layout.forEach { (floor, types) ->
            types.forEachIndexed { index, type ->
                val number = "$floor${(index + 1).toString().padStart(2, '0')}"
                add(Room(id = "room-$number", number = number, floor = floor, type = type))
            }
        }
    }

    fun bookings(rooms: List<Room>, today: LocalDate): List<Booking> {
        val idFor = { number: String -> rooms.first { it.number == number }.id }
        return current(idFor, today) + history(idFor, today)
    }

    /** The stays the front desk is working today: in-house, arriving and departing. */
    private fun current(roomId: (String) -> String, today: LocalDate) = listOf(
        Booking(
            id = "bk-1001",
            roomId = roomId("101"),
            guestName = "Aditi Sharma",
            guestPhone = "+91 98250 11223",
            guestEmail = "aditi.sharma@example.com",
            checkIn = today.minusDays(2),
            checkOut = today.plusDays(1),
            guests = 2,
            status = BookingStatus.CHECKED_IN,
            channel = BookingChannel.DIRECT,
            notes = "Late check-out requested"
        ),
        Booking(
            id = "bk-1002",
            roomId = roomId("204"),
            guestName = "Marcus Bell",
            guestPhone = "+44 7700 900341",
            guestEmail = "m.bell@example.com",
            checkIn = today.minusDays(1),
            checkOut = today,
            guests = 3,
            status = BookingStatus.CHECKED_IN,
            channel = BookingChannel.BOOKING_COM,
            notes = "Corporate account"
        ),
        Booking(
            id = "bk-1003",
            roomId = roomId("302"),
            guestName = "Lena Fischer",
            guestPhone = "+49 151 23456789",
            checkIn = today.minusDays(3),
            checkOut = today,
            guests = 2,
            status = BookingStatus.CHECKED_IN,
            channel = BookingChannel.AGODA
        ),
        Booking(
            id = "bk-1004",
            roomId = roomId("103"),
            guestName = "Rahul Menon",
            guestPhone = "+91 99887 66554",
            guestEmail = "rahul.menon@example.com",
            checkIn = today,
            checkOut = today.plusDays(2),
            guests = 1,
            status = BookingStatus.RESERVED,
            channel = BookingChannel.DIRECT,
            notes = "Arriving on the evening flight"
        ),
        Booking(
            id = "bk-1005",
            roomId = roomId("202"),
            guestName = "Sofia Rossi",
            guestPhone = "+39 340 1122334",
            checkIn = today,
            checkOut = today.plusDays(4),
            guests = 2,
            status = BookingStatus.RESERVED,
            channel = BookingChannel.AIRBNB,
            notes = "Honeymoon — flowers arranged"
        ),
        Booking(
            id = "bk-1006",
            roomId = roomId("304"),
            guestName = "James Okoro",
            guestPhone = "+234 802 555 0199",
            guestEmail = "j.okoro@example.com",
            checkIn = today.plusDays(1),
            checkOut = today.plusDays(5),
            guests = 4,
            status = BookingStatus.RESERVED,
            channel = BookingChannel.BOOKING_COM
        ),
        Booking(
            id = "bk-1007",
            roomId = roomId("104"),
            guestName = "Aditi Sharma",
            guestPhone = "+91 98250 11223",
            guestEmail = "aditi.sharma@example.com",
            checkIn = today.minusDays(20),
            checkOut = today.minusDays(17),
            guests = 1,
            status = BookingStatus.CHECKED_OUT,
            channel = BookingChannel.DIRECT
        ),
        Booking(
            id = "bk-1008",
            roomId = roomId("201"),
            guestName = "Chen Wei",
            guestPhone = "+86 138 0013 8000",
            checkIn = today.minusDays(9),
            checkOut = today.minusDays(6),
            guests = 2,
            status = BookingStatus.CHECKED_OUT,
            channel = BookingChannel.HOTELS_COM
        ),
        Booking(
            id = "bk-1009",
            roomId = roomId("301"),
            guestName = "Priya Nair",
            guestPhone = "+91 90040 22110",
            checkIn = today.minusDays(4),
            checkOut = today.minusDays(2),
            guests = 2,
            status = BookingStatus.CANCELLED,
            channel = BookingChannel.OTHER,
            notes = "Flight cancelled"
        )
    )

    /**
     * Six months of closed stays, spread deterministically so the revenue and
     * reservation charts have a believable shape on every launch.
     */
    private fun history(roomId: (String) -> String, today: LocalDate): List<Booking> {
        val guests = listOf(
            "Elena Petrova" to "+7 916 555 0143",
            "Tom Whitfield" to "+44 7911 123456",
            "Yuki Tanaka" to "+81 90 1234 5678",
            "Carlos Mendes" to "+55 11 98765 4321",
            "Amara Diallo" to "+221 77 555 0132",
            "Nina Kovac" to "+385 91 555 0177",
            "Owen Doyle" to "+353 86 555 0121",
            "Fatima Al Suwaidi" to "+971 50 555 0166"
        )
        val roomNumbers = listOf("101", "102", "104", "201", "203", "204", "301", "303", "304")
        val channels = BookingChannel.entries
        // Roughly how busy each of the last six months was, oldest month first.
        val volumes = listOf(5, 7, 6, 9, 8, 4)

        var id = 2000
        return volumes.flatMapIndexed { monthIndex, count ->
            val month = today.minusMonths((volumes.size - 1 - monthIndex).toLong())
            (0 until count).mapNotNull { index ->
                val day = ((index * 4) % 25) + 1
                val start = month.withDayOfMonth(minOf(day, month.lengthOfMonth()))
                // Skip anything the calendar would place in the future.
                if (!start.isBefore(today.minusDays(1))) return@mapNotNull null
                val (name, phone) = guests[(id + index) % guests.size]
                Booking(
                    id = "bk-${id++}",
                    roomId = roomId(roomNumbers[(id + index) % roomNumbers.size]),
                    guestName = name,
                    guestPhone = phone,
                    checkIn = start,
                    checkOut = start.plusDays(((index % 3) + 1).toLong()),
                    guests = (index % 2) + 1,
                    status = if (index % 7 == 6) BookingStatus.CANCELLED else BookingStatus.CHECKED_OUT,
                    channel = channels[(index + monthIndex) % channels.size]
                )
            }
        }
    }

    /** Room states that match the seeded bookings, plus a little housekeeping work. */
    fun applyRoomStates(rooms: List<Room>, bookings: List<Booking>): List<Room> {
        val occupiedRoomIds = bookings.filter { it.status == BookingStatus.CHECKED_IN }
            .map { it.roomId }
            .toSet()
        return rooms.map { room ->
            when {
                room.id in occupiedRoomIds -> room.copy(status = RoomStatus.OCCUPIED)
                room.number == "102" -> room.copy(status = RoomStatus.CLEANING)
                room.number == "203" -> room.copy(
                    status = RoomStatus.MAINTENANCE,
                    notes = "Air-conditioning repair, back in service Friday"
                )
                else -> room
            }
        }
    }

    fun tasks(today: LocalDate): List<HotelTask> = listOf(
        HotelTask(
            id = "task-1",
            title = "Set up conference room B for the 10 AM meeting",
            due = today,
            area = TaskArea.EVENTS,
            priority = TaskPriority.HIGH
        ),
        HotelTask(
            id = "task-2",
            title = "Restock housekeeping supplies on the 3rd floor",
            due = today,
            area = TaskArea.HOUSEKEEPING,
            priority = TaskPriority.NORMAL
        ),
        HotelTask(
            id = "task-3",
            title = "Inspect and clean the pool area",
            due = today.plusDays(1),
            area = TaskArea.MAINTENANCE,
            priority = TaskPriority.NORMAL
        ),
        HotelTask(
            id = "task-4",
            title = "Check-in assistance during peak hours (4 PM - 6 PM)",
            due = today.plusDays(1),
            area = TaskArea.FRONT_DESK,
            priority = TaskPriority.LOW
        ),
        HotelTask(
            id = "task-5",
            title = "Service the lift in the east wing",
            due = today.plusDays(2),
            area = TaskArea.MAINTENANCE,
            priority = TaskPriority.HIGH
        ),
        HotelTask(
            id = "task-6",
            title = "Deep clean room 203 after the AC repair",
            due = today.minusDays(1),
            area = TaskArea.HOUSEKEEPING,
            priority = TaskPriority.NORMAL,
            done = true
        )
    )

    fun activity(today: LocalDate): List<ActivityEntry> {
        fun at(hour: Int, minute: Int) = LocalDateTime.of(today, LocalTime.of(hour, minute))
        return listOf(
            ActivityEntry(
                id = "act-1",
                kind = ActivityKind.EVENTS,
                title = "Conference room setup",
                detail = "Events team set up conference room B for the 10 AM meeting, " +
                    "including AV equipment and refreshments.",
                at = at(12, 0)
            ),
            ActivityEntry(
                id = "act-2",
                kind = ActivityKind.CHECK_OUT,
                title = "Guest check-out",
                detail = "Sarah Johnson completed the check-out process and released " +
                    "room 305 back to housekeeping.",
                at = at(11, 30)
            ),
            ActivityEntry(
                id = "act-3",
                kind = ActivityKind.HOUSEKEEPING,
                title = "Housekeeping round finished",
                detail = "Second floor turned over and inspected, all rooms released for sale.",
                at = at(10, 15)
            ),
            ActivityEntry(
                id = "act-4",
                kind = ActivityKind.MAINTENANCE,
                title = "Maintenance logged",
                detail = "Room 203 air-conditioning repair booked with the contractor for Friday.",
                at = at(9, 5)
            ),
            ActivityEntry(
                id = "act-5",
                kind = ActivityKind.BOOKING,
                title = "New reservation",
                detail = "James Okoro booked suite 304 for four nights through Booking.com.",
                at = at(8, 40)
            )
        )
    }

    fun reviews(today: LocalDate): List<Review> = listOf(
        Review(
            id = "rev-1",
            guestName = "Marcus Bell",
            roomNumber = "204",
            rating = 4.8f,
            comment = "Spotless room and the fastest check-in I have had all year.",
            date = today.minusDays(1)
        ),
        Review(
            id = "rev-2",
            guestName = "Chen Wei",
            roomNumber = "201",
            rating = 4.4f,
            comment = "Great location, breakfast could open a little earlier.",
            date = today.minusDays(6)
        ),
        Review(
            id = "rev-3",
            guestName = "Elena Petrova",
            roomNumber = "301",
            rating = 5f,
            comment = "The suite was beautiful and the concierge booked everything for us.",
            date = today.minusDays(12)
        ),
        Review(
            id = "rev-4",
            guestName = "Owen Doyle",
            roomNumber = "104",
            rating = 3.9f,
            comment = "Comfortable bed, but the corridor was noisy late at night.",
            date = today.minusDays(19)
        ),
        Review(
            id = "rev-5",
            guestName = "Yuki Tanaka",
            roomNumber = "303",
            rating = 4.6f,
            comment = "Very attentive staff, and the rooftop view was worth the trip.",
            date = today.minusDays(27)
        )
    )

    /** Facet scores are the property's running averages, not derived from the samples above. */
    fun rating(): RatingBreakdown = RatingBreakdown(
        overall = 4.6f,
        reviews = 2546,
        facilities = 4.4f,
        cleanliness = 4.7f,
        services = 4.6f,
        comfort = 4.8f,
        location = 4.5f
    )

    fun inventory(): List<InventoryItem> = listOf(
        InventoryItem("inv-1", "Bath towels", "Linen", 184, 60, "pcs"),
        InventoryItem("inv-2", "Bed sheets (queen)", "Linen", 46, 50, "sets"),
        InventoryItem("inv-3", "Shampoo bottles", "Amenities", 320, 120, "pcs"),
        InventoryItem("inv-4", "Soap bars", "Amenities", 88, 100, "pcs"),
        InventoryItem("inv-5", "Coffee pods", "Minibar", 540, 200, "pcs"),
        InventoryItem("inv-6", "Bottled water", "Minibar", 96, 150, "btl"),
        InventoryItem("inv-7", "Cleaning solution", "Housekeeping", 24, 10, "L"),
        InventoryItem("inv-8", "Light bulbs", "Maintenance", 61, 25, "pcs")
    )

    fun threads(today: LocalDate): List<MessageThread> {
        fun at(daysBack: Long, hour: Int, minute: Int) =
            LocalDateTime.of(today.minusDays(daysBack), LocalTime.of(hour, minute))
        return listOf(
            MessageThread(
                id = "msg-1",
                guestName = "Aditi Sharma",
                roomNumber = "101",
                unread = true,
                messages = listOf(
                    ChatMessage(true, "Hi, could we keep the room until 2 PM tomorrow?", at(0, 9, 12)),
                    ChatMessage(false, "Good morning! Late check-out until 2 PM is noted.", at(0, 9, 20)),
                    ChatMessage(true, "Perfect, thank you. Could we also get an extra pillow?", at(0, 9, 41))
                )
            ),
            MessageThread(
                id = "msg-2",
                guestName = "Sofia Rossi",
                roomNumber = "202",
                unread = true,
                messages = listOf(
                    ChatMessage(true, "We land at 8 PM, is a late arrival fine?", at(0, 8, 5)),
                    ChatMessage(false, "Absolutely, the desk is staffed all night.", at(0, 8, 11)),
                    ChatMessage(true, "Wonderful. Any chance of a room with a view?", at(0, 8, 30))
                )
            ),
            MessageThread(
                id = "msg-3",
                guestName = "James Okoro",
                roomNumber = "304",
                unread = false,
                messages = listOf(
                    ChatMessage(true, "Do you have an airport pickup service?", at(1, 17, 2)),
                    ChatMessage(false, "We do, 40 dollars each way. I can book it for your arrival.", at(1, 17, 15)),
                    ChatMessage(true, "Please do, thanks!", at(1, 17, 22))
                )
            ),
            MessageThread(
                id = "msg-4",
                guestName = "Marcus Bell",
                roomNumber = "204",
                unread = false,
                messages = listOf(
                    ChatMessage(true, "Can I get an invoice addressed to my company?", at(2, 14, 40)),
                    ChatMessage(false, "Sent to m.bell@example.com just now.", at(2, 14, 52))
                )
            )
        )
    }
}
