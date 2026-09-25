package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AdminNavSection(val title: String, val subtitle: String) {
    DOCTOR_VERIFICATION("Doctor Verification", "License approvals & credentials review"),
    CARETAKER_ASSISTANCE("Caretaker Assistance", "Caregiver call & home visit requests"),
    PATIENTS("Patients", "Registered patient profiles & adherence"),
    DOCTORS("Doctors", "Verified specialists & practitioners"),
    CARETAKERS("Caretakers", "Caregiver links & emergency permissions"),
    ANNOUNCEMENTS("Announcements", "System notices & clinical broadcasts"),
    REPORTS("Reports", "Adherence analytics & compliance audit"),
    AUDIT_LOGS("Audit Logs", "Security events & system audit trail"),
    SYSTEM_SETTINGS("System Settings", "Platform thresholds & clinical configuration")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: MedTimeViewModel,
    modifier: Modifier = Modifier
) {
    val allUsers by viewModel.allUsers.collectAsState()
    val doctors by viewModel.doctors.collectAsState()
    val allAppointments by viewModel.allAppointments.collectAsState()
    val allCaretakerLinks by viewModel.allCaretakerLinks.collectAsState()
    val allAssistanceRequests by viewModel.allAssistanceRequests.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val lastBackup by viewModel.lastBackupSummary.collectAsState()
    val isBackingUp by viewModel.isBackingUp.collectAsState()
    val lowStockBuffer by viewModel.lowStockBufferDays.collectAsState()
    val escalationMinutes by viewModel.missedDoseEscalationMinutes.collectAsState()
    val emergencySosAutoDispatch by viewModel.emergencySosAutoDispatch.collectAsState()
    val maintenanceMode by viewModel.maintenanceMode.collectAsState()
    val requireDoctorLicenseUpload by viewModel.requireDoctorLicenseUpload.collectAsState()

    var selectedSection by remember { mutableStateOf(AdminNavSection.DOCTOR_VERIFICATION) }
    var searchQuery by remember { mutableStateOf("") }
    var showNewAnnouncementDialog by remember { mutableStateOf(false) }
    var showComplianceReportDialog by remember { mutableStateOf(false) }
    var showClearAuditLogsDialog by remember { mutableStateOf(false) }
    var selectedDoctorForLicensePreview by remember { mutableStateOf<UserEntity?>(null) }

    val patients = remember(allUsers) { allUsers.filter { it.role == "PATIENT" } }
    val caretakers = remember(allUsers) { allUsers.filter { it.role == "CARETAKER" } }
    val pendingDoctors = remember(doctors) { doctors.filter { !it.isDoctorVerified || it.doctorVerificationStatus == "PENDING" } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin Portal Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MedSuccess)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ADMIN PORTAL ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MedSuccess
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "MedTime Platform Administration",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Clinical Compliance, Provider Credentialing & System Management",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Security",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // Live Platform Metrics Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Pending Review",
                    value = pendingDoctors.size.toString(),
                    subtitle = "Doctor licenses",
                    icon = Icons.Default.VerifiedUser,
                    containerColor = if (pendingDoctors.isNotEmpty()) MedWarningLight else MedBlueLight,
                    contentColor = if (pendingDoctors.isNotEmpty()) MedWarning else MedBluePrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Patients",
                    value = patients.size.toString(),
                    subtitle = "${caretakers.size} caretakers",
                    icon = Icons.Default.People,
                    containerColor = MedSuccessLight,
                    contentColor = MedSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Broadcasts",
                    value = announcements.size.toString(),
                    subtitle = "${auditLogs.size} audit events",
                    icon = Icons.Default.Campaign,
                    containerColor = Color(0xFFEDE7F6),
                    contentColor = Color(0xFF512DA8),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 8-Section Navigation Bar (ADMIN tree)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MedSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedSection.ordinal,
                        containerColor = MedSurface,
                        contentColor = MedBluePrimary,
                        edgePadding = 12.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AdminNavSection.values().forEach { section ->
                            val isSelected = selectedSection == section
                            Tab(
                                selected = isSelected,
                                onClick = { selectedSection = section },
                                text = {
                                    Text(
                                        text = section.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                },
                                icon = {
                                    val pendingReqsCount = allAssistanceRequests.count { it.status == "PENDING" }
                                    if (section == AdminNavSection.CARETAKER_ASSISTANCE && pendingReqsCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = MedError) {
                                                    Text("$pendingReqsCount")
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PhoneCallback,
                                                contentDescription = section.title,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = when (section) {
                                                AdminNavSection.DOCTOR_VERIFICATION -> Icons.Default.FactCheck
                                                AdminNavSection.CARETAKER_ASSISTANCE -> Icons.Default.PhoneCallback
                                                AdminNavSection.PATIENTS -> Icons.Default.People
                                                AdminNavSection.DOCTORS -> Icons.Default.MedicalServices
                                                AdminNavSection.CARETAKERS -> Icons.Default.Favorite
                                                AdminNavSection.ANNOUNCEMENTS -> Icons.Default.Campaign
                                                AdminNavSection.REPORTS -> Icons.Default.Assessment
                                                AdminNavSection.AUDIT_LOGS -> Icons.Default.History
                                                AdminNavSection.SYSTEM_SETTINGS -> Icons.Default.Settings
                                            },
                                            contentDescription = section.title,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("admin_tab_${section.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        // Section Title & Quick Info Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedSection.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Text(
                        text = selectedSection.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                }

                when (selectedSection) {
                    AdminNavSection.ANNOUNCEMENTS -> {
                        Button(
                            onClick = { showNewAnnouncementDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_admin_new_announcement")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Broadcast Notice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    AdminNavSection.REPORTS -> {
                        FilledTonalButton(
                            onClick = { showComplianceReportDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_admin_export_report")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    AdminNavSection.AUDIT_LOGS -> {
                        OutlinedButton(
                            onClick = { showClearAuditLogsDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MedError),
                            modifier = Modifier.testTag("btn_admin_clear_logs")
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear Trail", fontSize = 12.sp)
                        }
                    }
                    else -> {}
                }
            }
        }

        // Section Content Rendering
        when (selectedSection) {
            AdminNavSection.DOCTOR_VERIFICATION -> {
                DoctorVerificationSection(
                    doctors = doctors,
                    onApprove = { docId -> viewModel.verifyDoctor(docId, true) },
                    onReject = { docId -> viewModel.verifyDoctor(docId, false) },
                    onViewLicense = { doc -> selectedDoctorForLicensePreview = doc }
                )
            }
            AdminNavSection.CARETAKER_ASSISTANCE -> {
                AdminCaretakerAssistanceSection(
                    requests = allAssistanceRequests,
                    allUsers = allUsers,
                    onUpdateStatus = { reqId, status, notes, rejectReason ->
                        viewModel.adminUpdateAssistanceStatus(reqId, status, notes, rejectReason)
                    }
                )
            }
            AdminNavSection.PATIENTS -> {
                AdminPatientsSection(
                    patients = patients,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )
            }
            AdminNavSection.DOCTORS -> {
                AdminDoctorsSection(
                    doctors = doctors,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onToggleVerification = { docId, approved -> viewModel.verifyDoctor(docId, approved) }
                )
            }
            AdminNavSection.CARETAKERS -> {
                AdminCaretakersSection(
                    caretakers = caretakers,
                    caretakerLinks = allCaretakerLinks
                )
            }
            AdminNavSection.ANNOUNCEMENTS -> {
                AdminAnnouncementsSection(
                    announcements = announcements,
                    onDeleteAnnouncement = { id -> viewModel.deleteAnnouncement(id) },
                    onAddNew = { showNewAnnouncementDialog = true }
                )
            }
            AdminNavSection.REPORTS -> {
                AdminReportsSection(
                    allUsers = allUsers,
                    doctors = doctors,
                    appointments = allAppointments,
                    auditLogs = auditLogs,
                    onOpenExportDialog = { showComplianceReportDialog = true }
                )
            }
            AdminNavSection.AUDIT_LOGS -> {
                AdminAuditLogsSection(
                    auditLogs = auditLogs,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )
            }
            AdminNavSection.SYSTEM_SETTINGS -> {
                AdminSystemSettingsSection(
                    viewModel = viewModel,
                    lowStockBuffer = lowStockBuffer,
                    escalationMinutes = escalationMinutes,
                    emergencySosAutoDispatch = emergencySosAutoDispatch,
                    maintenanceMode = maintenanceMode,
                    requireDoctorLicenseUpload = requireDoctorLicenseUpload,
                    lastBackup = lastBackup,
                    isBackingUp = isBackingUp
                )
            }
        }
    }

    // Dialog 1: Create Broadcast Announcement
    if (showNewAnnouncementDialog) {
        CreateAnnouncementDialog(
            onDismiss = { showNewAnnouncementDialog = false },
            onPublish = { title, content, targetRole, priority ->
                viewModel.createAnnouncement(title, content, targetRole, priority)
                showNewAnnouncementDialog = false
            }
        )
    }

    // Dialog 2: Compliance Report Modal
    if (showComplianceReportDialog) {
        ComplianceReportDialog(
            allUsers = allUsers,
            doctors = doctors,
            appointments = allAppointments,
            auditLogs = auditLogs,
            onDismiss = { showComplianceReportDialog = false }
        )
    }

    // Dialog 3: Clear Audit Logs Confirmation
    if (showClearAuditLogsDialog) {
        AlertDialog(
            onDismissRequest = { showClearAuditLogsDialog = false },
            title = { Text("Clear System Audit Logs?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently clear the audit logs? This action will be logged in the fresh audit trail.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAuditLogs()
                        showClearAuditLogsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedError)
                ) {
                    Text("Clear All Logs")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAuditLogsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog 4: Doctor Medical License Certificate Preview
    selectedDoctorForLicensePreview?.let { doc ->
        DoctorLicensePreviewDialog(
            doctor = doc,
            onDismiss = { selectedDoctorForLicensePreview = null },
            onApprove = {
                viewModel.verifyDoctor(doc.id, true)
                selectedDoctorForLicensePreview = null
            },
            onReject = {
                viewModel.verifyDoctor(doc.id, false)
                selectedDoctorForLicensePreview = null
            }
        )
    }
}

// -----------------------------------------------------------------------------------------
// 1. DOCTOR VERIFICATION SECTION
// -----------------------------------------------------------------------------------------
fun androidx.compose.foundation.lazy.LazyListScope.DoctorVerificationSection(
    doctors: List<UserEntity>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    onViewLicense: (UserEntity) -> Unit
) {
    if (doctors.isEmpty()) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No medical practitioners registered in directory.", color = MedTextSecondary)
                }
            }
        }
    } else {
        items(doctors, key = { "doc_verify_${it.id}" }) { doc ->
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, MedBluePrimary, CircleShape),
                                color = MedBlueLight
                            ) {
                                AsyncImage(
                                    model = doc.doctorProfilePhotoUrl.ifBlank { "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=400" },
                                    contentDescription = "Doctor photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = doc.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (doc.isDoctorVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified",
                                            tint = MedBluePrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${doc.doctorSpecialty.ifBlank { "General Medicine" }} • ${doc.doctorHospital.ifBlank { "City General Hospital" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary
                                )
                                Text(
                                    text = "License ID: ${doc.doctorLicense.ifBlank { "MED-LIC-PENDING" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MedBlueDark
                                )
                                Text(
                                    text = "Issuing Council: ${doc.doctorIssuingCouncil} (${doc.doctorYearsExperience} yrs exp)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MedTextSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (doc.isDoctorVerified) MedSuccessLight else MedWarningLight
                        ) {
                            Text(
                                text = if (doc.isDoctorVerified) "VERIFIED" else "PENDING",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (doc.isDoctorVerified) MedSuccess else MedWarning
                            )
                        }
                    }

                    // License preview banner
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MedBackground)
                            .clickable { onViewLicense(doc) }
                            .padding(10.dp),
                        color = MedBackground
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MedBluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Medical License Document",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MedTextPrimary
                                    )
                                    Text(
                                        text = "Tap to view certificate & licensing credentials",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MedTextSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MedBluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onReject(doc.id) },
                            modifier = Modifier.height(36.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MedError)
                        ) {
                            Text("Reject / Revoke", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onApprove(doc.id) },
                            modifier = Modifier.height(36.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MedSuccess)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve License", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 2. PATIENTS SECTION
// -----------------------------------------------------------------------------------------
fun androidx.compose.foundation.lazy.LazyListScope.AdminPatientsSection(
    patients: List<UserEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    item {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search patients by name, email, or phone...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MedBluePrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MedSurface,
                unfocusedContainerColor = MedSurface
            )
        )
    }

    val filtered = if (searchQuery.isBlank()) patients else patients.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.email.contains(searchQuery, ignoreCase = true) ||
        it.phone.contains(searchQuery, ignoreCase = true)
    }

    if (filtered.isEmpty()) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No patients matching query.", color = MedTextSecondary)
                }
            }
        }
    } else {
        items(filtered, key = { "patient_${it.id}" }) { patient ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MedBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MedBluePrimary)
                            }
                            Column {
                                Text(patient.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(patient.email, style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MedBlueLight
                        ) {
                            Text(
                                text = "Blood: ${patient.bloodGroup}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MedBluePrimary
                            )
                        }
                    }

                    HorizontalDivider(color = MedDivider)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Emergency Contact", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                            Text("${patient.emergencyContactName} (${patient.emergencyContactRelation})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            Text(patient.emergencyContactPhone, style = MaterialTheme.typography.labelSmall, color = MedBluePrimary)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Care Linking Code", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                            Text(patient.caretakerLinkingCode, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MedBlueDark)
                            Text("Allergies: ${patient.allergies.take(18)}...", style = MaterialTheme.typography.labelSmall, color = MedWarning)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 3. DOCTORS SECTION
// -----------------------------------------------------------------------------------------
fun androidx.compose.foundation.lazy.LazyListScope.AdminDoctorsSection(
    doctors: List<UserEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleVerification: (String, Boolean) -> Unit
) {
    item {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search doctors by specialty or hospital...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MedBluePrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MedSurface,
                unfocusedContainerColor = MedSurface
            )
        )
    }

    val filtered = if (searchQuery.isBlank()) doctors else doctors.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.doctorSpecialty.contains(searchQuery, ignoreCase = true) ||
        it.doctorHospital.contains(searchQuery, ignoreCase = true)
    }

    items(filtered, key = { "doc_dir_${it.id}" }) { doc ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, MedBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = MedSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            color = MedSuccessLight
                        ) {
                            AsyncImage(
                                model = doc.doctorProfilePhotoUrl.ifBlank { "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=400" },
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(doc.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                if (doc.isDoctorVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Verified, contentDescription = "Verified", tint = MedBluePrimary, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text("${doc.doctorSpecialty} • ${doc.doctorHospital}", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (doc.isDoctorVerified) MedSuccessLight else MedWarningLight
                    ) {
                        Text(
                            text = if (doc.isDoctorVerified) "ACTIVE" else "PENDING",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (doc.isDoctorVerified) MedSuccess else MedWarning
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lic: ${doc.doctorLicense} (${doc.doctorYearsExperience} yrs exp)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MedTextSecondary
                    )

                    TextButton(
                        onClick = { onToggleVerification(doc.id, !doc.isDoctorVerified) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (doc.isDoctorVerified) "Revoke Access" else "Approve Access",
                            fontSize = 11.sp,
                            color = if (doc.isDoctorVerified) MedError else MedSuccess
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 4. CARETAKERS SECTION
// -----------------------------------------------------------------------------------------
fun androidx.compose.foundation.lazy.LazyListScope.AdminCaretakersSection(
    caretakers: List<UserEntity>,
    caretakerLinks: List<CaretakerLinkEntity>
) {
    if (caretakers.isEmpty()) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No caretakers currently registered.", color = MedTextSecondary)
                }
            }
        }
    } else {
        items(caretakers, key = { "caretaker_${it.id}" }) { caretaker ->
            val linksForCaretaker = caretakerLinks.filter { it.caretakerId == caretaker.id }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MedWarningLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = MedWarning)
                            }
                            Column {
                                Text(caretaker.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text("${caretaker.email} • ${caretaker.phone}", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MedSurfaceVariant
                        ) {
                            Text(
                                text = "${linksForCaretaker.size} Linked",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                        }
                    }

                    if (linksForCaretaker.isNotEmpty()) {
                        HorizontalDivider(color = MedDivider)
                        Text(
                            text = "Linked Patients & Emergency Scope:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        linksForCaretaker.forEach { link ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${link.patientName} (Code: ${link.linkingCode})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextPrimary
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (link.canReceiveAlerts) {
                                        Surface(color = MedErrorLight, shape = RoundedCornerShape(4.dp)) {
                                            Text("SOS Alerts", modifier = Modifier.padding(3.dp), fontSize = 9.sp, color = MedError, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (link.canViewAdherence) {
                                        Surface(color = MedSuccessLight, shape = RoundedCornerShape(4.dp)) {
                                            Text("Adherence", modifier = Modifier.padding(3.dp), fontSize = 9.sp, color = MedSuccess, fontWeight = FontWeight.Bold)
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
}

// -----------------------------------------------------------------------------------------
// 5. ANNOUNCEMENTS SECTION
// -----------------------------------------------------------------------------------------
fun androidx.compose.foundation.lazy.LazyListScope.AdminAnnouncementsSection(
    announcements: List<AnnouncementEntity>,
    onDeleteAnnouncement: (String) -> Unit,
    onAddNew: () -> Unit
) {
    if (announcements.isEmpty()) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No active announcements broadcasted.", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text("Broadcast urgent clinical alerts or system maintenance notices to all users.", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onAddNew, colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)) {
                        Text("Create First Announcement")
                    }
                }
            }
        }
    } else {
        items(announcements, key = { "ann_${it.id}" }) { ann ->
            val priorityColor = when (ann.priority) {
                "URGENT", "CRITICAL" -> MedError
                "WARNING" -> MedWarning
                "INFO" -> MedBluePrimary
                else -> MedSuccess
            }
            val priorityBg = when (ann.priority) {
                "URGENT", "CRITICAL" -> MedErrorLight
                "WARNING" -> MedWarningLight
                "INFO" -> MedBlueLight
                else -> MedSuccessLight
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = priorityBg
                            ) {
                                Text(
                                    text = ann.priority,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = priorityColor,
                                    fontSize = 10.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MedSurfaceVariant
                            ) {
                                Text(
                                    text = "Audience: ${ann.targetRole}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { onDeleteAnnouncement(ann.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MedTextTertiary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Text(
                        text = ann.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )

                    Text(
                        text = ann.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Author: ${ann.authorName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MedTextSecondary
                        )
                        val timeStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(ann.timestamp))
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MedTextSecondary
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 6. REPORTS SECTION
// -----------------------------------------------------------------------------------------
fun androidx.compose.foundation.lazy.LazyListScope.AdminReportsSection(
    allUsers: List<UserEntity>,
    doctors: List<UserEntity>,
    appointments: List<AppointmentEntity>,
    auditLogs: List<AuditLogEntity>,
    onOpenExportDialog: () -> Unit
) {
    item {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MedSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Clinical Adherence & Platform Health Analytics",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MedBlueLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Adherence Avg", style = MaterialTheme.typography.labelSmall, color = MedBlueDark)
                            Text("94.2%", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MedBluePrimary)
                            Text("Platform wide", style = MaterialTheme.typography.labelSmall, color = MedBlueDark)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MedSuccessLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Consultations", style = MaterialTheme.typography.labelSmall, color = MedSuccess)
                            Text("${appointments.size}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MedSuccess)
                            Text("${appointments.count { it.status == "ACCEPTED" }} Active", style = MaterialTheme.typography.labelSmall, color = MedSuccess)
                        }
                    }
                }

                HorizontalDivider(color = MedDivider)

                Text("System Role Distribution", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))

                val patientCount = allUsers.count { it.role == "PATIENT" }
                val doctorCount = allUsers.count { it.role == "DOCTOR" }
                val caretakerCount = allUsers.count { it.role == "CARETAKER" }
                val adminCount = allUsers.count { it.role == "ADMIN" }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Patients:", style = MaterialTheme.typography.bodySmall)
                        Text("$patientCount (${if (allUsers.isNotEmpty()) patientCount * 100 / allUsers.size else 0}%)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Medical Doctors:", style = MaterialTheme.typography.bodySmall)
                        Text("$doctorCount (${doctors.count { it.isDoctorVerified }} verified)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Caretakers:", style = MaterialTheme.typography.bodySmall)
                        Text("$caretakerCount registered", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("System Admins:", style = MaterialTheme.typography.bodySmall)
                        Text("$adminCount active", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onOpenExportDialog,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate & Export Full Compliance Report")
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 7. AUDIT LOGS SECTION
// -----------------------------------------------------------------------------------------
fun androidx.compose.foundation.lazy.LazyListScope.AdminAuditLogsSection(
    auditLogs: List<AuditLogEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    item {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Filter logs by action, actor, or resource...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MedBluePrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MedSurface,
                unfocusedContainerColor = MedSurface
            )
        )
    }

    val filtered = if (searchQuery.isBlank()) auditLogs else auditLogs.filter {
        it.action.contains(searchQuery, ignoreCase = true) ||
        it.performedBy.contains(searchQuery, ignoreCase = true) ||
        it.details.contains(searchQuery, ignoreCase = true) ||
        it.targetResource.contains(searchQuery, ignoreCase = true)
    }

    if (filtered.isEmpty()) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No audit log events match query.", color = MedTextSecondary)
                }
            }
        }
    } else {
        items(filtered, key = { "audit_${it.id}" }) { log ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MedBlueLight
                        ) {
                            Text(
                                text = log.action,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                color = MedBluePrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        val timeStr = SimpleDateFormat("MMM dd • hh:mm:ss a", Locale.getDefault()).format(Date(log.timestamp))
                        Text(timeStr, style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(log.details, style = MaterialTheme.typography.bodySmall, color = MedTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Actor: ${log.performedBy} • Target: ${log.targetResource}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MedTextTertiary
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 8. SYSTEM SETTINGS SECTION
// -----------------------------------------------------------------------------------------
fun androidx.compose.foundation.lazy.LazyListScope.AdminSystemSettingsSection(
    viewModel: MedTimeViewModel,
    lowStockBuffer: Int,
    escalationMinutes: Int,
    emergencySosAutoDispatch: Boolean,
    maintenanceMode: Boolean,
    requireDoctorLicenseUpload: Boolean,
    lastBackup: CloudBackupSummary?,
    isBackingUp: Boolean
) {
    item {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MedSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Clinical Thresholds & Dispatch Rules",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )

                // Low stock buffer setting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dynamic Low-Stock Buffer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Buffer calculation based on daily frequency ($lowStockBuffer days supply)", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (lowStockBuffer > 1) viewModel.lowStockBufferDays.value = lowStockBuffer - 1 }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text("$lowStockBuffer d", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        IconButton(onClick = { if (lowStockBuffer < 14) viewModel.lowStockBufferDays.value = lowStockBuffer + 1 }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }

                HorizontalDivider(color = MedDivider)

                // Missed dose escalation delay
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Missed-Dose Caregiver Escalation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Time window before caregiver automated SMS is dispatched ($escalationMinutes min)", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (escalationMinutes > 5) viewModel.missedDoseEscalationMinutes.value = escalationMinutes - 5 }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text("$escalationMinutes m", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        IconButton(onClick = { if (escalationMinutes < 60) viewModel.missedDoseEscalationMinutes.value = escalationMinutes + 5 }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }

                HorizontalDivider(color = MedDivider)

                // Emergency SOS simulated SMS toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Automated SOS Caretaker Gateway", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Simulate live SMS dispatch to emergency contacts and caregiver phones", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                    }
                    Switch(
                        checked = emergencySosAutoDispatch,
                        onCheckedChange = { viewModel.emergencySosAutoDispatch.value = it }
                    )
                }

                HorizontalDivider(color = MedDivider)

                // Require License Certificate Upload
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Strict Medical Credentialing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Mandate medical council license certificate before doctor verification", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                    }
                    Switch(
                        checked = requireDoctorLicenseUpload,
                        onCheckedChange = { viewModel.requireDoctorLicenseUpload.value = it }
                    )
                }

                HorizontalDivider(color = MedDivider)

                // Maintenance Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Platform Maintenance Banner", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Display scheduled maintenance advisory across patient and doctor portals", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                    }
                    Switch(
                        checked = maintenanceMode,
                        onCheckedChange = { viewModel.maintenanceMode.value = it }
                    )
                }
            }
        }
    }

    // Cloud Database Backup & Disaster Recovery Card
    item {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MedSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MedBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = MedBluePrimary)
                        }
                        Column {
                            Text("Database Cloud Backup & Recovery", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Encrypted SQLite snapshot & compliance archive", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        }
                    }
                }

                lastBackup?.let { summary ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MedBackground,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Last Encrypted Cloud Snapshot:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
                            Text("${summary.formattedDate} (${summary.totalRecords} records, ${String.format(Locale.US, "%.1f", summary.sizeKb)} KB)", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            Text("Checksum: ${summary.checksum}", style = MaterialTheme.typography.labelSmall, color = MedBluePrimary)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.performManualCloudBackup() },
                        enabled = !isBackingUp,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isBackingUp) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Backing up...")
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Backup Now")
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.restoreFromCloudBackup() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore")
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// DIALOGS
// -----------------------------------------------------------------------------------------
@Composable
fun CreateAnnouncementDialog(
    onDismiss: () -> Unit,
    onPublish: (title: String, content: String, targetRole: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var targetRole by remember { mutableStateOf("ALL") }
    var priority by remember { mutableStateOf("NORMAL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Broadcast System Notice", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notice Headline / Title") },
                    placeholder = { Text("e.g. Seasonal Flu Vaccination Drive") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Detailed Notice Content") },
                    placeholder = { Text("Enter detailed broadcast information...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Text("Target Audience:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ALL", "PATIENT", "DOCTOR", "CARETAKER").forEach { role ->
                        FilterChip(
                            selected = targetRole == role,
                            onClick = { targetRole = role },
                            label = { Text(role, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Priority Level:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("INFO", "NORMAL", "URGENT", "CRITICAL").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onPublish(title, content, targetRole, priority)
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
            ) {
                Text("Broadcast Notice")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ComplianceReportDialog(
    allUsers: List<UserEntity>,
    doctors: List<UserEntity>,
    appointments: List<AppointmentEntity>,
    auditLogs: List<AuditLogEntity>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Assessment, contentDescription = null, tint = MedBluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clinical Compliance & Audit Report", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MedBackground, RoundedCornerShape(10.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("MEDTIME HEALTHCARE SYSTEM COMPLIANCE SUMMARY", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MedBlueDark)
                Text("Date Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}", style = MaterialTheme.typography.labelSmall)
                HorizontalDivider()
                Text("• Total User Accounts: ${allUsers.size}", style = MaterialTheme.typography.bodySmall)
                Text("• Verified Medical Practitioners: ${doctors.count { it.isDoctorVerified }} / ${doctors.size}", style = MaterialTheme.typography.bodySmall)
                Text("• Scheduled Tele-Consultations: ${appointments.size}", style = MaterialTheme.typography.bodySmall)
                Text("• Security & Clinical Audit Events: ${auditLogs.size}", style = MaterialTheme.typography.bodySmall)
                Text("• Data Integrity & Encryption: SHA-256 AES-GCM Encrypted", style = MaterialTheme.typography.bodySmall)
                Text("• Caregiver Escalation Compliance: 100% Delivery Rate", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
            ) {
                Text("Export PDF / Share")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun DoctorLicensePreviewDialog(
    doctor: UserEntity,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MedSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Medical License Review", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        color = MedBlueLight
                    ) {
                        AsyncImage(
                            model = doctor.doctorProfilePhotoUrl.ifBlank { "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=400" },
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column {
                        Text(doctor.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("${doctor.doctorSpecialty} • ${doctor.doctorHospital}", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        Text("License ID: ${doctor.doctorLicense}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, color = MedBluePrimary)
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MedBorder, RoundedCornerShape(12.dp)),
                    color = MedBackground
                ) {
                    AsyncImage(
                        model = doctor.doctorLicenseImageUrl.ifBlank { "https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=600" },
                        contentDescription = "Medical License Document",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    text = "Issuing Council: ${doctor.doctorIssuingCouncil}\nExperience: ${doctor.doctorYearsExperience} Years in Clinical Practice",
                    style = MaterialTheme.typography.bodySmall,
                    color = MedTextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MedError)
                    ) {
                        Text("Reject License")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = MedSuccess)
                    ) {
                        Text("Verify & Approve")
                    }
                }
            }
        }
    }
}

/**
 * Admin Caretaker Assistance Management Section (Call & Visit triage)
 */
@OptIn(ExperimentalMaterial3Api::class)
fun androidx.compose.foundation.lazy.LazyListScope.AdminCaretakerAssistanceSection(
    requests: List<CaretakerAssistanceRequestEntity>,
    allUsers: List<UserEntity>,
    onUpdateStatus: (requestId: String, status: String, notes: String, rejectReason: String) -> Unit
) {
    item {
        var selectedFilter by remember { mutableStateOf("ALL") }
        val context = LocalContext.current

        var activeRejectRequestId by remember { mutableStateOf<String?>(null) }
        var rejectReasonInput by remember { mutableStateOf("") }
        var activeAcceptRequestId by remember { mutableStateOf<String?>(null) }
        var adminNotesInput by remember { mutableStateOf("") }

        val pendingCount = requests.count { it.status == "PENDING" }
        val acceptedCount = requests.count { it.status == "ACCEPTED" }
        val inProgressCount = requests.count { it.status == "CARETAKER_ON_THE_WAY" || it.status == "ARRIVED" }
        val completedCount = requests.count { it.status == "COMPLETED" }
        val emergencyCount = requests.count { it.isEmergency }
        val paidCount = requests.count { it.paymentStatus == "PAID" }

        val filteredRequests = remember(requests, selectedFilter) {
            when (selectedFilter) {
                "PENDING" -> requests.filter { it.status == "PENDING" }
                "ACCEPTED" -> requests.filter { it.status == "ACCEPTED" }
                "IN_PROGRESS" -> requests.filter { it.status == "CARETAKER_ON_THE_WAY" || it.status == "ARRIVED" }
                "COMPLETED" -> requests.filter { it.status == "COMPLETED" }
                "REJECTED" -> requests.filter { it.status == "REJECTED" || it.status == "CANCELLED" }
                "PAID_VISITS" -> requests.filter { it.paymentStatus == "PAID" || it.paymentStatus == "REFUNDED" }
                else -> requests
            }
        }

        val activeRejectRequest = remember(activeRejectRequestId, requests) {
            requests.firstOrNull { it.id == activeRejectRequestId }
        }

        // Rejection Dialog with automatic refund notice
        if (activeRejectRequestId != null) {
            AlertDialog(
                onDismissRequest = { activeRejectRequestId = null },
                title = { Text("Reject Assistance Request") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (activeRejectRequest?.paymentStatus == "PAID" && activeRejectRequest.paidAmount > 0) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MedSuccessLight),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = MedSuccess)
                                    Text(
                                        text = "⚠️ Auto-Refund: Rejecting this visit will automatically refund ₹${activeRejectRequest.paidAmount.toInt()} back to ${activeRejectRequest.caretakerName}'s MedTime Wallet.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MedSuccess
                                    )
                                }
                            }
                        }

                        Text(
                            "Please provide a reason for rejecting this caretaker request. This will be visible to the caretaker.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                        OutlinedTextField(
                            value = rejectReasonInput,
                            onValueChange = { rejectReasonInput = it },
                            label = { Text("Rejection Reason") },
                            placeholder = { Text("e.g., Patient already contacted by doctor / Nurse unavailable.") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            activeRejectRequestId?.let { reqId ->
                                onUpdateStatus(reqId, "REJECTED", "", rejectReasonInput.ifBlank { "Declined by Administrator." })
                            }
                            activeRejectRequestId = null
                            rejectReasonInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedError)
                    ) {
                        Text("Confirm Rejection")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeRejectRequestId = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Accept with Notes Dialog
        if (activeAcceptRequestId != null) {
            AlertDialog(
                onDismissRequest = { activeAcceptRequestId = null },
                title = { Text("Approve Assistance Request") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Approve this request for triage. You can attach instructions or dispatch notes for the caretaker.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                        OutlinedTextField(
                            value = adminNotesInput,
                            onValueChange = { adminNotesInput = it },
                            label = { Text("Admin Notes / Instructions (Optional)") },
                            placeholder = { Text("e.g., Authorized for visit. Patient confirmed home.") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            activeAcceptRequestId?.let { reqId ->
                                onUpdateStatus(reqId, "ACCEPTED", adminNotesInput.ifBlank { "Approved by Administrator." }, "")
                            }
                            activeAcceptRequestId = null
                            adminNotesInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedSuccess)
                    ) {
                        Text("Approve Request")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeAcceptRequestId = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Metrics Summary Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Pending Review",
                    value = "$pendingCount",
                    subtitle = "Calls & visits",
                    icon = Icons.Default.PendingActions,
                    containerColor = if (pendingCount > 0) MedWarningLight else MedSurface,
                    contentColor = if (pendingCount > 0) MedWarning else MedTextPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "In Progress",
                    value = "$inProgressCount",
                    subtitle = "Active dispatch",
                    icon = Icons.Default.DirectionsCar,
                    containerColor = MedBlueLight,
                    contentColor = MedBluePrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Completed",
                    value = "$completedCount",
                    subtitle = "Resolved requests",
                    icon = Icons.Default.CheckCircle,
                    containerColor = MedSuccessLight,
                    contentColor = MedSuccess,
                    modifier = Modifier.weight(1f)
                )
            }

            // Filter Tabs Row
            ScrollableTabRow(
                selectedTabIndex = when (selectedFilter) {
                    "ALL" -> 0
                    "PENDING" -> 1
                    "ACCEPTED" -> 2
                    "IN_PROGRESS" -> 3
                    "PAID_VISITS" -> 4
                    "COMPLETED" -> 5
                    "REJECTED" -> 6
                    else -> 0
                },
                containerColor = MedSurface,
                contentColor = MedBluePrimary,
                edgePadding = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(12.dp))
            ) {
                val filters = listOf(
                    "ALL" to "All (${requests.size})",
                    "PENDING" to "Pending ($pendingCount)",
                    "ACCEPTED" to "Accepted ($acceptedCount)",
                    "IN_PROGRESS" to "In Progress ($inProgressCount)",
                    "PAID_VISITS" to "Paid Visits ($paidCount)",
                    "COMPLETED" to "Completed ($completedCount)",
                    "REJECTED" to "Rejected"
                )

                filters.forEachIndexed { index, pair ->
                    val isSelected = selectedFilter == pair.first
                    Tab(
                        selected = isSelected,
                        onClick = { selectedFilter = pair.first },
                        text = {
                            Text(
                                text = pair.second,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.testTag("admin_assistance_filter_${pair.first.lowercase()}")
                    )
                }
            }

            // Requests List
            if (filteredRequests.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No assistance requests found in this filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MedTextSecondary
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    filteredRequests.forEach { req ->
                        val statusColor = when (req.status) {
                            "PENDING" -> MedWarning
                            "ACCEPTED" -> MedSuccess
                            "CARETAKER_ON_THE_WAY" -> MedBluePrimary
                            "ARRIVED" -> MedInfo
                            "COMPLETED" -> MedSuccess
                            "REJECTED" -> MedError
                            "CANCELLED" -> MedTextSecondary
                            else -> MedTextPrimary
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_assistance_card_${req.id}"),
                            colors = CardDefaults.cardColors(containerColor = MedSurface),
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Top row: Request Type Badge + Status + Emergency Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (req.requestType == "CALL") MedSuccessLight else MedWarningLight
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (req.requestType == "CALL") Icons.Default.PhoneCallback else Icons.Default.DirectionsCar,
                                                    contentDescription = null,
                                                    tint = if (req.requestType == "CALL") MedSuccess else MedWarning,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = if (req.requestType == "CALL") "CALL PATIENT" else "VISIT PATIENT",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (req.requestType == "CALL") MedSuccess else MedWarning
                                                )
                                            }
                                        }

                                        // Payment Status Badge
                                        if (req.requestType == "VISIT") {
                                            val isPaid = req.paymentStatus == "PAID"
                                            val isRefunded = req.paymentStatus == "REFUNDED"
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = when {
                                                    isPaid -> MedSuccessLight
                                                    isRefunded -> MedWarningLight
                                                    else -> MedBackground
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = when {
                                                            isPaid -> Icons.Default.CheckCircle
                                                            isRefunded -> Icons.Default.CurrencyExchange
                                                            else -> Icons.Default.Payment
                                                        },
                                                        contentDescription = null,
                                                        tint = when {
                                                            isPaid -> MedSuccess
                                                            isRefunded -> MedWarning
                                                            else -> MedTextSecondary
                                                        },
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = when {
                                                            isPaid -> "PAID ₹${req.paidAmount.toInt()}"
                                                            isRefunded -> "REFUNDED ₹${req.paidAmount.toInt()}"
                                                            else -> "UNPAID"
                                                        },
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = when {
                                                            isPaid -> MedSuccess
                                                            isRefunded -> MedWarning
                                                            else -> MedTextSecondary
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        if (req.isEmergency) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MedErrorLight
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Icon(Icons.Default.Warning, contentDescription = null, tint = MedError, modifier = Modifier.size(12.dp))
                                                    Text("EMERGENCY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MedError)
                                                }
                                            }
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = statusColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = req.status,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = statusColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(color = MedDivider)

                                // Patient & Caretaker Info Side-by-Side or Stacked
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Patient Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Patient", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                            Text(req.patientName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                            val displayAddress = req.addressSnapshot.ifBlank { req.patientAddress }
                                            if (displayAddress.isNotBlank()) {
                                                Text("📍 $displayAddress", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                            }
                                        }

                                        FilledTonalButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:${req.patientPhone}")
                                                }
                                                context.startActivity(intent)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = MedSuccessLight, contentColor = MedSuccess)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Call Patient", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }

                                    // Caretaker Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Caretaker", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                            Text(req.caretakerName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                            Text("Location: ${req.caretakerLocationName.ifBlank { "Indiranagar, Bengaluru" }}", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:${req.caretakerPhone}")
                                                }
                                                context.startActivity(intent)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.PhoneInTalk, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Call Caretaker", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }

                                // Reason & Notes & Fare Breakdown
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MedBackground,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Reason: ${req.reason}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                        if (req.notes.isNotBlank()) {
                                            Text("Notes: ${req.notes}", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                        }
                                        if (req.requestType == "VISIT") {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Distance: ${req.distanceKm} km (Billable: ${req.billableKm} km)", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                                Text("Fee: ₹${req.totalVisitCharge.toInt()} (Base ₹${req.baseCharge.toInt()} + Dist ₹${req.additionalDistanceCharge.toInt()})", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MedSuccess)
                                            }
                                            if (req.transactionId.isNotBlank()) {
                                                Text("Payment Txn ID: ${req.transactionId}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MedTextTertiary)
                                            }
                                        }
                                    }
                                }

                                if (req.adminNotes.isNotBlank()) {
                                    Text("Admin Note: ${req.adminNotes}", style = MaterialTheme.typography.labelSmall, color = MedBluePrimary)
                                }
                                if (req.rejectionReason.isNotBlank()) {
                                    Text("Rejection: ${req.rejectionReason}", style = MaterialTheme.typography.labelSmall, color = MedError)
                                }

                                val dateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(req.createdAt))
                                Text("Submitted: $dateStr", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)

                                // Admin Action Buttons
                                if (req.status == "PENDING") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { activeRejectRequestId = req.id },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MedError),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Reject", style = MaterialTheme.typography.labelSmall)
                                        }

                                        Button(
                                            onClick = { activeAcceptRequestId = req.id },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = MedSuccess),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                } else if (req.status == "ACCEPTED" || req.status == "CARETAKER_ON_THE_WAY" || req.status == "ARRIVED") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onUpdateStatus(req.id, "CANCELLED", "Cancelled by Admin Desk.", "") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Cancel Request", style = MaterialTheme.typography.labelSmall)
                                        }

                                        Button(
                                            onClick = { onUpdateStatus(req.id, "COMPLETED", "Completed & confirmed by Admin.", "") },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = MedSuccess),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Mark Completed", style = MaterialTheme.typography.labelSmall)
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
}
