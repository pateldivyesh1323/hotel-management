package com.example.hotel_booking_app.data;

public final class PriceBreakdown {
    public final int nights;
    public final int nightlyRate;
    public final int subtotal;
    public final int breakfast;
    public final int discount;
    public final int taxes;
    public final int total;

    public PriceBreakdown(int nights, int nightlyRate, int subtotal, int breakfast,
                          int discount, int taxes, int total) {
        this.nights = nights;
        this.nightlyRate = nightlyRate;
        this.subtotal = subtotal;
        this.breakfast = breakfast;
        this.discount = discount;
        this.taxes = taxes;
        this.total = total;
    }
}
