package com.example.hotel_booking_app.data;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class StayRequest {
    public final LocalDate checkIn;
    public final LocalDate checkOut;
    public final int guests;

    public StayRequest(LocalDate checkIn, LocalDate checkOut, int guests) {
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guests = guests;
    }

    public int getNights() {
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public StayRequest withGuests(int newGuests) {
        return new StayRequest(checkIn, checkOut, newGuests);
    }

    public StayRequest withCheckIn(LocalDate newCheckIn) {
        return new StayRequest(newCheckIn, checkOut, guests);
    }

    public StayRequest withCheckOut(LocalDate newCheckOut) {
        return new StayRequest(checkIn, newCheckOut, guests);
    }
}
