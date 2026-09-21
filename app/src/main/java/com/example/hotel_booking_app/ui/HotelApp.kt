package com.example.hotel_booking_app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.hotel_booking_app.data.HotelRepository
import com.example.hotel_booking_app.data.StayRequest
import com.example.hotel_booking_app.ui.booking.CheckoutScreen
import com.example.hotel_booking_app.ui.explore.ExploreScreen
import com.example.hotel_booking_app.ui.explore.HotelDetailScreen
import com.example.hotel_booking_app.ui.profile.ProfileScreen
import com.example.hotel_booking_app.ui.saved.SavedScreen
import com.example.hotel_booking_app.ui.trips.TripDetailScreen
import com.example.hotel_booking_app.ui.trips.TripsScreen
import java.time.LocalDate

private enum class Tab(val label: String, val icon: ImageVector) {
    EXPLORE("Explore", Icons.Filled.Search),
    SAVED("Saved", Icons.Filled.Favorite),
    TRIPS("Trips", Icons.Filled.Luggage),
    PROFILE("Profile", Icons.Filled.Person)
}

private const val ROUTE_MAIN = "main"
private const val ROUTE_HOTEL = "hotel:"
private const val ROUTE_CHECKOUT = "checkout:"
private const val ROUTE_TRIP = "trip:"

private const val DEFAULT_LEAD_DAYS = 7L
private const val DEFAULT_NIGHTS = 2L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelApp(repo: HotelRepository, modifier: Modifier = Modifier) {
    var tab by rememberSaveable { mutableStateOf(Tab.EXPLORE.name) }
    var backStack by rememberSaveable { mutableStateOf(listOf(ROUTE_MAIN)) }
    var query by rememberSaveable { mutableStateOf("") }
    var checkInDay by rememberSaveable {
        mutableStateOf(repo.currentDate().plusDays(DEFAULT_LEAD_DAYS).toEpochDay())
    }
    var checkOutDay by rememberSaveable {
        mutableStateOf(repo.currentDate().plusDays(DEFAULT_LEAD_DAYS + DEFAULT_NIGHTS).toEpochDay())
    }
    var guests by rememberSaveable { mutableStateOf(2) }
    var message by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val stay = StayRequest(LocalDate.ofEpochDay(checkInDay), LocalDate.ofEpochDay(checkOutDay), guests)
    val route = backStack.last()
    val onMain = route == ROUTE_MAIN
    val currentTab = Tab.valueOf(tab)

    fun push(newRoute: String) {
        backStack = backStack + newRoute
    }

    fun pop() {
        if (backStack.size > 1) backStack = backStack.dropLast(1)
    }

    fun changeStay(updated: StayRequest) {
        checkInDay = updated.checkIn.toEpochDay()
        checkOutDay = updated.checkOut.toEpochDay()
        guests = updated.guests
    }

    BackHandler(enabled = !onMain) { pop() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            message = null
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (!onMain) {
                TopAppBar(
                    title = { Text(titleFor(route)) },
                    navigationIcon = {
                        IconButton(onClick = { pop() }, modifier = Modifier.testTag("back")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (onMain) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Tab.entries.forEach { entry ->
                        NavigationBarItem(
                            selected = entry == currentTab,
                            onClick = { tab = entry.name },
                            icon = { Icon(entry.icon, contentDescription = entry.label) },
                            label = { Text(entry.label) },
                            modifier = Modifier.testTag("tab_${entry.name}"),
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { scaffoldPadding ->
        val padding = screenPadding(scaffoldPadding)

        when {
            route.startsWith(ROUTE_HOTEL) -> HotelDetailScreen(
                repo = repo,
                hotelId = route.removePrefix(ROUTE_HOTEL),
                stay = stay,
                onStayChange = ::changeStay,
                onBookRoom = { push("$ROUTE_CHECKOUT$it") },
                contentPadding = padding
            )

            route.startsWith(ROUTE_CHECKOUT) -> CheckoutScreen(
                repo = repo,
                offerId = route.removePrefix(ROUTE_CHECKOUT),
                stay = stay,
                onBooked = { bookingId ->
                    message = "Booking confirmed"
                    tab = Tab.TRIPS.name
                    backStack = listOf(ROUTE_MAIN, "$ROUTE_TRIP$bookingId")
                },
                contentPadding = padding
            )

            route.startsWith(ROUTE_TRIP) -> TripDetailScreen(
                repo = repo,
                bookingId = route.removePrefix(ROUTE_TRIP),
                onOpenHotel = { push("$ROUTE_HOTEL$it") },
                onMessage = { message = it },
                contentPadding = padding
            )

            else -> when (currentTab) {
                Tab.EXPLORE -> ExploreScreen(
                    repo = repo,
                    query = query,
                    onQueryChange = { query = it },
                    stay = stay,
                    onStayChange = ::changeStay,
                    onOpenHotel = { push("$ROUTE_HOTEL$it") },
                    contentPadding = padding
                )

                Tab.SAVED -> SavedScreen(
                    repo = repo,
                    stay = stay,
                    onOpenHotel = { push("$ROUTE_HOTEL$it") },
                    contentPadding = padding
                )

                Tab.TRIPS -> TripsScreen(
                    repo = repo,
                    onOpenTrip = { push("$ROUTE_TRIP$it") },
                    contentPadding = padding
                )

                Tab.PROFILE -> ProfileScreen(
                    repo = repo,
                    onMessage = { message = it },
                    contentPadding = padding
                )
            }
        }
    }
}

private fun titleFor(route: String): String = when {
    route.startsWith(ROUTE_HOTEL) -> "Hotel"
    route.startsWith(ROUTE_CHECKOUT) -> "Confirm booking"
    route.startsWith(ROUTE_TRIP) -> "Booking"
    else -> "Hotel Booking"
}

@Composable
private fun screenPadding(scaffold: PaddingValues): PaddingValues {
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = scaffold.calculateStartPadding(direction) + 16.dp,
        end = scaffold.calculateEndPadding(direction) + 16.dp,
        top = scaffold.calculateTopPadding() + 12.dp,
        bottom = scaffold.calculateBottomPadding() + 24.dp
    )
}
