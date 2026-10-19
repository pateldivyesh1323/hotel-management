package com.example.hotel_booking_app.data;

public enum RefundStatus {
    NONE("No refund"),
    PENDING("Refund pending"),
    REFUNDED("Refunded");

    public final String label;

    RefundStatus(String label) {
        this.label = label;
    }
}
