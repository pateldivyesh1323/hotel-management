package com.example.hotel_booking_app

import com.example.hotel_booking_app.data.BookingRules
import com.example.hotel_booking_app.data.BookingStatus
import com.example.hotel_booking_app.data.Amenity
import com.example.hotel_booking_app.data.SampleData
import com.example.hotel_booking_app.data.SearchFilters
import com.example.hotel_booking_app.data.SortOrder
import com.example.hotel_booking_app.data.StayRequest
import com.example.hotel_booking_app.data.TripStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BookingRulesTest {

    private val today = LocalDate.of(2026, 6, 1)
    private val hotels = SampleData.hotels()
    private val offers = SampleData.offers()
    private val noFilters = SearchFilters(0f, null, emptySet())
    private val stay = StayRequest(today.plusDays(10), today.plusDays(12), 2)

    @Test
    fun backToBackStaysDoNotOverlap() {
        assertTrue(!BookingRules.overlaps(today, today.plusDays(2), today.plusDays(2), today.plusDays(4)))
        assertTrue(BookingRules.overlaps(today, today.plusDays(3), today.plusDays(2), today.plusDays(4)))
    }

    @Test
    fun confirmedBookingsConsumeUnitsAndCancelledOnesDoNot() {
        val offer = offers.first { it.id == "htl-lisbon-suite" }
        val booking = SampleData.bookings(today).first().copy(
            roomId = offer.id,
            hotelId = offer.hotelId,
            checkIn = stay.checkIn,
            checkOut = stay.checkOut
        )
        assertEquals(offer.units - 1, BookingRules.unitsLeft(offer, listOf(booking), stay.checkIn, stay.checkOut))
        val cancelled = booking.copy(status = BookingStatus.CANCELLED)
        assertEquals(offer.units, BookingRules.unitsLeft(offer, listOf(cancelled), stay.checkIn, stay.checkOut))
    }

    @Test
    fun searchFiltersByDestinationAndSortsByPrice() {
        val london = BookingRules.search(hotels, offers, emptyList(), "london", stay, noFilters, SortOrder.RECOMMENDED)
        assertEquals(listOf("htl-london"), london.map { it.hotel.id })

        val cheapestFirst = BookingRules.search(hotels, offers, emptyList(), "", stay, noFilters, SortOrder.PRICE_LOW)
        assertEquals(cheapestFirst.map { it.fromRate }.sorted(), cheapestFirst.map { it.fromRate })
    }

    @Test
    fun largePartiesOnlySeeRoomsThatSleepThem() {
        val party = stay.copy(guests = 4)
        val open = BookingRules.availableOffers(offers.filter { it.hotelId == "htl-goa" }, emptyList(), party)
        assertEquals(listOf("htl-goa-suite"), open.map { it.id })
    }

    @Test
    fun pricingAddsTaxesToTheNightlyTotal() {
        val price = BookingRules.price(200, 3, 2, false, 0)
        assertEquals(600, price.subtotal)
        assertEquals(72, price.taxes)
        assertEquals(672, price.total)
    }

    @Test
    fun breakfastAndPromoAdjustThePrice() {
        val price = BookingRules.price(200, 3, 2, true, 10)
        assertEquals(72, price.breakfast)
        assertEquals(60, price.discount)
        assertEquals(685, price.total)
        assertEquals(10, BookingRules.promoPercent(" welcome10 "))
        assertNull(BookingRules.promoPercent("nope"))
    }

    @Test
    fun filtersNarrowResults() {
        val pricey = BookingRules.search(hotels, offers, emptyList(), "", stay, SearchFilters(0f, 120, emptySet()), SortOrder.PRICE_LOW)
        assertTrue(pricey.all { it.fromRate <= 120 } && pricey.isNotEmpty())
        val pools = BookingRules.search(hotels, offers, emptyList(), "", stay, SearchFilters(4.8f, null, setOf(Amenity.POOL)), SortOrder.RATING)
        assertTrue(pools.all { it.hotel.rating >= 4.8f && Amenity.POOL in it.hotel.amenities } && pools.isNotEmpty())
    }

    @Test
    fun validationRejectsBadInput() {
        val offer = offers.first()
        assertNotNull(BookingRules.validate("", "a@b.co", offer, stay, today))
        assertNotNull(BookingRules.validate("Ann", "nope", offer, stay, today))
        assertNotNull(BookingRules.validate("Ann", "a@b.co", offer, stay.copy(checkOut = stay.checkIn), today))
        assertNotNull(BookingRules.validate("Ann", "a@b.co", offer, stay.copy(guests = 3), today))
        assertNull(BookingRules.validate("Ann", "a@b.co", offer, stay, today))
    }

    @Test
    fun tripStagesAndCancellationFollowTheDates() {
        val trips = SampleData.bookings(today)
        assertEquals(setOf("bk-1001", "bk-1002"), BookingRules.trips(trips, TripStage.UPCOMING, today).map { it.id }.toSet())
        assertEquals(setOf("bk-1003", "bk-1004"), BookingRules.trips(trips, TripStage.PAST, today).map { it.id }.toSet())
        assertEquals(listOf("bk-1005"), BookingRules.trips(trips, TripStage.CANCELLED, today).map { it.id })
        assertTrue(BookingRules.canCancel(trips.first { it.id == "bk-1001" }, today))
        assertTrue(!BookingRules.canCancel(trips.first { it.id == "bk-1003" }, today))
    }
}
