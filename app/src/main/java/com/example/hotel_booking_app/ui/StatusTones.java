package com.example.hotel_booking_app.ui;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.TripStage;

public final class StatusTones {

    public static final class Tone {
        public final int container;
        public final int content;

        Tone(int container, int content) {
            this.container = container;
            this.content = content;
        }
    }

    private StatusTones() {
    }

    public static Tone forStage(Context context, TripStage stage) {
        switch (stage) {
            case UPCOMING:
                return resolve(context, R.color.status_green_container, R.color.status_green);
            case CANCELLED:
                return resolve(context, R.color.status_red_container, R.color.status_red);
            case PAST:
            default:
                return resolve(context, R.color.status_grey_container, R.color.status_grey);
        }
    }

    private static Tone resolve(Context context, int containerRes, int contentRes) {
        return new Tone(
                ContextCompat.getColor(context, containerRes),
                ContextCompat.getColor(context, contentRes));
    }
}
