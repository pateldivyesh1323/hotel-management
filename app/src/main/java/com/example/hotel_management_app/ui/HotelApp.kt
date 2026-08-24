package com.example.hotel_management_app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.unit.dp
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.ui.bookings.BookingDetailScreen
import com.example.hotel_management_app.ui.bookings.BookingsScreen
import com.example.hotel_management_app.ui.bookings.NewBookingScreen
import com.example.hotel_management_app.ui.calendar.CalendarScreen
import com.example.hotel_management_app.ui.dashboard.DashboardScreen
import com.example.hotel_management_app.ui.financials.FinancialsScreen
import com.example.hotel_management_app.ui.guests.GuestsScreen
import com.example.hotel_management_app.ui.housekeeping.HousekeepingScreen
import com.example.hotel_management_app.ui.inventory.InventoryScreen
import com.example.hotel_management_app.ui.messages.MessagesScreen
import com.example.hotel_management_app.ui.messages.ThreadScreen
import com.example.hotel_management_app.ui.more.OperationsScreen
import com.example.hotel_management_app.ui.reviews.ReviewsScreen
import com.example.hotel_management_app.ui.rooms.RoomDetailScreen
import com.example.hotel_management_app.ui.rooms.RoomsScreen
import com.example.hotel_management_app.ui.tasks.TasksScreen

private enum class Tab(val label: String, val icon: ImageVector) {
    DASHBOARD("Home", Icons.Filled.Home),
    BOOKINGS("Bookings", Icons.Filled.CalendarMonth),
    ROOMS("Rooms", Icons.Filled.MeetingRoom),
    GUESTS("Guests", Icons.Filled.People),
    OPERATIONS("More", Icons.Filled.GridView)
}

/**
 * Routes are encoded as strings so the whole back stack survives configuration changes
 * without pulling in a navigation library.
 */
