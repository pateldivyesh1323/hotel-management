package com.example.hotel_booking_app.data

import java.time.LocalDate

object BookingRules {

    const val TAX_PERCENT = 12
    const val BREAKFAST_RATE = 12

    private val promoCodes = mapOf("WELCOME10" to 10, "SUMMER15" to 15, "STAYLONGER20" to 20)

    val promoCodeHints: String get() = promoCodes.keys.joinToString(", ")

    fun promoPercent(code: String): Int? = promoCodes[code.trim().uppercase()]

    fun overlaps(
        firstIn: LocalDate,
        firstOut: LocalDate,
        secondIn: LocalDate,
        secondOut: LocalDate
    ): Boolean = firstIn < secondOut && secondIn < firstOut

    fun unitsLeft(
        offer: RoomOffer,
        bookings: List<Booking>,
        checkIn: LocalDate,
        checkOut: LocalDate
    ): Int {
        val taken = bookings.count {
            it.roomId == offer.id &&
                it.holdsRoom &&
                overlaps(it.checkIn, it.checkOut, checkIn, checkOut)
        }
        return (offer.units - taken).coerceAtLeast(0)
    }

    fun availableOffers(
        offers: List<RoomOffer>,
        bookings: List<Booking>,
        stay: StayRequest
    ): List<RoomOffer> = offers
        .filter {
            it.type.maxGuests >= stay.guests &&
                unitsLeft(it, bookings, stay.checkIn, stay.checkOut) > 0
        }
        .sortedBy { it.nightlyRate }

    fun matchesQuery(hotel: Hotel, query: String): Boolean {
        val needle = query.trim().lowercase()
        return needle.isEmpty() ||
            hotel.name.lowercase().contains(needle) ||
            hotel.city.lowercase().contains(needle) ||
            hotel.country.lowercase().contains(needle)
    }

    fun matchesFilters(hotel: Hotel, filters: SearchFilters): Boolean =
        hotel.rating >= filters.minRating && hotel.amenities.containsAll(filters.amenities)

    fun search(
        hotels: List<Hotel>,
        offers: List<RoomOffer>,
        bookings: List<Booking>,
        query: String,
        stay: StayRequest,
        filters: SearchFilters,
        sort: SortOrder
    ): List<HotelResult> {
        val results = hotels
            .filter { matchesQuery(it, query) && matchesFilters(it, filters) }
            .mapNotNull { hotel ->
                val open = availableOffers(offers.filter { it.hotelId == hotel.id }, bookings, stay)
                open.minOfOrNull { it.nightlyRate }
                    ?.takeIf { filters.maxNightlyRate == null || it <= filters.maxNightlyRate }
                    ?.let { HotelResult(hotel, it) }
            }
        return when (sort) {
            SortOrder.RECOMMENDED -> results.sortedByDescending { it.hotel.rating * it.hotel.reviewCount }
            SortOrder.PRICE_LOW -> results.sortedBy { it.fromRate }
            SortOrder.PRICE_HIGH -> results.sortedByDescending { it.fromRate }
            SortOrder.RATING -> results.sortedByDescending { it.hotel.rating }
        }
    }

    fun price(
        nightlyRate: Int,
        nights: Int,
        guests: Int,
        breakfastIncluded: Boolean,
        promoPercent: Int
    ): PriceBreakdown {
        val subtotal = nightlyRate * nights
        val breakfast = if (breakfastIncluded) BREAKFAST_RATE * guests * nights else 0
        val discount = subtotal * promoPercent / 100
        val taxable = subtotal + breakfast - discount
        val taxes = taxable * TAX_PERCENT / 100
        return PriceBreakdown(nights, nightlyRate, subtotal, breakfast, discount, taxes, taxable + taxes)
    }

    fun price(booking: Booking): PriceBreakdown = price(
        booking.nightlyRate,
        booking.nights,
        booking.guests,
        booking.breakfastIncluded,
        booking.promoPercent
    )

    fun validate(
        guestName: String,
        guestEmail: String,
        offer: RoomOffer,
        stay: StayRequest,
        today: LocalDate
    ): String? = when {
        guestName.isBlank() -> "Enter the guest's name"
        !guestEmail.contains('@') || guestEmail.trim().length < 5 -> "Enter a valid email address"
        stay.checkIn.isBefore(today) -> "Check-in can't be in the past"
        !stay.checkOut.isAfter(stay.checkIn) -> "Check-out must be after check-in"
        stay.guests < 1 -> "A booking needs at least one guest"
        stay.guests > offer.type.maxGuests ->
            "${offer.type.label} rooms sleep up to ${offer.type.maxGuests} guests"
        else -> null
    }

    fun stage(booking: Booking, today: LocalDate): TripStage = when {
        booking.status == BookingStatus.CANCELLED -> TripStage.CANCELLED
        !booking.checkOut.isAfter(today) -> TripStage.PAST
        else -> TripStage.UPCOMING
    }

    fun canCancel(booking: Booking, today: LocalDate): Boolean =
        booking.status == BookingStatus.CONFIRMED && booking.checkIn.isAfter(today)

    fun trips(bookings: List<Booking>, stage: TripStage, today: LocalDate): List<Booking> {
        val matching = bookings.filter { stage(it, today) == stage }
        return if (stage == TripStage.UPCOMING) {
            matching.sortedBy { it.checkIn }
        } else {
            matching.sortedByDescending { it.checkIn }
        }
    }
}
