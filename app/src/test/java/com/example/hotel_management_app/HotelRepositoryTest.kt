package com.example.hotel_management_app

import com.example.hotel_management_app.data.BookingStatus
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.data.RoomStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Exercises the front-desk workflows against the seeded sample property. */
class HotelRepositoryTest {

    private val today: LocalDate = LocalDate.of(2026, 8, 14)

    private fun repo() = HotelRepository(storage = null, today = { today })

    @Test
    fun `the sample property seeds rooms and bookings`() {
        val repo = repo()
        assertEquals(12, repo.rooms.size)
        assertTrue(repo.bookings.isNotEmpty())
    }

    @Test
    fun `seeded room states agree with the seeded bookings`() {
        val repo = repo()
        repo.bookings.filter { it.status == BookingStatus.CHECKED_IN }.forEach { stay ->
            assertEquals(RoomStatus.OCCUPIED, repo.room(stay.roomId)?.status)
        }
    }

    @Test
    fun `checking in occupies the room`() {
        val repo = repo()
        val arrival = repo.arrivalsToday().first()

        assertNull(repo.checkIn(arrival.id))

        assertEquals(BookingStatus.CHECKED_IN, repo.booking(arrival.id)?.status)
        assertEquals(RoomStatus.OCCUPIED, repo.room(arrival.roomId)?.status)
    }

    @Test
    fun `checking out sends the room to housekeeping`() {
        val repo = repo()
        val departure = repo.departuresToday().first()

        assertNull(repo.checkOut(departure.id))

        assertEquals(BookingStatus.CHECKED_OUT, repo.booking(departure.id)?.status)
        assertEquals(RoomStatus.CLEANING, repo.room(departure.roomId)?.status)
    }

    @Test
    fun `a guest cannot be checked in twice`() {
        val repo = repo()
        val arrival = repo.arrivalsToday().first()
        repo.checkIn(arrival.id)

        assertNotNull(repo.checkIn(arrival.id))
    }

    @Test
    fun `an occupied room cannot be double booked over the same nights`() {
        val repo = repo()
        val occupied = repo.bookings.first { it.status == BookingStatus.CHECKED_IN }

        val failure = repo.createBooking(
            roomId = occupied.roomId,
            guestName = "Walk-in Guest",
            guestPhone = "",
            guestEmail = "",
            checkIn = occupied.checkIn,
            checkOut = occupied.checkOut,
            guests = 1,
            notes = ""
        )

        assertNotNull(failure)
    }

    @Test
    fun `a booking for free dates is accepted and gets a fresh reference`() {
        val repo = repo()
        val room = repo.availableRooms(today.plusDays(30), today.plusDays(32), 1).first()
        val before = repo.bookings.size

        assertNull(
            repo.createBooking(
                roomId = room.id,
                guestName = "  Nadia Haddad  ",
                guestPhone = "+1 555 0100",
                guestEmail = "nadia@example.com",
                checkIn = today.plusDays(30),
                checkOut = today.plusDays(32),
                guests = 1,
                notes = "Quiet floor"
            )
        )

        assertEquals(before + 1, repo.bookings.size)
        val created = repo.bookings.last()
        assertEquals("Nadia Haddad", created.guestName)
        assertEquals(BookingStatus.RESERVED, created.status)
        assertTrue(repo.bookings.map { it.id }.distinct().size == repo.bookings.size)
    }

    @Test
    fun `cancelling frees the dates for someone else`() {
        val repo = repo()
        val reservation = repo.bookings.first { it.status == BookingStatus.RESERVED }

        assertNull(repo.cancelBooking(reservation.id))
        assertEquals(BookingStatus.CANCELLED, repo.booking(reservation.id)?.status)

        assertNull(
            repo.createBooking(
                roomId = reservation.roomId,
                guestName = "Replacement Guest",
                guestPhone = "",
                guestEmail = "",
                checkIn = reservation.checkIn,
                checkOut = reservation.checkOut,
                guests = 1,
                notes = ""
            )
        )
    }

    @Test
    fun `an occupied room's status cannot be changed under the guest`() {
        val repo = repo()
        val stay = repo.bookings.first { it.status == BookingStatus.CHECKED_IN }

        assertNotNull(repo.setRoomStatus(stay.roomId, RoomStatus.AVAILABLE))
        assertEquals(RoomStatus.OCCUPIED, repo.room(stay.roomId)?.status)
    }

    @Test
    fun `a cleaned room becomes available again`() {
        val repo = repo()
        val dirty = repo.rooms.first { it.status == RoomStatus.CLEANING }

        assertNull(repo.setRoomStatus(dirty.id, RoomStatus.AVAILABLE))
        assertEquals(RoomStatus.AVAILABLE, repo.room(dirty.id)?.status)
    }

    @Test
    fun `a room out of service is never offered for booking`() {
        val repo = repo()
        val closed = repo.rooms.first { it.status == RoomStatus.MAINTENANCE }

        val offered = repo.availableRooms(today.plusDays(40), today.plusDays(41), 1)

        assertTrue(offered.none { it.id == closed.id })
    }
}
