package com.example.hotel_management_app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.hotel_management_app.data.Room
import com.example.hotel_management_app.data.RoomType
import com.example.hotel_management_app.ui.theme.isDarkScheme

/**
 * Room photography, drawn rather than shipped.
 *
 * Every room gets an interior illustration composed from its type (how the room is
 * furnished) and its number (which of the four decor schemes it was done out in), so the
 * rack has pictures without an asset bundle or a network call — and, like the charts, the
 * pictures stay on the app's own palette and follow it into dark mode, where the same
 * room is drawn after dusk with the bedside lamp lit.
 */
@Composable
fun RoomImage(
    room: Room,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    val night = isDarkScheme()
    val scheme = remember(room.number, night) { schemeFor(room.number, night) }
    Box(modifier.clip(shape)) {
        Canvas(Modifier.fillMaxSize()) { drawRoom(scheme, room.type) }
        overlay()
    }
}

/** The small square picture that fronts a list row. */
@Composable
fun RoomThumbnail(
    room: Room,
    modifier: Modifier = Modifier,
    size: Int = 52
) {
    RoomImage(
        room = room,
        modifier = modifier.size(size.dp),
        shape = MaterialTheme.shapes.small
    )
}

/**
 * Darkens the foot of a picture so white text laid over it stays readable whatever the
 * room's decor scheme happens to be.
 */
fun imageScrim(): Brush = Brush.verticalGradient(
    0.42f to Color.Transparent,
    0.74f to Color(0x4D0A0F08),
    1f to Color(0xA60A0F08)
)

// --- Decor schemes ---------------------------------------------------------------------

/** Every colour one illustration needs, so a scheme can be swapped in a single line. */
private data class RoomScheme(
    val night: Boolean,
    val wallTop: Color,
    val wallBottom: Color,
    val floorFar: Color,
    val floorNear: Color,
    val skirting: Color,
    val headboard: Color,
    val headboardLine: Color,
    val wood: Color,
    val duvet: Color,
    val duvetFold: Color,
    val runner: Color,
    val linen: Color,
    val linenShade: Color,
    val accent: Color,
    val skyTop: Color,
    val skyBottom: Color,
    val orb: Color,
    val hills: Color,
    val curtain: Color,
    val trim: Color,
    val rug: Color,
    val rugRing: Color,
    val pot: Color,
    val leaf: Color,
    val leafDeep: Color,
    val lampShade: Color,
    val glow: Color,
    val art: Color,
    val artFrame: Color
)

private val SageMorning = RoomScheme(
    night = false,
    wallTop = Color(0xFFF7F5ED), wallBottom = Color(0xFFE6E7D9),
    floorFar = Color(0xFFB98A55), floorNear = Color(0xFFD9B07B),
    skirting = Color(0xFFF1EFE5),
    headboard = Color(0xFF6E8E6A), headboardLine = Color(0xFF5C7C58),
    wood = Color(0xFF8A6A45),
    duvet = Color(0xFF2F5D50), duvetFold = Color(0xFF244A40),
    runner = Color(0xFFC7D98F),
    linen = Color(0xFFFFFFFF), linenShade = Color(0xFFE9EDE0),
    accent = Color(0xFFB4DE2A),
    skyTop = Color(0xFFBFE0F2), skyBottom = Color(0xFFEDF5E4),
    orb = Color(0xFFFFE9A8),
    hills = Color(0xFFA7C79A),
    curtain = Color(0xFFE4E8D7), trim = Color(0xFFFDFDF8),
    rug = Color(0xFFDED8C6), rugRing = Color(0xFFC7BFA6),
    pot = Color(0xFFC98A5E), leaf = Color(0xFF5E9C6B), leafDeep = Color(0xFF3F7A50),
    lampShade = Color(0xFFF4E4BA), glow = Color(0xFFFFD98A),
    art = Color(0xFF9EC4B5), artFrame = Color(0xFFF1EFE5)
)

