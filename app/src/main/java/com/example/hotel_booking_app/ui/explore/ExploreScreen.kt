package com.example.hotel_booking_app.ui.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hotel_booking_app.data.Amenity
import com.example.hotel_booking_app.data.BookingRules
import com.example.hotel_booking_app.data.HotelRepository
import com.example.hotel_booking_app.data.SearchFilters
import com.example.hotel_booking_app.data.SortOrder
import com.example.hotel_booking_app.data.StayRequest
import com.example.hotel_booking_app.ui.components.EmptyHint
import com.example.hotel_booking_app.ui.components.HotelCard
import com.example.hotel_booking_app.ui.components.IconBadge
import com.example.hotel_booking_app.ui.components.PanelCard
import com.example.hotel_booking_app.ui.guestsLabel
import com.example.hotel_booking_app.ui.money
import com.example.hotel_booking_app.ui.nightsLabel
import com.example.hotel_booking_app.ui.stayRange

private val RatingSteps = listOf(0f, 4f, 4.5f, 4.8f)
private val PriceSteps = listOf(0, 150, 250, 400)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(
    repo: HotelRepository,
    query: String,
    onQueryChange: (String) -> Unit,
    stay: StayRequest,
    onOpenHotel: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    var sortName by rememberSaveable { mutableStateOf(SortOrder.RECOMMENDED.name) }
    val sort = SortOrder.valueOf(sortName)
    var minRating by rememberSaveable { mutableStateOf(0f) }
    var maxPrice by rememberSaveable { mutableStateOf(0) }
    var amenityNames by rememberSaveable { mutableStateOf(listOf<String>()) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val filters = SearchFilters(
        minRating = minRating,
        maxNightlyRate = maxPrice.takeIf { it > 0 },
        amenities = amenityNames.map { Amenity.valueOf(it) }.toSet()
    )
    val results = repo.search(query, stay, filters, sort)

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = if (query.isBlank()) "All destinations" else query,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${stayRange(stay.checkIn, stay.checkOut)} · ${nightsLabel(stay.nights)} · ${guestsLabel(stay.guests)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            PanelCard(
                modifier = Modifier.fillMaxWidth(),
                container = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(icon = Icons.Filled.LocalOffer, container = MaterialTheme.colorScheme.surface)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Book now, save more",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Use ${BookingRules.promoCodeHints} at checkout",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(repo.cities(), key = { it }) { city ->
                    FilterChip(
                        selected = query.equals(city, ignoreCase = true),
                        onClick = { onQueryChange(if (query.equals(city, ignoreCase = true)) "" else city) },
                        label = { Text(city) },
                        modifier = Modifier.testTag("city_$city")
                    )
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SortOrder.entries, key = { it.name }) { entry ->
                    FilterChip(
                        selected = sort == entry,
                        onClick = { sortName = entry.name },
                        label = { Text(entry.label) }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (results.size == 1) "1 hotel available" else "${results.size} hotels available",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { showFilters = !showFilters }, modifier = Modifier.testTag("toggle_filters")) {
                    Icon(Icons.Filled.Tune, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (filters.active) "Filters (on)" else "Filters")
                }
            }
        }

        if (showFilters) {
            item {
                PanelCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Minimum guest rating", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RatingSteps.forEach { step ->
                                FilterChip(
                                    selected = minRating == step,
                                    onClick = { minRating = step },
                                    label = { Text(if (step == 0f) "Any" else "$step+") },
                                    modifier = Modifier.testTag("rating_$step")
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Max price per night", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PriceSteps.forEach { step ->
                                FilterChip(
                                    selected = maxPrice == step,
                                    onClick = { maxPrice = step },
                                    label = { Text(if (step == 0) "Any" else money(step)) },
                                    modifier = Modifier.testTag("price_$step")
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Amenities", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Amenity.entries.forEach { amenity ->
                                FilterChip(
                                    selected = amenity.name in amenityNames,
                                    onClick = {
                                        amenityNames = if (amenity.name in amenityNames) {
                                            amenityNames - amenity.name
                                        } else {
                                            amenityNames + amenity.name
                                        }
                                    },
                                    label = { Text(amenity.label) }
                                )
                            }
                        }
                        if (filters.active) {
                            TextButton(onClick = {
                                minRating = 0f
                                maxPrice = 0
                                amenityNames = emptyList()
                            }) { Text("Clear filters") }
                        }
                    }
                }
            }
        }

        if (results.isEmpty()) {
            item {
                EmptyHint("No hotels are free for these dates. Try other dates, fewer guests or a different destination.")
            }
        }

        items(results, key = { it.hotel.id }) { result ->
            HotelCard(
                hotel = result.hotel,
                fromRate = result.fromRate,
                favorite = repo.isFavorite(result.hotel.id),
                onToggleFavorite = { repo.toggleFavorite(result.hotel.id) },
                onClick = { onOpenHotel(result.hotel.id) }
            )
        }
    }
}
