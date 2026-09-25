package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.MedicineEntity
import com.example.ui.components.AddMedicineDialog
import com.example.ui.components.MedicineCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesScreen(
    viewModel: MedTimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val medicines by viewModel.medicines.collectAsState()
    val history by viewModel.history.collectAsState()

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            viewModel.rescheduleAllReminders()
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Active Prescriptions, 1 = Adherence Logs
    var showAddDialog by remember { mutableStateOf(false) }
<<<<<<< HEAD
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }
    var initialScannedPill by remember { mutableStateOf<com.example.ui.components.ScannedMedicationDetails?>(null) }
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465

    var medicineToEdit by remember { mutableStateOf<MedicineEntity?>(null) }
    var medicineToDelete by remember { mutableStateOf<MedicineEntity?>(null) }

<<<<<<< HEAD
    if (showVoiceDialog) {
        com.example.ui.components.VoiceMedicationLoggingDialog(
            onDismiss = { showVoiceDialog = false },
            onVoiceParsed = { spokenText ->
                viewModel.parseAndLogVoiceIntake(spokenText)
            }
        )
    }

    if (showScannerDialog) {
        com.example.ui.components.BarcodePillScannerDialog(
            onDismiss = { showScannerDialog = false },
            onMedicationScanned = { scanned ->
                initialScannedPill = scanned
                showScannerDialog = false
                showAddDialog = true
            }
        )
    }

    if (showAddDialog) {
        AddMedicineDialog(
            onDismiss = {
                showAddDialog = false
                initialScannedPill = null
            },
            initialScannedDetails = initialScannedPill,
=======
    if (showAddDialog) {
        AddMedicineDialog(
            onDismiss = { showAddDialog = false },
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

    // Delete Confirmation
    if (medicineToDelete != null) {
        val med = medicineToDelete!!
        AlertDialog(
            onDismissRequest = { medicineToDelete = null },
            title = { Text("Delete Prescription?") },
            text = { Text("Are you sure you want to remove ${med.name} (${med.dosage}) from your active medication list? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMedicine(med)
                        medicineToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedError)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { medicineToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Dialog
    if (medicineToEdit != null) {
        var editName by remember { mutableStateOf(medicineToEdit!!.name) }
        var editDosage by remember { mutableStateOf(medicineToEdit!!.dosage) }
        var editInstructions by remember { mutableStateOf(medicineToEdit!!.instructions) }
        var editFrequency by remember { mutableStateOf(medicineToEdit!!.frequency) }
        var editTimes by remember { mutableStateOf(medicineToEdit!!.reminderTimes) }
        var editStock by remember { mutableStateOf(medicineToEdit!!.stockQuantity.toString()) }

        AlertDialog(
            onDismissRequest = { medicineToEdit = null },
            title = { Text("Edit ${medicineToEdit!!.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Medicine Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDosage,
                        onValueChange = { editDosage = it },
                        label = { Text("Dosage") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editInstructions,
                        onValueChange = { editInstructions = it },
                        label = { Text("Instructions") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editFrequency,
                        onValueChange = { editFrequency = it },
                        label = { Text("Frequency") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editTimes,
                        onValueChange = { editTimes = it },
                        label = { Text("Reminder Times") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editStock,
                        onValueChange = { editStock = it },
                        label = { Text("Stock Quantity (Doses)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = medicineToEdit!!.copy(
                            name = editName,
                            dosage = editDosage,
                            instructions = editInstructions,
                            frequency = editFrequency,
                            reminderTimes = editTimes,
                            stockQuantity = editStock.toIntOrNull() ?: medicineToEdit!!.stockQuantity
                        )
                        viewModel.updateMedicine(updated)
                        medicineToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { medicineToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MedBluePrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Add Medicine", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_medicine")
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(MedBackground)
        ) {
            // Header & Tabs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MedSurface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MedSurface,
                    contentColor = MedBluePrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Active Prescriptions (${medicines.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Adherence Log (${history.size})", fontWeight = FontWeight.Bold) }
                    )
                }

                if (selectedTab == 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search medication, dosage...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MedTextSecondary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("search_medicine_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

<<<<<<< HEAD
                    Spacer(modifier = Modifier.height(8.dp))

                    // Action Buttons: Voice Intake & Barcode Scan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showVoiceDialog = true },
                            modifier = Modifier.weight(1f).height(38.dp).testTag("button_voice_intake_screen"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Voice Log Intake", fontSize = 11.sp, color = MedBluePrimary, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showScannerDialog = true },
                            modifier = Modifier.weight(1f).height(38.dp).testTag("button_scan_bottle_screen"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan Bottle / Strip", fontSize = 11.sp, color = MedBluePrimary, fontWeight = FontWeight.Bold)
                        }
                    }

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MedWarning.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            color = MedWarning.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MedWarning)
                                    Text(
                                        text = "Enable notifications to receive scheduled local dose alerts.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MedTextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Enable", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            // Tab 0: Active Prescriptions
            if (selectedTab == 0) {
<<<<<<< HEAD
                val lowStockMeds = medicines.filter { it.isLowStock() }
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                val filtered = medicines.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.instructions.contains(searchQuery, ignoreCase = true) ||
                            it.notes.contains(searchQuery, ignoreCase = true)
                }

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Medication, contentDescription = null, tint = MedTextTertiary, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Medications Found", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                if (searchQuery.isNotBlank()) "No match for '$searchQuery'" else "Tap 'Add Medicine' below to schedule your prescriptions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
<<<<<<< HEAD
                        if (lowStockMeds.isNotEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MedWarningLight,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = MedWarning, modifier = Modifier.size(20.dp))
                                        Column {
                                            Text(
                                                text = "${lowStockMeds.size} Medication(s) Below Dosage Threshold",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFFE65100)
                                            )
                                            Text(
                                                text = "Tap 'Refill +30' on individual prescriptions to update stock levels and notify caretakers.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MedTextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                        items(filtered, key = { it.id }) { med ->
                            MedicineCard(
                                medicine = med,
                                onEdit = { medicineToEdit = med },
<<<<<<< HEAD
                                onDelete = { medicineToDelete = med },
                                onRefill = { amount -> viewModel.refillMedicine(med.id, amount) }
=======
                                onDelete = { medicineToDelete = med }
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                            )
                        }
                    }
                }
            } else {
                // Tab 1: Adherence Log
                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MedTextTertiary, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Adherence Logs Yet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                "When you take, snooze, or skip doses, your verified timestamps appear here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(history, key = { it.id }) { log ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, MedBorder, RoundedCornerShape(14.dp)),
                                colors = CardDefaults.cardColors(containerColor = MedSurface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (log.action == "TAKEN") MedSuccessLight else MedWarningLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (log.action == "TAKEN") Icons.Default.Check else Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = if (log.action == "TAKEN") MedSuccess else MedWarning,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "${log.medicineName} (${log.dosage})",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            val timeStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(log.actionTimestamp))
                                            Text(
                                                text = "$timeStr • ${log.notes}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MedTextSecondary
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (log.action == "TAKEN") MedSuccessLight else MedWarningLight
                                    ) {
                                        Text(
                                            text = log.action,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (log.action == "TAKEN") MedSuccess else MedWarning
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
