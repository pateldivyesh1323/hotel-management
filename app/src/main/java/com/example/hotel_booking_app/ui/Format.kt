package com.example.hotel_booking_app.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayMonth = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
private val dayMonthYear = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

fun LocalDate.short(): String = format(dayMonth)

fun LocalDate.full(): String = format(dayMonthYear)

/** "12 Aug – 15 Aug", collapsing the year unless the stay crosses into another one. */
fun stayRange(checkIn: LocalDate, checkOut: LocalDate): String =
    if (checkIn.year == checkOut.year) {
        "${checkIn.short()} – ${checkOut.short()}"
    } else {
        "${checkIn.full()} – ${checkOut.full()}"
    }

fun money(amount: Int): String = "$" + "%,d".format(amount)

fun nightsLabel(nights: Int): String = if (nights == 1) "1 night" else "$nights nights"

fun guestsLabel(guests: Int): String = if (guests == 1) "1 guest" else "$guests guests"

fun ratingLabel(rating: Float): String = "%.1f".format(rating)
