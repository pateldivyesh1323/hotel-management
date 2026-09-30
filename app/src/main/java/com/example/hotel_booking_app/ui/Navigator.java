package com.example.hotel_booking_app.ui;

import com.example.hotel_booking_app.data.HotelRepository;
import com.example.hotel_booking_app.data.StayRequest;

public interface Navigator {

    HotelRepository repository();

    StayRequest stay();

    void updateStay(StayRequest stay);

    String query();

    void updateQuery(String query);

    void openResults();

    void openHotel(String hotelId);

    void openCheckout(String offerId);

    void openTrip(String bookingId);

    void openTripAfterBooking(String bookingId);

    void showMessage(String message);
}
