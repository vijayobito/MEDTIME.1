package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationEntity
import com.example.ui.theme.MedBlueLight
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedError
import com.example.ui.theme.MedSuccess
import com.example.ui.theme.MedSuccessLight
import com.example.ui.theme.MedSurface
import com.example.ui.theme.MedTextPrimary
import com.example.ui.theme.MedTextSecondary
import com.example.ui.theme.MedWarning
import com.example.ui.theme.MedWarningLight
import com.example.ui.viewmodel.MedTimeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotificationHistoryScreen(
    viewModel: MedTimeViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsState()
    val todayReminders by viewModel.todayReminders.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, CARETAKER, MISSED, EMERGENCY, LOW_STOCK, APPOINTMENT

    // Filter notifications based on search and category
    val filteredNotifications = remember(notifications, searchQuery, selectedFilter) {
        notifications.filter { item ->
            val matchesCategory = when (selectedFilter) {
                "CARETAKER" -> item.type == "CARETAKER" || item.type == "MISSED_DOSE" || item.recipientRole.contains("CARETAKER")
                "MISSED" -> item.type == "MISSED_DOSE"
                "EMERGENCY" -> item.type == "EMERGENCY_SOS"
                "LOW_STOCK" -> item.type == "LOW_STOCK"
                "APPOINTMENT" -> item.type == "APPOINTMENT"
                "MEDICINE" -> item.type == "MEDICINE"
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.message.contains(searchQuery, ignoreCase = true) ||
                item.recipientName.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesQuery
        }
    }

    // Caretaker alert counts for summary KPI badges
    val missedAlertCount = notifications.count { it.type == "MISSED_DOSE" }
    val emergencyCount = notifications.count { it.type == "EMERGENCY_SOS" }
    val lowStockCount = notifications.count { it.type == "LOW_STOCK" }
    val caretakerTotal = notifications.count { it.recipientRole.contains("CARETAKER") || it.type == "MISSED_DOSE" || it.type == "CARETAKER" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Notification History & Alerts",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = "Caretaker transparency & alert dispatch logs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MedTextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.markAllNotificationsRead() },
                        modifier = Modifier.testTag("history_mark_all_read")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Mark All Read",
                            tint = MedBluePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MedSurface)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Caretaker Alert Transparency KPI Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransparencyKpiCard(
                        title = "Caretaker Dispatches",
                        count = caretakerTotal.toString(),
                        subtitle = "SMS & Push logs",
                        icon = Icons.Default.PeopleAlt,
                        color = MedBluePrimary,
                        containerColor = MedBlueLight,
                        modifier = Modifier.weight(1f)
                    )
                    TransparencyKpiCard(
                        title = "Missed Escalations",
                        count = missedAlertCount.toString(),
                        subtitle = "Notified caregivers",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFD32F2F),
                        containerColor = Color(0xFFFFEBEE),
                        modifier = Modifier.weight(1f)
                    )
                    TransparencyKpiCard(
                        title = "Low Stock Alerts",
                        count = lowStockCount.toString(),
                        subtitle = "Threshold triggers",
                        icon = Icons.Default.Medication,
                        color = MedWarning,
                        containerColor = MedWarningLight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search alerts by medicine, caretaker, or keyword...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MedTextSecondary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.ClearAll, contentDescription = "Clear", tint = MedTextSecondary)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input")
                )
            }

            item {
                // Filter Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "ALL" to "All (${notifications.size})",
                        "CARETAKER" to "Caretaker ($caretakerTotal)",
                        "MISSED" to "Missed Doses ($missedAlertCount)",
                        "LOW_STOCK" to "Low Stock ($lowStockCount)",
                        "EMERGENCY" to "Emergency SOS ($emergencyCount)",
                        "APPOINTMENT" to "Appointments"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MedBluePrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_chip_$key")
                        )
                    }
                }
            }

            item {
                // Testing & Dispatch Actions Header
                Card(
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MedBluePrimary.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Alert Dispatch & Caretaker Transparency",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MedTextPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "MedTime keeps an immutable ledger of every notification and SMS escalation dispatched to you and your authorized family caregivers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val sampleRem = todayReminders.firstOrNull() ?: com.example.data.model.MedicineReminderEntity(
                                        id = java.util.UUID.randomUUID().toString(),
                                        medicineId = "med_1",
                                        patientId = currentUser?.id ?: "patient_1",
                                        medicineName = "Lisinopril",
                                        dosage = "10 mg",
                                        form = "Tablet",
                                        instructions = "After breakfast",
                                        scheduledDate = "Today",
                                        scheduledTime = "08:00 AM",
                                        status = "MISSED"
                                    )
                                    viewModel.dispatchMissedDoseCaretakerAlert(
                                        reminder = sampleRem,
                                        caretakerName = "Emily Davis",
                                        caretakerPhone = "+1 (555) 234-5678"
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_dispatch_missed_dose")
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFD32F2F))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Missed Alert", fontSize = 11.sp, color = Color(0xFFD32F2F))
                            }

                            Button(
                                onClick = {
                                    viewModel.dispatchEmergencySos(
                                        contactName = currentUser?.emergencyContactName ?: "Sarah Doe",
                                        phone = currentUser?.emergencyContactPhone ?: "+1 (555) 987-6543"
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_dispatch_sos")
                            ) {
                                Icon(Icons.Default.PhoneInTalk, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Trigger SOS Log", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            if (filteredNotifications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MedTextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No notification history found for this filter.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MedTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(filteredNotifications, key = { it.id }) { item ->
                    NotificationHistoryItemCard(
                        notification = item,
                        onMarkRead = { viewModel.markNotificationRead(item.id) },
                        onDelete = { viewModel.deleteNotification(item.id) },
                        onCallContact = { phone ->
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$phone")
                            }
                            context.startActivity(intent)
                        },
                        onResendAlert = {
                            viewModel.triggerTestNotification(
                                medicineName = item.title,
                                dosage = "Dispatched to Caregiver"
                            )
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TransparencyKpiCard(
    title: String,
    count: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Text(
                    text = count,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MedTextPrimary,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MedTextSecondary,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun NotificationHistoryItemCard(
    notification: NotificationEntity,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit,
    onCallContact: (String) -> Unit,
    onResendAlert: () -> Unit
) {
    val isCritical = notification.severity == "CRITICAL" || notification.type == "MISSED_DOSE" || notification.type == "EMERGENCY_SOS"
    val isWarning = notification.severity == "WARNING" || notification.type == "LOW_STOCK"

    val icon = when (notification.type) {
        "MISSED_DOSE" -> Icons.Default.Warning
        "EMERGENCY_SOS" -> Icons.Default.PhoneInTalk
        "LOW_STOCK" -> Icons.Default.Medication
        "APPOINTMENT" -> Icons.Default.CalendarMonth
        "CARETAKER" -> Icons.Default.PeopleAlt
        else -> Icons.Default.Info
    }

    val iconColor = when {
        isCritical -> Color(0xFFD32F2F)
        isWarning -> MedWarning
        notification.type == "APPOINTMENT" -> MedBluePrimary
        else -> MedSuccess
    }

    val iconBg = when {
        isCritical -> Color(0xFFFFEBEE)
        isWarning -> MedWarningLight
        notification.type == "APPOINTMENT" -> MedBlueLight
        else -> MedSuccessLight
    }

    val timeFormatted = remember(notification.timestamp) {
        SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(notification.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MedSurface else MedSurface.copy(alpha = 0.95f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (!notification.isRead) MedBluePrimary.copy(alpha = 0.4f) else Color(0xFFE0E0E0)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_history_card_${notification.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = MedTextSecondary
                        )
                    }
                }

                // Severity / Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isCritical -> Color(0xFFFFCDD2)
                        isWarning -> MedWarningLight
                        else -> MedBlueLight
                    }
                ) {
                    Text(
                        text = if (isCritical) "CRITICAL ALERT" else if (isWarning) "WARNING" else "DELIVERED",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isCritical) Color(0xFFC62828) else if (isWarning) Color(0xFFE65100) else MedBluePrimary,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body message
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodySmall,
                color = MedTextPrimary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Transparency Dispatch Info Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F7FA),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.PeopleAlt, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(13.dp))
                        Text(
                            text = "Target: ${notification.recipientName.ifBlank { "Patient & Caregivers" }}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MedTextPrimary,
                            fontSize = 10.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(4.dp), color = MedBlueLight) {
                            Text(
                                text = "📱 ${notification.deliveryChannels}",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MedBluePrimary,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isCritical) {
                        OutlinedButton(
                            onClick = { onCallContact("+15552345678") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFD32F2F))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call Caregiver", fontSize = 11.sp, color = Color(0xFFD32F2F))
                        }
                    }

                    OutlinedButton(
                        onClick = onResendAlert,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp), tint = MedBluePrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resend Alert", fontSize = 11.sp, color = MedBluePrimary)
                    }
                }

                Row {
                    if (!notification.isRead) {
                        IconButton(onClick = onMarkRead, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Check, contentDescription = "Mark Read", tint = MedSuccess, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MedTextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
