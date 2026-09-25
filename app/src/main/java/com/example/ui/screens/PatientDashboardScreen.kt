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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicalDocumentEntity
<<<<<<< HEAD
import com.example.ui.components.*
import com.example.ui.scanner.QrScannerDialog
import com.example.ui.scanner.ShowQrCodeDialog
=======
import com.example.ui.components.AddDocumentDialog
import com.example.ui.components.AddMedicineDialog
import com.example.ui.components.BookAppointmentDialog
import com.example.ui.components.DocumentDetailsAndPreviewDialog
import com.example.ui.components.ReminderCard
import com.example.ui.components.StatCard
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@Composable
fun PatientDashboardScreen(
    viewModel: MedTimeViewModel,
    onNavigateToMedicines: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToMaps: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    onNavigateToCaretakers: () -> Unit,
<<<<<<< HEAD
    onNavigateToNotificationHistory: () -> Unit = {},
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val stats by viewModel.dashboardStats.collectAsState()
    val reminders by viewModel.todayReminders.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val appointments by viewModel.patientAppointments.collectAsState()
    val doctors by viewModel.doctors.collectAsState()
    val documents by viewModel.documents.collectAsState()
<<<<<<< HEAD
    val backupSummary by viewModel.lastBackupSummary.collectAsState()
    val isBackingUp by viewModel.isBackingUp.collectAsState()
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465

    var showAddMedDialog by remember { mutableStateOf(false) }
    var showBookApptDialog by remember { mutableStateOf(false) }
    var showAddDocDialog by remember { mutableStateOf(false) }
<<<<<<< HEAD
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }
    var initialScannedPill by remember { mutableStateOf<com.example.ui.components.ScannedMedicationDetails?>(null) }
    var previewDoc by remember { mutableStateOf<MedicalDocumentEntity?>(null) }
    var selectedDocFormatFilter by remember { mutableStateOf("ALL") }
    var showQrCodeDialog by remember { mutableStateOf(false) }
    var showQrScannerDialog by remember { mutableStateOf(false) }
    var showExportPdfDialog by remember { mutableStateOf(false) }
    var showCloudBackupDialog by remember { mutableStateOf(false) }

    if (showVoiceDialog) {
        VoiceMedicationLoggingDialog(
            onDismiss = { showVoiceDialog = false },
            onVoiceParsed = { spokenText ->
                viewModel.parseAndLogVoiceIntake(spokenText)
            }
        )
    }

    if (showScannerDialog) {
        BarcodePillScannerDialog(
            onDismiss = { showScannerDialog = false },
            onMedicationScanned = { scanned ->
                initialScannedPill = scanned
                showScannerDialog = false
                showAddMedDialog = true
            }
        )
    }

    if (showExportPdfDialog && currentUser != null) {
        ExportDoctorPdfDialog(
            user = currentUser!!,
            adherencePercent = stats.adherencePercent,
            totalScheduled = stats.totalScheduled,
            takenCount = stats.takenCount,
            missedCount = stats.missedCount,
            medicines = medicines,
            appointments = appointments,
            onDismiss = { showExportPdfDialog = false },
            onGenerateAndShare = { ctx ->
                viewModel.generateDoctorSummaryPdf(ctx) { file ->
                    if (file != null) {
                        com.example.util.ClinicalReportPdfGenerator.sharePdf(ctx, file, currentUser?.name ?: "Patient")
                    }
                }
            }
        )
    }

    if (showCloudBackupDialog) {
        AlertDialog(
            onDismissRequest = { showCloudBackupDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = MedBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Database Cloud Backup", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                CloudBackupCard(
                    backupSummary = backupSummary,
                    isBackingUp = isBackingUp,
                    onTriggerBackup = {
                        viewModel.performManualCloudBackup()
                    },
                    onTriggerRestore = {
                        viewModel.restoreFromCloudBackup()
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { showCloudBackupDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    if (showQrCodeDialog && currentUser != null) {
        ShowQrCodeDialog(
            user = currentUser!!,
            onDismiss = { showQrCodeDialog = false }
        )
    }

    if (showQrScannerDialog) {
        QrScannerDialog(
            onDismiss = { showQrScannerDialog = false },
            onAccountScanned = { payload ->
                viewModel.processScannedQrCode(payload)
            }
        )
    }
=======
    var previewDoc by remember { mutableStateOf<MedicalDocumentEntity?>(null) }
    var selectedDocFormatFilter by remember { mutableStateOf("ALL") }
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465

    if (showAddDocDialog) {
        AddDocumentDialog(
            onDismiss = { showAddDocDialog = false },
            onAddDocument = { title, type, clinic, date, size, notes, fileName, fileFormat, mimeType, fileUri, fileSizeBytes, pageCount, resolution, tags, isFavorite ->
                viewModel.addDocument(
                    title = title,
                    type = type,
                    clinic = clinic,
                    date = date,
                    size = size,
                    notes = notes,
                    fileName = fileName,
                    fileFormat = fileFormat,
                    mimeType = mimeType,
                    fileUri = fileUri,
                    fileSizeBytes = fileSizeBytes,
                    pageCount = pageCount,
                    resolution = resolution,
                    tags = tags,
                    isFavorite = isFavorite
                )
            }
        )
    }

    if (previewDoc != null) {
        DocumentDetailsAndPreviewDialog(
            document = previewDoc!!,
            onDismiss = { previewDoc = null },
            onToggleFavorite = { fav ->
                viewModel.toggleDocumentFavorite(previewDoc!!.id, fav)
                previewDoc = previewDoc!!.copy(isFavorite = fav)
            }
        )
    }

    if (showAddMedDialog) {
        AddMedicineDialog(
<<<<<<< HEAD
            onDismiss = {
                showAddMedDialog = false
                initialScannedPill = null
            },
            initialScannedDetails = initialScannedPill,
=======
            onDismiss = { showAddMedDialog = false },
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
            onSave = { name, dosage, form, instructions, frequency, reminderTimes, startDate, endDate, stock, notes ->
                viewModel.addMedicine(
                    name = name,
                    dosage = dosage,
                    form = form,
                    instructions = instructions,
                    frequency = frequency,
                    reminderTimes = reminderTimes,
                    startDate = startDate,
                    endDate = endDate,
                    stock = stock,
                    notes = notes,
                    colorHex = 0xFF1565C0
                )
<<<<<<< HEAD
                initialScannedPill = null
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
            }
        )
    }

    if (showBookApptDialog) {
        BookAppointmentDialog(
            doctors = doctors,
            onDismiss = { showBookApptDialog = false },
            onBook = { docId, docName, spec, date, time, reason, reminderEnabled, reminderMinutesBefore ->
                viewModel.bookAppointment(
                    doctorId = docId,
                    doctorName = docName,
                    doctorSpecialty = spec,
                    date = date,
                    time = time,
                    reason = reason,
                    reminderEnabled = reminderEnabled,
                    reminderMinutesBefore = reminderMinutesBefore
                )
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Patient Profile & Adherence Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MedBluePrimary)
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
                        Column {
                            Text(
                                text = "Welcome back,",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = currentUser?.name ?: "Patient",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = Color.White
                            )
                        }

                        // Circular Adherence Badge
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${stats.adherencePercent}%",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Adherence",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Adherence Progress Bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Today's Medication Adherence",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "${stats.takenCount}/${stats.totalScheduled} taken",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { if (stats.totalScheduled > 0) stats.takenCount.toFloat() / stats.totalScheduled else 1f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF69F0AE),
                            trackColor = Color.White.copy(alpha = 0.25f),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Emergency Quick Trigger
                    Button(
                        onClick = {
                            val phone = currentUser?.emergencyContactPhone ?: "911"
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$phone")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("emergency_call_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhoneInTalk, contentDescription = "Emergency SOS", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Emergency SOS • Call ${currentUser?.emergencyContactName ?: "911"} (${currentUser?.emergencyContactPhone ?: "911"})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 2. Real Metric Stat Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Scheduled",
                    value = stats.totalScheduled.toString(),
                    subtitle = "Doses today",
                    icon = Icons.Default.CalendarToday,
                    containerColor = MedBlueLight,
                    contentColor = MedBlueDark,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Taken",
                    value = stats.takenCount.toString(),
                    subtitle = "Logged doses",
                    icon = Icons.Default.CheckCircle,
                    containerColor = MedSuccessLight,
                    contentColor = MedSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Pending",
                    value = stats.pendingCount.toString(),
                    subtitle = "Remaining",
                    icon = Icons.Default.Schedule,
                    containerColor = MedWarningLight,
                    contentColor = MedWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

<<<<<<< HEAD
        // 3. Daily Medicine Adherence Chart (Taken vs. Missed vs. Pending)
        item {
            DailyAdherenceChartCard(
                stats = stats,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 4. Quick Action Hub
=======
        // 3. Quick Action Hub
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        ActionChip(
<<<<<<< HEAD
                            title = "Voice Intake",
                            icon = Icons.Default.Mic,
                            color = Color(0xFF673AB7),
                            onClick = { showVoiceDialog = true }
                        )
                    }
                    item {
                        ActionChip(
                            title = "Scan Bottle",
                            icon = Icons.Default.DocumentScanner,
                            color = Color(0xFF00897B),
                            onClick = { showScannerDialog = true }
                        )
                    }
                    item {
                        ActionChip(
                            title = "Doctor Report",
                            icon = Icons.Default.PictureAsPdf,
                            color = Color(0xFFC2185B),
                            onClick = { showExportPdfDialog = true }
                        )
                    }
                    item {
                        ActionChip(
                            title = "Alert Logs",
                            icon = Icons.Default.NotificationsActive,
                            color = Color(0xFFE65100),
                            onClick = onNavigateToNotificationHistory
                        )
                    }
                    item {
                        ActionChip(
                            title = "Cloud Backup",
                            icon = Icons.Default.CloudSync,
                            color = MedBluePrimary,
                            onClick = { showCloudBackupDialog = true }
                        )
                    }
                    item {
                        ActionChip(
                            title = "Scan QR",
                            icon = Icons.Default.QrCodeScanner,
                            color = Color(0xFF00897B),
                            onClick = { showQrScannerDialog = true }
                        )
                    }
                    item {
                        ActionChip(
                            title = "My Link QR",
                            icon = Icons.Default.QrCode,
                            color = MedBluePrimary,
                            onClick = { showQrCodeDialog = true }
                        )
                    }
                    item {
                        ActionChip(
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                            title = "Add Med",
                            icon = Icons.Default.Add,
                            color = MedBluePrimary,
                            onClick = { showAddMedDialog = true }
                        )
                    }
                    item {
                        ActionChip(
                            title = "Book Doctor",
                            icon = Icons.Default.MedicalServices,
                            color = MedInfo,
                            onClick = { showBookApptDialog = true }
                        )
                    }
                    item {
                        ActionChip(
                            title = "AI Health Assistant",
                            icon = Icons.Default.Psychology,
                            color = Color(0xFF6A1B9A),
                            onClick = onNavigateToAi
                        )
                    }
                    item {
                        ActionChip(
<<<<<<< HEAD
                            title = "Nearby Hospitals & Routes",
=======
                            title = "Nearby Hospitals",
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                            icon = Icons.Default.LocalHospital,
                            color = MedError,
                            onClick = onNavigateToMaps
                        )
                    }
                    item {
                        ActionChip(
                            title = "Medical Records",
                            icon = Icons.Default.FolderShared,
                            color = MedBlueDark,
                            onClick = onNavigateToDocuments
                        )
                    }
                    item {
                        ActionChip(
                            title = "Caretaker Sync",
                            icon = Icons.Default.PeopleAlt,
                            color = Color(0xFFE65100),
                            onClick = onNavigateToCaretakers
                        )
                    }
                }
            }
        }

<<<<<<< HEAD
        // 5. Emergency Contact & SOS Dashboard Card (Phone Call & SMS Triggers)
        item {
            EmergencyContactDashboardCard(
                user = currentUser,
                onUpdateContact = { name, phone, relation ->
                    viewModel.updateEmergencyContact(name, phone, relation)
                }
            )
        }

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
        // 4. Today's Medicine Reminders
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Medication Schedule",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Text(
                        text = "${reminders.size} scheduled doses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                }
                TextButton(onClick = onNavigateToMedicines) {
                    Text("Manage All", color = MedBluePrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (reminders.isEmpty()) {
            item {
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = MedSuccess,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Reminders Pending",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "You're all caught up on your medications for today!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                    }
                }
            }
        } else {
            items(reminders, key = { it.id }) { rem ->
                ReminderCard(
                    reminder = rem,
                    onTake = { viewModel.markReminderTaken(rem) },
                    onSnooze = { viewModel.snoozeReminder(rem, 15) },
                    onSkip = { viewModel.skipReminder(rem) }
                )
            }
        }

        // 5. Upcoming Consultations Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Consultations",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                TextButton(onClick = onNavigateToAppointments) {
                    Text("View All", color = MedBluePrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        val pendingOrAccepted = appointments.filter { it.status == "PENDING" || it.status == "ACCEPTED" }
        if (pendingOrAccepted.isEmpty()) {
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
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "No pending doctor appointments.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MedTextSecondary
                        )
                        Button(
                            onClick = { showBookApptDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Book Now", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(pendingOrAccepted.take(2)) { appt ->
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
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(appt.doctorName, fontWeight = FontWeight.Bold, color = MedTextPrimary)
                            Text("${appt.doctorSpecialty} • ${appt.appointmentDate} at ${appt.appointmentTime}", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (appt.status == "ACCEPTED") MedSuccessLight else MedWarningLight
                        ) {
                            Text(
                                text = appt.status,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (appt.status == "ACCEPTED") MedSuccess else MedWarning
                            )
                        }
                    }
                }
            }
        }

        // 6. Medical Documents (PDFs & Images) Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Medical Documents (PDFs & Images)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Text(
                        text = "${documents.size} stored records • ${documents.count { it.fileFormat.equals("PDF", ignoreCase = true) }} PDFs, ${documents.count { it.fileFormat.equals("IMAGE", ignoreCase = true) }} Scans",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { showAddDocDialog = true },
                        modifier = Modifier.testTag("btn_dashboard_add_document")
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Upload Document", tint = MedBluePrimary)
                    }
                    TextButton(onClick = onNavigateToDocuments) {
                        Text("View All", color = MedBluePrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Filter chips for Document Formats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedDocFormatFilter == "ALL",
                    onClick = { selectedDocFormatFilter = "ALL" },
                    label = { Text("All (${documents.size})", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedDocFormatFilter == "PDF",
                    onClick = { selectedDocFormatFilter = "PDF" },
                    label = { Text("PDFs (${documents.count { it.fileFormat.equals("PDF", ignoreCase = true) }})", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(14.dp))
                    }
                )
                FilterChip(
                    selected = selectedDocFormatFilter == "IMAGE",
                    onClick = { selectedDocFormatFilter = "IMAGE" },
                    label = { Text("Scans & Photos (${documents.count { it.fileFormat.equals("IMAGE", ignoreCase = true) }})", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.PermMedia, contentDescription = null, tint = Color(0xFF673AB7), modifier = Modifier.size(14.dp))
                    }
                )
            }
        }

        val displayedDocs = when (selectedDocFormatFilter) {
            "PDF" -> documents.filter { it.fileFormat.equals("PDF", ignoreCase = true) }
            "IMAGE" -> documents.filter { it.fileFormat.equals("IMAGE", ignoreCase = true) }
            else -> documents
        }

        if (displayedDocs.isEmpty()) {
            item {
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = MedTextTertiary, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No uploaded documents in this format", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { showAddDocDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Medical Record", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(displayedDocs.take(4), key = { it.id }) { doc ->
                val isPdf = doc.fileFormat.equals("PDF", ignoreCase = true)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MedBorder, RoundedCornerShape(16.dp))
                        .clickable { previewDoc = doc },
                    colors = CardDefaults.cardColors(containerColor = MedSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isPdf) Color(0xFFFFEBEE) else Color(0xFFEDE7F6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.PermMedia,
                                        contentDescription = null,
                                        tint = if (isPdf) Color(0xFFD32F2F) else Color(0xFF673AB7),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = doc.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MedTextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = doc.fileName.ifBlank { if (isPdf) "clinical_report.pdf" else "diagnostic_scan.jpg" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MedBlueDark,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "${doc.doctorOrClinic} • ${doc.dateAdded}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MedTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.toggleDocumentFavorite(doc.id, !doc.isFavorite) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (doc.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = if (doc.isFavorite) Color(0xFFFFB300) else MedTextTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Metadata badge row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isPdf) Color(0xFFFFEBEE) else Color(0xFFEDE7F6)
                                ) {
                                    Text(
                                        text = doc.fileFormat,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPdf) Color(0xFFD32F2F) else Color(0xFF673AB7)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MedBlueLight
                                ) {
                                    Text(
                                        text = doc.type,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MedBluePrimary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFEEEEEE)
                                ) {
                                    Text(
                                        text = if (isPdf) "${doc.pageCount} pgs" else doc.resolution.ifBlank { "Scan" },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        color = MedTextSecondary
                                    )
                                }
                            }

                            Text(
                                text = doc.fileSize,
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextTertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionChip(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(1.dp, MedBorder, RoundedCornerShape(14.dp)),
        color = MedSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(16.dp))
            }
            Text(title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = MedTextPrimary)
        }
    }
}
