package com.example.hotel_booking_app.data;

public final class HotelResult {
    public final Hotel hotel;
    public final int fromRate;

    public HotelResult(Hotel hotel, int fromRate) {
        this.hotel = hotel;
        this.fromRate = fromRate;
    }
}
