package com.example.hotel_management_app.ui.messages

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.hotel_management_app.data.ChatMessage
import com.example.hotel_management_app.data.HotelRepository
import com.example.hotel_management_app.data.MessageThread
import com.example.hotel_management_app.ui.clockTime
import com.example.hotel_management_app.ui.components.EmptyHint
import com.example.hotel_management_app.ui.components.GuestAvatar
import com.example.hotel_management_app.ui.components.PanelCard
import com.example.hotel_management_app.ui.components.SectionHeader

/** Guest conversations, newest reply first. */
@Composable
fun MessagesScreen(
    repo: HotelRepository,
    onOpenThread: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val threads = repo.threads.sortedWith(
        compareByDescending<MessageThread> { it.unread }.thenByDescending { it.lastAt }
    )

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionHeader(
                title = "Conversations",
                trailing = {
                    Text(
                        text = "${repo.unreadMessages()} unread",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
        if (threads.isEmpty()) {
            item { EmptyHint("No guest conversations yet") }
        } else {
            items(threads, key = { it.id }) { thread ->
                ThreadRow(thread = thread, onClick = { onOpenThread(thread.id) })
            }
        }
    }
}

@Composable
private fun ThreadRow(
    thread: MessageThread,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PanelCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GuestAvatar(thread.guestName)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = thread.guestName,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Room ${thread.roomNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = thread.preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                thread.lastAt?.let {
                    Text(
                        text = it.clockTime(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (thread.unread) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier
                            .size(9.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            }
        }
    }
}

/** One conversation, with a reply box that appends to the thread. */
@Composable
fun ThreadScreen(
    repo: HotelRepository,
    threadId: String,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val thread = repo.thread(threadId)
    var draft by rememberSaveable(threadId) { mutableStateOf("") }

    LaunchedEffect(threadId) { repo.markThreadRead(threadId) }

    if (thread == null) {
        EmptyHint("That conversation is no longer available", modifier)
        return
    }

    Column(modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GuestAvatar(thread.guestName)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = thread.guestName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Room ${thread.roomNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(thread.messages) { message -> MessageBubble(message) }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Reply to ${thread.guestName}") },
                shape = MaterialTheme.shapes.large,
                maxLines = 3
            )
            IconButton(
                onClick = {
                    repo.reply(threadId, draft)
                    draft = ""
                },
                enabled = draft.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send reply")
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, modifier: Modifier = Modifier) {
    val container = if (message.fromGuest) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val content = if (message.fromGuest) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.fromGuest) Arrangement.Start else Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .background(
                    container,
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (message.fromGuest) 4.dp else 18.dp,
                        bottomEnd = if (message.fromGuest) 18.dp else 4.dp
                    )
                )
                .padding(12.dp)
        ) {
            Text(text = message.text, style = MaterialTheme.typography.bodyMedium, color = content)
            Spacer(Modifier.height(4.dp))
            Text(
                text = message.at.clockTime(),
                style = MaterialTheme.typography.labelSmall,
                color = content.copy(alpha = 0.65f)
            )
        }
    }
}
