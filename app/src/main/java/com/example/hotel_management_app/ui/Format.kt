package com.example.hotel_management_app.ui

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayMonth = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
private val dayMonthYear = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
private val weekdayLong = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
private val clock = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

fun LocalDate.short(): String = format(dayMonth)

fun LocalDate.full(): String = format(dayMonthYear)

fun LocalDate.headline(): String = format(weekdayLong)

fun LocalDateTime.clockTime(): String = format(clock)

/** "12 Aug – 15 Aug", collapsing the year unless the stay crosses into another one. */
fun stayRange(checkIn: LocalDate, checkOut: LocalDate): String =
    if (checkIn.year == checkOut.year) {
        "${checkIn.short()} – ${checkOut.short()}"
    } else {
        "${checkIn.full()} – ${checkOut.full()}"
    }

fun money(amount: Int): String = "$" + "%,d".format(amount)

/** "$315K" — for axis labels and tiles that cannot afford the full figure. */
fun compactMoney(amount: Float): String {
    val value = amount.toInt()
    return when {
        value >= 1_000_000 -> "$" + trimmed(value / 1_000_000f) + "M"
        value >= 1_000 -> "$" + trimmed(value / 1_000f) + "K"
        else -> "$$value"
    }
}

/** Keeps one decimal only when it carries information, so "$7.5K" but "$10K". */
private fun trimmed(value: Float): String =
    if (value >= 10f || value % 1f < 0.05f) "%.0f".format(value) else "%.1f".format(value)

fun nightsLabel(nights: Int): String = if (nights == 1) "1 night" else "$nights nights"

fun guestsLabel(guests: Int): String = if (guests == 1) "1 guest" else "$guests guests"

/** "Today", "Tomorrow", or the date — how the task list dates its rows. */
fun dueLabel(due: LocalDate, today: LocalDate): String = when (due) {
    today -> "Today"
    today.plusDays(1) -> "Tomorrow"
    today.minusDays(1) -> "Yesterday"
    else -> due.full()
}

/** "AS" for Aditi Sharma — used by the guest avatars. */
fun initialsOf(name: String): String = name.trim()
    .split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .map { it.first().uppercaseChar() }
    .joinToString("")
    .ifEmpty { "?" }
