package com.example.hotel_booking_app.data

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class StoredState(
    val bookings: List<Booking>,
    val reviews: List<Review>,
    val favorites: Set<String>,
    val profile: GuestProfile
)

class HotelStorage(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("booking_state_v2", Context.MODE_PRIVATE)

    fun save(state: StoredState) {
        val root = JSONObject().apply {
            put("bookings", JSONArray().also { array -> state.bookings.forEach { array.put(it.toJson()) } })
            put("reviews", JSONArray().also { array -> state.reviews.forEach { array.put(it.toJson()) } })
            put("favorites", JSONArray(state.favorites.toList()))
            put("profile", state.profile.toJson())
        }
        prefs.edit().putString(KEY_STATE, root.toString()).apply()
    }

    fun loadThemeMode(): ThemeMode =
        prefs.getString(KEY_THEME, null)
            ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
            ?: ThemeMode.SYSTEM

    fun saveThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }

    fun load(): StoredState? {
        val raw = prefs.getString(KEY_STATE, null) ?: return null
        return runCatching {
            val root = JSONObject(raw)
            val bookings = root.getJSONArray("bookings")
            val reviews = root.getJSONArray("reviews")
            val favorites = root.getJSONArray("favorites")
            StoredState(
                bookings = (0 until bookings.length()).map { bookings.getJSONObject(it).toBooking() },
                reviews = (0 until reviews.length()).map { reviews.getJSONObject(it).toReview() },
                favorites = (0 until favorites.length()).map { favorites.getString(it) }.toSet(),
                profile = root.getJSONObject("profile").toProfile()
            )
        }.onFailure {
            Log.w(TAG, "Discarding unreadable saved state", it)
            prefs.edit().remove(KEY_STATE).apply()
        }.getOrNull()
    }

    private fun Booking.toJson() = JSONObject().apply {
        put("id", id)
        put("hotelId", hotelId)
        put("roomId", roomId)
        put("guestName", guestName)
        put("guestPhone", guestPhone)
        put("guestEmail", guestEmail)
        put("checkIn", checkIn.toString())
        put("checkOut", checkOut.toString())
        put("guests", guests)
        put("status", status.name)
        put("specialRequests", specialRequests)
        put("bookedOn", bookedOn.toString())
        put("nightlyRate", nightlyRate)
        put("breakfastIncluded", breakfastIncluded)
        put("promoPercent", promoPercent)
        put("payAtHotel", payAtHotel)
    }

    private fun JSONObject.toBooking() = Booking(
        id = getString("id"),
        hotelId = getString("hotelId"),
        roomId = getString("roomId"),
        guestName = getString("guestName"),
        guestPhone = getString("guestPhone"),
        guestEmail = getString("guestEmail"),
        checkIn = LocalDate.parse(getString("checkIn")),
        checkOut = LocalDate.parse(getString("checkOut")),
        guests = getInt("guests"),
        status = BookingStatus.valueOf(getString("status")),
        specialRequests = getString("specialRequests"),
        bookedOn = LocalDate.parse(getString("bookedOn")),
        nightlyRate = getInt("nightlyRate"),
        breakfastIncluded = getBoolean("breakfastIncluded"),
        promoPercent = getInt("promoPercent"),
        payAtHotel = getBoolean("payAtHotel")
    )

    private fun Review.toJson() = JSONObject().apply {
        put("id", id)
        put("hotelId", hotelId)
        put("bookingId", bookingId)
        put("guestName", guestName)
        put("rating", rating.toDouble())
        put("comment", comment)
        put("date", date.toString())
    }

    private fun JSONObject.toReview() = Review(
        id = getString("id"),
        hotelId = getString("hotelId"),
        bookingId = getString("bookingId"),
        guestName = getString("guestName"),
        rating = getDouble("rating").toFloat(),
        comment = getString("comment"),
        date = LocalDate.parse(getString("date"))
    )

    private fun GuestProfile.toJson() = JSONObject().apply {
        put("name", name)
        put("email", email)
        put("phone", phone)
    }

    private fun JSONObject.toProfile() = GuestProfile(
        name = getString("name"),
        email = getString("email"),
        phone = getString("phone")
    )

    private companion object {
        const val TAG = "HotelStorage"
        const val KEY_STATE = "state_json"
        const val KEY_THEME = "theme_mode"
    }
}
