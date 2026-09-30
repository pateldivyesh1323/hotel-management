package com.example.hotel_booking_app.data;

import java.time.LocalDate;

public final class Review {
    public final String id;
    public final String hotelId;
    public final String bookingId;
    public final String guestName;
    public final float rating;
    public final String comment;
    public final LocalDate date;

    public Review(String id, String hotelId, String bookingId, String guestName,
                  float rating, String comment, LocalDate date) {
        this.id = id;
        this.hotelId = hotelId;
        this.bookingId = bookingId;
        this.guestName = guestName;
        this.rating = rating;
        this.comment = comment;
        this.date = date;
    }
}
