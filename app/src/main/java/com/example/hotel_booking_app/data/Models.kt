package com.example.hotel_booking_app.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class RoomType(val label: String, val maxGuests: Int) {
    STANDARD("Standard", 2),
    DELUXE("Deluxe", 3),
    SUITE("Suite", 4)
}

enum class Amenity(val label: String) {
    WIFI("Free Wi-Fi"),
    POOL("Pool"),
    BREAKFAST("Breakfast"),
    PARKING("Parking"),
    SPA("Spa"),
    GYM("Gym"),
    AIRPORT_SHUTTLE("Airport shuttle"),
    PET_FRIENDLY("Pet friendly")
}

data class Hotel(
    val id: String,
    val name: String,
    val city: String,
    val country: String,
    val address: String,
    val description: String,
    val stars: Int,
    val rating: Float,
    val reviewCount: Int,
    val amenities: List<Amenity>,
    val highlights: List<String>,
    val photoIndex: Int
) {
    val location: String get() = "$city, $country"
}

data class RoomOffer(
    val id: String,
    val hotelId: String,
    val type: RoomType,
    val nightlyRate: Int,
    val units: Int,
    val beds: String,
    val sizeSqm: Int
)

data class Review(
    val id: String,
    val hotelId: String,
    val bookingId: String,
    val guestName: String,
    val rating: Float,
    val comment: String,
    val date: LocalDate
)

enum class BookingStatus(val label: String) {
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled")
}

enum class TripStage(val label: String) {
    UPCOMING("Upcoming"),
    PAST("Past"),
    CANCELLED("Cancelled")
}

data class Booking(
    val id: String,
    val hotelId: String,
    val roomId: String,
    val guestName: String,
    val guestPhone: String,
    val guestEmail: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val guests: Int,
    val status: BookingStatus,
    val specialRequests: String,
    val bookedOn: LocalDate,
    val nightlyRate: Int,
    val breakfastIncluded: Boolean,
    val promoPercent: Int,
    val payAtHotel: Boolean
) {
    val nights: Int
        get() = ChronoUnit.DAYS.between(checkIn, checkOut).toInt()

    val holdsRoom: Boolean
        get() = status == BookingStatus.CONFIRMED
}

data class GuestProfile(
    val name: String,
    val email: String,
    val phone: String
)

data class StayRequest(
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val guests: Int
) {
    val nights: Int
        get() = ChronoUnit.DAYS.between(checkIn, checkOut).toInt()
}

data class HotelResult(
    val hotel: Hotel,
    val fromRate: Int
)

enum class SortOrder(val label: String) {
    RECOMMENDED("Recommended"),
    PRICE_LOW("Price: low to high"),
    PRICE_HIGH("Price: high to low"),
    RATING("Guest rating")
}

data class PriceBreakdown(
    val nights: Int,
    val nightlyRate: Int,
    val subtotal: Int,
    val breakfast: Int,
    val discount: Int,
    val taxes: Int,
    val total: Int
)

data class SearchFilters(
    val minRating: Float,
    val maxNightlyRate: Int?,
    val amenities: Set<Amenity>
) {
    val active: Boolean
        get() = minRating > 0f || maxNightlyRate != null || amenities.isNotEmpty()
}

sealed interface BookingOutcome {
    data class Confirmed(val bookingId: String) : BookingOutcome
    data class Rejected(val message: String) : BookingOutcome
}

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}
