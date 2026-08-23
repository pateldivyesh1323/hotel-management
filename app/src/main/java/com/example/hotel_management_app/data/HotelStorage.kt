package com.example.hotel_management_app.data

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Everything the app writes back to disk between launches. */
data class StoredState(
    val rooms: List<Room>,
    val bookings: List<Booking>,
    val tasks: List<HotelTask>,
    val inventory: List<InventoryItem>
)

/**
 * Local persistence for the property. The data set is small (one hotel's rooms, bookings,
 * task list and stock), so it is serialised to JSON in SharedPreferences rather than a
 * database.
 */
class HotelStorage(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("hotel_state", Context.MODE_PRIVATE)

    fun save(
        rooms: List<Room>,
        bookings: List<Booking>,
        tasks: List<HotelTask>,
        inventory: List<InventoryItem>
    ) {
        val root = JSONObject().apply {
            put("rooms", rooms.toArray { it.toJson() })
            put("bookings", bookings.toArray { it.toJson() })
            put("tasks", tasks.toArray { it.toJson() })
            put("inventory", inventory.toArray { it.toJson() })
        }
        prefs.edit().putString(KEY_STATE, root.toString()).apply()
    }

    /** Returns null on a first run, or when the stored payload can no longer be read. */
    fun load(): StoredState? {
        val raw = prefs.getString(KEY_STATE, null) ?: return null
        return runCatching {
            val root = JSONObject(raw)
            StoredState(
                rooms = root.getJSONArray("rooms").map { it.toRoom() },
                bookings = root.getJSONArray("bookings").map { it.toBooking() },
                tasks = root.optJSONArray("tasks")?.map { it.toTask() }.orEmpty(),
                inventory = root.optJSONArray("inventory")?.map { it.toItem() }.orEmpty()
            )
        }.onFailure {
            Log.w(TAG, "Discarding unreadable saved state", it)
            prefs.edit().remove(KEY_STATE).apply()
        }.getOrNull()
    }

    private inline fun <T> List<T>.toArray(transform: (T) -> JSONObject): JSONArray =
        JSONArray().also { array -> forEach { array.put(transform(it)) } }

    private inline fun <T> JSONArray.map(transform: (JSONObject) -> T): List<T> =
        (0 until length()).map { transform(getJSONObject(it)) }

    private fun Room.toJson() = JSONObject().apply {
        put("id", id)
        put("number", number)
        put("floor", floor)
        put("type", type.name)
        put("status", status.name)
        put("notes", notes)
    }

    private fun JSONObject.toRoom() = Room(
        id = getString("id"),
        number = getString("number"),
        floor = getInt("floor"),
        type = RoomType.valueOf(getString("type")),
        status = RoomStatus.valueOf(getString("status")),
        notes = optString("notes")
    )

    private fun Booking.toJson() = JSONObject().apply {
        put("id", id)
        put("roomId", roomId)
        put("guestName", guestName)
        put("guestPhone", guestPhone)
        put("guestEmail", guestEmail)
        put("checkIn", checkIn.toString())
        put("checkOut", checkOut.toString())
        put("guests", guests)
        put("status", status.name)
        put("channel", channel.name)
        put("notes", notes)
    }

    private fun JSONObject.toBooking() = Booking(
        id = getString("id"),
        roomId = getString("roomId"),
        guestName = getString("guestName"),
        guestPhone = optString("guestPhone"),
        guestEmail = optString("guestEmail"),
        checkIn = LocalDate.parse(getString("checkIn")),
        checkOut = LocalDate.parse(getString("checkOut")),
        guests = optInt("guests", 1),
        status = BookingStatus.valueOf(getString("status")),
        channel = optString("channel").toEnum(BookingChannel.DIRECT),
        notes = optString("notes")
    )

    private fun HotelTask.toJson() = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("due", due.toString())
        put("area", area.name)
        put("priority", priority.name)
        put("done", done)
    }

    private fun JSONObject.toTask() = HotelTask(
        id = getString("id"),
        title = getString("title"),
        due = LocalDate.parse(getString("due")),
        area = optString("area").toEnum(TaskArea.FRONT_DESK),
        priority = optString("priority").toEnum(TaskPriority.NORMAL),
        done = optBoolean("done")
    )

    private fun InventoryItem.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("category", category)
        put("quantity", quantity)
        put("threshold", threshold)
        put("unit", unit)
    }

    private fun JSONObject.toItem() = InventoryItem(
        id = getString("id"),
        name = getString("name"),
        category = optString("category"),
        quantity = optInt("quantity"),
        threshold = optInt("threshold"),
        unit = optString("unit")
    )

    /** Falls back to [fallback] so a value written by an older build never crashes a load. */
    private inline fun <reified T : Enum<T>> String?.toEnum(fallback: T): T =
        runCatching { enumValueOf<T>(this!!) }.getOrDefault(fallback)

    private companion object {
        const val TAG = "HotelStorage"
        const val KEY_STATE = "state_json"
    }
}
