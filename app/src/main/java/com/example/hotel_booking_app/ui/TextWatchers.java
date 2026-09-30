package com.example.hotel_booking_app.ui;

import android.text.Editable;
import android.text.TextWatcher;

public final class TextWatchers {

    public interface OnChanged {
        void onChanged(String text);
    }

    private TextWatchers() {
    }

    public static TextWatcher onChanged(OnChanged callback) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                callback.onChanged(s.toString());
            }
        };
    }
}
