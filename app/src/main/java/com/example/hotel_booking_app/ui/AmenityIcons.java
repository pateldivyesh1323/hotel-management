package com.example.hotel_booking_app.ui;

import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.Amenity;

public final class AmenityIcons {

    private AmenityIcons() {
    }

    public static int icon(Amenity amenity) {
        switch (amenity) {
            case WIFI:
                return R.drawable.ic_wifi;
            case POOL:
                return R.drawable.ic_pool;
            case BREAKFAST:
                return R.drawable.ic_free_breakfast;
            case PARKING:
                return R.drawable.ic_directions_car;
            case SPA:
                return R.drawable.ic_spa;
            case GYM:
                return R.drawable.ic_fitness_center;
            case AIRPORT_SHUTTLE:
                return R.drawable.ic_local_airport;
            case PET_FRIENDLY:
            default:
                return R.drawable.ic_pets;
        }
    }
}
