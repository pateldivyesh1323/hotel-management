package com.example.hotel_management_app

import com.example.hotel_management_app.data.Booking
import com.example.hotel_management_app.data.BookingRules
import com.example.hotel_management_app.data.BookingStatus
import com.example.hotel_management_app.data.Room
import com.example.hotel_management_app.data.RoomStatus
import com.example.hotel_management_app.data.RoomType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BookingRulesTest {

    private val today: LocalDate = LocalDate.of(2026, 8, 14)

    private val standard = Room("r1", "101", 1, RoomType.STANDARD)
    private val suite = Room("r2", "201", 2, RoomType.SUITE)
    private val closed = Room("r3", "301", 3, RoomType.DELUXE, RoomStatus.MAINTENANCE)

    private fun booking(
        id: String = "b1",
        roomId: String = "r1",
        from: Long = 0,
        to: Long = 2,
        status: BookingStatus = BookingStatus.RESERVED
    ) = Booking(
        id = id,
        roomId = roomId,
        guestName = "Test Guest",
        checkIn = today.plusDays(from),
        checkOut = today.plusDays(to),
        status = status
    )

    @Test
    fun `stays that share nights overlap`() {
        assertTrue(
            BookingRules.overlaps(
                today, today.plusDays(3),
                today.plusDays(2), today.plusDays(4)
            )
        )
    }

    @Test
    fun `a departure on the day of an arrival does not overlap`() {
        val checkOutDay = today.plusDays(2)
        assertTrue(
            !BookingRules.overlaps(today, checkOutDay, checkOutDay, checkOutDay.plusDays(2))
        )
    }

    @Test
    fun `an occupied room is not offered for clashing dates`() {
        val bookings = listOf(booking(from = 1, to = 4))
        val free = BookingRules.availableRooms(
            rooms = listOf(standard, suite),
            bookings = bookings,
            checkIn = today.plusDays(2),
            checkOut = today.plusDays(3)
        )
        assertEquals(listOf("201"), free.map { it.number })
    }

    @Test
    fun `the same room is offered again once the stay has ended`() {
        val bookings = listOf(booking(from = 1, to = 4))
        val free = BookingRules.availableRooms(
            rooms = listOf(standard),
            bookings = bookings,
            checkIn = today.plusDays(4),
            checkOut = today.plusDays(6)
        )
        assertEquals(listOf("101"), free.map { it.number })
    }

    @Test
    fun `cancelled bookings release the room`() {
        val bookings = listOf(booking(from = 0, to = 5, status = BookingStatus.CANCELLED))
        assertNull(
            BookingRules.conflictFor(bookings, "r1", today.plusDays(1), today.plusDays(2))
        )
    }

    @Test
    fun `rooms under maintenance are never offered`() {
        val free = BookingRules.availableRooms(
            rooms = listOf(closed),
            bookings = emptyList(),
            checkIn = today,
            checkOut = today.plusDays(1)
        )
        assertTrue(free.isEmpty())
    }

    @Test
    fun `rooms too small for the party are not offered`() {
        val free = BookingRules.availableRooms(
            rooms = listOf(standard, suite),
            bookings = emptyList(),
            checkIn = today,
            checkOut = today.plusDays(1),
            guests = 4
        )
        assertEquals(listOf("201"), free.map { it.number })
    }

    @Test
    fun `validation rejects a stay that ends before it starts`() {
        assertNotNull(
            BookingRules.validate("Ada", standard, today.plusDays(3), today.plusDays(1), 1)
        )
    }

    @Test
    fun `validation rejects an unnamed guest`() {
        assertNotNull(BookingRules.validate("  ", standard, today, today.plusDays(1), 1))
    }

    @Test
    fun `validation rejects a party larger than the room sleeps`() {
        assertNotNull(BookingRules.validate("Ada", standard, today, today.plusDays(1), 3))
    }

    @Test
    fun `a well formed booking passes validation`() {
        assertNull(BookingRules.validate("Ada", standard, today, today.plusDays(2), 2))
    }

    @Test
    fun `a same day booking still bills one night`() {
        val sameDay = booking(from = 0, to = 0)
        assertEquals(1, sameDay.nights)
    }

    @Test
    fun `arrivals and departures are read off today's date`() {
        val arriving = booking(id = "arrive", from = 0, to = 2)
        val leaving = booking(
            id = "leave",
            roomId = "r2",
            from = -2,
            to = 0,
            status = BookingStatus.CHECKED_IN
        )
        val all = listOf(arriving, leaving)
        assertEquals(listOf("arrive"), BookingRules.arrivalsOn(all, today).map { it.id })
        assertEquals(listOf("leave"), BookingRules.departuresOn(all, today).map { it.id })
    }

    @Test
    fun `stats count rooms by status and tonight's revenue`() {
        val rooms = listOf(
            standard.copy(status = RoomStatus.OCCUPIED),
            suite,
            closed
        )
        val bookings = listOf(booking(from = -1, to = 2, status = BookingStatus.CHECKED_IN))
        val stats = BookingRules.stats(rooms, bookings, today)

        assertEquals(3, stats.totalRooms)
        assertEquals(1, stats.occupied)
        assertEquals(1, stats.available)
        assertEquals(1, stats.maintenance)
        assertEquals(RoomType.STANDARD.nightlyRate, stats.revenueToday)
        assertEquals(1f / 3f, stats.occupancyRate, 0.001f)
    }

    @Test
    fun `guest history is grouped by name and phone`() {
        val rooms = listOf(standard)
        val stays = listOf(
            booking(id = "b1", from = -10, to = -8, status = BookingStatus.CHECKED_OUT)
                .copy(guestPhone = "555"),
            booking(id = "b2", from = -3, to = -1, status = BookingStatus.CHECKED_OUT)
                .copy(guestPhone = "555"),
            booking(id = "b3", from = 1, to = 2).copy(guestName = "Other", guestPhone = "999")
        )
        val guests = BookingRules.guestSummaries(stays, rooms)

        assertEquals(2, guests.size)
        val repeat = guests.first { it.phone == "555" }
        assertEquals(2, repeat.stays)
        assertEquals(4, repeat.nights)
        assertEquals(4 * RoomType.STANDARD.nightlyRate, repeat.spend)
    }
}
