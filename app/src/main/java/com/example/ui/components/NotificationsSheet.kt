package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    notifications: List<NotificationEntity>,
    onDismiss: () -> Unit,
    onMarkRead: (String) -> Unit,
<<<<<<< HEAD
    onMarkAllRead: () -> Unit,
    onViewFullHistory: () -> Unit = {}
=======
    onMarkAllRead: () -> Unit
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MedSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications (${notifications.size})",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
<<<<<<< HEAD
                Row {
                    TextButton(onClick = {
                        onDismiss()
                        onViewFullHistory()
                    }) {
                        Text("Audit Logs", color = MedBluePrimary, fontWeight = FontWeight.Bold)
                    }
                    if (notifications.any { !it.isRead }) {
                        TextButton(onClick = onMarkAllRead) {
                            Text("Mark all read", color = MedBluePrimary)
                        }
=======
                if (notifications.any { !it.isRead }) {
                    TextButton(onClick = onMarkAllRead) {
                        Text("Mark all read", color = MedBluePrimary)
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                    }
                }
            }

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = MedTextTertiary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Notifications", fontWeight = FontWeight.Bold)
                        Text("You have no unread updates at this time.", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MedBorder, RoundedCornerShape(12.dp))
                                .clickable { onMarkRead(notif.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (notif.isRead) MedSurface else MedBlueLight.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (notif.type) {
                                                "MEDICINE" -> MedSuccessLight
                                                "APPOINTMENT" -> MedBlueLight
                                                else -> MedWarningLight
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (notif.type) {
                                            "MEDICINE" -> Icons.Default.Medication
                                            "APPOINTMENT" -> Icons.Default.CalendarToday
                                            else -> Icons.Default.Notifications
                                        },
                                        contentDescription = null,
                                        tint = when (notif.type) {
                                            "MEDICINE" -> MedSuccess
                                            "APPOINTMENT" -> MedBluePrimary
                                            else -> MedWarning
                                        },
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = notif.title,
                                        fontWeight = if (notif.isRead) FontWeight.Normal else FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = notif.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MedTextSecondary
                                    )
                                    val timeStr = SimpleDateFormat("MMM dd • hh:mm a", Locale.getDefault()).format(Date(notif.timestamp))
                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MedTextTertiary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
