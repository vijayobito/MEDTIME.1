package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerNotificationsScreen(
    viewModel: MedTimeViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {},
    onNavigateToChat: (patientId: String) -> Unit = {},
    onNavigateToPatientInfo: (patientId: String) -> Unit = {},
    onNavigateToMedicines: () -> Unit = {},
    onNavigateToAppointments: () -> Unit = {},
    onNavigateToRequestTracker: (requestId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.notifications.collectAsState()
    var selectedNotificationId by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val selectedNotification = remember(notifications, selectedNotificationId) {
        if (selectedNotificationId != null) {
            notifications.firstOrNull { it.id == selectedNotificationId }
        } else null
    }

    // If a notification is selected, render the dedicated Detail Screen
    if (selectedNotification != null) {
        CaretakerNotificationDetailScreen(
            notification = selectedNotification,
            viewModel = viewModel,
            onNavigateBack = { selectedNotificationId = null },
            onNavigateToWallet = onNavigateToWallet,
            onNavigateToChat = onNavigateToChat,
            onNavigateToPatientInfo = onNavigateToPatientInfo,
            onNavigateToMedicines = onNavigateToMedicines,
            onNavigateToAppointments = onNavigateToAppointments,
            onNavigateToRequestTracker = onNavigateToRequestTracker,
            modifier = modifier
        )
        return
    }

    val unreadCount = remember(notifications) {
        notifications.count { !it.isRead }
    }

    val filteredNotifications = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "WALLET" -> notifications.filter {
                it.type.contains("WALLET", ignoreCase = true) ||
                it.title.contains("Wallet", ignoreCase = true) ||
                it.title.contains("Top-up", ignoreCase = true)
            }
            "REQUESTS" -> notifications.filter {
                it.type.contains("VISIT", ignoreCase = true) ||
                it.type.contains("CALL", ignoreCase = true) ||
                it.type == "CARETAKER" ||
                it.title.contains("Assistance", ignoreCase = true)
            }
            "MEDS" -> notifications.filter {
                it.type == "MEDICINE" ||
                it.type == "MEDICATION_ALERT" ||
                it.type == "MISSED_DOSE" ||
                it.type == "LOW_STOCK" ||
                it.title.contains("Dose", ignoreCase = true) ||
                it.title.contains("Stock", ignoreCase = true)
            }
            "APPOINTMENTS" -> notifications.filter { it.type == "APPOINTMENT" || it.title.contains("Appointment", ignoreCase = true) }
            "MESSAGES" -> notifications.filter { it.type == "DOCTOR" || it.type == "MESSAGE" }
            "UNREAD" -> notifications.filter { !it.isRead }
            else -> notifications
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Caretaker Alerts", fontWeight = FontWeight.Bold, color = MedTextPrimary)
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = MedErrorLight
                            ) {
                                Text(
                                    text = "$unreadCount new",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MedError
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("caretaker_alerts_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MedTextPrimary)
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = { viewModel.markAllNotificationsRead() },
                            modifier = Modifier.testTag("mark_all_read_button")
                        ) {
                            Text("Mark All Read", color = MedBluePrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MedSurface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MedBackground)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Filter categories
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    val filterOptions = listOf(
                        "ALL" to "All (${notifications.size})",
                        "UNREAD" to "Unread ($unreadCount)",
                        "WALLET" to "Wallet",
                        "REQUESTS" to "Assistance Requests",
                        "MEDS" to "Med Alerts",
                        "APPOINTMENTS" to "Appointments",
                        "MESSAGES" to "Messages"
                    )
                    items(filterOptions) { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MedBluePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (filteredNotifications.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MedSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.NotificationsNone,
                                    contentDescription = null,
                                    tint = MedTextSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "No alerts in this category",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MedTextPrimary
                                )
                                Text(
                                    text = "New alerts, assistance requests, and wallet updates will appear here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredNotifications, key = { it.id }) { notif ->
                    val isCritical = notif.severity == "CRITICAL" || notif.type == "EMERGENCY_SOS"
                    val isWarning = notif.severity == "HIGH" || notif.severity == "WARNING" || notif.type == "MISSED_DOSE"
                    val isWallet = notif.type.contains("WALLET", ignoreCase = true) || notif.title.contains("Wallet", ignoreCase = true)
                    val dateStr = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(notif.timestamp))

                    var showItemMenu by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                // Mark as read immediately on tap
                                viewModel.markNotificationRead(notif.id)
                                selectedNotificationId = notif.id
                            }
                            .testTag("notification_item_${notif.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                !notif.isRead && isCritical -> MedErrorLight
                                !notif.isRead && isWarning -> MedWarningLight.copy(alpha = 0.5f)
                                !notif.isRead -> MedBlueLight.copy(alpha = 0.6f)
                                else -> MedSurface
                            }
                        ),
                        elevation = CardDefaults.cardElevation(if (notif.isRead) 1.dp else 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Icon Avatar
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCritical -> MedErrorLight
                                            isWarning -> MedWarningLight
                                            isWallet -> MedSuccessLight
                                            else -> MedBlueLight
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        isCritical -> Icons.Default.Warning
                                        isWarning -> Icons.Default.AccessTime
                                        isWallet -> Icons.Default.AccountBalanceWallet
                                        notif.type.contains("VISIT", ignoreCase = true) -> Icons.Default.PersonPinCircle
                                        notif.type.contains("CALL", ignoreCase = true) -> Icons.Default.PhoneCallback
                                        notif.type == "APPOINTMENT" -> Icons.Default.CalendarMonth
                                        notif.type == "CARETAKER" -> Icons.Default.PeopleAlt
                                        notif.type == "DOCTOR" || notif.type == "MESSAGE" -> Icons.Default.Chat
                                        else -> Icons.Default.Notifications
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        isCritical -> MedError
                                        isWarning -> MedWarning
                                        isWallet -> MedSuccess
                                        else -> MedBluePrimary
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Notification Content Preview
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.SemiBold
                                        ),
                                        color = MedTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (!notif.isRead) {
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 6.dp)
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(MedBluePrimary)
                                        )
                                    }
                                }

                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (!notif.isRead) MedTextPrimary else MedTextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 18.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = MedTextSecondary
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Tap to open",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MedBluePrimary
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = MedBluePrimary,
                                            modifier = Modifier.size(14.dp)
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
}
