package com.example.hotel_booking_app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.example.hotel_booking_app.data.TripStage

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
fun blueTone(): StatusTone =
    tone(StatusBlueContainer, StatusBlue, StatusBlueContainerDark, StatusBlueDark)

@Composable
@ReadOnlyComposable
fun greyTone(): StatusTone =
    tone(StatusGreyContainer, StatusGrey, StatusGreyContainerDark, StatusGreyDark)

@Composable
@ReadOnlyComposable
fun TripStage.tone(): StatusTone = when (this) {
    TripStage.UPCOMING -> greenTone()
    TripStage.PAST -> greyTone()
    TripStage.CANCELLED -> redTone()
}