private const val ROUTE_MAIN = "main"
private const val ROUTE_ROOM = "room:"
private const val ROUTE_BOOKING = "booking:"
private const val ROUTE_NEW_BOOKING = "new:"
private const val ROUTE_THREAD = "thread:"
private const val ROUTE_HOUSEKEEPING = "housekeeping"
private const val ROUTE_TASKS = "tasks"
private const val ROUTE_MESSAGES = "messages"
private const val ROUTE_INVENTORY = "inventory"
private const val ROUTE_CALENDAR = "calendar"
private const val ROUTE_FINANCIALS = "financials"
private const val ROUTE_REVIEWS = "reviews"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelApp(repo: HotelRepository, modifier: Modifier = Modifier) {
    var tab by rememberSaveable { mutableStateOf(Tab.DASHBOARD.name) }
    var backStack by rememberSaveable { mutableStateOf(listOf(ROUTE_MAIN)) }
    var message by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val route = backStack.last()
    val onMain = route == ROUTE_MAIN
    val currentTab = Tab.valueOf(tab)

    fun push(newRoute: String) {
        backStack = backStack + newRoute
    }

    fun pop() {
        if (backStack.size > 1) backStack = backStack.dropLast(1)
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
                        IconButton(onClick = { pop() }) {
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
                        val badge = when (entry) {
                            Tab.OPERATIONS -> repo.unreadMessages() + repo.lowStock().size
                            else -> 0
                        }
                        NavigationBarItem(
                            selected = entry == currentTab,
                            onClick = { tab = entry.name },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (badge > 0) Badge { Text(badge.toString()) }
                                    }
                                ) {
                                    Icon(entry.icon, contentDescription = entry.label)
                                }
                            },
                            label = { Text(entry.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (onMain && currentTab != Tab.OPERATIONS) {
                ExtendedFloatingActionButton(
                    onClick = { push(ROUTE_NEW_BOOKING) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("New booking") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { scaffoldPadding ->
        // Screens scroll under the bars, so the horizontal inset becomes a gutter and the
        // vertical inset is handled by each list's content padding.
        val padding = screenPadding(scaffoldPadding)

        when {
            route.startsWith(ROUTE_ROOM) -> RoomDetailScreen(
                repo = repo,
                roomId = route.removePrefix(ROUTE_ROOM),
                onOpenBooking = { push("$ROUTE_BOOKING$it") },
                onBookRoom = { push("$ROUTE_NEW_BOOKING$it") },
                onMessage = { message = it },
                contentPadding = padding
            )

            route.startsWith(ROUTE_BOOKING) -> BookingDetailScreen(
                repo = repo,
                bookingId = route.removePrefix(ROUTE_BOOKING),
                onOpenRoom = { push("$ROUTE_ROOM$it") },
                onMessage = { message = it },
                contentPadding = padding
            )

            route.startsWith(ROUTE_NEW_BOOKING) -> NewBookingScreen(
                repo = repo,
                presetRoomId = route.removePrefix(ROUTE_NEW_BOOKING).ifBlank { null },
                onSaved = { confirmation ->
                    message = confirmation
                    backStack = listOf(ROUTE_MAIN)
                    tab = Tab.BOOKINGS.name
                },
                contentPadding = padding
            )

            route.startsWith(ROUTE_THREAD) -> ThreadScreen(
                repo = repo,
                threadId = route.removePrefix(ROUTE_THREAD),
                contentPadding = padding
            )

            route == ROUTE_HOUSEKEEPING -> HousekeepingScreen(
                repo = repo,
                onOpenRoom = { push("$ROUTE_ROOM$it") },
                onMessage = { message = it },
                contentPadding = padding
            )

            route == ROUTE_TASKS -> TasksScreen(
                repo = repo,
                onMessage = { message = it },
                contentPadding = padding
            )

            route == ROUTE_MESSAGES -> MessagesScreen(
                repo = repo,
                onOpenThread = { push("$ROUTE_THREAD$it") },
                contentPadding = padding
            )

            route == ROUTE_INVENTORY -> InventoryScreen(repo = repo, contentPadding = padding)

            route == ROUTE_CALENDAR -> CalendarScreen(
                repo = repo,
                onOpenBooking = { push("$ROUTE_BOOKING$it") },
                contentPadding = padding
            )

            route == ROUTE_FINANCIALS -> FinancialsScreen(repo = repo, contentPadding = padding)

            route == ROUTE_REVIEWS -> ReviewsScreen(repo = repo, contentPadding = padding)

            else -> when (currentTab) {
                Tab.DASHBOARD -> DashboardScreen(
                    repo = repo,
                    onOpenBooking = { push("$ROUTE_BOOKING$it") },
                    onOpenRoom = { push("$ROUTE_ROOM$it") },
                    onOpenTasks = { push(ROUTE_TASKS) },
                    onOpenMessages = { push(ROUTE_MESSAGES) },
                    onOpenReviews = { push(ROUTE_REVIEWS) },
                    onOpenBookings = { tab = Tab.BOOKINGS.name },
                    onOpenRooms = { tab = Tab.ROOMS.name },
                    onMessage = { message = it },
                    contentPadding = padding
                )

                Tab.ROOMS -> RoomsScreen(
                    repo = repo,
                    onOpenRoom = { push("$ROUTE_ROOM$it") },
                    contentPadding = padding
                )

                Tab.BOOKINGS -> BookingsScreen(
                    repo = repo,
                    onOpenBooking = { push("$ROUTE_BOOKING$it") },
                    contentPadding = padding
                )

                Tab.GUESTS -> GuestsScreen(
                    repo = repo,
                    onOpenBooking = { push("$ROUTE_BOOKING$it") },
                    contentPadding = padding
                )

                Tab.OPERATIONS -> OperationsScreen(
                    repo = repo,
                    onOpenHousekeeping = { push(ROUTE_HOUSEKEEPING) },
                    onOpenTasks = { push(ROUTE_TASKS) },
                    onOpenMessages = { push(ROUTE_MESSAGES) },
                    onOpenInventory = { push(ROUTE_INVENTORY) },
                    onOpenCalendar = { push(ROUTE_CALENDAR) },
                    onOpenFinancials = { push(ROUTE_FINANCIALS) },
                    onOpenReviews = { push(ROUTE_REVIEWS) },
                    onMessage = { message = it },
                    contentPadding = padding
                )
            }
        }
    }
}

private fun titleFor(route: String): String = when {
    route.startsWith(ROUTE_ROOM) -> "Room"
    route.startsWith(ROUTE_BOOKING) -> "Booking"
    route.startsWith(ROUTE_NEW_BOOKING) -> "New booking"
    route.startsWith(ROUTE_THREAD) -> "Conversation"
    route == ROUTE_HOUSEKEEPING -> "Housekeeping"
    route == ROUTE_TASKS -> "Tasks"
    route == ROUTE_MESSAGES -> "Messages"
    route == ROUTE_INVENTORY -> "Inventory"
    route == ROUTE_CALENDAR -> "Calendar"
    route == ROUTE_FINANCIALS -> "Financials"
    route == ROUTE_REVIEWS -> "Reviews"
    else -> "Front desk"
}

/** Adds a comfortable page gutter on top of the scaffold's own insets. */
@Composable
private fun screenPadding(scaffold: PaddingValues): PaddingValues {
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = scaffold.calculateStartPadding(direction) + 16.dp,
        end = scaffold.calculateEndPadding(direction) + 16.dp,
        top = scaffold.calculateTopPadding() + 12.dp,
        bottom = scaffold.calculateBottomPadding() + 96.dp
    )
}
