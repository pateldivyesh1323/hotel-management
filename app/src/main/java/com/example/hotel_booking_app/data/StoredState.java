package com.example.hotel_booking_app.data;

import java.util.List;
import java.util.Set;

public final class StoredState {
    public final List<Hotel> hotels;
    public final List<RoomOffer> offers;
    public final List<Booking> bookings;
    public final List<Review> reviews;
    public final Set<String> favorites;
    public final GuestProfile profile;

    public StoredState(List<Hotel> hotels, List<RoomOffer> offers, List<Booking> bookings,
                       List<Review> reviews, Set<String> favorites, GuestProfile profile) {
        this.hotels = hotels;
        this.offers = offers;
        this.bookings = bookings;
        this.reviews = reviews;
        this.favorites = favorites;
        this.profile = profile;
    }
}
