package com.example.hotel_booking_app.data;

import androidx.annotation.Nullable;

public final class BookingOutcome {
    public final boolean confirmed;
    @Nullable
    public final String bookingId;
    @Nullable
    public final String message;

    private BookingOutcome(boolean confirmed, @Nullable String bookingId, @Nullable String message) {
        this.confirmed = confirmed;
        this.bookingId = bookingId;
        this.message = message;
    }

    public static BookingOutcome confirmed(String bookingId) {
        return new BookingOutcome(true, bookingId, null);
    }

    public static BookingOutcome rejected(String message) {
        return new BookingOutcome(false, null, message);
    }
}
