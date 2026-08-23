package com.example.hotel_management_app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hotel_management_app.data.SeriesPoint
import com.example.hotel_management_app.data.Slice
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Charts are drawn straight onto a [Canvas] rather than pulled in from a library: the
 * dashboard only needs four shapes, and drawing them here keeps them on the app's own
 * palette in both themes.
 *
 * Axis labels are laid out as ordinary text around each canvas, so they inherit the app's
 * typography and stay legible when the system font scale changes.
 */

/** Smoothed area chart with an optional tapped point, as on the revenue card. */
@Composable
fun AreaChart(
    points: List<SeriesPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    selectedIndex: Int? = null,
    onSelect: (Int) -> Unit = {},
    valueLabel: (Float) -> String = { it.toInt().toString() },
    height: Int = 150
) {
    if (points.isEmpty()) return
    val axisColor = MaterialTheme.colorScheme.outline
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surface = MaterialTheme.colorScheme.surface
    val maxValue = max(points.maxOf { it.value }, 1f)
    // Round the top of the scale up so the gridline labels stay tidy.
    val ceiling = niceCeiling(maxValue)
    val gridSteps = 4

    Column(modifier) {
        Row {
            Column(
                modifier = Modifier.height(height.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                (gridSteps downTo 0).forEach { step ->
                    Text(
                        text = valueLabel(ceiling * step / gridSteps),
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(height.dp)
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            val step = size.width / max(points.size - 1, 1)
                            onSelect((offset.x / step).toInt().coerceIn(points.indices))
                        }
                    }
            ) {
                val stepX = size.width / max(points.size - 1, 1)
                fun pointAt(index: Int) = Offset(
                    x = stepX * index,
                    y = size.height - (points[index].value / ceiling) * size.height
                )

                repeat(gridSteps + 1) { step ->
                    val y = size.height * step / gridSteps
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f))
                    )
                }

                val line = Path().apply {
                    moveTo(pointAt(0).x, pointAt(0).y)
                    for (index in 0 until points.lastIndex) {
                        val from = pointAt(index)
                        val to = pointAt(index + 1)
                        val midX = (from.x + to.x) / 2f
                        // Two mirrored control points give the gentle S-curve of the reference.
                        cubicTo(midX, from.y, midX, to.y, to.x, to.y)
                    }
                }
                val area = Path().apply {
                    addPath(line)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(
                    path = area,
                    brush = Brush.verticalGradient(
                        listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0f))
                    )
                )
                drawPath(
                    path = line,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                selectedIndex?.let { index ->
                    val point = pointAt(index.coerceIn(points.indices))
                    drawLine(
                        color = lineColor.copy(alpha = 0.5f),
                        start = Offset(point.x, 0f),
                        end = Offset(point.x, size.height),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                    drawCircle(color = surface, radius = 7.dp.toPx(), center = point)
                    drawCircle(
                        color = lineColor,
                        radius = 7.dp.toPx(),
                        center = point,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth().padding(start = 34.dp)) {
            points.forEachIndexed { index, point ->
                Text(
                    text = point.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index == selectedIndex) lineColor else labelColor,
                    fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

/** Two-series column chart — booked against cancelled, per day. */
@Composable
fun GroupedBarChart(
    primary: List<SeriesPoint>,
    secondary: List<SeriesPoint>,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary,
    height: Int = 140
) {
    if (primary.isEmpty()) return
    val axisColor = MaterialTheme.colorScheme.outline
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val ceiling = niceCeiling(
        max(
            max(primary.maxOf { it.value }, secondary.maxOfOrNull { it.value } ?: 0f),
            1f
        )
    )
    val gridSteps = 4
    val grown by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 550),
        label = "bars"
    )

    Column(modifier) {
        Row {
            Column(
                modifier = Modifier.height(height.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                (gridSteps downTo 0).forEach { step ->
                    Text(
                        text = (ceiling * step / gridSteps).toInt().toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Canvas(
                Modifier
                    .weight(1f)
                    .height(height.dp)
            ) {
                repeat(gridSteps + 1) { step ->
                    val y = size.height * step / gridSteps
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f))
                    )
                }
                val slot = size.width / primary.size
                val barWidth = min(slot * 0.26f, 16.dp.toPx())
                val radius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2.5f)
                primary.forEachIndexed { index, point ->
                    val centre = slot * index + slot / 2f
                    listOf(
                        point.value to primaryColor,
                        (secondary.getOrNull(index)?.value ?: 0f) to secondaryColor
                    ).forEachIndexed { series, (value, color) ->
                        if (value <= 0f) return@forEachIndexed
                        val barHeight = (value / ceiling) * size.height * grown
                        val left = centre + (series - 1) * (barWidth + 3.dp.toPx()) + 1.5.dp.toPx()
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(left, size.height - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = radius
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth().padding(start = 26.dp)) {
            primary.forEach { point ->
                Text(
                    text = point.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

/** Ring chart with a gap between slices, as on the booking-by-platform card. */
@Composable
fun DonutChart(
    slices: List<Slice>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    size: Int = 150,
    centerLabel: String? = null,
    centerCaption: String? = null
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    if (total <= 0f) return
    val sweepFraction by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 700),
        label = "donut"
    )
    val onSurface = MaterialTheme.colorScheme.onSurface
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier.size(size.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size.dp)) {
            val thickness = this.size.minDimension * 0.26f
            val inset = thickness / 2f
            var start = -90f
            slices.forEachIndexed { index, slice ->
                val sweep = (slice.value / total) * 360f * sweepFraction
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = start,
                    sweepAngle = (sweep - 2f).coerceAtLeast(0.5f),
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(this.size.width - thickness, this.size.height - thickness),
                    style = Stroke(width = thickness, cap = StrokeCap.Butt)
                )
                start += sweep
            }
        }
        if (centerLabel != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = centerLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = onSurface
                )
                centerCaption?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = muted
                    )
                }
            }
        }
    }
}

/** Legend rows for a donut, each with its share of the total. */
@Composable
fun ChartLegend(
    slices: List<Slice>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        slices.forEachIndexed { index, slice ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(9.dp)
                        .background(colors[index % colors.size], CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${((slice.value / total) * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = slice.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

/** Label, track and score — the rating card's per-facet rows. */
@Composable
fun ScoreBar(
    label: String,
    score: Float,
    modifier: Modifier = Modifier,
    max: Float = 5f,
    color: Color = MaterialTheme.colorScheme.primary,
    valueText: String? = null
) {
    val fraction by animateFloatAsState(
        targetValue = (score / max).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "score"
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(84.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Box(
            Modifier
                .weight(1f)
                .height(7.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .height(7.dp)
                    .background(color, RoundedCornerShape(50))
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = valueText ?: "%.1f".format(score),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * One horizontal bar split into proportional, separately coloured blocks, each carrying
 * its own count. Narrow blocks drop the share, and very narrow ones drop the number too,
 * rather than clipping text that no longer fits.
 */
@Composable
fun SegmentedBar(
    slices: List<Slice>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    height: Int = 62
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    if (total <= 0f) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        slices.forEachIndexed { index, slice ->
            if (slice.value <= 0f) return@forEachIndexed
            val fill = colors[index % colors.size]
            val share = slice.value / total
            // Dark ink on the pale lime end of the palette, light ink on the deep greens.
            val ink = if (fill.luminance() > 0.5f) Color(0xFF15200A) else Color.White
            Box(
                modifier = Modifier
                    .weight(slice.value)
                    .fillMaxHeight()
                    .background(fill, RoundedCornerShape(10.dp))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (share >= 0.07f) {
                        Text(
                            text = slice.value.toInt().toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ink,
                            maxLines = 1
                        )
                    }
                    if (share >= 0.14f) {
                        Text(
                            text = "${(share * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = ink.copy(alpha = 0.75f),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/** Rounds a chart's top value up to a round number so the gridlines read cleanly. */
private fun niceCeiling(value: Float): Float {
    if (value <= 4f) return 4f
    val magnitude = Math.pow(10.0, kotlin.math.floor(kotlin.math.log10(value.toDouble()))).toFloat()
    val normalised = value / magnitude
    val stepped = when {
        normalised <= 1f -> 1f
        normalised <= 2f -> 2f
        normalised <= 4f -> 4f
        normalised <= 5f -> 5f
        else -> 10f
    }
    return stepped * magnitude
}
