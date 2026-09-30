package com.example.hotel_booking_app.data;

public enum ThemeMode {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark");

    public final String label;

    ThemeMode(String label) {
        this.label = label;
    }
}
