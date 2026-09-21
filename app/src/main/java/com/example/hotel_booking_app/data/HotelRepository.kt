package com.example.hotel_booking_app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.time.LocalDate

class HotelRepository(
    private val storage: HotelStorage?,
    private val today: () -> LocalDate
) {
    private val catalog: List<Hotel> = SampleData.hotels()
    private val offers: List<RoomOffer> = SampleData.offers()
    private val _reviews: SnapshotStateList<Review> = mutableStateListOf()
    private val _bookings: SnapshotStateList<Booking> = mutableStateListOf()
    private val _favorites: SnapshotStateList<String> = mutableStateListOf()
    private var _profile by mutableStateOf(SampleData.profile)
    private var _themeMode by mutableStateOf(storage?.loadThemeMode() ?: ThemeMode.SYSTEM)

    val hotels: List<Hotel> get() = catalog
    val bookings: List<Booking> get() = _bookings
    val profile: GuestProfile get() = _profile
    val themeMode: ThemeMode get() = _themeMode

    init {
        val saved = storage?.load()
        if (saved != null) {
            _bookings.addAll(saved.bookings)
            _reviews.addAll(SampleData.reviews(today()) + saved.reviews)
            _favorites.addAll(saved.favorites)
            _profile = saved.profile
        } else {
            seed()
        }
    }

    fun hotel(id: String?): Hotel? = catalog.firstOrNull { it.id == id }

    fun offer(id: String?): RoomOffer? = offers.firstOrNull { it.id == id }

    fun offersFor(hotelId: String): List<RoomOffer> = offers.filter { it.hotelId == hotelId }

    fun reviewsFor(hotelId: String): List<Review> =
        _reviews.filter { it.hotelId == hotelId }.sortedByDescending { it.date }

    fun booking(id: String?): Booking? = _bookings.firstOrNull { it.id == id }

    fun currentDate(): LocalDate = today()

    fun cities(): List<String> = catalog.map { it.city }.distinct()

    fun search(
        query: String,
        stay: StayRequest,
        filters: SearchFilters,
        sort: SortOrder
    ): List<HotelResult> =
        BookingRules.search(catalog, offers, _bookings, query, stay, filters, sort)

    fun reviewFor(bookingId: String): Review? = _reviews.firstOrNull { it.bookingId == bookingId }

    fun availableOffers(hotelId: String, stay: StayRequest): List<RoomOffer> =
        BookingRules.availableOffers(offersFor(hotelId), _bookings, stay)

    fun unitsLeft(offer: RoomOffer, stay: StayRequest): Int =
        BookingRules.unitsLeft(offer, _bookings, stay.checkIn, stay.checkOut)

    fun trips(stage: TripStage): List<Booking> = BookingRules.trips(_bookings, stage, today())

    fun stage(booking: Booking): TripStage = BookingRules.stage(booking, today())

    fun canCancel(booking: Booking): Boolean = BookingRules.canCancel(booking, today())

    fun isFavorite(hotelId: String): Boolean = hotelId in _favorites

    fun favoriteHotels(): List<Hotel> = catalog.filter { it.id in _favorites }

    fun toggleFavorite(hotelId: String) {
        if (!_favorites.remove(hotelId)) _favorites.add(hotelId)
        persist()
    }

    fun book(
        offerId: String,
        guestName: String,
        guestPhone: String,
        guestEmail: String,
        stay: StayRequest,
        specialRequests: String,
        breakfastIncluded: Boolean,
        promoCode: String,
        payAtHotel: Boolean
    ): BookingOutcome {
        val offer = offer(offerId) ?: return BookingOutcome.Rejected("That room is no longer offered")
        BookingRules.validate(guestName, guestEmail, offer, stay, today())?.let {
            return BookingOutcome.Rejected(it)
        }
        if (unitsLeft(offer, stay) == 0) {
            return BookingOutcome.Rejected("${offer.type.label} rooms are sold out for those dates")
        }
        val promoPercent = if (promoCode.isBlank()) {
            0
        } else {
            BookingRules.promoPercent(promoCode)
                ?: return BookingOutcome.Rejected("Promo code ${promoCode.trim()} isn't valid")
        }
        val booking = Booking(
            id = newBookingId(),
            hotelId = offer.hotelId,
            roomId = offer.id,
            guestName = guestName.trim(),
            guestPhone = guestPhone.trim(),
            guestEmail = guestEmail.trim(),
            checkIn = stay.checkIn,
            checkOut = stay.checkOut,
            guests = stay.guests,
            status = BookingStatus.CONFIRMED,
            specialRequests = specialRequests.trim(),
            bookedOn = today(),
            nightlyRate = offer.nightlyRate,
            breakfastIncluded = breakfastIncluded,
            promoPercent = promoPercent,
            payAtHotel = payAtHotel
        )
        _bookings.add(booking)
        _profile = GuestProfile(booking.guestName, booking.guestEmail, booking.guestPhone)
        persist()
        return BookingOutcome.Confirmed(booking.id)
    }

    fun cancelBooking(bookingId: String): String? {
        val booking = booking(bookingId) ?: return "Booking not found"
        if (!canCancel(booking)) {
            return "Only a confirmed booking that hasn't started can be cancelled"
        }
        _bookings[_bookings.indexOf(booking)] = booking.copy(status = BookingStatus.CANCELLED)
        persist()
        return null
    }

    fun addReview(bookingId: String, rating: Float, comment: String): String? {
        val booking = booking(bookingId) ?: return "Booking not found"
        if (stage(booking) != TripStage.PAST) return "You can review a stay once it is over"
        if (reviewFor(bookingId) != null) return "You have already reviewed this stay"
        if (comment.isBlank()) return "Tell other guests a little about your stay"
        _reviews.add(
            Review(
                id = "rv-${booking.id}",
                hotelId = booking.hotelId,
                bookingId = booking.id,
                guestName = booking.guestName,
                rating = rating,
                comment = comment.trim(),
                date = today()
            )
        )
        persist()
        return null
    }

    fun saveProfile(profile: GuestProfile): String? {
        if (profile.name.isBlank()) return "Enter your name"
        if (!profile.email.contains('@')) return "Enter a valid email address"
        _profile = profile.copy(name = profile.name.trim(), email = profile.email.trim(), phone = profile.phone.trim())
        persist()
        return null
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode = mode
        storage?.saveThemeMode(mode)
    }

    fun resetToSample() {
        _bookings.clear()
        _reviews.clear()
        _favorites.clear()
        _profile = SampleData.profile
        seed()
    }

    private fun seed() {
        _reviews.addAll(SampleData.reviews(today()))
        _bookings.addAll(SampleData.bookings(today()))
        persist()
    }

    private fun newBookingId(): String {
        val highest = _bookings.mapNotNull { it.id.removePrefix("bk-").toIntOrNull() }.maxOrNull()
        return "bk-${(highest ?: 1000) + 1}"
    }

    private fun persist() {
        storage?.save(StoredState(_bookings.toList(), _reviews.filter { it.bookingId.isNotEmpty() }, _favorites.toSet(), _profile))
    }
}
