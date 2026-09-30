package com.example.hotel_booking_app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.hotel_booking_app.data.Amenity;
import com.example.hotel_booking_app.data.Booking;
import com.example.hotel_booking_app.data.BookingRules;
import com.example.hotel_booking_app.data.BookingStatus;
import com.example.hotel_booking_app.data.Hotel;
import com.example.hotel_booking_app.data.HotelResult;
import com.example.hotel_booking_app.data.RoomOffer;
import com.example.hotel_booking_app.data.SampleData;
import com.example.hotel_booking_app.data.SearchFilters;
import com.example.hotel_booking_app.data.SortOrder;
import com.example.hotel_booking_app.data.StayRequest;
import com.example.hotel_booking_app.data.TripStage;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookingRulesJavaTest {

    private final LocalDate today = LocalDate.of(2026, 6, 1);
    private final List<Hotel> hotels = SampleData.hotels();
    private final List<RoomOffer> offers = SampleData.offers();
    private final SearchFilters noFilters = new SearchFilters(0f, null, Collections.<Amenity>emptySet());
    private final StayRequest stay = new StayRequest(today.plusDays(10), today.plusDays(12), 2);

    private RoomOffer offer(String id) {
        for (RoomOffer offer : offers) {
            if (offer.id.equals(id)) {
                return offer;
            }
        }
        throw new IllegalStateException("Missing offer " + id);
    }

    private List<String> hotelIds(List<HotelResult> results) {
        List<String> ids = new ArrayList<>();
        for (HotelResult result : results) {
            ids.add(result.hotel.id);
        }
        return ids;
    }

    @Test
    public void backToBackStaysDoNotOverlap() {
        assertFalse(BookingRules.overlaps(today, today.plusDays(2), today.plusDays(2), today.plusDays(4)));
        assertTrue(BookingRules.overlaps(today, today.plusDays(3), today.plusDays(2), today.plusDays(4)));
    }

    @Test
    public void confirmedBookingsConsumeUnitsAndCancelledOnesDoNot() {
        RoomOffer offer = offer("htl-udaipur-suite");
        Booking booking = new Booking("bk-x", offer.hotelId, offer.id, "Aditi", "", "a@b.co",
                stay.checkIn, stay.checkOut, 2, BookingStatus.CONFIRMED, "", today, offer.nightlyRate,
                false, 0, false);
        assertEquals(offer.units - 1,
                BookingRules.unitsLeft(offer, Collections.singletonList(booking), stay.checkIn, stay.checkOut));
        Booking cancelled = booking.withStatus(BookingStatus.CANCELLED);
        assertEquals(offer.units,
                BookingRules.unitsLeft(offer, Collections.singletonList(cancelled), stay.checkIn, stay.checkOut));
    }

    @Test
    public void searchFiltersByDestinationAndSortsByPrice() {
        List<HotelResult> delhi = BookingRules.search(hotels, offers, Collections.<Booking>emptyList(),
                "delhi", stay, noFilters, SortOrder.RECOMMENDED);
        assertEquals(Collections.singletonList("htl-delhi"), hotelIds(delhi));

        List<HotelResult> cheapestFirst = BookingRules.search(hotels, offers, Collections.<Booking>emptyList(),
                "", stay, noFilters, SortOrder.PRICE_LOW);
        List<Integer> rates = new ArrayList<>();
        for (HotelResult result : cheapestFirst) {
            rates.add(result.fromRate);
        }
        List<Integer> sorted = new ArrayList<>(rates);
        Collections.sort(sorted);
        assertEquals(sorted, rates);
    }

    @Test
    public void largePartiesOnlySeeRoomsThatSleepThem() {
        StayRequest party = stay.withGuests(4);
        List<RoomOffer> goaOffers = new ArrayList<>();
        for (RoomOffer offer : offers) {
            if (offer.hotelId.equals("htl-goa")) {
                goaOffers.add(offer);
            }
        }
        List<RoomOffer> open = BookingRules.availableOffers(goaOffers, Collections.<Booking>emptyList(), party);
        List<String> ids = new ArrayList<>();
        for (RoomOffer offer : open) {
            ids.add(offer.id);
        }
        assertEquals(Collections.singletonList("htl-goa-suite"), ids);
    }

    @Test
    public void pricingAddsTaxesToTheNightlyTotal() {
        assertEquals(600, BookingRules.price(200, 3, 2, false, 0).subtotal);
        assertEquals(72, BookingRules.price(200, 3, 2, false, 0).taxes);
        assertEquals(672, BookingRules.price(200, 3, 2, false, 0).total);
    }

    @Test
    public void breakfastAndPromoAdjustThePrice() {
        assertEquals(3000, BookingRules.price(200, 3, 2, true, 10).breakfast);
        assertEquals(60, BookingRules.price(200, 3, 2, true, 10).discount);
        assertEquals(3964, BookingRules.price(200, 3, 2, true, 10).total);
        assertEquals(Integer.valueOf(10), BookingRules.promoPercent(" welcome10 "));
        assertNull(BookingRules.promoPercent("nope"));
    }

    @Test
    public void filtersNarrowResults() {
        List<HotelResult> pricey = BookingRules.search(hotels, offers, Collections.<Booking>emptyList(),
                "", stay, new SearchFilters(0f, 7000, Collections.<Amenity>emptySet()), SortOrder.PRICE_LOW);
        assertFalse(pricey.isEmpty());
        for (HotelResult result : pricey) {
            assertTrue(result.fromRate <= 7000);
        }

        Set<Amenity> pool = new HashSet<>(Arrays.asList(Amenity.POOL));
        List<HotelResult> pools = BookingRules.search(hotels, offers, Collections.<Booking>emptyList(),
                "", stay, new SearchFilters(4.8f, null, pool), SortOrder.RATING);
        assertFalse(pools.isEmpty());
        for (HotelResult result : pools) {
            assertTrue(result.hotel.rating >= 4.8f);
            assertTrue(result.hotel.amenities.contains(Amenity.POOL));
        }
    }

    @Test
    public void validationRejectsBadInput() {
        RoomOffer offer = offers.get(0);
        assertNotNull(BookingRules.validate("", "a@b.co", offer, stay, today));
        assertNotNull(BookingRules.validate("Ann", "nope", offer, stay, today));
        assertNotNull(BookingRules.validate("Ann", "a@b.co", offer, stay.withCheckOut(stay.checkIn), today));
        assertNotNull(BookingRules.validate("Ann", "a@b.co", offer, stay.withGuests(3), today));
        assertNull(BookingRules.validate("Ann", "a@b.co", offer, stay, today));
    }

    @Test
    public void tripStagesAndCancellationFollowTheDates() {
        List<Booking> trips = SampleData.bookings(today);
        assertEquals(new HashSet<>(Arrays.asList("bk-1001", "bk-1002")),
                idSet(BookingRules.trips(trips, TripStage.UPCOMING, today)));
        assertEquals(new HashSet<>(Arrays.asList("bk-1003", "bk-1004")),
                idSet(BookingRules.trips(trips, TripStage.PAST, today)));
        assertEquals(Collections.singletonList("bk-1005"),
                bookingIds(BookingRules.trips(trips, TripStage.CANCELLED, today)));
        assertTrue(BookingRules.canCancel(find(trips, "bk-1001"), today));
        assertFalse(BookingRules.canCancel(find(trips, "bk-1003"), today));
    }

    private Booking find(List<Booking> bookings, String id) {
        for (Booking booking : bookings) {
            if (booking.id.equals(id)) {
                return booking;
            }
        }
        throw new IllegalStateException("Missing booking " + id);
    }

    private List<String> bookingIds(List<Booking> bookings) {
        List<String> ids = new ArrayList<>();
        for (Booking booking : bookings) {
            ids.add(booking.id);
        }
        return ids;
    }

    private Set<String> idSet(List<Booking> bookings) {
        return new HashSet<>(bookingIds(bookings));
    }
}
