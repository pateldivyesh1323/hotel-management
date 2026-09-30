package com.example.hotel_booking_app.ui;

import com.example.hotel_booking_app.R;
import com.example.hotel_booking_app.data.RoomOffer;
import com.example.hotel_booking_app.data.RoomType;

public final class Photos {

    private static final int[] HOTEL_PHOTOS = {
            R.drawable.hotel_lobby,
            R.drawable.room_suite_1,
            R.drawable.room_deluxe_2,
            R.drawable.room_suite_2,
            R.drawable.room_deluxe_1,
            R.drawable.room_standard_2
    };

    private static final int[] GUEST_AVATARS = {
            R.drawable.guest_avatar_1,
            R.drawable.guest_avatar_2,
            R.drawable.guest_avatar_3,
            R.drawable.guest_avatar_4,
            R.drawable.guest_avatar_5,
            R.drawable.guest_avatar_6,
            R.drawable.guest_avatar_7,
            R.drawable.guest_avatar_8
    };

    private Photos() {
    }

    public static int hotelPhoto(int photoIndex) {
        return HOTEL_PHOTOS[Math.floorMod(photoIndex, HOTEL_PHOTOS.length)];
    }

    public static int avatar(String name) {
        return GUEST_AVATARS[Math.floorMod(charSum(name), GUEST_AVATARS.length)];
    }

    public static int roomPhoto(RoomOffer offer) {
        int variant = Math.floorMod(charSum(offer.id), 2);
        switch (offer.type) {
            case STANDARD:
                return variant == 0 ? R.drawable.room_standard_1 : R.drawable.room_standard_2;
            case DELUXE:
                return variant == 0 ? R.drawable.room_deluxe_1 : R.drawable.room_deluxe_2;
            case SUITE:
            default:
                return variant == 0 ? R.drawable.room_suite_1 : R.drawable.room_suite_2;
        }
    }

    private static int charSum(String text) {
        int sum = 0;
        for (int i = 0; i < text.length(); i++) {
            sum += text.charAt(i);
        }
        return sum;
    }
}
