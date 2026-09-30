package com.example.hotel_booking_app;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.hotel_booking_app.data.HotelRepository;
import com.example.hotel_booking_app.data.HotelStorage;
import com.example.hotel_booking_app.data.ThemeMode;

import java.time.LocalDate;

public class HotelApplication extends Application {

    private HotelRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        repository = new HotelRepository(new HotelStorage(this), LocalDate::now);
        applyThemeMode(repository.getThemeMode());
    }

    public HotelRepository repository() {
        return repository;
    }

    public static void applyThemeMode(ThemeMode mode) {
        switch (mode) {
            case LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}
