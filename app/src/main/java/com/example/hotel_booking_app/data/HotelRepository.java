package com.example.hotel_booking_app.data;

import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private final List<Account> accounts = new ArrayList<>();
    private GuestProfile profile = SampleData.PROFILE;
    private String currentUserEmail = "";
    private ThemeMode themeMode;

    public HotelRepository(@Nullable HotelStorage storage, Supplier<LocalDate> today) {
        this.storage = storage;
        this.today = today;
        this.themeMode = storage != null ? storage.loadThemeMode() : ThemeMode.SYSTEM;

        StoredState saved = storage != null ? storage.load() : null;
        if (saved != null) {
            if (saved.hotels != null && !saved.hotels.isEmpty()) {
                catalog.clear();
                catalog.addAll(saved.hotels);
            }
            if (saved.offers != null && !saved.offers.isEmpty()) {
                offers.clear();
                offers.addAll(saved.offers);
            }
            bookings.addAll(saved.bookings);
            reviews.addAll(SampleData.reviews(today.get()));
            reviews.addAll(saved.reviews);
            favorites.addAll(saved.favorites);
            profile = saved.profile;
            if (saved.accounts != null && !saved.accounts.isEmpty()) {
                accounts.addAll(saved.accounts);
            } else {
                accounts.addAll(SampleData.accounts());
            }
            currentUserEmail = saved.currentUserEmail != null ? saved.currentUserEmail : "";
        } else {
            accounts.addAll(SampleData.accounts());
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
        List<Booking> staged = BookingRules.trips(bookings, stage, today.get());
        List<Booking> withHotel = new ArrayList<>();
        for (Booking booking : staged) {
            if (hotel(booking.hotelId) != null) {
                withHotel.add(booking);
            }
        }
        return withHotel;
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
                               StayRequest stay, int rooms, String specialRequests,
                               boolean breakfastIncluded, String promoCode, boolean payAtHotel) {
        RoomOffer offer = offer(offerId);
        if (offer == null) {
            return BookingOutcome.rejected("That room is no longer offered");
        }
        String problem = BookingRules.validate(guestName, guestEmail, offer, stay, rooms, today.get());
        if (problem != null) {
            return BookingOutcome.rejected(problem);
        }
        int available = unitsLeft(offer, stay);
        if (available == 0) {
            return BookingOutcome.rejected(offer.type.label + " rooms are sold out for those dates");
        }
        if (rooms > available) {
            return BookingOutcome.rejected("Only " + available + " " + offer.type.label
                    + " room" + (available == 1 ? "" : "s") + " left for those dates");
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
                rooms,
                BookingStatus.PENDING,
                specialRequests.trim(),
                today.get(),
                offer.nightlyRate,
                breakfastIncluded,
                promoPercent,
                payAtHotel,
                null,
                "",
                RefundStatus.NONE);
        bookings.add(booking);
        profile = new GuestProfile(booking.guestName, booking.guestEmail, booking.guestPhone);
        persist();
        return BookingOutcome.confirmed(booking.id);
    }

    @Nullable
    public String cancelBooking(String bookingId, String reason) {
        Booking booking = booking(bookingId);
        if (booking == null) {
            return "Booking not found";
        }
        if (!canCancel(booking)) {
            return "Only a confirmed booking that hasn't started can be cancelled";
        }
        cancel(booking, BookingStatus.CANCELLED, reason, "Cancelled by guest");
        persist();
        return null;
    }

    @Nullable
    public String confirmBooking(String bookingId) {
        return decide(bookingId, BookingStatus.CONFIRMED);
    }

    @Nullable
    public String rejectBooking(String bookingId, String reason) {
        Booking booking = booking(bookingId);
        if (booking == null) {
            return "Booking not found";
        }
        if (booking.status != BookingStatus.PENDING) {
            return "Only a pending reservation can be rejected";
        }
        cancel(booking, BookingStatus.REJECTED, reason, "Rejected by admin");
        persist();
        return null;
    }

    @Nullable
    private String decide(String bookingId, BookingStatus decision) {
        Booking booking = booking(bookingId);
        if (booking == null) {
            return "Booking not found";
        }
        if (booking.status != BookingStatus.PENDING) {
            return "Only a pending reservation can be " + decision.label.toLowerCase();
        }
        bookings.set(bookings.indexOf(booking), booking.withStatus(decision));
        persist();
        return null;
    }

    @Nullable
    public String setBookingStatus(String bookingId, BookingStatus status) {
        Booking booking = booking(bookingId);
        if (booking == null) {
            return "Booking not found";
        }
        if (booking.status == status) {
            return null;
        }
        Booking updated;
        if (status == BookingStatus.CANCELLED || status == BookingStatus.REJECTED) {
            if (booking.cancelledOn != null) {
                updated = booking.withStatus(status);
            } else {
                String fallback = status == BookingStatus.REJECTED ? "Rejected by admin"
                        : "Cancelled by admin";
                updated = cancellation(booking, status, fallback, fallback);
            }
        } else {
            updated = booking.withCancellation(status, null, "", RefundStatus.NONE);
        }
        bookings.set(bookings.indexOf(booking), updated);
        persist();
        return null;
    }

    @Nullable
    public String setRefundStatus(String bookingId, RefundStatus refundStatus) {
        Booking booking = booking(bookingId);
        if (booking == null) {
            return "Booking not found";
        }
        bookings.set(bookings.indexOf(booking), booking.withRefundStatus(refundStatus));
        persist();
        return null;
    }

    private void cancel(Booking booking, BookingStatus status, String reason, String fallback) {
        bookings.set(bookings.indexOf(booking), cancellation(booking, status, reason, fallback));
    }

    private Booking cancellation(Booking booking, BookingStatus status, String reason, String fallback) {
        String resolvedReason = reason.trim().isEmpty() ? fallback : reason.trim();
        RefundStatus refund = booking.payAtHotel ? RefundStatus.NONE : RefundStatus.PENDING;
        return booking.withCancellation(status, today.get(), resolvedReason, refund);
    }

    public String addHotel(String name, String city, String country, String address,
                           String description, int stars, List<Amenity> amenities, int photoIndex,
                           int baseRate) {
        String hotelId = newHotelId();
        catalog.add(new Hotel(hotelId, name, city, country, address, description, stars, 0f, 0,
                new ArrayList<>(amenities), new ArrayList<>(), photoIndex));
        for (RoomType type : RoomType.values()) {
            offers.add(new RoomOffer(
                    hotelId + "-" + type.name().toLowerCase(Locale.ROOT),
                    hotelId,
                    type,
                    SampleData.rateFor(baseRate, type),
                    SampleData.ROOM_UNITS,
                    SampleData.beds(type),
                    SampleData.sizeSqm(type)));
        }
        persist();
        return hotelId;
    }

    @Nullable
    public String updateOffer(String offerId, int nightlyRate, int units) {
        RoomOffer offer = offer(offerId);
        if (offer == null) {
            return "That room is no longer offered";
        }
        offers.set(offers.indexOf(offer), offer.withPriceAndUnits(nightlyRate, units));
        persist();
        return null;
    }

    @Nullable
    public String updateHotel(String hotelId, String name, String city, String country, String address,
                              String description, int stars, List<Amenity> amenities, int photoIndex) {
        Hotel hotel = hotel(hotelId);
        if (hotel == null) {
            return "That hotel is no longer listed";
        }
        catalog.set(catalog.indexOf(hotel), hotel.withInfo(name, city, country, address, description,
                stars, new ArrayList<>(amenities), photoIndex));
        persist();
        return null;
    }

    @Nullable
    public String deleteHotel(String hotelId) {
        Hotel hotel = hotel(hotelId);
        if (hotel == null) {
            return "That hotel is no longer listed";
        }
        catalog.remove(hotel);
        offers.removeIf(offer -> offer.hotelId.equals(hotelId));
        persist();
        return null;
    }

    public int totalHotels() {
        return catalog.size();
    }

    public int totalBookings() {
        return bookings.size();
    }

    public int cancelledCount() {
        int count = 0;
        for (Booking booking : bookings) {
            if (booking.status == BookingStatus.CANCELLED || booking.status == BookingStatus.REJECTED) {
                count++;
            }
        }
        return count;
    }

    public int revenue() {
        int total = 0;
        for (Booking booking : bookings) {
            if (booking.status == BookingStatus.CONFIRMED) {
                total += BookingRules.price(booking).total;
            }
        }
        return total;
    }

    public int totalUsers() {
        return users().size();
    }

    public List<GuestAccount> users() {
        Map<String, GuestAccount> byEmail = new LinkedHashMap<>();
        for (Booking booking : bookings) {
            String key = booking.guestEmail.toLowerCase(Locale.ROOT);
            GuestAccount existing = byEmail.get(key);
            int count = existing == null ? 1 : existing.bookings + 1;
            byEmail.put(key, new GuestAccount(booking.guestName, booking.guestEmail,
                    booking.guestPhone, count));
        }
        return new ArrayList<>(byEmail.values());
    }

    public List<Booking> bookingsForGuest(String email) {
        List<Booking> matching = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.guestEmail.equalsIgnoreCase(email)) {
                matching.add(booking);
            }
        }
        Collections.sort(matching, (a, b) -> b.bookedOn.compareTo(a.bookedOn));
        return matching;
    }

    public List<Booking> cancellations() {
        List<Booking> cancelled = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.status == BookingStatus.CANCELLED || booking.status == BookingStatus.REJECTED) {
                cancelled.add(booking);
            }
        }
        Collections.sort(cancelled, (a, b) -> compareCancelledOn(b, a));
        return cancelled;
    }

    private int compareCancelledOn(Booking a, Booking b) {
        if (a.cancelledOn == null && b.cancelledOn == null) {
            return 0;
        }
        if (a.cancelledOn == null) {
            return -1;
        }
        if (b.cancelledOn == null) {
            return 1;
        }
        return a.cancelledOn.compareTo(b.cancelledOn);
    }

    public int pendingCount() {
        int count = 0;
        for (Booking booking : bookings) {
            if (booking.status == BookingStatus.PENDING) {
                count++;
            }
        }
        return count;
    }

    public int availableRoomsTonight() {
        LocalDate checkIn = today.get();
        LocalDate checkOut = checkIn.plusDays(1);
        int available = 0;
        for (RoomOffer offer : offers) {
            available += BookingRules.unitsLeft(offer, bookings, checkIn, checkOut);
        }
        return available;
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
        Account current = currentUser();
        if (current != null) {
            accounts.set(accounts.indexOf(current), current.withProfile(profile.name, profile.phone));
        }
        persist();
        return null;
    }

    @Nullable
    public Account currentUser() {
        return accountByEmail(currentUserEmail);
    }

    public boolean isLoggedIn() {
        return currentUser() != null;
    }

    public boolean isAdmin() {
        Account current = currentUser();
        return current != null && current.admin;
    }

    @Nullable
    public String logIn(String email, String password) {
        String trimmed = email.trim();
        if (trimmed.isEmpty() || password.isEmpty()) {
            return "Enter your email and password";
        }
        Account account = accountByEmail(trimmed);
        if (account == null || !PasswordHash.matches(password, account.passwordHash)) {
            return "Incorrect email or password";
        }
        currentUserEmail = account.email;
        profile = new GuestProfile(account.name, account.email, account.phone);
        persist();
        return null;
    }

    @Nullable
    public String signUp(String name, String email, String phone, String password) {
        String trimmedName = name.trim();
        String trimmedEmail = email.trim();
        if (trimmedName.isEmpty()) {
            return "Enter your name";
        }
        if (!trimmedEmail.contains("@")) {
            return "Enter a valid email address";
        }
        if (password.length() < 6) {
            return "Password must be at least 6 characters";
        }
        if (accountByEmail(trimmedEmail) != null) {
            return "An account with this email already exists";
        }
        Account account = new Account(trimmedEmail, trimmedName, phone.trim(),
                PasswordHash.of(password), false);
        accounts.add(account);
        currentUserEmail = account.email;
        profile = new GuestProfile(account.name, account.email, account.phone);
        persist();
        return null;
    }

    public void logOut() {
        currentUserEmail = "";
        persist();
    }

    @Nullable
    private Account accountByEmail(String email) {
        for (Account account : accounts) {
            if (account.email.equalsIgnoreCase(email)) {
                return account;
            }
        }
        return null;
    }

    public void setThemeMode(ThemeMode mode) {
        themeMode = mode;
        if (storage != null) {
            storage.saveThemeMode(mode);
        }
    }

    public void resetToSample() {
        catalog.clear();
        catalog.addAll(SampleData.hotels());
        offers.clear();
        offers.addAll(SampleData.offers());
        bookings.clear();
        reviews.clear();
        favorites.clear();
        Account current = currentUser();
        profile = current != null
                ? new GuestProfile(current.name, current.email, current.phone)
                : SampleData.PROFILE;
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

    private String newHotelId() {
        int highest = 0;
        for (Hotel hotel : catalog) {
            if (hotel.id.startsWith("htl-own-")) {
                try {
                    highest = Math.max(highest, Integer.parseInt(hotel.id.substring(8)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "htl-own-" + (highest + 1);
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
        storage.save(new StoredState(new ArrayList<>(catalog), new ArrayList<>(offers),
                new ArrayList<>(bookings), userReviews, favoriteSet, profile,
                new ArrayList<>(accounts), currentUserEmail));
    }
}
