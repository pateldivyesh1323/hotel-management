package com.example.hotel_booking_app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hotel_booking_app.data.GuestProfile
import com.example.hotel_booking_app.data.HotelRepository
import com.example.hotel_booking_app.data.ThemeMode
import com.example.hotel_booking_app.data.TripStage
import com.example.hotel_booking_app.ui.components.GuestAvatar
import com.example.hotel_booking_app.ui.components.PanelCard
import com.example.hotel_booking_app.ui.components.SectionHeader

@Composable
fun ProfileScreen(
    repo: HotelRepository,
    onMessage: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf(repo.profile.name) }
    var email by rememberSaveable { mutableStateOf(repo.profile.email) }
    var phone by rememberSaveable { mutableStateOf(repo.profile.phone) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GuestAvatar(name = repo.profile.name, size = 56)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = repo.profile.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${repo.trips(TripStage.UPCOMING).size} upcoming · " +
                            "${repo.trips(TripStage.PAST).size} past stays",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { SectionHeader("Guest details") }

        item {
            PanelCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name"),
                        label = { Text("Full name") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_email"),
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_phone"),
                        label = { Text("Phone") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onMessage(
                                repo.saveProfile(GuestProfile(name, email, phone)) ?: "Details saved"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_save")
                    ) { Text("Save details") }
                }
            }
        }

        item { SectionHeader("Settings") }

        item { AppearanceCard(mode = repo.themeMode, onSelect = repo::setThemeMode) }

        item {
            OutlinedButton(
                onClick = {
                    repo.resetToSample()
                    name = repo.profile.name
                    email = repo.profile.email
                    phone = repo.profile.phone
                    onMessage("Bookings reset to the sample data")
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Reset demo data") }
        }
    }
}

@Composable
private fun AppearanceCard(
    mode: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    PanelCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(text = "Appearance", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { entry ->
                    FilterChip(
                        selected = mode == entry,
                        onClick = { onSelect(entry) },
                        label = { Text(entry.label) }
                    )
                }
            }
        }
    }
}
