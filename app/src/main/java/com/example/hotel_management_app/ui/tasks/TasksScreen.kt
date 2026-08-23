package com.example.hotel_management_app.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.data.HotelTask
import com.example.hotel_management_app.data.TaskArea
import com.example.hotel_management_app.data.TaskPriority
import com.example.hotel_management_app.ui.components.EmptyHint
import com.example.hotel_management_app.ui.components.PanelCard
import com.example.hotel_management_app.ui.components.PanelSection
import com.example.hotel_management_app.ui.components.SectionHeader
import com.example.hotel_management_app.ui.components.StatusPill
import com.example.hotel_management_app.ui.dueLabel
import com.example.hotel_management_app.ui.theme.tone
import java.time.LocalDate

/**
 * The shift's to-do list. Tasks are grouped by whether they are still owed, because that
 * is the only distinction that matters when a shift starts.
 */
@Composable
fun TasksScreen(
    repo: HotelRepository,
    onMessage: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val today = repo.currentDate()
    var area by rememberSaveable { mutableStateOf<String?>(null) }
    var composing by rememberSaveable { mutableStateOf(false) }

    val filtered = repo.tasks.filter { area == null || it.area.name == area }
    val open = filtered.filter { !it.done }.sortedWith(compareBy({ it.due }, { it.priority.ordinal }))
    val done = filtered.filter { it.done }.sortedByDescending { it.due }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Open work",
                trailing = {
                    IconButton(onClick = { composing = !composing }) {
                        Icon(Icons.Filled.Add, contentDescription = "New task")
                    }
                }
            )
        }

        if (composing) {
            item {
                NewTaskForm(
                    today = today,
                    onCancel = { composing = false },
                    onCreate = { title, due, taskArea, priority ->
                        val error = repo.addTask(title, due, taskArea, priority)
                        if (error != null) {
                            onMessage(error)
                        } else {
                            composing = false
                            onMessage("Task added")
                        }
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
                    selected = area == null,
                    onClick = { area = null },
                    label = { Text("All") }
                )
                TaskArea.entries.forEach { entry ->
                    FilterChip(
                        selected = area == entry.name,
                        onClick = { area = if (area == entry.name) null else entry.name },
                        label = { Text(entry.label) }
                    )
                }
            }
        }

        item { SectionHeader("Outstanding (${open.size})") }
        if (open.isEmpty()) {
            item { EmptyHint("Nothing outstanding in this area") }
        } else {
            items(open, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    dueText = dueLabel(task.due, today),
                    overdue = task.due.isBefore(today),
                    onToggle = { repo.toggleTask(task.id) },
                    onDelete = { repo.deleteTask(task.id) }
                )
            }
        }

        if (done.isNotEmpty()) {
            item { SectionHeader("Completed (${done.size})") }
            items(done, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    dueText = dueLabel(task.due, today),
                    overdue = false,
                    onToggle = { repo.toggleTask(task.id) },
                    onDelete = { repo.deleteTask(task.id) }
                )
            }
        }
    }
}

@Composable
private fun NewTaskForm(
    today: LocalDate,
    onCancel: () -> Unit,
    onCreate: (String, LocalDate, TaskArea, TaskPriority) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by rememberSaveable { mutableStateOf("") }
    var days by rememberSaveable { mutableStateOf(0) }
    var area by rememberSaveable { mutableStateOf(TaskArea.FRONT_DESK.name) }
    var priority by rememberSaveable { mutableStateOf(TaskPriority.NORMAL.name) }

    PanelSection(title = "New task", modifier = modifier) {
        Column {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("What needs doing?") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )
            Spacer(Modifier.height(10.dp))
            Text("Due", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0 to "Today", 1 to "Tomorrow", 3 to "In 3 days").forEach { (offset, label) ->
                    FilterChip(
                        selected = days == offset,
                        onClick = { days = offset },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Area", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskArea.entries.forEach { entry ->
                    FilterChip(
                        selected = area == entry.name,
                        onClick = { area = entry.name },
                        label = { Text(entry.label) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Priority", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskPriority.entries.forEach { entry ->
                    FilterChip(
                        selected = priority == entry.name,
                        onClick = { priority = entry.name },
                        label = { Text(entry.label) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onCreate(
                            title,
                            today.plusDays(days.toLong()),
                            TaskArea.valueOf(area),
                            TaskPriority.valueOf(priority)
                        )
                        title = ""
                    }
                ) { Text("Add task") }
                TextButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: HotelTask,
    dueText: String,
    overdue: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    PanelCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        if (task.done) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        RoundedCornerShape(7.dp)
                    )
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                if (task.done) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.done) TextDecoration.LineThrough else null
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusPill(label = task.priority.label, tone = task.priority.tone())
                    Text(
                        text = "${task.area.label} · ${if (overdue) "Overdue — $dueText" else dueText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (overdue) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete task",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
