package com.example.hotel_booking_app.data;

public final class GuestAccount {
    public final String name;
    public final String email;
    public final String phone;
    public final int bookings;

    public GuestAccount(String name, String email, String phone, int bookings) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.bookings = bookings;
    }
}