private val ClayDusk = SageMorning.copy(
    wallTop = Color(0xFFF9F2EA), wallBottom = Color(0xFFEDE1D4),
    floorFar = Color(0xFFA9764C), floorNear = Color(0xFFD09C6D),
    skirting = Color(0xFFF6EFE6),
    headboard = Color(0xFFB4664A), headboardLine = Color(0xFF9A523A),
    wood = Color(0xFF7E5637),
    duvet = Color(0xFF7C3B2C), duvetFold = Color(0xFF682F22),
    runner = Color(0xFFE8C58A),
    accent = Color(0xFFE08A5A),
    skyTop = Color(0xFFFBD6A6), skyBottom = Color(0xFFF8EDDA),
    orb = Color(0xFFFFB870),
    hills = Color(0xFFC49A79),
    curtain = Color(0xFFF2E3CF),
    rug = Color(0xFFEBDFCB), rugRing = Color(0xFFD5C2A5),
    pot = Color(0xFF8E6A4E), leaf = Color(0xFF6F9C5C), leafDeep = Color(0xFF4F7B44),
    art = Color(0xFFD9A05B), artFrame = Color(0xFFF6EFE6)
)

private val HarbourBlue = SageMorning.copy(
    wallTop = Color(0xFFF3F6F9), wallBottom = Color(0xFFE1E8EF),
    floorFar = Color(0xFF9E8B76), floorNear = Color(0xFFC6B097),
    skirting = Color(0xFFF4F7FA),
    headboard = Color(0xFF3F5C7A), headboardLine = Color(0xFF334C66),
    wood = Color(0xFF7C6B57),
    duvet = Color(0xFF1F3A5A), duvetFold = Color(0xFF162C46),
    runner = Color(0xFF9EC9DC),
    accent = Color(0xFF4E8FB5),
    skyTop = Color(0xFFA9D6F2), skyBottom = Color(0xFFE8F3FB),
    orb = Color(0xFFFFF0BE),
    hills = Color(0xFF8FB6C4),
    curtain = Color(0xFFDDE7EE),
    rug = Color(0xFFE4E9ED), rugRing = Color(0xFFC5D1D9),
    pot = Color(0xFFB0793F), leaf = Color(0xFF4F9078), leafDeep = Color(0xFF35705C),
    art = Color(0xFF6FA0B8), artFrame = Color(0xFFF4F7FA)
)

private val AmberSuite = SageMorning.copy(
    wallTop = Color(0xFFF8F4E9), wallBottom = Color(0xFFECE4D1),
    floorFar = Color(0xFF7E5C3C), floorNear = Color(0xFFAA7E53),
    skirting = Color(0xFFF5F1E4),
    headboard = Color(0xFF5E6B36), headboardLine = Color(0xFF4B592B),
    wood = Color(0xFF6E4F31),
    duvet = Color(0xFF2F3A26), duvetFold = Color(0xFF25301E),
    runner = Color(0xFFE4C25C),
    accent = Color(0xFFD8A62B),
    skyTop = Color(0xFFCFE4C8), skyBottom = Color(0xFFF4F7E8),
    orb = Color(0xFFFFE07A),
    hills = Color(0xFF9BBE8A),
    curtain = Color(0xFFF0E9D5),
    rug = Color(0xFFE7E1CD), rugRing = Color(0xFFCDC3A4),
    pot = Color(0xFFA9683E), leaf = Color(0xFF5D8C4E), leafDeep = Color(0xFF436C3A),
    art = Color(0xFFB8A45E), artFrame = Color(0xFFF5F1E4)
)

private val Schemes = listOf(SageMorning, ClayDusk, HarbourBlue, AmberSuite)

private fun schemeFor(number: String, night: Boolean): RoomScheme {
    val scheme = Schemes[number.sumOf { it.code } % Schemes.size]
    return if (night) scheme.afterDark() else scheme
}

/**
 * The same room after sunset: the daylight surfaces fall away towards the dark scheme's
 * background while the bedside lamp keeps its warmth, which is what makes the picture
 * read as one room at two times of day rather than as two unrelated pictures.
 */
