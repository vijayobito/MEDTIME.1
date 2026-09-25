package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.model.MedicineReminderEntity
import kotlinx.coroutines.flow.collect
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerPatientMonitorScreen(
    viewModel: MedTimeViewModel,
    initialPatientId: String = "",
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val caretakerLinks by viewModel.caretakerLinks.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    val approvedLinks = remember(caretakerLinks) {
        caretakerLinks.filter { it.status.equals("APPROVED", ignoreCase = true) }
    }

    var selectedPatientId by remember(approvedLinks, initialPatientId) {
        mutableStateOf(
            if (initialPatientId.isNotBlank()) initialPatientId
            else approvedLinks.firstOrNull()?.patientId ?: "patient_1"
        )
    }

    val selectedPatientUser = remember(allUsers, selectedPatientId) {
        allUsers.firstOrNull { it.id == selectedPatientId } ?: allUsers.firstOrNull { it.role == "PATIENT" }
    }

    var reminders by remember { mutableStateOf<List<MedicineReminderEntity>>(emptyList()) }
    LaunchedEffect(selectedPatientId) {
        viewModel.getTodayRemindersForPatient(selectedPatientId).collect {
            reminders = it
        }
    }

    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredReminders = remember(reminders, selectedFilter) {
        when (selectedFilter) {
            "PENDING" -> reminders.filter { it.status == "PENDING" || it.status == "SNOOZED" }
            "TAKEN" -> reminders.filter { it.status == "TAKEN" }
            "MISSED" -> reminders.filter { it.status == "MISSED" || it.status == "SKIPPED" }
            else -> reminders
        }
    }

    val totalCount = reminders.size
    val takenCount = reminders.count { it.status == "TAKEN" }
    val pendingCount = reminders.count { it.status == "PENDING" || it.status == "SNOOZED" }
    val missedCount = reminders.count { it.status == "MISSED" || it.status == "SKIPPED" }
    val adherencePercent = if (totalCount > 0) ((takenCount.toFloat() / totalCount.toFloat()) * 100).toInt() else 100

    var showNudgeDialogForMed by remember { mutableStateOf<MedicineReminderEntity?>(null) }

    if (showNudgeDialogForMed != null && selectedPatientUser != null) {
        val rem = showNudgeDialogForMed!!
        AlertDialog(
            onDismissRequest = { showNudgeDialogForMed = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MedBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Dose Reminder")
                }
            },
            text = {
                Text(
                    text = "Send an urgent reminder to ${selectedPatientUser.name} for ${rem.medicineName} (${rem.dosage}) scheduled at ${rem.scheduledTime}?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MedTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNudgeDialogForMed = null
                        viewModel.sendCaretakerNudge(
                            patientId = selectedPatientUser.id,
                            patientName = selectedPatientUser.name,
                            medicineName = rem.medicineName
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Alert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNudgeDialogForMed = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
    ) {
        // Top Header
        Surface(color = MedSurface, shadowElevation = 2.dp) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MedTextPrimary)
                        }
                        Text(
                            text = "Live Patient Monitor",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MedSuccessLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MedSuccess))
                            Text("Live Feed", style = MaterialTheme.typography.labelSmall, color = MedSuccess, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (approvedLinks.size > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(approvedLinks) { link ->
                            val isSel = link.patientId == selectedPatientId
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedPatientId = link.patientId },
                                label = { Text(link.patientName, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MedBlueLight,
                                    selectedLabelColor = MedBluePrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Patient Header & Adherence Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Monitoring: ${selectedPatientUser?.name ?: "Vijay Kumar"}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MedTextPrimary
                                )
                                Text(
                                    text = "Real-time dose consumption & adherence log",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary
                                )
                            }
                            Text(
                                text = "$adherencePercent%",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (adherencePercent >= 80) MedSuccess else MedWarning
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (adherencePercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (adherencePercent >= 80) MedSuccess else MedWarning,
                            trackColor = MedSurfaceVariant
                        )

                        // Read-only advisory notice
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MedSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MedTextSecondary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Read-Only Patient View: Medication history is recorded directly by the patient.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MedTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All ($totalCount)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedFilter == "PENDING",
                        onClick = { selectedFilter = "PENDING" },
                        label = { Text("Upcoming ($pendingCount)") },
                        modifier = Modifier.weight(1.1f)
                    )
                    FilterChip(
                        selected = selectedFilter == "TAKEN",
                        onClick = { selectedFilter = "TAKEN" },
                        label = { Text("Taken ($takenCount)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedFilter == "MISSED",
                        onClick = { selectedFilter = "MISSED" },
                        label = { Text("Missed ($missedCount)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Reminders List
            if (filteredReminders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MedSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No medication records found in this category.", style = MaterialTheme.typography.bodyMedium, color = MedTextSecondary)
                        }
                    }
                }
            } else {
                items(filteredReminders) { rem ->
                    val isTaken = rem.status == "TAKEN"
                    val isMissed = rem.status == "MISSED" || rem.status == "SKIPPED"
                    val isPending = rem.status == "PENDING" || rem.status == "SNOOZED"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MedSurface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isTaken -> MedSuccessLight
                                                    isMissed -> MedErrorLight
                                                    else -> MedBlueLight
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                isTaken -> Icons.Default.Check
                                                isMissed -> Icons.Default.Close
                                                else -> Icons.Default.AccessTime
                                            },
                                            contentDescription = null,
                                            tint = when {
                                                isTaken -> MedSuccess
                                                isMissed -> MedError
                                                else -> MedBluePrimary
                                            }
                                        )
                                    }

                                    Column {
                                        Text(rem.medicineName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text("${rem.dosage} • ${rem.form}", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when {
                                        isTaken -> MedSuccessLight
                                        isMissed -> MedErrorLight
                                        else -> MedBlueLight
                                    }
                                ) {
                                    Text(
                                        text = rem.status,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = when {
                                            isTaken -> MedSuccess
                                            isMissed -> MedError
                                            else -> MedBluePrimary
                                        }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Scheduled for ${rem.scheduledTime} (${rem.instructions})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary
                                )

                                if (isPending || isMissed) {
                                    FilledTonalButton(
                                        onClick = { showNudgeDialogForMed = rem },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MedBlueLight,
                                            contentColor = MedBluePrimary
                                        )
                                    ) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Nudge", style = MaterialTheme.typography.labelSmall)
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
