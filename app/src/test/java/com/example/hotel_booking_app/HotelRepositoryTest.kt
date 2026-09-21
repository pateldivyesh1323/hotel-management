package com.example.hotel_booking_app

import com.example.hotel_booking_app.data.BookingOutcome
import com.example.hotel_booking_app.data.HotelRepository
import com.example.hotel_booking_app.data.StayRequest
import com.example.hotel_booking_app.data.TripStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HotelRepositoryTest {

    private val today = LocalDate.of(2026, 6, 1)
    private val stay = StayRequest(today.plusDays(50), today.plusDays(52), 2)

    private fun repo() = HotelRepository(null) { today }

    @Test
    fun bookingAddsAnUpcomingTripThatCanBeCancelled() {
        val repo = repo()
        val before = repo.trips(TripStage.UPCOMING).size

        val outcome = repo.book("htl-bali-deluxe", "Ann Lee", "", "ann@example.com", stay, "", false, "", false)
        val id = (outcome as BookingOutcome.Confirmed).bookingId

        assertEquals(before + 1, repo.trips(TripStage.UPCOMING).size)
        assertNull(repo.cancelBooking(id))
        assertEquals(1, repo.trips(TripStage.CANCELLED).count { it.id == id })
    }

    @Test
    fun soldOutRoomsAreRejected() {
        val repo = repo()
        val units = repo.offer("htl-bali-suite")!!.units
        repeat(units) {
            assertTrue(repo.book("htl-bali-suite", "Ann", "", "ann@example.com", stay, "", false, "", false) is BookingOutcome.Confirmed)
        }
        val outcome = repo.book("htl-bali-suite", "Ann", "", "ann@example.com", stay, "", false, "", false)
        assertTrue(outcome is BookingOutcome.Rejected)
        assertTrue(repo.availableOffers("htl-bali", stay).none { it.id == "htl-bali-suite" })
    }

    @Test
    fun pastBookingsCannotBeCancelled() {
        assertNotNull(repo().cancelBooking("bk-1003"))
    }

    @Test
    fun invalidPromoIsRejectedAndValidOneStored() {
        val repo = repo()
        assertTrue(repo.book("htl-goa-deluxe", "Ann", "", "ann@example.com", stay, "", false, "BOGUS", false) is BookingOutcome.Rejected)
        val id = (repo.book("htl-goa-deluxe", "Ann", "", "ann@example.com", stay, "", true, "welcome10", true) as BookingOutcome.Confirmed).bookingId
        assertEquals(10, repo.booking(id)!!.promoPercent)
    }

    @Test
    fun onlyPastStaysCanBeReviewedOnce() {
        val repo = repo()
        assertNotNull(repo.addReview("bk-1001", 5f, "Great"))
        assertNull(repo.addReview("bk-1003", 4f, "Lovely rooftop dinner"))
        assertNotNull(repo.addReview("bk-1003", 4f, "Again"))
        assertEquals(4f, repo.reviewFor("bk-1003")!!.rating)
    }

    @Test
    fun favoritesToggle() {
        val repo = repo()
        repo.toggleFavorite("htl-goa")
        assertTrue(repo.isFavorite("htl-goa"))
        repo.toggleFavorite("htl-goa")
        assertTrue(repo.favoriteHotels().isEmpty())
    }
}
