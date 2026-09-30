package com.example.hotel_booking_app.ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Format {

    private static final DateTimeFormatter DAY_MONTH =
            DateTimeFormatter.ofPattern("d MMM", Locale.getDefault());
    private static final DateTimeFormatter DAY_MONTH_YEAR =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault());

    private Format() {
    }

    public static String shortDate(LocalDate date) {
        return date.format(DAY_MONTH);
    }

    public static String fullDate(LocalDate date) {
        return date.format(DAY_MONTH_YEAR);
    }

    public static String stayRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.getYear() == checkOut.getYear()) {
            return shortDate(checkIn) + " – " + shortDate(checkOut);
        }
        return fullDate(checkIn) + " – " + fullDate(checkOut);
    }

    public static String money(int amount) {
        return "₹" + String.format(Locale.getDefault(), "%,d", amount);
    }

    public static String nightsLabel(int nights) {
        return nights == 1 ? "1 night" : nights + " nights";
    }

    public static String guestsLabel(int guests) {
        return guests == 1 ? "1 guest" : guests + " guests";
    }

    public static String ratingLabel(float rating) {
        return String.format(Locale.getDefault(), "%.1f", rating);
    }

    public static String stars(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append('★');
        }
        return builder.toString();
    }
}
