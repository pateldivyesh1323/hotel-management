package com.example.hotel_booking_app.data;

import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class BookingRules {

    public static final int TAX_PERCENT = 12;
    public static final int BREAKFAST_RATE = 500;

    private static final Map<String, Integer> PROMO_CODES = new LinkedHashMap<>();

    static {
        PROMO_CODES.put("WELCOME10", 10);
        PROMO_CODES.put("SUMMER15", 15);
        PROMO_CODES.put("STAYLONGER20", 20);
    }

    private BookingRules() {
    }

    public static String promoCodeHints() {
        return String.join(", ", PROMO_CODES.keySet());
    }

    @Nullable
    public static Integer promoPercent(String code) {
        return PROMO_CODES.get(code.trim().toUpperCase(Locale.ROOT));
    }

    public static boolean overlaps(LocalDate firstIn, LocalDate firstOut,
                                   LocalDate secondIn, LocalDate secondOut) {
        return firstIn.isBefore(secondOut) && secondIn.isBefore(firstOut);
    }

    public static int unitsLeft(RoomOffer offer, List<Booking> bookings,
                                LocalDate checkIn, LocalDate checkOut) {
        int taken = 0;
        for (Booking booking : bookings) {
            if (booking.roomId.equals(offer.id) && booking.holdsRoom()
                    && overlaps(booking.checkIn, booking.checkOut, checkIn, checkOut)) {
                taken++;
            }
        }
        return Math.max(offer.units - taken, 0);
    }

    public static List<RoomOffer> availableOffers(List<RoomOffer> offers, List<Booking> bookings,
                                                  StayRequest stay) {
        List<RoomOffer> open = new ArrayList<>();
        for (RoomOffer offer : offers) {
            if (offer.type.maxGuests >= stay.guests
                    && unitsLeft(offer, bookings, stay.checkIn, stay.checkOut) > 0) {
                open.add(offer);
            }
        }
        Collections.sort(open, Comparator.comparingInt(o -> o.nightlyRate));
        return open;
    }

    public static boolean matchesQuery(Hotel hotel, String query) {
        String needle = query.trim().toLowerCase(Locale.ROOT);
        return needle.isEmpty()
                || hotel.name.toLowerCase(Locale.ROOT).contains(needle)
                || hotel.city.toLowerCase(Locale.ROOT).contains(needle)
                || hotel.country.toLowerCase(Locale.ROOT).contains(needle);
    }

    public static boolean matchesFilters(Hotel hotel, SearchFilters filters) {
        return hotel.rating >= filters.minRating && hotel.amenities.containsAll(filters.amenities);
    }

    public static List<HotelResult> search(List<Hotel> hotels, List<RoomOffer> offers,
                                           List<Booking> bookings, String query, StayRequest stay,
                                           SearchFilters filters, SortOrder sort) {
        List<HotelResult> results = new ArrayList<>();
        for (Hotel hotel : hotels) {
            if (!matchesQuery(hotel, query) || !matchesFilters(hotel, filters)) {
                continue;
            }
            List<RoomOffer> forHotel = new ArrayList<>();
            for (RoomOffer offer : offers) {
                if (offer.hotelId.equals(hotel.id)) {
                    forHotel.add(offer);
                }
            }
            List<RoomOffer> open = availableOffers(forHotel, bookings, stay);
            if (open.isEmpty()) {
                continue;
            }
            int fromRate = Integer.MAX_VALUE;
            for (RoomOffer offer : open) {
                fromRate = Math.min(fromRate, offer.nightlyRate);
            }
            if (filters.maxNightlyRate != null && fromRate > filters.maxNightlyRate) {
                continue;
            }
            results.add(new HotelResult(hotel, fromRate));
        }
        sortResults(results, sort);
        return results;
    }

    private static void sortResults(List<HotelResult> results, SortOrder sort) {
        switch (sort) {
            case RECOMMENDED:
                Collections.sort(results, (a, b) -> Float.compare(
                        b.hotel.rating * b.hotel.reviewCount, a.hotel.rating * a.hotel.reviewCount));
                break;
            case PRICE_LOW:
                Collections.sort(results, Comparator.comparingInt(r -> r.fromRate));
                break;
            case PRICE_HIGH:
                Collections.sort(results, (a, b) -> Integer.compare(b.fromRate, a.fromRate));
                break;
            case RATING:
                Collections.sort(results, (a, b) -> Float.compare(b.hotel.rating, a.hotel.rating));
                break;
        }
    }

    public static PriceBreakdown price(int nightlyRate, int nights, int guests,
                                       boolean breakfastIncluded, int promoPercent) {
        int subtotal = nightlyRate * nights;
        int breakfast = breakfastIncluded ? BREAKFAST_RATE * guests * nights : 0;
        int discount = subtotal * promoPercent / 100;
        int taxable = subtotal + breakfast - discount;
        int taxes = taxable * TAX_PERCENT / 100;
        return new PriceBreakdown(nights, nightlyRate, subtotal, breakfast, discount, taxes, taxable + taxes);
    }

    public static PriceBreakdown price(Booking booking) {
        return price(booking.nightlyRate, booking.getNights(), booking.guests,
                booking.breakfastIncluded, booking.promoPercent);
    }

    @Nullable
    public static String validate(String guestName, String guestEmail, RoomOffer offer,
                                  StayRequest stay, LocalDate today) {
        if (guestName.trim().isEmpty()) {
            return "Enter the guest's name";
        }
        if (!guestEmail.contains("@") || guestEmail.trim().length() < 5) {
            return "Enter a valid email address";
        }
        if (stay.checkIn.isBefore(today)) {
            return "Check-in can't be in the past";
        }
        if (!stay.checkOut.isAfter(stay.checkIn)) {
            return "Check-out must be after check-in";
        }
        if (stay.guests < 1) {
            return "A booking needs at least one guest";
        }
        if (stay.guests > offer.type.maxGuests) {
            return offer.type.label + " rooms sleep up to " + offer.type.maxGuests + " guests";
        }
        return null;
    }

    public static TripStage stage(Booking booking, LocalDate today) {
        if (booking.status == BookingStatus.CANCELLED) {
            return TripStage.CANCELLED;
        }
        if (!booking.checkOut.isAfter(today)) {
            return TripStage.PAST;
        }
        return TripStage.UPCOMING;
    }

    public static boolean canCancel(Booking booking, LocalDate today) {
        return booking.status == BookingStatus.CONFIRMED && booking.checkIn.isAfter(today);
    }

    public static List<Booking> trips(List<Booking> bookings, TripStage stage, LocalDate today) {
        List<Booking> matching = new ArrayList<>();
        for (Booking booking : bookings) {
            if (stage(booking, today) == stage) {
                matching.add(booking);
            }
        }
        if (stage == TripStage.UPCOMING) {
            Collections.sort(matching, Comparator.comparing(b -> b.checkIn));
        } else {
            Collections.sort(matching, (a, b) -> b.checkIn.compareTo(a.checkIn));
        }
        return matching;
    }
}
