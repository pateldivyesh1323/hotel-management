package com.example.hotel_booking_app.data;

public enum BookingStatus {
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled");

    public final String label;

    BookingStatus(String label) {
        this.label = label;
    }
}
