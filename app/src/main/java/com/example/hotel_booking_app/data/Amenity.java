package com.example.hotel_booking_app.data;

public enum Amenity {
    WIFI("Free Wi-Fi"),
    POOL("Pool"),
    BREAKFAST("Breakfast"),
    PARKING("Parking"),
    SPA("Spa"),
    GYM("Gym"),
    AIRPORT_SHUTTLE("Airport shuttle"),
    PET_FRIENDLY("Pet friendly");

    public final String label;

    Amenity(String label) {
        this.label = label;
    }
}
