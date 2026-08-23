package com.example.hotel_management_app

import com.example.hotel_management_app.data.ActivityKind
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.data.TaskArea
import com.example.hotel_management_app.data.TaskPriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Exercises the tasks, inventory, messaging and activity feed added around the desk. */
class OperationsTest {

    private val today: LocalDate = LocalDate.of(2026, 8, 14)

    private fun repo() = HotelRepository(storage = null, today = { today })

    @Test
    fun `open tasks exclude the completed ones and lead with the earliest due`() {
        val repo = repo()
        val open = repo.openTasks()

        assertTrue(open.none { it.done })
        assertEquals(open.map { it.due }.sorted(), open.map { it.due })
    }

    @Test
    fun `toggling a task moves it out of the open list and logs it`() {
        val repo = repo()
        val task = repo.openTasks().first()

        repo.toggleTask(task.id)

        assertTrue(repo.openTasks().none { it.id == task.id })
        assertEquals(ActivityKind.HOUSEKEEPING, repo.activity.first().kind)
        assertEquals(task.title, repo.activity.first().detail)
    }

    @Test
    fun `a task needs a title`() {
        val repo = repo()
        val before = repo.tasks.size

        assertNotNull(repo.addTask("  ", today, TaskArea.FRONT_DESK, TaskPriority.NORMAL))
        assertEquals(before, repo.tasks.size)
    }

    @Test
    fun `adding a task puts it on the open list`() {
        val repo = repo()

        assertNull(repo.addTask("Polish the lobby", today, TaskArea.HOUSEKEEPING, TaskPriority.HIGH))
        assertTrue(repo.openTasks().any { it.title == "Polish the lobby" })
    }

    @Test
    fun `stock cannot be driven below zero`() {
        val repo = repo()
        val item = repo.inventory.first()

        repo.adjustStock(item.id, -(item.quantity + 500))

        assertEquals(0, repo.inventory.first { it.id == item.id }.quantity)
    }

    @Test
    fun `crossing the reorder level raises a low-stock activity`() {
        val repo = repo()
        val item = repo.inventory.first { !it.low }

        repo.adjustStock(item.id, -(item.quantity - item.threshold))

        assertEquals(ActivityKind.INVENTORY, repo.activity.first().kind)
        assertTrue(repo.lowStock().any { it.id == item.id })
    }

    @Test
    fun `restocking lifts an item back above its reorder level`() {
        val repo = repo()
        val item = repo.inventory.first { it.low }

        repo.restock(item.id)

        assertFalse(repo.inventory.first { it.id == item.id }.low)
    }

    @Test
    fun `replying marks the thread read and appends the message`() {
        val repo = repo()
        val thread = repo.threads.first { it.unread }
        val before = thread.messages.size

        repo.reply(thread.id, "  On its way  ")

        val updated = repo.thread(thread.id)!!
        assertFalse(updated.unread)
        assertEquals(before + 1, updated.messages.size)
        assertEquals("On its way", updated.messages.last().text)
        assertFalse(updated.messages.last().fromGuest)
    }

    @Test
    fun `an empty reply is ignored`() {
        val repo = repo()
        val thread = repo.threads.first()

        repo.reply(thread.id, "   ")

        assertEquals(thread.messages.size, repo.thread(thread.id)!!.messages.size)
    }

    @Test
    fun `checking a guest in is recorded in the activity feed`() {
        val repo = repo()
        val arrival = repo.arrivalsToday().first()

        assertNull(repo.checkIn(arrival.id))

        val logged = repo.activity.first()
        assertEquals(ActivityKind.CHECK_IN, logged.kind)
        assertTrue(logged.detail.contains(arrival.guestName))
    }

    @Test
    fun `the availability mix accounts for every room`() {
        val repo = repo()

        assertEquals(
            repo.rooms.size.toFloat(),
            repo.availabilityMix()
                .filter { it.label != "Reserved" }
                .sumOf { it.value.toDouble() }
                .toFloat(),
            0.01f
        )
    }
}
