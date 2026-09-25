package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppointmentEntity
import kotlinx.coroutines.flow.collect
import com.example.data.model.CaretakerLinkEntity
import com.example.data.model.MedicalDocumentEntity
import com.example.data.model.MedicineEntity
import com.example.data.model.MedicineReminderEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerPatientInfoScreen(
    viewModel: MedTimeViewModel,
    initialPatientId: String = "",
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToPermissions: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val caretakerLinks by viewModel.caretakerLinks.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allAppointments by viewModel.allAppointments.collectAsState()

    val approvedLinks = remember(caretakerLinks) {
        caretakerLinks.filter { it.status.equals("APPROVED", ignoreCase = true) }
    }

    var selectedPatientId by remember(approvedLinks, initialPatientId) {
        mutableStateOf(
            if (initialPatientId.isNotBlank()) initialPatientId
            else approvedLinks.firstOrNull()?.patientId ?: "patient_1"
        )
    }

    val currentLink = remember(approvedLinks, selectedPatientId) {
        approvedLinks.firstOrNull { it.patientId == selectedPatientId }
    }

    val patientUser = remember(allUsers, selectedPatientId) {
        allUsers.firstOrNull { it.id == selectedPatientId } ?: allUsers.firstOrNull { it.role == "PATIENT" }
    }

    var patientMedicines by remember { mutableStateOf<List<MedicineEntity>>(emptyList()) }
    var patientReminders by remember { mutableStateOf<List<MedicineReminderEntity>>(emptyList()) }
    var patientDocuments by remember { mutableStateOf<List<MedicalDocumentEntity>>(emptyList()) }

    LaunchedEffect(selectedPatientId) {
        viewModel.getMedicinesForPatient(selectedPatientId).collect {
            patientMedicines = it
        }
    }
    LaunchedEffect(selectedPatientId) {
        viewModel.getTodayRemindersForPatient(selectedPatientId).collect {
            patientReminders = it
        }
    }
    LaunchedEffect(selectedPatientId) {
        viewModel.getDocumentsForPatient(selectedPatientId).collect {
            patientDocuments = it
        }
    }

    val patientAppointments = remember(allAppointments, selectedPatientId) {
        allAppointments.filter { it.patientId == selectedPatientId }
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Medicines", "Appointments", "Documents")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
    ) {
        // Header
        Surface(
            color = MedSurface,
            shadowElevation = 2.dp
        ) {
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
                            text = "Patient Information",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                    }

                    if (patientUser != null) {
                        IconButton(
                            onClick = {
                                val phone = patientUser.phone.ifBlank { "+15552345678" }
                                val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$phone") }
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Call Patient", tint = MedSuccess)
                        }
                    }
                }

                // Patient switcher if multiple
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

        // Secondary Tab Row
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MedSurface,
            contentColor = MedBluePrimary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium) }
                )
            }
        }

        // Content Body
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: OVERVIEW
                    // Patient Demographic Card
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
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    val initials = patientUser?.name?.take(2)?.uppercase() ?: "PT"
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(MedBlueLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initials,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MedBluePrimary
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = patientUser?.name ?: "Vijay Kumar",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MedTextPrimary
                                        )
                                        Text(
                                            text = "Sync Code: ${patientUser?.caretakerLinkingCode ?: "MED-7842"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MedBluePrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                HorizontalDivider(color = MedBorderLight)

                                // Bio info items
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Blood Group", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                        Text(patientUser?.bloodGroup?.ifBlank { "O+" } ?: "O+", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Allergies", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                        Text(patientUser?.allergies?.ifBlank { "None documented" } ?: "Amoxicillin", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Emergency Contact", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                        Text(
                                            "${patientUser?.emergencyContactName?.ifBlank { "Ananya Kumar" } ?: "Ananya Kumar"} (${patientUser?.emergencyContactRelation?.ifBlank { "Spouse" } ?: "Spouse"})",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Emergency Phone", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                        Text(patientUser?.emergencyContactPhone?.ifBlank { "+1 (555) 987-6543" } ?: "+1 (555) 987-6543", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }
                    }

                    // Permissions status card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MedSurfaceVariant)
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
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(20.dp))
                                        Text("Authorized Permissions", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                    TextButton(onClick = { onNavigateToPermissions(selectedPatientId) }) {
                                        Text("Manage", style = MaterialTheme.typography.labelMedium, color = MedBluePrimary)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = MedSuccessLight, modifier = Modifier.weight(1f)) {
                                        Text("✓ Medicines", modifier = Modifier.padding(6.dp), style = MaterialTheme.typography.labelSmall, color = MedSuccess, fontWeight = FontWeight.Bold)
                                    }
                                    Surface(shape = RoundedCornerShape(8.dp), color = MedSuccessLight, modifier = Modifier.weight(1f)) {
                                        Text("✓ Adherence", modifier = Modifier.padding(6.dp), style = MaterialTheme.typography.labelSmall, color = MedSuccess, fontWeight = FontWeight.Bold)
                                    }
                                    Surface(shape = RoundedCornerShape(8.dp), color = MedSuccessLight, modifier = Modifier.weight(1f)) {
                                        Text("✓ Consults", modifier = Modifier.padding(6.dp), style = MaterialTheme.typography.labelSmall, color = MedSuccess, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: MEDICINES
                    item {
                        Text(
                            text = "Prescribed Medications (${patientMedicines.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                    }

                    if (patientMedicines.isEmpty()) {
                        item {
                            Text("No medications found for this patient.", style = MaterialTheme.typography.bodyMedium, color = MedTextSecondary)
                        }
                    } else {
                        items(patientMedicines) { med ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MedSurface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(med.colorHex)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Medication, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            }
                                            Column {
                                                Text(med.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                                Text("${med.dosage} • ${med.form}", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MedBlueLight
                                        ) {
                                            Text(
                                                med.frequency,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MedBluePrimary
                                            )
                                        }
                                    }
                                    Text("Instructions: ${med.instructions}", style = MaterialTheme.typography.bodySmall, color = MedTextPrimary)
                                    Text("Reminder Times: ${med.reminderTimes}", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: APPOINTMENTS
                    item {
                        Text(
                            text = "Doctor Consultations (${patientAppointments.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                    }

                    if (patientAppointments.isEmpty()) {
                        item {
                            Text("No appointments scheduled for this patient.", style = MaterialTheme.typography.bodyMedium, color = MedTextSecondary)
                        }
                    } else {
                        items(patientAppointments) { appt ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MedSurface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(appt.doctorName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (appt.status == "ACCEPTED") MedSuccessLight else MedWarningLight
                                        ) {
                                            Text(
                                                appt.status,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (appt.status == "ACCEPTED") MedSuccess else MedWarning
                                            )
                                        }
                                    }
                                    Text("${appt.doctorSpecialty} • ${appt.appointmentDate} at ${appt.appointmentTime}", style = MaterialTheme.typography.bodySmall, color = MedBluePrimary)
                                    if (appt.reason.isNotBlank()) {
                                        Text("Reason: ${appt.reason}", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: DOCUMENTS
                    item {
                        Text(
                            text = "Medical Records & Prescriptions (${patientDocuments.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                    }

                    if (patientDocuments.isEmpty()) {
                        item {
                            Text("No diagnostic records or documents available.", style = MaterialTheme.typography.bodyMedium, color = MedTextSecondary)
                        }
                    } else {
                        items(patientDocuments) { doc ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MedSurface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MedError, modifier = Modifier.size(24.dp))
                                            Text(doc.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        }
                                        Surface(shape = RoundedCornerShape(6.dp), color = MedSurfaceVariant) {
                                            Text(doc.fileFormat, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    Text("${doc.doctorOrClinic} • Added ${doc.dateAdded}", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                    if (doc.notes.isNotBlank()) {
                                        Text("Notes: ${doc.notes}", style = MaterialTheme.typography.bodySmall, color = MedTextPrimary)
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