private fun RoomScheme.afterDark(): RoomScheme {
    val ink = Color(0xFF0D110A)
    fun Color.dim(amount: Float) = mix(ink, amount)
    return copy(
        night = true,
        wallTop = wallTop.dim(0.76f), wallBottom = wallBottom.dim(0.82f),
        floorFar = floorFar.dim(0.74f), floorNear = floorNear.dim(0.66f),
        skirting = skirting.dim(0.72f),
        headboard = headboard.dim(0.44f), headboardLine = headboardLine.dim(0.44f),
        wood = wood.dim(0.50f),
        duvet = duvet.dim(0.36f), duvetFold = duvetFold.dim(0.36f),
        runner = runner.dim(0.32f),
        linen = linen.dim(0.56f), linenShade = linenShade.dim(0.62f),
        accent = accent.dim(0.20f),
        skyTop = Color(0xFF141F3C), skyBottom = Color(0xFF31465C),
        orb = Color(0xFFEFEEDC),
        hills = Color(0xFF20323A),
        curtain = curtain.dim(0.72f), trim = trim.dim(0.70f),
        rug = rug.dim(0.70f), rugRing = rugRing.dim(0.68f),
        pot = pot.dim(0.56f), leaf = leaf.dim(0.46f), leafDeep = leafDeep.dim(0.46f),
        art = art.dim(0.46f), artFrame = artFrame.dim(0.66f)
    )
}

private fun Color.mix(other: Color, amount: Float): Color = Color(
    red = red + (other.red - red) * amount,
    green = green + (other.green - green) * amount,
    blue = blue + (other.blue - blue) * amount,
    alpha = alpha
)

// --- The illustration ------------------------------------------------------------------

/**
 * Everything is placed as a fraction of the canvas, so the same scene composes correctly
 * as a 52dp thumbnail and as a full-width hero.
 */
private fun DrawScope.drawRoom(s: RoomScheme, type: RoomType) {
    val w = size.width
    val h = size.height
    val horizon = h * 0.70f
    val lavish = type != RoomType.STANDARD

    drawRect(
        brush = Brush.verticalGradient(listOf(s.wallTop, s.wallBottom), 0f, horizon),
        size = Size(w, horizon)
    )
    drawRect(
        brush = Brush.verticalGradient(listOf(s.floorFar, s.floorNear), horizon, h),
        topLeft = Offset(0f, horizon),
        size = Size(w, h - horizon)
    )
    drawRect(s.skirting, Offset(0f, horizon - h * 0.022f), Size(w, h * 0.022f))

    val windowLeft = w * 0.665f
    val windowRight = w * 0.945f
    drawWindow(s, windowLeft, h * 0.10f, windowRight, h * 0.505f, arched = type == RoomType.SUITE)
    drawFloorLight(s, windowLeft, windowRight, horizon, w, h)
    if (lavish) drawCurtains(s, w, h)

    when (type) {
        RoomType.STANDARD -> drawArt(s, w * 0.470f, h * 0.185f, w * 0.600f, h * 0.385f)
        RoomType.DELUXE -> {
            drawArt(s, w * 0.452f, h * 0.180f, w * 0.548f, h * 0.345f)
            drawArt(s, w * 0.566f, h * 0.215f, w * 0.638f, h * 0.340f)
        }
        RoomType.SUITE -> drawPendant(s, w, h)
    }

    drawRug(s, w, h, wide = lavish)
    drawBed(s, w, h, type)
    drawNightstand(s, w, h)
    if (lavish) drawPlant(s, w, h)
    drawFinish(s, w, h)
}

