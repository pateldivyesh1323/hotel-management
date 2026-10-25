package com.example.hotel_booking_app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class HotelStorage {

    private static final String TAG = "HotelStorage";
    private static final String KEY_STATE = "state_json";
    private static final String KEY_THEME = "theme_mode";

    private final SharedPreferences prefs;

    public HotelStorage(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences("booking_state_v2", Context.MODE_PRIVATE);
    }

    public void save(StoredState state) {
        try {
            JSONObject root = new JSONObject();
            JSONArray hotels = new JSONArray();
            for (Hotel hotel : state.hotels) {
                hotels.put(hotelToJson(hotel));
            }
            JSONArray offers = new JSONArray();
            for (RoomOffer offer : state.offers) {
                offers.put(offerToJson(offer));
            }
            JSONArray bookings = new JSONArray();
            for (Booking booking : state.bookings) {
                bookings.put(bookingToJson(booking));
            }
            JSONArray reviews = new JSONArray();
            for (Review review : state.reviews) {
                reviews.put(reviewToJson(review));
            }
            root.put("hotels", hotels);
            root.put("offers", offers);
            root.put("bookings", bookings);
            root.put("reviews", reviews);
            root.put("favorites", new JSONArray(state.favorites));
            root.put("profile", profileToJson(state.profile));
            JSONArray accounts = new JSONArray();
            for (Account account : state.accounts) {
                accounts.put(accountToJson(account));
            }
            root.put("accounts", accounts);
            root.put("currentUserEmail", state.currentUserEmail);
            prefs.edit().putString(KEY_STATE, root.toString()).apply();
        } catch (JSONException e) {
            throw new IllegalStateException("Failed to serialize booking state", e);
        }
    }

    public ThemeMode loadThemeMode() {
        String name = prefs.getString(KEY_THEME, null);
        if (name != null) {
            for (ThemeMode mode : ThemeMode.values()) {
                if (mode.name().equals(name)) {
                    return mode;
                }
            }
        }
        return ThemeMode.SYSTEM;
    }

    public void saveThemeMode(ThemeMode mode) {
        prefs.edit().putString(KEY_THEME, mode.name()).apply();
    }

    @Nullable
    public StoredState load() {
        String raw = prefs.getString(KEY_STATE, null);
        if (raw == null) {
            return null;
        }
        try {
            JSONObject root = new JSONObject(raw);
            List<Hotel> hotels = null;
            JSONArray hotelsJson = root.optJSONArray("hotels");
            if (hotelsJson != null) {
                hotels = new ArrayList<>();
                for (int i = 0; i < hotelsJson.length(); i++) {
                    hotels.add(hotelFromJson(hotelsJson.getJSONObject(i)));
                }
            }
            List<RoomOffer> offers = null;
            JSONArray offersJson = root.optJSONArray("offers");
            if (offersJson != null) {
                offers = new ArrayList<>();
                for (int i = 0; i < offersJson.length(); i++) {
                    offers.add(offerFromJson(offersJson.getJSONObject(i)));
                }
            }
            JSONArray bookingsJson = root.getJSONArray("bookings");
            JSONArray reviewsJson = root.getJSONArray("reviews");
            JSONArray favoritesJson = root.getJSONArray("favorites");
            List<Booking> bookings = new ArrayList<>();
            for (int i = 0; i < bookingsJson.length(); i++) {
                bookings.add(bookingFromJson(bookingsJson.getJSONObject(i)));
            }
            List<Review> reviews = new ArrayList<>();
            for (int i = 0; i < reviewsJson.length(); i++) {
                reviews.add(reviewFromJson(reviewsJson.getJSONObject(i)));
            }
            Set<String> favorites = new LinkedHashSet<>();
            for (int i = 0; i < favoritesJson.length(); i++) {
                favorites.add(favoritesJson.getString(i));
            }
            List<Account> accounts = null;
            JSONArray accountsJson = root.optJSONArray("accounts");
            if (accountsJson != null) {
                accounts = new ArrayList<>();
                for (int i = 0; i < accountsJson.length(); i++) {
                    accounts.add(accountFromJson(accountsJson.getJSONObject(i)));
                }
            }
            return new StoredState(hotels, offers, bookings, reviews, favorites,
                    profileFromJson(root.getJSONObject("profile")),
                    accounts, root.optString("currentUserEmail", ""));
        } catch (JSONException e) {
            Log.w(TAG, "Discarding unreadable saved state", e);
            prefs.edit().remove(KEY_STATE).apply();
            return null;
        }
    }

    private JSONObject hotelToJson(Hotel hotel) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", hotel.id);
        json.put("name", hotel.name);
        json.put("city", hotel.city);
        json.put("country", hotel.country);
        json.put("address", hotel.address);
        json.put("description", hotel.description);
        json.put("stars", hotel.stars);
        json.put("rating", (double) hotel.rating);
        json.put("reviewCount", hotel.reviewCount);
        JSONArray amenities = new JSONArray();
        for (Amenity amenity : hotel.amenities) {
            amenities.put(amenity.name());
        }
        json.put("amenities", amenities);
        json.put("highlights", new JSONArray(hotel.highlights));
        json.put("photoIndex", hotel.photoIndex);
        return json;
    }

    private Hotel hotelFromJson(JSONObject json) throws JSONException {
        JSONArray amenitiesJson = json.getJSONArray("amenities");
        List<Amenity> amenities = new ArrayList<>();
        for (int i = 0; i < amenitiesJson.length(); i++) {
            amenities.add(Amenity.valueOf(amenitiesJson.getString(i)));
        }
        JSONArray highlightsJson = json.getJSONArray("highlights");
        List<String> highlights = new ArrayList<>();
        for (int i = 0; i < highlightsJson.length(); i++) {
            highlights.add(highlightsJson.getString(i));
        }
        return new Hotel(
                json.getString("id"),
                json.getString("name"),
                json.getString("city"),
                json.getString("country"),
                json.getString("address"),
                json.getString("description"),
                json.getInt("stars"),
                (float) json.getDouble("rating"),
                json.getInt("reviewCount"),
                amenities,
                highlights,
                json.getInt("photoIndex"));
    }

    private JSONObject offerToJson(RoomOffer offer) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", offer.id);
        json.put("hotelId", offer.hotelId);
        json.put("type", offer.type.name());
        json.put("nightlyRate", offer.nightlyRate);
        json.put("units", offer.units);
        json.put("beds", offer.beds);
        json.put("sizeSqm", offer.sizeSqm);
        return json;
    }

    private RoomOffer offerFromJson(JSONObject json) throws JSONException {
        return new RoomOffer(
                json.getString("id"),
                json.getString("hotelId"),
                RoomType.valueOf(json.getString("type")),
                json.getInt("nightlyRate"),
                json.getInt("units"),
                json.getString("beds"),
                json.getInt("sizeSqm"));
    }

    private JSONObject bookingToJson(Booking booking) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", booking.id);
        json.put("hotelId", booking.hotelId);
        json.put("roomId", booking.roomId);
        json.put("guestName", booking.guestName);
        json.put("guestPhone", booking.guestPhone);
        json.put("guestEmail", booking.guestEmail);
        json.put("checkIn", booking.checkIn.toString());
        json.put("checkOut", booking.checkOut.toString());
        json.put("guests", booking.guests);
        json.put("rooms", booking.rooms);
        json.put("status", booking.status.name());
        json.put("specialRequests", booking.specialRequests);
        json.put("bookedOn", booking.bookedOn.toString());
        json.put("nightlyRate", booking.nightlyRate);
        json.put("breakfastIncluded", booking.breakfastIncluded);
        json.put("promoPercent", booking.promoPercent);
        json.put("payAtHotel", booking.payAtHotel);
        if (booking.cancelledOn != null) {
            json.put("cancelledOn", booking.cancelledOn.toString());
        }
        json.put("cancellationReason", booking.cancellationReason);
        json.put("refundStatus", booking.refundStatus.name());
        return json;
    }

    private Booking bookingFromJson(JSONObject json) throws JSONException {
        return new Booking(
                json.getString("id"),
                json.getString("hotelId"),
                json.getString("roomId"),
                json.getString("guestName"),
                json.getString("guestPhone"),
                json.getString("guestEmail"),
                LocalDate.parse(json.getString("checkIn")),
                LocalDate.parse(json.getString("checkOut")),
                json.getInt("guests"),
                json.optInt("rooms", 1),
                BookingStatus.valueOf(json.getString("status")),
                json.getString("specialRequests"),
                LocalDate.parse(json.getString("bookedOn")),
                json.getInt("nightlyRate"),
                json.getBoolean("breakfastIncluded"),
                json.getInt("promoPercent"),
                json.getBoolean("payAtHotel"),
                json.has("cancelledOn") ? LocalDate.parse(json.getString("cancelledOn")) : null,
                json.optString("cancellationReason", ""),
                RefundStatus.valueOf(json.optString("refundStatus", RefundStatus.NONE.name())));
    }

    private JSONObject reviewToJson(Review review) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", review.id);
        json.put("hotelId", review.hotelId);
        json.put("bookingId", review.bookingId);
        json.put("guestName", review.guestName);
        json.put("rating", (double) review.rating);
        json.put("comment", review.comment);
        json.put("date", review.date.toString());
        return json;
    }

    private Review reviewFromJson(JSONObject json) throws JSONException {
        return new Review(
                json.getString("id"),
                json.getString("hotelId"),
                json.getString("bookingId"),
                json.getString("guestName"),
                (float) json.getDouble("rating"),
                json.getString("comment"),
                LocalDate.parse(json.getString("date")));
    }

    private JSONObject profileToJson(GuestProfile profile) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", profile.name);
        json.put("email", profile.email);
        json.put("phone", profile.phone);
        return json;
    }

    private GuestProfile profileFromJson(JSONObject json) throws JSONException {
        return new GuestProfile(
                json.getString("name"),
                json.getString("email"),
                json.getString("phone"));
    }

    private JSONObject accountToJson(Account account) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("email", account.email);
        json.put("name", account.name);
        json.put("phone", account.phone);
        json.put("passwordHash", account.passwordHash);
        json.put("admin", account.admin);
        return json;
    }

    private Account accountFromJson(JSONObject json) throws JSONException {
        return new Account(
                json.getString("email"),
                json.getString("name"),
                json.getString("phone"),
                json.getString("passwordHash"),
                json.getBoolean("admin"));
    }
}
