package com.example.hotel_booking_app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.hotel_booking_app.data.BookingOutcome;
import com.example.hotel_booking_app.data.BookingStatus;
import com.example.hotel_booking_app.data.HotelRepository;
import com.example.hotel_booking_app.data.RefundStatus;
import com.example.hotel_booking_app.data.RoomOffer;
import com.example.hotel_booking_app.data.SampleData;
import com.example.hotel_booking_app.data.StayRequest;
import com.example.hotel_booking_app.data.TripStage;

import java.util.ArrayList;

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
                stay, 1, "", false, "", false);
        assertTrue(outcome.confirmed);
        String id = outcome.bookingId;

        assertEquals(before + 1, repo.trips(TripStage.UPCOMING).size());
        assertNull(repo.cancelBooking(id, ""));
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
                    stay, 1, "", false, "", false).confirmed);
        }
        assertTrue(!repo.book("htl-munnar-suite", "Ann", "", "ann@example.com",
                stay, 1, "", false, "", false).confirmed);
        for (RoomOffer open : repo.availableOffers("htl-munnar", stay)) {
            assertTrue(!open.id.equals("htl-munnar-suite"));
        }
    }

    @Test
    public void pastBookingsCannotBeCancelled() {
        assertNotNull(repo().cancelBooking("bk-1003", ""));
    }

    @Test
    public void newBookingsStartPendingAndTheOwnerCanConfirmThem() {
        HotelRepository repo = repo();
        String id = repo.book("htl-munnar-deluxe", "Ann", "", "ann@example.com",
                stay, 1, "", false, "", false).bookingId;
        assertEquals(BookingStatus.PENDING, repo.booking(id).status);
        assertNull(repo.confirmBooking(id));
        assertEquals(BookingStatus.CONFIRMED, repo.booking(id).status);
        assertNotNull(repo.confirmBooking(id));
    }

    @Test
    public void rejectingAPendingBookingFreesItsRooms() {
        HotelRepository repo = repo();
        RoomOffer offer = repo.offer("htl-munnar-suite");
        int before = repo.unitsLeft(offer, stay);
        String id = repo.book("htl-munnar-suite", "Ann", "", "ann@example.com",
                stay, 2, "", false, "", false).bookingId;
        assertEquals(before - 2, repo.unitsLeft(offer, stay));
        assertNull(repo.rejectBooking(id, "Overbooked"));
        assertEquals(before, repo.unitsLeft(offer, stay));
        assertEquals("Overbooked", repo.booking(id).cancellationReason);
        assertNotNull(repo.booking(id).cancelledOn);
    }

    @Test
    public void bookingMoreRoomsThanAvailableIsRejected() {
        HotelRepository repo = repo();
        RoomOffer offer = repo.offer("htl-munnar-suite");
        assertTrue(!repo.book("htl-munnar-suite", "Ann", "", "ann@example.com",
                stay, offer.units + 1, "", false, "", false).confirmed);
    }

    @Test
    public void addedHotelGetsFiveRoomsPerType() {
        HotelRepository repo = repo();
        int before = repo.totalHotels();
        String hotelId = repo.addHotel("Test Stay", "Pune", "India", "1 MG Road",
                "A test hotel", 4, new java.util.ArrayList<>(), 0, 8000);
        assertEquals(before + 1, repo.totalHotels());
        assertEquals(3, repo.offersFor(hotelId).size());
        for (RoomOffer offer : repo.offersFor(hotelId)) {
            assertEquals(5, offer.units);
        }
    }

    @Test
    public void invalidPromoIsRejectedAndValidOneStored() {
        HotelRepository repo = repo();
        assertTrue(!repo.book("htl-goa-deluxe", "Ann", "", "ann@example.com",
                stay, 1, "", false, "BOGUS", false).confirmed);
        BookingOutcome outcome = repo.book("htl-goa-deluxe", "Ann", "", "ann@example.com",
                stay, 1, "", true, "welcome10", true);
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

    @Test
    public void cancellingStampsReasonDateAndRefund() {
        HotelRepository repo = repo();
        assertNull(repo.cancelBooking("bk-1001", "Family emergency"));
        assertEquals("Family emergency", repo.booking("bk-1001").cancellationReason);
        assertNotNull(repo.booking("bk-1001").cancelledOn);
        assertEquals(RefundStatus.PENDING, repo.booking("bk-1001").refundStatus);
    }

    @Test
    public void revenueDropsWhenAConfirmedBookingIsCancelled() {
        HotelRepository repo = repo();
        int before = repo.revenue();
        assertTrue(before > 0);
        repo.cancelBooking("bk-1001", "");
        assertTrue(repo.revenue() < before);
    }

    @Test
    public void deleteHotelRemovesItAndItsRooms() {
        HotelRepository repo = repo();
        int before = repo.totalHotels();
        assertNull(repo.deleteHotel("htl-goa"));
        assertNull(repo.hotel("htl-goa"));
        assertTrue(repo.offersFor("htl-goa").isEmpty());
        assertEquals(before - 1, repo.totalHotels());
    }

    @Test
    public void updateHotelKeepsIdAndChangesInfo() {
        HotelRepository repo = repo();
        assertNull(repo.updateHotel("htl-goa", "Palm Cove Renamed", "Goa", "India", "New Road",
                "Updated description", 5, new ArrayList<>(), 2));
        assertEquals("Palm Cove Renamed", repo.hotel("htl-goa").name);
        assertEquals(5, repo.hotel("htl-goa").stars);
    }

    @Test
    public void appStartsLoggedOutAndDemoAccountsCanLogIn() {
        HotelRepository repo = repo();
        assertFalse(repo.isLoggedIn());
        assertNull(repo.logIn("nehapatel@gmail.com", SampleData.DEMO_PASSWORD));
        assertTrue(repo.isLoggedIn());
        assertFalse(repo.isAdmin());
    }

    @Test
    public void wrongPasswordIsRejected() {
        HotelRepository repo = repo();
        assertNotNull(repo.logIn("nehapatel@gmail.com", "nope"));
        assertFalse(repo.isLoggedIn());
    }

    @Test
    public void adminAccountIsFlaggedAsAdmin() {
        HotelRepository repo = repo();
        assertNull(repo.logIn(SampleData.ADMIN_EMAIL, SampleData.DEMO_PASSWORD));
        assertTrue(repo.isAdmin());
    }

    @Test
    public void signUpCreatesAnAccountThenLogsIn() {
        HotelRepository repo = repo();
        assertNull(repo.signUp("New Guest", "new.account@example.com", "123", "secret1"));
        assertTrue(repo.isLoggedIn());
        assertEquals("new.account@example.com", repo.currentUser().email);
    }

    @Test
    public void signUpWithAnExistingEmailIsRejected() {
        HotelRepository repo = repo();
        assertNotNull(repo.signUp("Dup", "nehapatel@gmail.com", "1", "secret1"));
    }

    @Test
    public void logOutClearsTheSession() {
        HotelRepository repo = repo();
        repo.logIn(SampleData.ADMIN_EMAIL, SampleData.DEMO_PASSWORD);
        repo.logOut();
        assertFalse(repo.isLoggedIn());
    }

    @Test
    public void usersAreGroupedByEmail() {
        HotelRepository repo = repo();
        int before = repo.totalUsers();
        repo.book("htl-munnar-deluxe", "New Guest", "", "new.guest@example.com",
                stay, 1, "", false, "", false);
        assertEquals(before + 1, repo.totalUsers());
        repo.book("htl-munnar-suite", "New Guest", "", "new.guest@example.com",
                stay, 1, "", false, "", false);
        assertEquals(before + 1, repo.totalUsers());
        assertEquals(2, repo.bookingsForGuest("new.guest@example.com").size());
    }
}
