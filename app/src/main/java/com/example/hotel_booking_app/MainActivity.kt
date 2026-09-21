package com.example.hotel_booking_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.hotel_booking_app.data.HotelRepository
import com.example.hotel_booking_app.data.HotelStorage
import com.example.hotel_booking_app.data.ThemeMode
import com.example.hotel_booking_app.ui.HotelApp
import com.example.hotel_booking_app.ui.theme.Hotel_Booking_AppTheme
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    private val repository by lazy { HotelRepository(HotelStorage(applicationContext), LocalDate::now) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkTheme = when (repository.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            Hotel_Booking_AppTheme(darkTheme = darkTheme) {
                HotelApp(repo = repository, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
