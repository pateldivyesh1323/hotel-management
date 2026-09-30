package com.example.hotel_booking_app.data;

public final class RoomOffer {
    public final String id;
    public final String hotelId;
    public final RoomType type;
    public final int nightlyRate;
    public final int units;
    public final String beds;
    public final int sizeSqm;

    public RoomOffer(String id, String hotelId, RoomType type, int nightlyRate,
                     int units, String beds, int sizeSqm) {
        this.id = id;
        this.hotelId = hotelId;
        this.type = type;
        this.nightlyRate = nightlyRate;
        this.units = units;
        this.beds = beds;
        this.sizeSqm = sizeSqm;
    }
}
