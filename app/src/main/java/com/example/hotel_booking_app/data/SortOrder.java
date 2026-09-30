package com.example.hotel_booking_app.data;

public enum SortOrder {
    RECOMMENDED("Recommended"),
    PRICE_LOW("Price: low to high"),
    PRICE_HIGH("Price: high to low"),
    RATING("Guest rating");

    public final String label;

    SortOrder(String label) {
        this.label = label;
    }
}