private fun DrawScope.drawWindow(
    s: RoomScheme,
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    arched: Boolean
) {
    val trim = (x1 - x0) * 0.055f
    val span = x1 - x0
    val drop = y1 - y0
    val outer = windowPath(x0, y0, x1, y1, arched, trim)
    val glass = windowPath(x0 + trim, y0 + trim, x1 - trim, y1 - trim, arched, trim * 0.6f)

    drawPath(outer, s.trim)
    clipPath(glass) {
        drawRect(
            brush = Brush.verticalGradient(listOf(s.skyTop, s.skyBottom), y0, y1),
            topLeft = Offset(x0, y0),
            size = Size(span, drop)
        )
        val orbCenter = Offset(x0 + span * 0.30f, y0 + drop * 0.28f)
        drawCircle(s.orb.copy(alpha = 0.30f), span * 0.26f, orbCenter)
        drawCircle(s.orb, span * 0.13f, orbCenter)
        if (s.night) {
            // A handful of fixed stars, placed by hand so they never crowd the moon.
            listOf(0.62f to 0.20f, 0.78f to 0.34f, 0.50f to 0.46f, 0.86f to 0.16f)
                .forEach { (fx, fy) ->
                    drawCircle(
                        color = s.orb.copy(alpha = 0.75f),
                        radius = span * 0.017f,
                        center = Offset(x0 + span * fx, y0 + drop * fy)
                    )
                }
        }
        drawOval(
            color = s.hills,
            topLeft = Offset(x0 - span * 0.15f, y1 - drop * 0.34f),
            size = Size(span * 0.85f, drop * 0.44f)
        )
        drawOval(
            color = s.hills.mix(Color.White, if (s.night) 0.08f else 0.22f),
            topLeft = Offset(x0 + span * 0.42f, y1 - drop * 0.26f),
            size = Size(span * 0.80f, drop * 0.36f)
        )
    }

    val midX = (x0 + x1) / 2f
    val midY = y0 + drop * 0.62f
    drawLine(s.trim, Offset(midX, y0 + trim), Offset(midX, y1 - trim), trim * 0.75f)
    drawLine(s.trim, Offset(x0 + trim, midY), Offset(x1 - trim, midY), trim * 0.75f)
    drawPath(outer, s.trim.mix(Color.Black, 0.10f), style = Stroke(trim * 0.35f))
}

private fun windowPath(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    arched: Boolean,
    corner: Float
): Path {
    val path = Path()
    if (arched) {
        val radius = (x1 - x0) / 2f
        path.moveTo(x0, y1)
        path.lineTo(x0, y0 + radius)
        path.arcTo(Rect(x0, y0, x1, y0 + radius * 2f), 180f, 180f, false)
        path.lineTo(x1, y1)
        path.close()
    } else {
        path.addRoundRect(RoundRect(x0, y0, x1, y1, CornerRadius(corner, corner)))
    }
    return path
}

/** The wedge of daylight — moonlight after dark — the window throws across the floor. */
private fun DrawScope.drawFloorLight(
    s: RoomScheme,
    windowLeft: Float,
    windowRight: Float,
    horizon: Float,
    w: Float,
    h: Float
) {
    val wash = Path().apply {
        moveTo(windowLeft, horizon)
        lineTo(windowRight, horizon)
        lineTo(windowRight + w * 0.03f, h)
        lineTo(windowLeft - w * 0.30f, h)
        close()
    }
    drawPath(
        path = wash,
        brush = Brush.verticalGradient(
            listOf(s.orb.copy(alpha = if (s.night) 0.14f else 0.32f), Color.Transparent),
            horizon,
            h
        )
    )
}

/**
 * A lamp's spill. Drawn as a falloff rather than a flat disc, which at the alphas dark
 * mode needs would otherwise read as a pale circle stuck on the wall.
 */
