package com.example.hotel_booking_app.data;

import java.util.List;

public final class Hotel {
    public final String id;
    public final String name;
    public final String city;
    public final String country;
    public final String address;
    public final String description;
    public final int stars;
    public final float rating;
    public final int reviewCount;
    public final List<Amenity> amenities;
    public final List<String> highlights;
    public final int photoIndex;

    public Hotel(String id, String name, String city, String country, String address,
                 String description, int stars, float rating, int reviewCount,
                 List<Amenity> amenities, List<String> highlights, int photoIndex) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.country = country;
        this.address = address;
        this.description = description;
        this.stars = stars;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.amenities = amenities;
        this.highlights = highlights;
        this.photoIndex = photoIndex;
    }

    public String getLocation() {
        return city + ", " + country;
    }
}
