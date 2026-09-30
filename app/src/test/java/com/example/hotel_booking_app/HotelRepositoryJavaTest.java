package com.example.hotel_booking_app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.hotel_booking_app.data.BookingOutcome;
import com.example.hotel_booking_app.data.HotelRepository;
import com.example.hotel_booking_app.data.RoomOffer;
import com.example.hotel_booking_app.data.StayRequest;
import com.example.hotel_booking_app.data.TripStage;

import org.junit.Test;

import java.time.LocalDate;

public class HotelRepositoryJavaTest {

    private final LocalDate today = LocalDate.of(2026, 6, 1);
    private final StayRequest stay = new StayRequest(today.plusDays(50), today.plusDays(52), 2);

    private HotelRepository repo() {
        return new HotelRepository(null, () -> today);
    }

    @Test
    public void bookingAddsAnUpcomingTripThatCanBeCancelled() {
        HotelRepository repo = repo();
        int before = repo.trips(TripStage.UPCOMING).size();

        BookingOutcome outcome = repo.book("htl-munnar-deluxe", "Ann Lee", "", "ann@example.com",
                stay, "", false, "", false);
        assertTrue(outcome.confirmed);
        String id = outcome.bookingId;

        assertEquals(before + 1, repo.trips(TripStage.UPCOMING).size());
        assertNull(repo.cancelBooking(id));
        int cancelledWithId = 0;
        for (int i = 0; i < repo.trips(TripStage.CANCELLED).size(); i++) {
            if (repo.trips(TripStage.CANCELLED).get(i).id.equals(id)) {
                cancelledWithId++;
            }
        }
        assertEquals(1, cancelledWithId);
    }

    @Test
    public void soldOutRoomsAreRejected() {
        HotelRepository repo = repo();
        RoomOffer offer = repo.offer("htl-munnar-suite");
        assertNotNull(offer);
        for (int i = 0; i < offer.units; i++) {
            assertTrue(repo.book("htl-munnar-suite", "Ann", "", "ann@example.com",
                    stay, "", false, "", false).confirmed);
        }
        assertTrue(!repo.book("htl-munnar-suite", "Ann", "", "ann@example.com",
                stay, "", false, "", false).confirmed);
        for (RoomOffer open : repo.availableOffers("htl-munnar", stay)) {
            assertTrue(!open.id.equals("htl-munnar-suite"));
        }
    }

    @Test
    public void pastBookingsCannotBeCancelled() {
        assertNotNull(repo().cancelBooking("bk-1003"));
    }

    @Test
    public void invalidPromoIsRejectedAndValidOneStored() {
        HotelRepository repo = repo();
        assertTrue(!repo.book("htl-goa-deluxe", "Ann", "", "ann@example.com",
                stay, "", false, "BOGUS", false).confirmed);
        BookingOutcome outcome = repo.book("htl-goa-deluxe", "Ann", "", "ann@example.com",
                stay, "", true, "welcome10", true);
        assertTrue(outcome.confirmed);
        assertEquals(10, repo.booking(outcome.bookingId).promoPercent);
    }

    @Test
    public void onlyPastStaysCanBeReviewedOnce() {
        HotelRepository repo = repo();
        assertNotNull(repo.addReview("bk-1001", 5f, "Great"));
        assertNull(repo.addReview("bk-1003", 4f, "Lovely rooftop dinner"));
        assertNotNull(repo.addReview("bk-1003", 4f, "Again"));
        assertEquals(4f, repo.reviewFor("bk-1003").rating, 0.0001f);
    }

    @Test
    public void favoritesToggle() {
        HotelRepository repo = repo();
        repo.toggleFavorite("htl-goa");
        assertTrue(repo.isFavorite("htl-goa"));
        repo.toggleFavorite("htl-goa");
        assertTrue(repo.favoriteHotels().isEmpty());
    }
}
