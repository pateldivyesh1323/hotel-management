package com.example.hotel_booking_app.data;

public enum RoomType {
    STANDARD("Standard", 2),
    DELUXE("Deluxe", 3),
    SUITE("Suite", 4);

    public final String label;
    public final int maxGuests;

    RoomType(String label, int maxGuests) {
        this.label = label;
        this.maxGuests = maxGuests;
    }
}
