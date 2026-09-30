package com.example.hotel_booking_app.data;

import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class HotelRepository {

    private final HotelStorage storage;
    private final Supplier<LocalDate> today;

    private final List<Hotel> catalog = SampleData.hotels();
    private final List<RoomOffer> offers = SampleData.offers();
    private final List<Review> reviews = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();
    private final List<String> favorites = new ArrayList<>();
    private GuestProfile profile = SampleData.PROFILE;
    private ThemeMode themeMode;

    public HotelRepository(@Nullable HotelStorage storage, Supplier<LocalDate> today) {
        this.storage = storage;
        this.today = today;
        this.themeMode = storage != null ? storage.loadThemeMode() : ThemeMode.SYSTEM;

        StoredState saved = storage != null ? storage.load() : null;
        if (saved != null) {
            bookings.addAll(saved.bookings);
            reviews.addAll(SampleData.reviews(today.get()));
            reviews.addAll(saved.reviews);
            favorites.addAll(saved.favorites);
            profile = saved.profile;
        } else {
            seed();
        }
    }

    public List<Hotel> getHotels() {
        return catalog;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public GuestProfile getProfile() {
        return profile;
    }

    public ThemeMode getThemeMode() {
        return themeMode;
    }

    @Nullable
    public Hotel hotel(@Nullable String id) {
        for (Hotel hotel : catalog) {
            if (hotel.id.equals(id)) {
                return hotel;
            }
        }
        return null;
    }

    @Nullable
    public RoomOffer offer(@Nullable String id) {
        for (RoomOffer offer : offers) {
            if (offer.id.equals(id)) {
                return offer;
            }
        }
        return null;
    }

    public List<RoomOffer> offersFor(String hotelId) {
        List<RoomOffer> forHotel = new ArrayList<>();
        for (RoomOffer offer : offers) {
            if (offer.hotelId.equals(hotelId)) {
                forHotel.add(offer);
            }
        }
        return forHotel;
    }

    public List<Review> reviewsFor(String hotelId) {
        List<Review> forHotel = new ArrayList<>();
        for (Review review : reviews) {
            if (review.hotelId.equals(hotelId)) {
                forHotel.add(review);
            }
        }
        Collections.sort(forHotel, (a, b) -> b.date.compareTo(a.date));
        return forHotel;
    }

    @Nullable
    public Booking booking(@Nullable String id) {
        for (Booking booking : bookings) {
            if (booking.id.equals(id)) {
                return booking;
            }
        }
        return null;
    }

    public LocalDate currentDate() {
        return today.get();
    }

    public List<String> cities() {
        List<String> cities = new ArrayList<>();
        for (Hotel hotel : catalog) {
            if (!cities.contains(hotel.city)) {
                cities.add(hotel.city);
            }
        }
        return cities;
    }

    public List<HotelResult> search(String query, StayRequest stay, SearchFilters filters, SortOrder sort) {
        return BookingRules.search(catalog, offers, bookings, query, stay, filters, sort);
    }

    @Nullable
    public Review reviewFor(String bookingId) {
        for (Review review : reviews) {
            if (review.bookingId.equals(bookingId)) {
                return review;
            }
        }
        return null;
    }

    public List<RoomOffer> availableOffers(String hotelId, StayRequest stay) {
        return BookingRules.availableOffers(offersFor(hotelId), bookings, stay);
    }

    public int unitsLeft(RoomOffer offer, StayRequest stay) {
        return BookingRules.unitsLeft(offer, bookings, stay.checkIn, stay.checkOut);
    }

    public List<Booking> trips(TripStage stage) {
        return BookingRules.trips(bookings, stage, today.get());
    }

    public TripStage stage(Booking booking) {
        return BookingRules.stage(booking, today.get());
    }

    public boolean canCancel(Booking booking) {
        return BookingRules.canCancel(booking, today.get());
    }

    public boolean isFavorite(String hotelId) {
        return favorites.contains(hotelId);
    }

    public List<Hotel> favoriteHotels() {
        List<Hotel> saved = new ArrayList<>();
        for (Hotel hotel : catalog) {
            if (favorites.contains(hotel.id)) {
                saved.add(hotel);
            }
        }
        return saved;
    }

    public void toggleFavorite(String hotelId) {
        if (!favorites.remove(hotelId)) {
            favorites.add(hotelId);
        }
        persist();
    }

    public BookingOutcome book(String offerId, String guestName, String guestPhone, String guestEmail,
                               StayRequest stay, String specialRequests, boolean breakfastIncluded,
                               String promoCode, boolean payAtHotel) {
        RoomOffer offer = offer(offerId);
        if (offer == null) {
            return BookingOutcome.rejected("That room is no longer offered");
        }
        String problem = BookingRules.validate(guestName, guestEmail, offer, stay, today.get());
        if (problem != null) {
            return BookingOutcome.rejected(problem);
        }
        if (unitsLeft(offer, stay) == 0) {
            return BookingOutcome.rejected(offer.type.label + " rooms are sold out for those dates");
        }
        int promoPercent;
        if (promoCode.trim().isEmpty()) {
            promoPercent = 0;
        } else {
            Integer percent = BookingRules.promoPercent(promoCode);
            if (percent == null) {
                return BookingOutcome.rejected("Promo code " + promoCode.trim() + " isn't valid");
            }
            promoPercent = percent;
        }
        Booking booking = new Booking(
                newBookingId(),
                offer.hotelId,
                offer.id,
                guestName.trim(),
                guestPhone.trim(),
                guestEmail.trim(),
                stay.checkIn,
                stay.checkOut,
                stay.guests,
                BookingStatus.CONFIRMED,
                specialRequests.trim(),
                today.get(),
                offer.nightlyRate,
                breakfastIncluded,
                promoPercent,
                payAtHotel);
        bookings.add(booking);
        profile = new GuestProfile(booking.guestName, booking.guestEmail, booking.guestPhone);
        persist();
        return BookingOutcome.confirmed(booking.id);
    }

    @Nullable
    public String cancelBooking(String bookingId) {
        Booking booking = booking(bookingId);
        if (booking == null) {
            return "Booking not found";
        }
        if (!canCancel(booking)) {
            return "Only a confirmed booking that hasn't started can be cancelled";
        }
        bookings.set(bookings.indexOf(booking), booking.withStatus(BookingStatus.CANCELLED));
        persist();
        return null;
    }

    @Nullable
    public String addReview(String bookingId, float rating, String comment) {
        Booking booking = booking(bookingId);
        if (booking == null) {
            return "Booking not found";
        }
        if (stage(booking) != TripStage.PAST) {
            return "You can review a stay once it is over";
        }
        if (reviewFor(bookingId) != null) {
            return "You have already reviewed this stay";
        }
        if (comment.trim().isEmpty()) {
            return "Tell other guests a little about your stay";
        }
        reviews.add(new Review(
                "rv-" + booking.id,
                booking.hotelId,
                booking.id,
                booking.guestName,
                rating,
                comment.trim(),
                today.get()));
        persist();
        return null;
    }

    @Nullable
    public String saveProfile(GuestProfile candidate) {
        if (candidate.name.trim().isEmpty()) {
            return "Enter your name";
        }
        if (!candidate.email.contains("@")) {
            return "Enter a valid email address";
        }
        profile = new GuestProfile(candidate.name.trim(), candidate.email.trim(), candidate.phone.trim());
        persist();
        return null;
    }

    public void setThemeMode(ThemeMode mode) {
        themeMode = mode;
        if (storage != null) {
            storage.saveThemeMode(mode);
        }
    }

    public void resetToSample() {
        bookings.clear();
        reviews.clear();
        favorites.clear();
        profile = SampleData.PROFILE;
        seed();
    }

    private void seed() {
        reviews.addAll(SampleData.reviews(today.get()));
        bookings.addAll(SampleData.bookings(today.get()));
        persist();
    }

    private String newBookingId() {
        int highest = 1000;
        for (Booking booking : bookings) {
            if (booking.id.startsWith("bk-")) {
                try {
                    highest = Math.max(highest, Integer.parseInt(booking.id.substring(3)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "bk-" + (highest + 1);
    }

    private void persist() {
        if (storage == null) {
            return;
        }
        List<Review> userReviews = new ArrayList<>();
        for (Review review : reviews) {
            if (!review.bookingId.isEmpty()) {
                userReviews.add(review);
            }
        }
        Set<String> favoriteSet = new LinkedHashSet<>(favorites);
        storage.save(new StoredState(new ArrayList<>(bookings), userReviews, favoriteSet, profile));
    }
}