private fun DrawScope.drawGlow(s: RoomScheme, center: Offset, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                s.glow.copy(alpha = if (s.night) 0.55f else 0.30f),
                Color.Transparent
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

private fun DrawScope.drawCurtains(s: RoomScheme, w: Float, h: Float) {
    val rodY = h * 0.072f
    drawLine(s.wood, Offset(w * 0.625f, rodY), Offset(w * 0.980f, rodY), h * 0.012f, StrokeCap.Round)
    drawCircle(s.wood, w * 0.011f, Offset(w * 0.625f, rodY))
    drawCircle(s.wood, w * 0.011f, Offset(w * 0.980f, rodY))
    listOf(w * 0.633f to w * 0.700f, w * 0.908f to w * 0.975f).forEach { (x0, x1) ->
        drawRoundRect(
            color = s.curtain,
            topLeft = Offset(x0, rodY),
            size = Size(x1 - x0, h * 0.520f),
            cornerRadius = CornerRadius(w * 0.020f, w * 0.020f)
        )
        // One fold keeps the drape from reading as a flat slab.
        drawLine(
            color = s.curtain.mix(Color.Black, 0.10f),
            start = Offset(x0 + (x1 - x0) * 0.55f, rodY + h * 0.03f),
            end = Offset(x0 + (x1 - x0) * 0.55f, rodY + h * 0.47f),
            strokeWidth = w * 0.007f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawArt(s: RoomScheme, x0: Float, y0: Float, x1: Float, y1: Float) {
    val inset = (x1 - x0) * 0.10f
    drawRoundRect(
        color = s.artFrame,
        topLeft = Offset(x0, y0),
        size = Size(x1 - x0, y1 - y0),
        cornerRadius = CornerRadius(inset * 0.4f, inset * 0.4f)
    )
    drawRoundRect(
        color = s.artFrame.mix(Color.Black, 0.16f),
        topLeft = Offset(x0, y0),
        size = Size(x1 - x0, y1 - y0),
        cornerRadius = CornerRadius(inset * 0.4f, inset * 0.4f),
        style = Stroke(inset * 0.25f)
    )
    val mount = Path().apply {
        addRoundRect(
            RoundRect(x0 + inset, y0 + inset, x1 - inset, y1 - inset, CornerRadius(0f, 0f))
        )
    }
    clipPath(mount) {
        drawRect(
            color = s.art.mix(Color.White, if (s.night) 0.10f else 0.55f),
            topLeft = Offset(x0 + inset, y0 + inset),
            size = Size(x1 - x0 - inset * 2f, y1 - y0 - inset * 2f)
        )
        // A miniature of the view out of the window, so the art belongs to the room.
        drawCircle(
            color = s.accent,
            radius = (x1 - x0) * 0.15f,
            center = Offset(x0 + (x1 - x0) * 0.64f, y0 + (y1 - y0) * 0.33f)
        )
        drawOval(
            color = s.art,
            topLeft = Offset(x0 - inset, y0 + (y1 - y0) * 0.52f),
            size = Size((x1 - x0) * 1.1f, (y1 - y0) * 0.9f)
        )
    }
}

/** The suite's pendant light, hung where the smaller rooms hang a picture. */
private fun DrawScope.drawPendant(s: RoomScheme, w: Float, h: Float) {
    val cx = w * 0.545f
    drawLine(s.wood, Offset(cx, 0f), Offset(cx, h * 0.165f), w * 0.006f)
    drawGlow(s, Offset(cx, h * 0.320f), w * 0.125f)
    val shade = Path().apply {
        moveTo(cx - w * 0.026f, h * 0.165f)
        lineTo(cx + w * 0.026f, h * 0.165f)
        lineTo(cx + w * 0.058f, h * 0.290f)
        lineTo(cx - w * 0.058f, h * 0.290f)
        close()
    }
    drawPath(shade, s.lampShade)
    drawPath(shade, s.lampShade.mix(Color.Black, 0.18f), style = Stroke(w * 0.004f))
}

private fun DrawScope.drawRug(s: RoomScheme, w: Float, h: Float, wide: Boolean) {
    val right = if (wide) w * 0.700f else w * 0.620f
    drawOval(s.rug, Offset(w * 0.015f, h * 0.700f), Size(right - w * 0.015f, h * 0.245f))
    drawOval(
        color = s.rugRing,
        topLeft = Offset(w * 0.075f, h * 0.730f),
        size = Size(right - w * 0.135f, h * 0.185f),
        style = Stroke(h * 0.010f)
    )
}

private fun DrawScope.drawBed(s: RoomScheme, w: Float, h: Float, type: RoomType) {
    val corner = w * 0.030f

    // Headboard first: the panelling is what separates the three room grades.
    drawRoundRect(
        color = s.headboard,
        topLeft = Offset(w * 0.065f, h * 0.255f),
        size = Size(w * 0.360f, h * 0.350f),
        cornerRadius = CornerRadius(corner, corner)
    )
    val panels = if (type == RoomType.SUITE) 4 else 3
    repeat(panels - 1) { index ->
        val x = w * 0.065f + w * 0.360f * (index + 1) / panels
        drawLine(
            color = s.headboardLine,
            start = Offset(x, h * 0.295f),
            end = Offset(x, h * 0.575f),
            strokeWidth = w * 0.006f,
            cap = StrokeCap.Round
        )
    }
    if (type == RoomType.SUITE) {
        drawLine(
            color = s.headboardLine,
            start = Offset(w * 0.090f, h * 0.435f),
            end = Offset(w * 0.400f, h * 0.435f),
            strokeWidth = w * 0.006f,
            cap = StrokeCap.Round
        )
    }
    drawRoundRect(
        color = s.headboard.mix(Color.White, 0.16f),
        topLeft = Offset(w * 0.085f, h * 0.272f),
        size = Size(w * 0.320f, h * 0.022f),
        cornerRadius = CornerRadius(corner, corner)
    )

    // The bed casts before it is drawn, so the shadow stays behind the frame.
    drawOval(
        color = Color.Black.copy(alpha = if (s.night) 0.22f else 0.11f),
        topLeft = Offset(w * 0.030f, h * 0.688f),
        size = Size(w * 0.590f, h * 0.078f)
    )

    drawRoundRect(
        color = s.wood,
        topLeft = Offset(w * 0.055f, h * 0.600f),
        size = Size(w * 0.510f, h * 0.100f),
        cornerRadius = CornerRadius(w * 0.012f, w * 0.012f)
    )
    listOf(w * 0.078f, w * 0.512f).forEach { x ->
        drawRoundRect(
            color = s.wood.mix(Color.Black, 0.22f),
            topLeft = Offset(x, h * 0.695f),
            size = Size(w * 0.030f, h * 0.042f),
            cornerRadius = CornerRadius(w * 0.006f, w * 0.006f)
        )
    }

    drawRoundRect(
        color = s.linen,
        topLeft = Offset(w * 0.050f, h * 0.525f),
        size = Size(w * 0.525f, h * 0.090f),
        cornerRadius = CornerRadius(w * 0.016f, w * 0.016f)
    )
    drawRoundRect(
        color = s.linenShade,
        topLeft = Offset(w * 0.050f, h * 0.592f),
        size = Size(w * 0.525f, h * 0.023f),
        cornerRadius = CornerRadius(w * 0.010f, w * 0.010f)
    )

    drawRoundRect(
        color = s.duvet,
        topLeft = Offset(w * 0.245f, h * 0.508f),
        size = Size(w * 0.340f, h * 0.130f),
        cornerRadius = CornerRadius(w * 0.018f, w * 0.018f)
    )
    drawRoundRect(
        color = s.duvetFold,
        topLeft = Offset(w * 0.245f, h * 0.508f),
        size = Size(w * 0.048f, h * 0.130f),
        cornerRadius = CornerRadius(w * 0.014f, w * 0.014f)
    )
    drawRoundRect(
        color = s.runner,
        topLeft = Offset(w * 0.420f, h * 0.498f),
        size = Size(w * 0.165f, h * 0.150f),
        cornerRadius = CornerRadius(w * 0.016f, w * 0.016f)
    )

    // Pillows stack back to front; the grade adds one to the pile.
    if (type != RoomType.STANDARD) {
        drawRoundRect(
            color = s.linenShade,
            topLeft = Offset(w * 0.078f, h * 0.408f),
            size = Size(w * 0.135f, h * 0.125f),
            cornerRadius = CornerRadius(w * 0.030f, w * 0.030f)
        )
    }
    drawRoundRect(
        color = s.linenShade,
        topLeft = Offset(w * 0.108f, h * 0.418f),
        size = Size(w * 0.140f, h * 0.120f),
        cornerRadius = CornerRadius(w * 0.030f, w * 0.030f)
    )
    drawRoundRect(
        color = s.linen,
        topLeft = Offset(w * 0.140f, h * 0.442f),
        size = Size(w * 0.145f, h * 0.105f),
        cornerRadius = CornerRadius(w * 0.030f, w * 0.030f)
    )
    if (type != RoomType.STANDARD) {
        drawRoundRect(
            color = s.accent,
            topLeft = Offset(w * 0.205f, h * 0.470f),
            size = Size(w * 0.088f, h * 0.078f),
            cornerRadius = CornerRadius(w * 0.022f, w * 0.022f)
        )
    }
}

private fun DrawScope.drawNightstand(s: RoomScheme, w: Float, h: Float) {
    drawRoundRect(
        color = s.wood,
        topLeft = Offset(w * 0.605f, h * 0.575f),
        size = Size(w * 0.115f, h * 0.125f),
        cornerRadius = CornerRadius(w * 0.010f, w * 0.010f)
    )
    drawLine(
        color = s.wood.mix(Color.Black, 0.22f),
        start = Offset(w * 0.618f, h * 0.625f),
        end = Offset(w * 0.707f, h * 0.625f),
        strokeWidth = w * 0.005f,
        cap = StrokeCap.Round
    )
    listOf(w * 0.612f, w * 0.696f).forEach { x ->
        drawRoundRect(
            color = s.wood.mix(Color.Black, 0.22f),
            topLeft = Offset(x, h * 0.696f),
            size = Size(w * 0.018f, h * 0.032f),
            cornerRadius = CornerRadius(w * 0.005f, w * 0.005f)
        )
    }

    val cx = w * 0.6625f
    drawGlow(s, Offset(cx, h * 0.500f), w * 0.140f)
    drawRoundRect(
        color = s.wood.mix(Color.Black, 0.15f),
        topLeft = Offset(cx - w * 0.024f, h * 0.552f),
        size = Size(w * 0.048f, h * 0.024f),
        cornerRadius = CornerRadius(w * 0.008f, w * 0.008f)
    )
    drawLine(s.wood.mix(Color.Black, 0.15f), Offset(cx, h * 0.495f), Offset(cx, h * 0.558f), w * 0.008f)
    val shade = Path().apply {
        moveTo(cx - w * 0.028f, h * 0.432f)
        lineTo(cx + w * 0.028f, h * 0.432f)
        lineTo(cx + w * 0.050f, h * 0.500f)
        lineTo(cx - w * 0.050f, h * 0.500f)
        close()
    }
    drawPath(shade, s.lampShade)
    drawPath(shade, s.lampShade.mix(Color.Black, 0.18f), style = Stroke(w * 0.004f))
}

private fun DrawScope.drawPlant(s: RoomScheme, w: Float, h: Float) {
    val cx = w * 0.845f
    val potTop = h * 0.610f
    // Leaves fan out of the pot's mouth, alternating tone so the fan reads as depth.
    listOf(-52f, -30f, -8f, 14f, 36f).forEachIndexed { index, angle ->
        rotate(angle, Offset(cx, potTop)) {
            drawOval(
                color = if (index % 2 == 0) s.leaf else s.leafDeep,
                topLeft = Offset(cx - w * 0.017f, potTop - h * 0.215f),
                size = Size(w * 0.034f, h * 0.215f)
            )
        }
    }
    val pot = Path().apply {
        moveTo(cx - w * 0.048f, potTop)
        lineTo(cx + w * 0.048f, potTop)
        lineTo(cx + w * 0.034f, h * 0.735f)
        lineTo(cx - w * 0.034f, h * 0.735f)
        close()
    }
    drawPath(pot, s.pot)
    drawRoundRect(
        color = s.pot.mix(Color.White, 0.14f),
        topLeft = Offset(cx - w * 0.054f, potTop - h * 0.014f),
        size = Size(w * 0.108f, h * 0.026f),
        cornerRadius = CornerRadius(w * 0.006f, w * 0.006f)
    )
}

/** Ceiling light and a settled shadow at the foot of the frame — the last five percent. */
private fun DrawScope.drawFinish(s: RoomScheme, w: Float, h: Float) {
    val ceiling = if (s.night) Color.Black else Color.White
    drawRect(
        brush = Brush.verticalGradient(
            listOf(ceiling.copy(alpha = if (s.night) 0.16f else 0.20f), Color.Transparent),
            0f,
            h * 0.38f
        ),
        size = Size(w, h * 0.38f)
    )
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color.Transparent, Color.Black.copy(alpha = if (s.night) 0.18f else 0.07f)),
            h * 0.62f,
            h
        ),
        topLeft = Offset(0f, h * 0.62f),
        size = Size(w, h * 0.38f)
    )
}
