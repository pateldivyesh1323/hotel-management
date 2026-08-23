package com.example.hotel_management_app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.example.hotel_management_app.data.BookingStatus
import com.example.hotel_management_app.data.RoomStatus
import com.example.hotel_management_app.data.TaskPriority

/** Container/content colour pair for a status pill. */
data class StatusTone(val container: Color, val content: Color)

@Composable
@ReadOnlyComposable
private fun tone(
    lightContainer: Color,
    lightContent: Color,
    darkContainer: Color,
    darkContent: Color
): StatusTone = if (isDarkScheme()) {
    StatusTone(darkContainer, darkContent)
} else {
    StatusTone(lightContainer, lightContent)
}

@Composable
@ReadOnlyComposable
fun greenTone(): StatusTone =
    tone(StatusGreenContainer, StatusGreen, StatusGreenContainerDark, StatusGreenDark)

@Composable
@ReadOnlyComposable
fun redTone(): StatusTone =
    tone(StatusRedContainer, StatusRed, StatusRedContainerDark, StatusRedDark)

@Composable
@ReadOnlyComposable
fun amberTone(): StatusTone =
    tone(StatusAmberContainer, StatusAmber, StatusAmberContainerDark, StatusAmberDark)

@Composable
@ReadOnlyComposable
fun blueTone(): StatusTone =
    tone(StatusBlueContainer, StatusBlue, StatusBlueContainerDark, StatusBlueDark)

@Composable
@ReadOnlyComposable
fun greyTone(): StatusTone =
    tone(StatusGreyContainer, StatusGrey, StatusGreyContainerDark, StatusGreyDark)

@Composable
@ReadOnlyComposable
fun RoomStatus.tone(): StatusTone = when (this) {
    RoomStatus.AVAILABLE -> greenTone()
    RoomStatus.OCCUPIED -> blueTone()
    RoomStatus.CLEANING -> amberTone()
    RoomStatus.MAINTENANCE -> redTone()
}

@Composable
@ReadOnlyComposable
fun BookingStatus.tone(): StatusTone = when (this) {
    BookingStatus.RESERVED -> amberTone()
    BookingStatus.CHECKED_IN -> greenTone()
    BookingStatus.CHECKED_OUT -> greyTone()
    BookingStatus.CANCELLED -> redTone()
}

@Composable
@ReadOnlyComposable
fun TaskPriority.tone(): StatusTone = when (this) {
    TaskPriority.HIGH -> redTone()
    TaskPriority.NORMAL -> amberTone()
    TaskPriority.LOW -> greyTone()
}
