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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppointmentEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

data class QueueItem(
    val id: String,
    val patientId: String,
    val patientName: String,
    val patientAge: Int,
    val patientGender: String,
    val tokenNumber: Int,
    val scheduledTime: String,
    val waitTimeMinutes: Int,
    val visitReason: String,
    val status: String, // "WAITING", "IN_CONSULTATION", "COMPLETED", "CANCELLED", "NO_SHOW"
    val vitals: String = "BP: 120/80 • HR: 72 bpm • Temp: 98.6°F"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorQueueScreen(
    viewModel: MedTimeViewModel,
    onNavigateToPrescribe: (patientId: String, patientName: String) -> Unit = { _, _ -> },
    onNavigateToPatientDetail: (patientId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val doctorAppointments by viewModel.doctorAppointments.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var activeConsultationItem by remember { mutableStateOf<QueueItem?>(null) }
    var showClinicalNotesDialog by remember { mutableStateOf<QueueItem?>(null) }
    var clinicalNotesText by remember { mutableStateOf("") }
    var prescriptionNotice by remember { mutableStateOf<String?>(null) }

    // Queue state
    var queueList by remember {
        mutableStateOf(
            listOf(
                QueueItem(
                    id = "q_1",
                    patientId = "patient_1",
                    patientName = "Robert Chen",
                    patientAge = 48,
                    patientGender = "Male",
                    tokenNumber = 101,
                    scheduledTime = "09:00 AM",
                    waitTimeMinutes = 12,
                    visitReason = "Hypertension routine follow-up & chest tightness",
                    status = "IN_CONSULTATION",
                    vitals = "BP: 138/88 • HR: 78 bpm • SpO2: 98% • Temp: 98.4°F"
                ),
                QueueItem(
                    id = "q_2",
                    patientId = "patient_2",
                    patientName = "Maria Garcia",
                    patientAge = 62,
                    patientGender = "Female",
                    tokenNumber = 102,
                    scheduledTime = "09:30 AM",
                    waitTimeMinutes = 24,
                    visitReason = "Diabetes type 2 review & Metformin refill",
                    status = "WAITING",
                    vitals = "BP: 125/82 • HR: 74 bpm • Glucose: 132 mg/dL"
                ),
                QueueItem(
                    id = "q_3",
                    patientId = "patient_3",
                    patientName = "James Wilson",
                    patientAge = 35,
                    patientGender = "Male",
                    tokenNumber = 103,
                    scheduledTime = "10:00 AM",
                    waitTimeMinutes = 8,
                    visitReason = "Lipid panel analysis & cholesterol management",
                    status = "WAITING",
                    vitals = "BP: 118/76 • HR: 68 bpm • SpO2: 99%"
                ),
                QueueItem(
                    id = "q_4",
                    patientId = "patient_4",
                    patientName = "Elena Rostova",
                    patientAge = 29,
                    patientGender = "Female",
                    tokenNumber = 104,
                    scheduledTime = "10:30 AM",
                    waitTimeMinutes = 0,
                    visitReason = "Seasonal allergies & sinus congestion",
                    status = "WAITING",
                    vitals = "BP: 115/75 • HR: 72 bpm • Temp: 99.1°F"
                ),
                QueueItem(
                    id = "q_5",
                    patientId = "patient_5",
                    patientName = "Vijay Kumar",
                    patientAge = 42,
                    patientGender = "Male",
                    tokenNumber = 100,
                    scheduledTime = "08:30 AM",
                    waitTimeMinutes = 0,
                    visitReason = "Cardiac checkup & ECG review",
                    status = "COMPLETED",
                    vitals = "BP: 120/80 • HR: 70 bpm • SpO2: 99%"
                )
            )
        )
    }

    val filteredQueue = remember(queueList, selectedStatusFilter) {
        if (selectedStatusFilter == "ALL") queueList
        else queueList.filter { it.status == selectedStatusFilter }
    }

    val waitingCount = queueList.count { it.status == "WAITING" }
    val inConsultationCount = queueList.count { it.status == "IN_CONSULTATION" }
    val completedCount = queueList.count { it.status == "COMPLETED" }

    if (showClinicalNotesDialog != null) {
        val item = showClinicalNotesDialog!!
        AlertDialog(
            onDismissRequest = { showClinicalNotesDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NoteAlt, contentDescription = null, tint = MedBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clinical Consultation Notes", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Patient: ${item.patientName} (${item.patientAge}y • ${item.patientGender})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Text(
                        text = "Vitals: ${item.vitals}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedBluePrimary
                    )
                    OutlinedTextField(
                        value = clinicalNotesText,
                        onValueChange = { clinicalNotesText = it },
                        label = { Text("Enter Clinical Findings & Advice") },
                        placeholder = { Text("Patient symptoms, examination, diagnosis and instructions...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Mark completed and save
                        queueList = queueList.map {
                            if (it.id == item.id) it.copy(status = "COMPLETED") else it
                        }
                        showClinicalNotesDialog = null
                        clinicalNotesText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Text("Save & Complete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClinicalNotesDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
            .testTag("doctor_queue_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Patient Queue",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Text(
                        text = "Live clinic triage & consultation desk",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MedBluePrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text(
                            text = "$waitingCount Waiting",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 2. QUEUE STATS STRIP
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QueueMetric(label = "Waiting", count = "$waitingCount", color = MedWarning)
                    VerticalDivider(modifier = Modifier.height(30.dp), color = MedBorder)
                    QueueMetric(label = "In Session", count = "$inConsultationCount", color = MedBluePrimary)
                    VerticalDivider(modifier = Modifier.height(30.dp), color = MedBorder)
                    QueueMetric(label = "Completed", count = "$completedCount", color = MedSuccess)
                }
            }
        }

        // 3. STATUS FILTER CHIPS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL" to "All (${queueList.size})", "WAITING" to "Waiting ($waitingCount)", "IN_CONSULTATION" to "Active ($inConsultationCount)", "COMPLETED" to "Completed ($completedCount)").forEach { (key, label) ->
                    val isSelected = selectedStatusFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = key },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MedBluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // 4. ACTIVE CONSULTATION HERO WORKSPACE (if any active)
        val activeItem = queueList.find { it.status == "IN_CONSULTATION" }
        if (activeItem != null && (selectedStatusFilter == "ALL" || selectedStatusFilter == "IN_CONSULTATION")) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, MedBluePrimary, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MedBlueLight.copy(alpha = 0.3f))
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MedBluePrimary)
                                )
                                Text(
                                    text = "ACTIVE CONSULTATION IN PROGRESS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MedBluePrimary,
                                    letterSpacing = 1.sp
                                )
                            }

                            Surface(shape = RoundedCornerShape(8.dp), color = MedBluePrimary) {
                                Text(
                                    text = "Token #${activeItem.tokenNumber}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(52.dp),
                                shape = CircleShape,
                                color = MedBluePrimary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = activeItem.patientName.take(2).uppercase(),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeItem.patientName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MedTextPrimary
                                )
                                Text(
                                    text = "${activeItem.patientAge} Years • ${activeItem.patientGender} • Slot: ${activeItem.scheduledTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary
                                )
                                Text(
                                    text = "Reason: ${activeItem.visitReason}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MedBluePrimary
                                )
                            }
                        }

                        // Vitals summary badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MedSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(18.dp))
                                Text(
                                    text = activeItem.vitals,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MedTextPrimary
                                )
                            }
                        }

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    showClinicalNotesDialog = activeItem
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.NoteAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Notes & Complete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    onNavigateToPrescribe(activeItem.patientId, activeItem.patientName)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32))
                            ) {
                                Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Write Rx", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 5. QUEUE ITEMS LIST
        items(filteredQueue, key = { it.id }) { item ->
            QueuePatientCard(
                item = item,
                onStartConsultation = {
                    queueList = queueList.map {
                        if (it.id == item.id) it.copy(status = "IN_CONSULTATION")
                        else if (it.status == "IN_CONSULTATION") it.copy(status = "WAITING")
                        else it
                    }
                },
                onCompleteConsultation = {
                    showClinicalNotesDialog = item
                },
                onPrescribe = {
                    onNavigateToPrescribe(item.patientId, item.patientName)
                },
                onViewPatient = {
                    onNavigateToPatientDetail(item.patientId)
                },
                onMarkStatus = { newStatus ->
                    queueList = queueList.map {
                        if (it.id == item.id) it.copy(status = newStatus) else it
                    }
                }
            )
        }
    }
}

