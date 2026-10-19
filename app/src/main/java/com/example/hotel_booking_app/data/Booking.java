package com.example.hotel_booking_app.data;

import androidx.annotation.Nullable;

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
    public final int rooms;
    public final BookingStatus status;
    public final String specialRequests;
    public final LocalDate bookedOn;
    public final int nightlyRate;
    public final boolean breakfastIncluded;
    public final int promoPercent;
    public final boolean payAtHotel;
    @Nullable
    public final LocalDate cancelledOn;
    public final String cancellationReason;
    public final RefundStatus refundStatus;

    public Booking(String id, String hotelId, String roomId, String guestName, String guestPhone,
                   String guestEmail, LocalDate checkIn, LocalDate checkOut, int guests, int rooms,
                   BookingStatus status, String specialRequests, LocalDate bookedOn, int nightlyRate,
                   boolean breakfastIncluded, int promoPercent, boolean payAtHotel,
                   @Nullable LocalDate cancelledOn, String cancellationReason, RefundStatus refundStatus) {
        this.id = id;
        this.hotelId = hotelId;
        this.roomId = roomId;
        this.guestName = guestName;
        this.guestPhone = guestPhone;
        this.guestEmail = guestEmail;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guests = guests;
        this.rooms = rooms;
        this.status = status;
        this.specialRequests = specialRequests;
        this.bookedOn = bookedOn;
        this.nightlyRate = nightlyRate;
        this.breakfastIncluded = breakfastIncluded;
        this.promoPercent = promoPercent;
        this.payAtHotel = payAtHotel;
        this.cancelledOn = cancelledOn;
        this.cancellationReason = cancellationReason;
        this.refundStatus = refundStatus;
    }

    public int getNights() {
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public boolean holdsRoom() {
        return status == BookingStatus.CONFIRMED || status == BookingStatus.PENDING;
    }

    public Booking withStatus(BookingStatus newStatus) {
        return new Booking(id, hotelId, roomId, guestName, guestPhone, guestEmail, checkIn, checkOut,
                guests, rooms, newStatus, specialRequests, bookedOn, nightlyRate, breakfastIncluded,
                promoPercent, payAtHotel, cancelledOn, cancellationReason, refundStatus);
    }

    public Booking withCancellation(BookingStatus newStatus, LocalDate newCancelledOn,
                                    String reason, RefundStatus newRefundStatus) {
        return new Booking(id, hotelId, roomId, guestName, guestPhone, guestEmail, checkIn, checkOut,
                guests, rooms, newStatus, specialRequests, bookedOn, nightlyRate, breakfastIncluded,
                promoPercent, payAtHotel, newCancelledOn, reason, newRefundStatus);
    }

    public Booking withRefundStatus(RefundStatus newRefundStatus) {
        return new Booking(id, hotelId, roomId, guestName, guestPhone, guestEmail, checkIn, checkOut,
                guests, rooms, status, specialRequests, bookedOn, nightlyRate, breakfastIncluded,
                promoPercent, payAtHotel, cancelledOn, cancellationReason, newRefundStatus);
    }
}
