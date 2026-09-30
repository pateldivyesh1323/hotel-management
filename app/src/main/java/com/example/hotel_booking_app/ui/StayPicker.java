package com.example.hotel_booking_app.ui;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.StayRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.function.Consumer;

public final class StayPicker {

    public interface OnChange {
        void onChange(StayRequest stay);
    }

    private static final int MAX_GUESTS = 8;

    private StayPicker() {
    }

    public static void bind(View root, FragmentManager fm, StayRequest stay, LocalDate today, OnChange callback) {
        TextView checkInValue = root.findViewById(R.id.stay_check_in_value);
        TextView checkOutValue = root.findViewById(R.id.stay_check_out_value);
        TextView nights = root.findViewById(R.id.stay_nights);
        TextView guests = root.findViewById(R.id.stay_guests);
        MaterialCardView checkInCard = root.findViewById(R.id.stay_check_in);
        MaterialCardView checkOutCard = root.findViewById(R.id.stay_check_out);
        MaterialButton minus = root.findViewById(R.id.guests_minus);
        MaterialButton plus = root.findViewById(R.id.guests_plus);

        checkInValue.setText(Format.fullDate(stay.checkIn));
        checkOutValue.setText(Format.fullDate(stay.checkOut));
        nights.setText(Format.nightsLabel(stay.getNights()));
        guests.setText(Format.guestsLabel(stay.guests));

        checkInCard.setOnClickListener(v -> pickDate(fm, stay.checkIn, today, date ->
                callback.onChange(moveCheckIn(stay, date))));
        checkOutCard.setOnClickListener(v -> pickDate(fm, stay.checkOut, stay.checkIn.plusDays(1), date ->
                callback.onChange(stay.withCheckOut(date))));

        minus.setEnabled(stay.guests > 1);
        plus.setEnabled(stay.guests < MAX_GUESTS);
        minus.setOnClickListener(v -> callback.onChange(stay.withGuests(stay.guests - 1)));
        plus.setOnClickListener(v -> callback.onChange(stay.withGuests(stay.guests + 1)));
    }

    public static StayRequest moveCheckIn(StayRequest stay, LocalDate checkIn) {
        LocalDate checkOut = stay.checkOut.isAfter(checkIn) ? stay.checkOut : checkIn.plusDays(1);
        return new StayRequest(checkIn, checkOut, stay.guests);
    }

    private static void pickDate(FragmentManager fm, LocalDate selected, LocalDate earliest,
                                 Consumer<LocalDate> onPicked) {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(utcMillis(earliest)))
                .build();
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.<Long>datePicker()
                .setSelection(utcMillis(selected))
                .setCalendarConstraints(constraints)
                .build();
        picker.addOnPositiveButtonClickListener(millis -> onPicked.accept(localDate(millis)));
        picker.show(fm, "stay_date");
    }

    private static long utcMillis(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    private static LocalDate localDate(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate();
    }
}
