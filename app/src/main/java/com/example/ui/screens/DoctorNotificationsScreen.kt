package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

data class DoctorNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val category: String, // "APPOINTMENT", "MESSAGE", "LAB_REPORT", "PRESCRIPTION", "SYSTEM"
    val isRead: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorNotificationsScreen(
    viewModel: MedTimeViewModel,
    modifier: Modifier = Modifier
) {
    var notifications by remember {
        mutableStateOf(
            listOf(
                DoctorNotificationItem(
                    id = "n_1",
                    title = "New Appointment Booking",
                    message = "Maria Garcia booked a Cardiology consultation for today at 11:00 AM.",
                    timeAgo = "15 mins ago",
                    category = "APPOINTMENT",
                    isRead = false
                ),
                DoctorNotificationItem(
                    id = "n_2",
                    title = "New Patient Lab Report Uploaded",
                    message = "Robert Chen uploaded 'Comprehensive Metabolic Panel (CMP).pdf' for your review.",
                    timeAgo = "1 hour ago",
                    category = "LAB_REPORT",
                    isRead = false
                ),
                DoctorNotificationItem(
                    id = "n_3",
                    title = "Patient Clinical Message",
                    message = "Vijay Kumar sent a message: 'My BP is 120/80 this morning.'",
                    timeAgo = "3 hours ago",
                    category = "MESSAGE",
                    isRead = true
                ),
                DoctorNotificationItem(
                    id = "n_4",
                    title = "Prescription Refill Request",
                    message = "James Wilson requested an authorized refill for Atorvastatin 20mg.",
                    timeAgo = "Yesterday",
                    category = "PRESCRIPTION",
                    isRead = true
                ),
                DoctorNotificationItem(
                    id = "n_5",
                    title = "Medical License Verification Active",
                    message = "Your medical credentials have been verified by the MedTime Credentialing Board.",
                    timeAgo = "3 days ago",
                    category = "SYSTEM",
                    isRead = true
                )
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
            .testTag("doctor_notifications_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Clinical Notifications",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Text(
                        text = "Appointments, lab uploads & clinical inquiries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                }

                TextButton(
                    onClick = {
                        notifications = notifications.map { it.copy(isRead = true) }
                    }
                ) {
                    Text("Mark all read", color = MedBluePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        items(notifications, key = { it.id }) { notif ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(16.dp))
                    .clickable {
                        notifications = notifications.map {
                            if (it.id == notif.id) it.copy(isRead = true) else it
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (!notif.isRead) MedBlueLight.copy(alpha = 0.35f) else MedSurface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = when (notif.category) {
                            "APPOINTMENT" -> MedBlueLight
                            "LAB_REPORT" -> Color(0xFFE8F5E9)
                            "MESSAGE" -> Color(0xFFEDE7F6)
                            "PRESCRIPTION" -> Color(0xFFFFF3E0)
                            else -> MedBackground
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (notif.category) {
                                    "APPOINTMENT" -> Icons.Default.CalendarMonth
                                    "LAB_REPORT" -> Icons.Default.Science
                                    "MESSAGE" -> Icons.Default.Chat
                                    "PRESCRIPTION" -> Icons.Default.Medication
                                    else -> Icons.Default.Notifications
                                },
                                contentDescription = null,
                                tint = when (notif.category) {
                                    "APPOINTMENT" -> MedBluePrimary
                                    "LAB_REPORT" -> Color(0xFF2E7D32)
                                    "MESSAGE" -> Color(0xFF673AB7)
                                    "PRESCRIPTION" -> Color(0xFFE65100)
                                    else -> MedTextPrimary
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = notif.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = notif.timeAgo,
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextSecondary
                            )
                        }
                        Text(
                            text = notif.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextPrimary
                        )
                    }

                    if (!notif.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MedBluePrimary)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
    }
}
