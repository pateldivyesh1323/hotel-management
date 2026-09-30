package com.example.hotel_booking_app.data;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class Booking {
    public final String id;
    public final String hotelId;
    public final String roomId;
    public final String guestName;
    public final String guestPhone;
    public final String guestEmail;
    public final LocalDate checkIn;
    public final LocalDate checkOut;
    public final int guests;
    public final BookingStatus status;
    public final String specialRequests;
    public final LocalDate bookedOn;
    public final int nightlyRate;
    public final boolean breakfastIncluded;
    public final int promoPercent;
    public final boolean payAtHotel;

    public Booking(String id, String hotelId, String roomId, String guestName, String guestPhone,
                   String guestEmail, LocalDate checkIn, LocalDate checkOut, int guests,
                   BookingStatus status, String specialRequests, LocalDate bookedOn, int nightlyRate,
                   boolean breakfastIncluded, int promoPercent, boolean payAtHotel) {
        this.id = id;
        this.hotelId = hotelId;
        this.roomId = roomId;
        this.guestName = guestName;
        this.guestPhone = guestPhone;
        this.guestEmail = guestEmail;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guests = guests;
        this.status = status;
        this.specialRequests = specialRequests;
        this.bookedOn = bookedOn;
        this.nightlyRate = nightlyRate;
        this.breakfastIncluded = breakfastIncluded;
        this.promoPercent = promoPercent;
        this.payAtHotel = payAtHotel;
    }

    public int getNights() {
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public boolean holdsRoom() {
        return status == BookingStatus.CONFIRMED;
    }

    public Booking withStatus(BookingStatus newStatus) {
        return new Booking(id, hotelId, roomId, guestName, guestPhone, guestEmail, checkIn, checkOut,
                guests, newStatus, specialRequests, bookedOn, nightlyRate, breakfastIncluded,
                promoPercent, payAtHotel);
    }
}
