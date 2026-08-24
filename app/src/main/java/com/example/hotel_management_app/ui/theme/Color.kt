package com.example.hotel_management_app.ui.theme

import androidx.compose.ui.graphics.Color

// The property's brand is one lime accent on neutral paper. The chrome — backgrounds,
// cards, borders, body copy — is deliberately hue-free warm grey, so the lime only ever
// appears where it means something: the primary action, the selected tab, the lead
// series in a chart. Everything used to carry a green tint, which made the accent
// disappear into its own background.

// --- Brand accent -----------------------------------------------------------------

val LimePrimary = Color(0xFFB4DE2A)
val LimeOnPrimary = Color(0xFF1C2708)
val LimeContainer = Color(0xFFEDF4D8)
val LimeOnContainer = Color(0xFF313D14)

val LimePrimaryDark = Color(0xFFC7EA4B)
val LimeOnPrimaryDark = Color(0xFF1C2708)
val LimeContainerDark = Color(0xFF3B4A18)
val LimeOnContainerDark = Color(0xFFE9F4CB)

// Secondary is a plain stone: the quiet container behind an occupant card or a chip,
// where a second hue would only compete with the accent.

val StoneSecondary = Color(0xFF5A5A55)
val StoneOnSecondary = Color(0xFFFFFFFF)
val StoneContainer = Color(0xFFECECE8)
val StoneOnContainer = Color(0xFF2B2B27)

val StoneSecondaryDark = Color(0xFFC6C6C0)
val StoneOnSecondaryDark = Color(0xFF2E2E2A)
val StoneContainerDark = Color(0xFF3A3A36)
val StoneOnContainerDark = Color(0xFFE6E6E1)

// Tertiary carries the app's one supporting hue — the same blue the charts lead with
// after the brand — so gradients and secondary accents have somewhere to go that is not
// another shade of the primary.

val BlueTertiary = Color(0xFF2A78D6)
val BlueOnTertiary = Color(0xFFFFFFFF)
val BlueContainer = Color(0xFFDCE9F8)
val BlueOnContainer = Color(0xFF10304F)

val BlueTertiaryDark = Color(0xFF8FBDF2)
val BlueOnTertiaryDark = Color(0xFF0B2440)
val BlueContainerDark = Color(0xFF1E3A57)
val BlueOnContainerDark = Color(0xFFD6E7F8)

// --- Neutral chrome ---------------------------------------------------------------

val LightBackground = Color(0xFFF6F6F5)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF0F0EE)
val LightOnSurface = Color(0xFF1A1A19)
val LightOnSurfaceVariant = Color(0xFF6E6E6A)
val LightOutline = Color(0xFFE3E3E0)

val DarkBackground = Color(0xFF121211)
val DarkSurface = Color(0xFF1C1C1B)
val DarkSurfaceVariant = Color(0xFF2A2A28)
val DarkOnSurface = Color(0xFFE9E9E6)
val DarkOnSurfaceVariant = Color(0xFFA3A39E)
val DarkOutline = Color(0xFF373734)

// --- Chart series -------------------------------------------------------------------

// Six hues in a fixed order, led by the brand. The order is the colour-blind-safety
// mechanism, not a preference: both modes were validated as a set against the surface
// they are drawn on (white cards in light, DarkSurface in dark), clearing the CVD
// separation, normal-vision, chroma and 3:1 contrast gates with roughly 1.8x headroom.
// Re-run the check before reordering or re-stepping any slot.
//
// The brand lime itself cannot be slot one: at 1.5:1 on a white card it is not a legible
// mark. Slot one is the lime family stepped down until it is.

val ChartLime = Color(0xFF7F9E18)
val ChartRose = Color(0xFFBE3E6C)
val ChartBlue = Color(0xFF2A78D6)
val ChartOrange = Color(0xFFEB6834)
val ChartViolet = Color(0xFF4A3AA7)
val ChartAqua = Color(0xFF199E70)

val ChartLimeDark = Color(0xFF83A21A)
val ChartRoseDark = Color(0xFFBE3E6C)
val ChartBlueDark = Color(0xFF4E96EA)
val ChartOrangeDark = Color(0xFFD95926)
val ChartVioletDark = Color(0xFF6B54C8)
val ChartAquaDark = Color(0xFF1BAF7A)

// --- Status accents -----------------------------------------------------------------

// Status keeps its meaning-bearing hues — green reads "clear", red "stop" — but the
// containers are re-mixed onto the neutral paper so they sit on it rather than tinting it.

val StatusGreen = Color(0xFF3F7A33)
val StatusGreenContainer = Color(0xFFDFEFD5)
val StatusGreenDark = Color(0xFFA8D496)
val StatusGreenContainerDark = Color(0xFF2C4622)

val StatusBlue = Color(0xFF1F6086)
val StatusBlueContainer = Color(0xFFDBE9F3)
val StatusBlueDark = Color(0xFF9CCDE8)
val StatusBlueContainerDark = Color(0xFF17415A)

val StatusAmber = Color(0xFF8A6410)
val StatusAmberContainer = Color(0xFFF7E9C8)
val StatusAmberDark = Color(0xFFEDCB84)
val StatusAmberContainerDark = Color(0xFF57420F)

val StatusRed = Color(0xFF9B2F2A)
val StatusRedContainer = Color(0xFFF8DEDB)
val StatusRedDark = Color(0xFFF0B4AE)
val StatusRedContainerDark = Color(0xFF63211E)

val StatusGrey = Color(0xFF5C5C58)
val StatusGreyContainer = Color(0xFFE9E9E5)
val StatusGreyDark = Color(0xFFBEBEB8)
val StatusGreyContainerDark = Color(0xFF3A3A36)
