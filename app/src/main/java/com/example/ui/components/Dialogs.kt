package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.MedicalDocumentEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        dosage: String,
        form: String,
        instructions: String,
        frequency: String,
        reminderTimes: String,
        startDate: String,
        endDate: String,
        stock: Int,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var form by remember { mutableStateOf("Tablet") }
    var instructions by remember { mutableStateOf("After food with water") }
    var frequency by remember { mutableStateOf("Daily") }
    var reminderTimes by remember { mutableStateOf("08:00 AM") }
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var startDate by remember { mutableStateOf(today) }
    var endDate by remember { mutableStateOf("2026-12-31") }
    var stockText by remember { mutableStateOf("30") }
    var notes by remember { mutableStateOf("") }

    var formExpanded by remember { mutableStateOf(false) }
    val formOptions = listOf("Tablet", "Capsule", "Syrup", "Injection", "Drops", "Inhaler")

    var freqExpanded by remember { mutableStateOf(false) }
    val freqOptions = listOf("Daily", "Twice a day", "Three times a day", "Every 8 hours", "As needed")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add New Medicine", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medicine Name *") },
                    placeholder = { Text("e.g. Atorvastatin") },
                    modifier = Modifier.fillMaxWidth().testTag("input_medicine_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage *") },
                    placeholder = { Text("e.g. 20 mg, 5 ml") },
                    modifier = Modifier.fillMaxWidth().testTag("input_medicine_dosage"),
                    singleLine = true
                )

                // Form Dropdown
                ExposedDropdownMenuBox(
                    expanded = formExpanded,
                    onExpandedChange = { formExpanded = !formExpanded }
                ) {
                    OutlinedTextField(
                        value = form,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Form") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = formExpanded,
                        onDismissRequest = { formExpanded = false }
                    ) {
                        formOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    form = opt
                                    formExpanded = false
                                }
                            )
                        }
                    }
                }

                // Frequency Dropdown
                ExposedDropdownMenuBox(
                    expanded = freqExpanded,
                    onExpandedChange = { freqExpanded = !freqExpanded }
                ) {
                    OutlinedTextField(
                        value = frequency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = freqExpanded,
                        onDismissRequest = { freqExpanded = false }
                    ) {
                        freqOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    frequency = opt
                                    freqExpanded = false
                                    if (opt == "Twice a day") {
                                        reminderTimes = "08:00 AM,08:00 PM"
                                    } else if (opt == "Three times a day") {
                                        reminderTimes = "08:00 AM,02:00 PM,08:00 PM"
                                    } else if (opt == "Daily") {
                                        reminderTimes = "08:00 AM"
                                    }
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reminderTimes,
                    onValueChange = { reminderTimes = it },
                    label = { Text("Reminder Times (comma separated)") },
                    placeholder = { Text("e.g. 08:00 AM,08:00 PM") },
                    modifier = Modifier.fillMaxWidth().testTag("input_reminder_times"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instructions") },
                    placeholder = { Text("e.g. After food with water") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text("Stock Doses") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Clinical Notes / Warnings") },
                    placeholder = { Text("e.g. Avoid grapefruit") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && dosage.isNotBlank()) {
                        val stock = stockText.toIntOrNull() ?: 30
                        onSave(name, dosage, form, instructions, frequency, reminderTimes, startDate, endDate, stock, notes)
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank() && dosage.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                modifier = Modifier.testTag("submit_add_medicine")
            ) {
                Text("Add Medicine")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentDialog(
    doctors: List<UserEntity>,
    onDismiss: () -> Unit,
    onBook: (
        doctorId: String,
        doctorName: String,
        specialty: String,
        date: String,
        time: String,
        reason: String,
        reminderEnabled: Boolean,
        reminderMinutesBefore: Int
    ) -> Unit
) {
    var selectedDoctor by remember { mutableStateOf(doctors.firstOrNull()) }
    var doctorExpanded by remember { mutableStateOf(false) }

    // Date Picker state
    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val defaultFormattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    var selectedDate by remember { mutableStateOf(defaultFormattedDate) }
    var dateDisplayLabel by remember { mutableStateOf("Tomorrow (${SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(calendar.time)})") }
    var showDatePickerModal by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis
    )

    // Time slots
    val timeSlots = listOf("09:00 AM", "10:30 AM", "11:15 AM", "02:00 PM", "03:30 PM", "04:45 PM", "06:00 PM")
    var selectedTimeSlot by remember { mutableStateOf("10:30 AM") }

    // Notification reminder toggle & lead-time
    var reminderEnabled by remember { mutableStateOf(true) }
    var reminderLeadMinutes by remember { mutableStateOf(60) } // 15, 30, 60, 1440

    // Symptoms / Reason
    var reason by remember { mutableStateOf("") }
    val reasonSuggestions = listOf(
        "Routine General Checkup",
        "Blood Pressure Review",
        "Diabetes HbA1c Follow-up",
        "Medication Adjustment",
        "Prescription Refill",
        "Chest Discomfort / Fatigue"
    )

    // Material 3 Date Picker Dialog
    if (showDatePickerModal) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerModal = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            // Adjust for UTC offset in DatePickerState
                            val pickedDate = Date(millis)
                            val ymd = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(pickedDate)
                            val friendly = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(pickedDate)
                            selectedDate = ymd
                            dateDisplayLabel = friendly
                        }
                        showDatePickerModal = false
                    },
                    modifier = Modifier.testTag("confirm_date_picker_button")
                ) {
                    Text("Select Date", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerModal = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = true,
                title = {
                    Text(
                        "Choose Consultation Date",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MedBlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MedBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text("Schedule Doctor Visit", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Book consultation with certified specialist", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Doctor Selector
                Text(
                    text = "1. Select Doctor & Specialty",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )

                ExposedDropdownMenuBox(
                    expanded = doctorExpanded,
                    onExpandedChange = { doctorExpanded = !doctorExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedDoctor?.let { "${it.name} (${it.doctorSpecialty})" } ?: "Select Specialist Doctor",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Consulting Doctor *") },
                        leadingIcon = {
                            Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MedBluePrimary)
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = doctorExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("select_doctor_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = doctorExpanded,
                        onDismissRequest = { doctorExpanded = false }
                    ) {
                        doctors.forEach { doc ->
                            DropdownMenuItem(
                                text = {
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(doc.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${doc.doctorSpecialty} • ${doc.doctorHospital}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MedBluePrimary
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = MedBluePrimary)
                                },
                                onClick = {
                                    selectedDoctor = doc
                                    doctorExpanded = false
                                }
                            )
                        }
                    }
                }

                // 2. Date Selection (Quick Chips + Calendar Picker)
                Text(
                    text = "2. Consultation Date",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MedBluePrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { showDatePickerModal = true }
                        .testTag("button_open_date_picker"),
                    color = MedBlueLight.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MedBluePrimary)
                            Column {
                                Text(
                                    text = dateDisplayLabel,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MedTextPrimary
                                )
                                Text(
                                    text = "Date: $selectedDate",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = { showDatePickerModal = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Change", fontSize = 12.sp)
                        }
                    }
                }

                // Quick Date Preset Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickPresets = listOf(
                        "Today" to 0,
                        "Tomorrow" to 1,
                        "In 3 Days" to 3,
                        "Next Week" to 7
                    )
                    quickPresets.forEach { (label, daysOffset) ->
                        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, daysOffset) }
                        val presetYmd = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                        val isSelected = selectedDate == presetYmd
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedDate = presetYmd
                                dateDisplayLabel = "$label (${SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(cal.time)})"
                            },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.testTag("date_preset_$label")
                        )
                    }
                }

                // 3. Available Time Slots
                Text(
                    text = "3. Preferred Time Slot",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )

                // Time Slot Grid
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("09:00 AM", "10:30 AM", "11:15 AM").forEach { slot ->
                            FilterChip(
                                selected = selectedTimeSlot == slot,
                                onClick = { selectedTimeSlot = slot },
                                label = { Text(slot, fontSize = 11.sp, fontWeight = if (selectedTimeSlot == slot) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = if (selectedTimeSlot == slot) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f).testTag("time_slot_$slot")
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("02:00 PM", "03:30 PM", "04:45 PM").forEach { slot ->
                            FilterChip(
                                selected = selectedTimeSlot == slot,
                                onClick = { selectedTimeSlot = slot },
                                label = { Text(slot, fontSize = 11.sp, fontWeight = if (selectedTimeSlot == slot) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = if (selectedTimeSlot == slot) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f).testTag("time_slot_$slot")
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedTimeSlot == "06:00 PM",
                            onClick = { selectedTimeSlot = "06:00 PM" },
                            label = { Text("06:00 PM (Evening)", fontSize = 11.sp) },
                            leadingIcon = if (selectedTimeSlot == "06:00 PM") {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            modifier = Modifier.fillMaxWidth().testTag("time_slot_evening")
                        )
                    }
                }

                // 4. Notification Reminder Toggle
                Text(
                    text = "4. Appointment Reminder Notification",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (reminderEnabled) MedBlueLight.copy(alpha = 0.6f) else MedBackground,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (reminderEnabled) MedBluePrimary.copy(alpha = 0.4f) else MedBorder
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (reminderEnabled) Icons.Default.NotificationsActive else Icons.Outlined.NotificationsOff,
                                    contentDescription = null,
                                    tint = if (reminderEnabled) MedBluePrimary else MedTextTertiary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "Notification Reminder",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (reminderEnabled) MedBluePrimary else MedTextPrimary
                                    )
                                    Text(
                                        text = "Receive alert before visit to prepare questions",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MedTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { reminderEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MedBluePrimary
                                ),
                                modifier = Modifier.testTag("dialog_toggle_reminder")
                            )
                        }

                        if (reminderEnabled) {
                            Divider(color = MedBluePrimary.copy(alpha = 0.2f), thickness = 0.8.dp)
                            Text(
                                text = "Remind me before appointment:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MedTextSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val leadTimes = listOf(
                                    "15 min" to 15,
                                    "30 min" to 30,
                                    "1 hour" to 60,
                                    "1 day" to 1440
                                )
                                leadTimes.forEach { (label, minutes) ->
                                    val isSelected = reminderLeadMinutes == minutes
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { reminderLeadMinutes = minutes },
                                        label = { Text(label, fontSize = 11.sp) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                        } else null,
                                        modifier = Modifier.weight(1f).testTag("reminder_lead_$label")
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Symptoms / Reason for Visit
                Text(
                    text = "5. Reason / Symptoms",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Describe Symptoms or Purpose *") },
                    placeholder = { Text("e.g. Regular BP checkup, medication refill") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_appointment_reason"),
                    maxLines = 3,
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null, tint = MedBluePrimary)
                    }
                )

                // Suggestions
                Text(
                    text = "Common topics:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MedTextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    reasonSuggestions.take(3).forEach { suggestion ->
                        SuggestionChip(
                            onClick = { reason = suggestion },
                            label = { Text(suggestion, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val doc = selectedDoctor
                    if (doc != null && selectedDate.isNotBlank() && reason.isNotBlank()) {
                        onBook(
                            doc.id,
                            doc.name,
                            doc.doctorSpecialty,
                            selectedDate,
                            selectedTimeSlot,
                            reason,
                            reminderEnabled,
                            reminderLeadMinutes
                        )
                        onDismiss()
                    }
                },
                enabled = selectedDoctor != null && selectedDate.isNotBlank() && reason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                modifier = Modifier.testTag("submit_book_appointment")
            ) {
                Icon(Icons.Default.EventAvailable, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Confirm & Schedule")
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
fun AddDocumentDialog(
    onDismiss: () -> Unit,
    onAddDocument: (
        title: String,
        type: String,
        clinic: String,
        date: String,
        size: String,
        notes: String,
        fileName: String,
        fileFormat: String,
        mimeType: String,
        fileUri: String,
        fileSizeBytes: Long,
        pageCount: Int,
        resolution: String,
        tags: String,
        isFavorite: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf("PDF") } // "PDF" or "IMAGE"
    var type by remember { mutableStateOf("Lab Report") }
    var clinic by remember { mutableStateOf("") }
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var date by remember { mutableStateOf(today) }
    var fileName by remember { mutableStateOf("") }
    var pageCountText by remember { mutableStateOf("1") }
    var resolutionText by remember { mutableStateOf("A4 / 300 DPI") }
    var tags by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isFavorite by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pickedImageUri = uri
            selectedFormat = "IMAGE"
            type = if (type == "Lab Report") "Scan" else type
            if (fileName.isBlank()) {
                val lastPath = uri.lastPathSegment ?: "medical_scan_${System.currentTimeMillis()}"
                fileName = if (lastPath.contains(".")) lastPath else "$lastPath.jpg"
            }
            if (title.isBlank()) {
                title = "Medical Image / Scan (${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date())})"
            }
            resolutionText = "1920x1080 px"
        }
    }

    val typeOptions = listOf("Lab Report", "Prescription", "Scan", "Doctor Notes", "Discharge Summary", "Insurance")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedFormat == "PDF") Color(0xFFFFEBEE) else Color(0xFFEDE7F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (selectedFormat == "PDF") Icons.Default.PictureAsPdf else Icons.Default.Image,
                        contentDescription = null,
                        tint = if (selectedFormat == "PDF") Color(0xFFD32F2F) else Color(0xFF673AB7),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text("Upload Medical Document", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Format Toggle: PDF vs Image
                Text("Document Format", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFormat == "PDF",
                        onClick = {
                            selectedFormat = "PDF"
                            resolutionText = "A4 / 300 DPI"
                            if (fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
                                fileName = fileName.substringBeforeLast(".") + ".pdf"
                            }
                        },
                        label = { Text("PDF Document", fontWeight = if (selectedFormat == "PDF") FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = if (selectedFormat == "PDF") Color(0xFFD32F2F) else MedTextSecondary, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.weight(1f).testTag("format_chip_pdf")
                    )

                    FilterChip(
                        selected = selectedFormat == "IMAGE",
                        onClick = {
                            selectedFormat = "IMAGE"
                            resolutionText = "2048x1536 px"
                            if (fileName.endsWith(".pdf")) {
                                fileName = fileName.substringBeforeLast(".") + ".jpg"
                            }
                        },
                        label = { Text("Medical Image / Scan", fontWeight = if (selectedFormat == "IMAGE") FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = {
                            Icon(Icons.Default.PermMedia, contentDescription = null, tint = if (selectedFormat == "IMAGE") Color(0xFF673AB7) else MedTextSecondary, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.weight(1f).testTag("format_chip_image")
                    )
                }

                // Zero-permission Photo Picker button for medical photos/scans
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (pickedImageUri != null) "Photo Selected (Change)" else "Pick Image from Gallery / Camera", fontSize = 12.sp)
                }

                if (pickedImageUri != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MedSuccessLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(16.dp))
                            Text("Image Attached: ${pickedImageUri?.lastPathSegment ?: "Device Photo"}", fontSize = 11.sp, color = MedSuccess, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // 2. Title & Type
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (fileName.isBlank() && it.isNotBlank()) {
                            val ext = if (selectedFormat == "IMAGE") "jpg" else "pdf"
                            fileName = it.lowercase().trim().replace("[^a-z0-9]+".toRegex(), "_") + ".$ext"
                        }
                    },
                    label = { Text("Document Title *") },
                    placeholder = { Text(if (selectedFormat == "PDF") "e.g. Comprehensive Lipid Panel" else "e.g. Chest X-Ray PA View") },
                    modifier = Modifier.fillMaxWidth().testTag("input_doc_title"),
                    singleLine = true
                )

                // Category Chips
                Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    typeOptions.take(3).forEach { cat ->
                        FilterChip(
                            selected = type == cat,
                            onClick = { type = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    typeOptions.drop(3).forEach { cat ->
                        FilterChip(
                            selected = type == cat,
                            onClick = { type = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 3. Hospital / Physician
                OutlinedTextField(
                    value = clinic,
                    onValueChange = { clinic = it },
                    label = { Text("Physician / Diagnostic Facility") },
                    placeholder = { Text("e.g. Quest Diagnostics, Dr. Mitchell") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 4. File Metadata Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("File Name") },
                        placeholder = { Text(if (selectedFormat == "IMAGE") "scan.jpg" else "report.pdf") },
                        modifier = Modifier.weight(1.3f),
                        singleLine = true
                    )

                    if (selectedFormat == "PDF") {
                        OutlinedTextField(
                            value = pageCountText,
                            onValueChange = { pageCountText = it },
                            label = { Text("Pages") },
                            modifier = Modifier.weight(0.7f),
                            singleLine = true
                        )
                    } else {
                        OutlinedTextField(
                            value = resolutionText,
                            onValueChange = { resolutionText = it },
                            label = { Text("Resolution") },
                            modifier = Modifier.weight(0.9f),
                            singleLine = true
                        )
                    }
                }

                // 5. Tags & Clinical Notes
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags / Keywords") },
                    placeholder = { Text("e.g. Cardiology, Routine, Cholesterol") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Clinical Impressions & Diagnosis") },
                    placeholder = { Text("e.g. Normal sinus rhythm, cholesterol within normal range") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Pin as Favorite Document", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isFavorite,
                        onCheckedChange = { isFavorite = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MedBluePrimary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val pages = pageCountText.toIntOrNull() ?: 1
                        val safeSize = if (selectedFormat == "PDF") "1.2 MB" else "2.4 MB"
                        val safeSizeBytes = if (selectedFormat == "PDF") 1258291L else 2516582L
                        val safeMime = if (selectedFormat == "IMAGE") "image/jpeg" else "application/pdf"
                        val safeExt = if (selectedFormat == "IMAGE") "jpg" else "pdf"
                        val finalFileName = fileName.ifBlank {
                            title.lowercase().trim().replace("[^a-z0-9]+".toRegex(), "_") + ".$safeExt"
                        }

                        onAddDocument(
                            title,
                            type,
                            clinic.ifBlank { "Clinical Laboratory" },
                            date,
                            safeSize,
                            notes,
                            finalFileName,
                            selectedFormat,
                            safeMime,
                            pickedImageUri?.toString() ?: "",
                            safeSizeBytes,
                            pages,
                            resolutionText,
                            tags,
                            isFavorite
                        )
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                modifier = Modifier.testTag("submit_add_document")
            ) {
                Text("Save Document")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Backward-compatible overload
@Composable
fun AddDocumentDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, type: String, clinic: String, date: String, size: String, notes: String) -> Unit
) {
    AddDocumentDialog(
        onDismiss = onDismiss,
        onAddDocument = { title, type, clinic, date, size, notes, _, _, _, _, _, _, _, _, _ ->
            onAdd(title, type, clinic, date, size, notes)
        }
    )
}

@Composable
fun DocumentDetailsAndPreviewDialog(
    document: MedicalDocumentEntity,
    onDismiss: () -> Unit,
    onToggleFavorite: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var isGrayscaleScan by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (document.fileFormat.equals("PDF", ignoreCase = true))
                                    Color(0xFFFFEBEE)
                                else Color(0xFFEDE7F6)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (document.fileFormat.equals("PDF", ignoreCase = true))
                                Icons.Default.PictureAsPdf
                            else Icons.Default.Image,
                            contentDescription = null,
                            tint = if (document.fileFormat.equals("PDF", ignoreCase = true))
                                Color(0xFFD32F2F)
                            else Color(0xFF673AB7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = document.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            color = MedTextPrimary
                        )
                        Text(
                            text = "${document.type} • ${document.fileFormat}",
                            fontSize = 11.sp,
                            color = MedTextSecondary
                        )
                    }
                }

                IconButton(onClick = { onToggleFavorite(!document.isFavorite) }) {
                    Icon(
                        imageVector = if (document.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (document.isFavorite) Color(0xFFFFB300) else MedTextTertiary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Simulated Clinical Document Preview Pane
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MedBorder, RoundedCornerShape(12.dp)),
                    color = if (document.fileFormat.equals("PDF", ignoreCase = true)) {
                        Color(0xFFFAFAFA)
                    } else {
                        if (isGrayscaleScan) Color(0xFF121212) else Color(0xFF1E293B)
                    }
                ) {
                    if (document.fileFormat.equals("PDF", ignoreCase = true)) {
                        // PDF Preview Canvas Simulation
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(16.dp))
                                    Text(document.fileName.ifBlank { "document.pdf" }, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedTextPrimary)
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFE0E0E0)) {
                                    Text("Page 1 of ${document.pageCount}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, color = MedTextSecondary)
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(document.doctorOrClinic, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MedBlueDark)
                                Text("Document Date: ${document.dateAdded}", fontSize = 10.sp, color = MedTextSecondary)
                                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.8.dp)
                                Text(
                                    text = document.notes.ifBlank { "Clinical findings and laboratory metrics documented per official hospital standards." },
                                    fontSize = 11.sp,
                                    color = MedTextPrimary,
                                    maxLines = 3
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = RoundedCornerShape(4.dp), color = MedSuccessLight) {
                                    Text("VERIFIED CLINICAL RECORD", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MedSuccess)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom", tint = MedTextSecondary, modifier = Modifier.size(16.dp))
                                    Icon(Icons.Default.Print, contentDescription = "Print", tint = MedTextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    } else {
                        // Medical Image / Diagnostic Scan Viewport Simulation
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (document.type.contains("Scan", ignoreCase = true)) Icons.Default.CameraAlt else Icons.Default.PermMedia,
                                    contentDescription = null,
                                    tint = if (isGrayscaleScan) Color(0xFFE0E0E0) else Color(0xFF64B5F6),
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "[ High-Resolution Diagnostic Scan Viewer ]",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Resolution: ${document.resolution.ifBlank { "2560x2048 px" }} • DICOM/RGB",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }

                            // Image Controls overlay
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { isGrayscaleScan = !isGrayscaleScan },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(if (isGrayscaleScan) "Color Mode" else "Invert / High-Contrast", fontSize = 9.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // 2. Comprehensive Room Metadata Specification
                Text(
                    text = "Document Metadata",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MedSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetadataRow(label = "File Name", value = document.fileName.ifBlank { "document_${document.id.take(6)}.pdf" })
                        MetadataRow(label = "File Format", value = "${document.fileFormat} (${document.mimeType})")
                        MetadataRow(label = "File Size", value = "${document.fileSize} (${document.fileSizeBytes.takeIf { it > 0 }?.let { "$it bytes" } ?: "Approx. storage"})")
                        MetadataRow(
                            label = if (document.fileFormat.equals("PDF", ignoreCase = true)) "Page Count" else "Resolution",
                            value = if (document.fileFormat.equals("PDF", ignoreCase = true)) "${document.pageCount} page(s)" else document.resolution.ifBlank { "Standard Imaging" }
                        )
                        MetadataRow(label = "Date Added", value = document.dateAdded)
                        MetadataRow(label = "Ordering Facility", value = document.doctorOrClinic)
                        if (document.tags.isNotBlank()) {
                            MetadataRow(label = "Tags", value = document.tags)
                        }
                        MetadataRow(label = "Upload Status", value = document.uploadStatus)
                    }
                }

                // 3. Clinical Findings / Notes
                Text(
                    text = "Clinical Notes & Findings",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MedBlueLight.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MedBluePrimary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = document.notes.ifBlank { "No specific physician annotations or warnings associated with this file." },
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = document.mimeType
                            putExtra(Intent.EXTRA_SUBJECT, "Medical Document: ${document.title}")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Medical Record: ${document.title}\nFacility: ${document.doctorOrClinic}\nDate: ${document.dateAdded}\nSummary: ${document.notes}"
                            )
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Medical Document")
                        context.startActivity(shareIntent)
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 12.sp)
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MedTextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MedTextPrimary,
            modifier = Modifier.weight(1.5f)
        )
    }
}
