package com.example.hotel_management_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.data.HotelStorage
import com.example.hotel_management_app.data.ThemeMode
import com.example.hotel_management_app.ui.HotelApp
import com.example.hotel_management_app.ui.theme.Hotel_Management_AppTheme

class MainActivity : ComponentActivity() {

    /**
     * The property state is small and entirely local, so the repository lives with the
     * activity and persists itself to disk on every change.
     */
    private val repository by lazy { HotelRepository(HotelStorage(applicationContext)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkTheme = when (repository.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            Hotel_Management_AppTheme(darkTheme = darkTheme) {
                HotelApp(repo = repository, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
