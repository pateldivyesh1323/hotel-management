package com.example.hotel_management_app

import com.example.hotel_management_app.data.Booking
import com.example.hotel_management_app.data.BookingChannel
import com.example.hotel_management_app.data.BookingRules
import com.example.hotel_management_app.data.BookingStatus
import com.example.hotel_management_app.data.Room
import com.example.hotel_management_app.data.RoomType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Covers the figures the dashboard charts and stat tiles are drawn from. */
class AnalyticsTest {

    private val today: LocalDate = LocalDate.of(2026, 8, 14)

    private val standard = Room("r1", "101", 1, RoomType.STANDARD)
    private val suite = Room("r2", "201", 2, RoomType.SUITE)
    private val rooms = listOf(standard, suite)

    private fun booking(
        id: String,
        roomId: String = "r1",
        from: Long = 0,
        nights: Long = 2,
        status: BookingStatus = BookingStatus.CHECKED_OUT,
        channel: BookingChannel = BookingChannel.DIRECT
    ) = Booking(
        id = id,
        roomId = roomId,
        guestName = "Test Guest",
        checkIn = today.plusDays(from),
        checkOut = today.plusDays(from + nights),
        status = status,
        channel = channel
    )

    @Test
    fun `revenue lands in the month the stay starts`() {
        val bookings = listOf(
            booking("b1", from = 0, nights = 2),
            booking("b2", from = -40, nights = 1)
        )
        val series = BookingRules.revenueByMonth(bookings, rooms, today, months = 2)

        assertEquals(2, series.size)
        assertEquals(RoomType.STANDARD.nightlyRate.toFloat(), series[0].value, 0.01f)
        assertEquals((RoomType.STANDARD.nightlyRate * 2).toFloat(), series[1].value, 0.01f)
    }

    @Test
    fun `cancelled stays are left out of revenue`() {
        val bookings = listOf(booking("b1", status = BookingStatus.CANCELLED))
        val series = BookingRules.revenueByMonth(bookings, rooms, today, months = 1)

        assertEquals(0f, series.single().value, 0.01f)
    }

    @Test
    fun `reservations split into booked and cancelled per day`() {
        val bookings = listOf(
            booking("b1", from = -1),
            booking("b2", from = -1, status = BookingStatus.CANCELLED),
            booking("b3", from = 0)
        )
        val (booked, cancelled) = BookingRules.reservationsByDay(bookings, today, days = 2)

        assertEquals(listOf(1f, 1f), booked.map { it.value })
        assertEquals(listOf(1f, 0f), cancelled.map { it.value })
    }

    @Test
    fun `channel mix counts live bookings largest first`() {
        val bookings = listOf(
            booking("b1", channel = BookingChannel.BOOKING_COM),
            booking("b2", channel = BookingChannel.BOOKING_COM),
            booking("b3", channel = BookingChannel.DIRECT),
            booking("b4", channel = BookingChannel.AGODA, status = BookingStatus.CANCELLED)
        )
        val mix = BookingRules.channelMix(bookings)

        assertEquals(BookingChannel.BOOKING_COM.label, mix.first().label)
        assertEquals(2f, mix.first().value, 0.01f)
        assertEquals(2, mix.size)
    }

    @Test
    fun `average daily rate weights by nights, not by booking count`() {
        val bookings = listOf(
            booking("b1", roomId = "r1", nights = 3),
            booking("b2", roomId = "r2", nights = 1)
        )
        val expected = (RoomType.STANDARD.nightlyRate * 3 + RoomType.SUITE.nightlyRate) / 4

        assertEquals(expected, BookingRules.averageDailyRate(bookings, rooms))
    }

    @Test
    fun `revpar divides in-house revenue across every room`() {
        val bookings = listOf(booking("b1", roomId = "r2", status = BookingStatus.CHECKED_IN))

        assertEquals(RoomType.SUITE.nightlyRate / 2, BookingRules.revPar(bookings, rooms))
    }

    @Test
    fun `occupancy on a date excludes the departure day`() {
        val stay = booking("b1", from = 0, nights = 2, status = BookingStatus.CHECKED_IN)

        assertEquals(1, BookingRules.occupancyOn(listOf(stay), today).size)
        assertEquals(1, BookingRules.occupancyOn(listOf(stay), today.plusDays(1)).size)
        assertEquals(0, BookingRules.occupancyOn(listOf(stay), today.plusDays(2)).size)
    }

    @Test
    fun `a week with no history reports growth rather than infinity`() {
        val trend = BookingRules.percentChange(current = 5f, previous = 0f)

        assertTrue(trend.up)
        assertEquals(100f, trend.percent, 0.01f)
    }

    @Test
    fun `a fall is reported as a negative trend`() {
        val trend = BookingRules.percentChange(current = 5f, previous = 10f)

        assertFalse(trend.up)
        assertEquals(-50f, trend.percent, 0.01f)
    }

    @Test
    fun `week trend compares the last seven days with the seven before`() {
        val bookings = listOf(
            booking("b1", from = -1),
            booking("b2", from = -3),
            booking("b3", from = -10)
        )
        val trend = BookingRules.weekTrend(bookings, today) { it.checkIn }

        assertEquals(100f, trend.percent, 0.01f)
    }
}
