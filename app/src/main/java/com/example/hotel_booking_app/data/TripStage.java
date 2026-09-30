package com.example.hotel_booking_app.data;

public enum TripStage {
    UPCOMING("Upcoming"),
    PAST("Past"),
    CANCELLED("Cancelled");

    public final String label;

    TripStage(String label) {
        this.label = label;
    }
}
