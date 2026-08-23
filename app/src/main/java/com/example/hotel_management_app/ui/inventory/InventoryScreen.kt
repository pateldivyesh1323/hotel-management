package com.example.hotel_management_app.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.data.InventoryItem
import com.example.hotel_management_app.ui.components.EmptyHint
import com.example.hotel_management_app.ui.components.PanelCard
import com.example.hotel_management_app.ui.components.SectionHeader
import com.example.hotel_management_app.ui.components.StatTile
import com.example.hotel_management_app.ui.components.StatusPill
import com.example.hotel_management_app.ui.theme.greenTone
import com.example.hotel_management_app.ui.theme.redTone

/**
 * Stock on hand. Anything at or below its reorder level is pulled to the top, because a
 * housekeeper finding out mid-round is the expensive way to learn it.
 */
@Composable
fun InventoryScreen(
    repo: HotelRepository,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    val categories = repo.inventory.map { it.category }.distinct().sorted()
    val items = repo.inventory
        .filter { category == null || it.category == category }
        .sortedWith(compareByDescending<InventoryItem> { it.low }.thenBy { it.name })
    val low = repo.lowStock()

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    value = repo.inventory.size.toString(),
                    label = "Tracked items",
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = low.size.toString(),
                    label = "Below reorder level",
                    modifier = Modifier.weight(1f),
                    container = if (low.isEmpty()) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = category == null,
                    onClick = { category = null },
                    label = { Text("All") }
                )
                categories.forEach { entry ->
                    FilterChip(
                        selected = category == entry,
                        onClick = { category = if (category == entry) null else entry },
                        label = { Text(entry) }
                    )
                }
            }
        }

        if (items.isEmpty()) {
            item { EmptyHint("Nothing tracked in this category") }
        } else {
            items(items, key = { it.id }) { item ->
                InventoryRow(
                    item = item,
                    onAdjust = { delta -> repo.adjustStock(item.id, delta) },
                    onRestock = { repo.restock(item.id) }
                )
            }
        }
    }
}

@Composable
private fun InventoryRow(
    item: InventoryItem,
    onAdjust: (Int) -> Unit,
    onRestock: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Par level is three times the reorder threshold, which is what "restock" tops up to.
    val par = (item.threshold * 3).coerceAtLeast(1)
    val fraction = (item.quantity.toFloat() / par).coerceIn(0f, 1f)
    PanelCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(text = item.name, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "${item.category} · reorder at ${item.threshold} ${item.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusPill(
                    label = if (item.low) "Low" else "In stock",
                    tone = if (item.low) redTone() else greenTone()
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(fraction)
                        .height(7.dp)
                        .background(
                            if (item.low) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            RoundedCornerShape(50)
                        )
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.quantity} ${item.unit}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onAdjust(-10) }) {
                    Icon(Icons.Filled.Remove, contentDescription = "Use 10")
                }
                Text(text = "10", style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = { onAdjust(10) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add 10")
                }
                Spacer(Modifier.width(4.dp))
                TextButton(onClick = onRestock) { Text("Restock") }
            }
        }
    }
}
