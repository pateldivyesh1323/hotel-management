package com.example.hotel_booking_app.ui.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hotel_booking_app.R
import com.example.hotel_booking_app.data.StayRequest
import com.example.hotel_booking_app.ui.components.PanelCard
import com.example.hotel_booking_app.ui.components.StayPicker
import java.time.LocalDate

@Composable
fun HomeScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    stay: StayRequest,
    today: LocalDate,
    onStayChange: (StayRequest) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.hotel_lobby),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0x99000000),
                        0.45f to Color(0x66000000),
                        1f to Color(0xCC000000)
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "The Ultimate Hotel Experience",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier
                    .background(Color(0x992A78D6), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Discover Your Perfect Getaway Destination",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Unparalleled luxury and comfort await at the world's most exclusive hotels and resorts. Start your journey today.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(24.dp))
            PanelCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("destination_field"),
                        label = { Text("Destination") },
                        placeholder = { Text("City or hotel name") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                                }
                            }
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    StayPicker(stay = stay, today = today, onChange = onStayChange)
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onSearch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("search_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = null)
                        Text(text = "  Search hotels", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
