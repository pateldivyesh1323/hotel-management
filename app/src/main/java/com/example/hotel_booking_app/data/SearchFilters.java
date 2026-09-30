package com.example.hotel_booking_app.data;

import androidx.annotation.Nullable;

import java.util.Set;

public final class SearchFilters {
    public final float minRating;
    @Nullable
    public final Integer maxNightlyRate;
    public final Set<Amenity> amenities;

    public SearchFilters(float minRating, @Nullable Integer maxNightlyRate, Set<Amenity> amenities) {
        this.minRating = minRating;
        this.maxNightlyRate = maxNightlyRate;
        this.amenities = amenities;
    }

    public boolean isActive() {
        return minRating > 0f || maxNightlyRate != null || !amenities.isEmpty();
    }
}