@Composable
private fun QueueMetric(label: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MedTextSecondary
        )
    }
}

@Composable
private fun QueuePatientCard(
    item: QueueItem,
    onStartConsultation: () -> Unit,
    onCompleteConsultation: () -> Unit,
    onPrescribe: () -> Unit,
    onViewPatient: () -> Unit,
    onMarkStatus: (String) -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MedSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MedBackground
                    ) {
                        Text(
                            text = "#${item.tokenNumber}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                    }

                    Text(
                        text = item.scheduledTime,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MedBluePrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (item.status) {
                        "IN_CONSULTATION" -> MedBlueLight
                        "WAITING" -> MedWarningLight
                        "COMPLETED" -> MedSuccessLight
                        else -> MedBorder
                    }
                ) {
                    Text(
                        text = item.status.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (item.status) {
                            "IN_CONSULTATION" -> MedBluePrimary
                            "WAITING" -> MedWarning
                            "COMPLETED" -> MedSuccess
                            else -> MedTextSecondary
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MedBlueLight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = item.patientName.take(1),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedBluePrimary
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.patientName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Text(
                        text = "${item.patientAge} yrs • ${item.patientGender} • Wait: ${item.waitTimeMinutes} mins",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                }

                Box {
                    IconButton(onClick = { expandedMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More actions", tint = MedTextSecondary)
                    }
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Patient Records") },
                            onClick = {
                                expandedMenu = false
                                onViewPatient()
                            },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Write Prescription") },
                            onClick = {
                                expandedMenu = false
                                onPrescribe()
                            },
                            leadingIcon = { Icon(Icons.Default.PostAdd, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Mark No Show") },
                            onClick = {
                                expandedMenu = false
                                onMarkStatus("NO_SHOW")
                            },
                            leadingIcon = { Icon(Icons.Default.Cancel, contentDescription = null) }
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MedBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Chief Complaint: ${item.visitReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MedTextPrimary,
                    modifier = Modifier.padding(10.dp)
                )
            }

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (item.status) {
                    "WAITING" -> {
                        Button(
                            onClick = onStartConsultation,
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Consultation", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "IN_CONSULTATION" -> {
                        Button(
                            onClick = onCompleteConsultation,
                            colors = ButtonDefaults.buttonColors(containerColor = MedSuccess),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Complete Session", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "COMPLETED" -> {
                        OutlinedButton(
                            onClick = onPrescribe,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View / Add Rx", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {
                        OutlinedButton(
                            onClick = { onMarkStatus("WAITING") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("Re-queue Patient", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
