package com.example.hotel_management_app.ui.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.data.Review
import com.example.hotel_management_app.ui.components.GuestAvatar
import com.example.hotel_management_app.ui.components.PanelCard
import com.example.hotel_management_app.ui.components.PanelSection
import com.example.hotel_management_app.ui.components.ScoreBar
import com.example.hotel_management_app.ui.components.SectionHeader
import com.example.hotel_management_app.ui.full
import kotlin.math.roundToInt

/** The property's reputation: the headline score, its facets, and what guests wrote. */
@Composable
fun ReviewsScreen(
    repo: HotelRepository,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val rating = repo.rating()
    val reviews = repo.reviews.sortedByDescending { it.date }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PanelSection(title = "Overall rating") {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "%.1f".format(rating.overall),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "/5",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            Stars(rating.overall)
                            Text(
                                text = "from ${"%,d".format(rating.reviews)} reviews",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(20.dp))
                        Column(Modifier.weight(1f)) {
                            rating.facets.forEach { (label, score) ->
                                ScoreBar(
                                    label = label,
                                    score = score,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { SectionHeader("Latest guest reviews") }
        items(reviews, key = { it.id }) { review -> ReviewCard(review) }
    }
}

@Composable
private fun ReviewCard(review: Review, modifier: Modifier = Modifier) {
    PanelCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GuestAvatar(review.guestName, size = 38)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = review.guestName,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Room ${review.roomNumber} · ${review.date.full()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "%.1f".format(review.rating),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Stars(review.rating)
            Spacer(Modifier.height(6.dp))
            Text(text = review.comment, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Five stars, filled to the nearest whole point. */
@Composable
private fun Stars(score: Float, modifier: Modifier = Modifier) {
    val filled = score.roundToInt().coerceIn(0, 5)
    Row(modifier) {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (index < filled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
