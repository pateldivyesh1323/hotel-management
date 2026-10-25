package com.example.hotel_booking_app.ui.auth;

import com.example.hotel_booking_app.data.HotelRepository;

public interface AuthHost {

    HotelRepository repository();

    void goToLogin();

    void goToSignup();

    void onAuthenticated();
}
