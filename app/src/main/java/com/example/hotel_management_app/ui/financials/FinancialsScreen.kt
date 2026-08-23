package com.example.hotel_management_app.ui.financials

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hotel_management_app.ui.components.AreaChart
import com.example.hotel_management_app.ui.components.ChartLegend
import com.example.hotel_management_app.ui.components.DonutChart
import com.example.hotel_management_app.ui.components.MetricTile
import com.example.hotel_management_app.ui.components.PanelSection
import com.example.hotel_management_app.ui.components.ScoreBar
import com.example.hotel_management_app.ui.components.SectionHeader
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.ui.compactMoney
import com.example.hotel_management_app.ui.money
import com.example.hotel_management_app.ui.theme.chartPalette

/**
 * How the property is trading: the revenue curve, the rates behind it, and where the
 * money is coming from by room category and by channel.
 */
@Composable
fun FinancialsScreen(
    repo: HotelRepository,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val palette = chartPalette()
    val stats = repo.stats()
    val revenue = repo.revenueSeries(6)
    val byType = repo.revenueByRoomType()
    val channels = repo.channelMix()
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    val typeTotal = byType.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(
                    label = "Total revenue",
                    value = compactMoney(stats.totalRevenue.toFloat()),
                    icon = Icons.Filled.Payments,
                    trend = repo.revenueTrend(),
                    caption = "from last month",
                    highlighted = true,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Tonight",
                    value = money(stats.revenueToday),
                    icon = Icons.Filled.Nightlight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(
                    label = "Average daily rate",
                    value = money(repo.averageDailyRate()),
                    icon = Icons.Filled.PriceChange,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "RevPAR",
                    value = money(repo.revPar()),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            PanelSection(
                title = "Revenue",
                trailing = {
                    Text(
                        text = "Last 6 months",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            ) {
                Column {
                    val index = selectedMonth?.coerceIn(revenue.indices)
                    Text(
                        text = if (index != null) {
                            "${revenue[index].label}: ${money(revenue[index].value.toInt())}"
                        } else {
                            "Tap the chart for a month"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    AreaChart(
                        points = revenue,
                        selectedIndex = index,
                        onSelect = { selectedMonth = it },
                        valueLabel = { compactMoney(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            PanelSection(title = "Revenue by room type") {
                Column {
                    byType.forEach { slice ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ScoreBar(
                                label = slice.label,
                                score = slice.value,
                                max = typeTotal,
                                valueText = compactMoney(slice.value),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            PanelSection(title = "Revenue by platform") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DonutChart(
                        slices = channels,
                        colors = palette,
                        size = 132,
                        centerLabel = compactMoney(stats.totalRevenue.toFloat()),
                        centerCaption = "booked"
                    )
                    Spacer(Modifier.width(14.dp))
                    ChartLegend(
                        slices = channels,
                        colors = palette,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
