package com.example.hotel_booking_app.data;

public enum BookingStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled");

    public final String label;

    BookingStatus(String label) {
        this.label = label;
    }
}
