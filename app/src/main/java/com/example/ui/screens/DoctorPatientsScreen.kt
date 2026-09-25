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
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

data class DoctorPatientRecord(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val bloodGroup: String,
    val phone: String,
    val email: String,
    val condition: String,
    val lastVisit: String,
    val nextAppointment: String,
    val allergies: String,
    val activeMedicationsCount: Int,
    val adherencePercent: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorPatientsScreen(
    viewModel: MedTimeViewModel,
    onNavigateToPrescribe: (patientId: String, patientName: String) -> Unit = { _, _ -> },
    onNavigateToChat: (patientId: String, patientName: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val allUsers by viewModel.allUsers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedPatientDetail by remember { mutableStateOf<DoctorPatientRecord?>(null) }
    var doctorNotesInput by remember { mutableStateOf("") }
    var doctorNotesList by remember { mutableStateOf(listOf("Patient is responding well to Lisinopril 10mg. BP normalized to 120/80.", "Advised low-sodium diet and daily 30-minute walking.")) }

    val samplePatients = remember {
        listOf(
            DoctorPatientRecord(
                id = "p_1",
                name = "Vijay Kumar",
                age = 42,
                gender = "Male",
                bloodGroup = "O+",
                phone = "+1 (555) 349-2910",
                email = "vijay.kumar@example.com",
                condition = "Hypertension & Hyperlipidemia",
                lastVisit = "2026-09-18",
                nextAppointment = "Today, 09:30 AM",
                allergies = "Penicillin, Peanuts",
                activeMedicationsCount = 3,
                adherencePercent = 94
            ),
            DoctorPatientRecord(
                id = "p_2",
                name = "Robert Chen",
                age = 58,
                gender = "Male",
                bloodGroup = "A+",
                phone = "+1 (555) 839-1029",
                email = "robert.chen@example.com",
                condition = "Type 2 Diabetes & Mild Arrhythmia",
                lastVisit = "2026-09-10",
                nextAppointment = "Tomorrow, 10:00 AM",
                allergies = "Sulfa Drugs",
                activeMedicationsCount = 4,
                adherencePercent = 88
            ),
            DoctorPatientRecord(
                id = "p_3",
                name = "Maria Garcia",
                age = 65,
                gender = "Female",
                bloodGroup = "B+",
                phone = "+1 (555) 920-1928",
                email = "maria.garcia@example.com",
                condition = "Osteoarthritis & Hypertension",
                lastVisit = "2026-08-28",
                nextAppointment = "Sep 28, 02:00 PM",
                allergies = "Aspirin, Shellfish",
                activeMedicationsCount = 2,
                adherencePercent = 96
            ),
            DoctorPatientRecord(
                id = "p_4",
                name = "James Wilson",
                age = 35,
                gender = "Male",
                bloodGroup = "AB-",
                phone = "+1 (555) 482-9102",
                email = "james.wilson@example.com",
                condition = "Post-Op Cardiac Rehab",
                lastVisit = "2026-09-15",
                nextAppointment = "Oct 02, 11:30 AM",
                allergies = "None reported",
                activeMedicationsCount = 2,
                adherencePercent = 90
            ),
            DoctorPatientRecord(
                id = "p_5",
                name = "Elena Rostova",
                age = 29,
                gender = "Female",
                bloodGroup = "O-",
                phone = "+1 (555) 592-8172",
                email = "elena.rostova@example.com",
                condition = "Sinus Tachycardia & Asthma",
                lastVisit = "2026-09-02",
                nextAppointment = "Oct 05, 03:00 PM",
                allergies = "Codeine",
                activeMedicationsCount = 1,
                adherencePercent = 85
            )
        )
    }

    val filteredPatients = remember(samplePatients, searchQuery, selectedFilter) {
        var list = samplePatients
        if (selectedFilter != "ALL") {
            list = when (selectedFilter) {
                "HIGH_ADHERENCE" -> list.filter { it.adherencePercent >= 90 }
                "CHRONIC" -> list.filter { it.activeMedicationsCount >= 3 }
                else -> list
            }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.condition.lowercase().contains(q) ||
                it.phone.contains(q) ||
                it.email.lowercase().contains(q)
            }
        }
        list
    }

    // Patient Detail Modal / Dialog
    if (selectedPatientDetail != null) {
        val patient = selectedPatientDetail!!
        var selectedDetailTab by remember { mutableIntStateOf(0) }
        val detailTabs = listOf("Overview", "History", "Medications", "Documents", "Doctor Notes")

        AlertDialog(
            onDismissRequest = { selectedPatientDetail = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = MedBluePrimary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = patient.name.take(1),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        Column {
                            Text(text = patient.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = "${patient.age}y • ${patient.gender} • Blood: ${patient.bloodGroup}", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedDetailTab,
                        edgePadding = 0.dp,
                        containerColor = MedSurface,
                        contentColor = MedBluePrimary
                    ) {
                        detailTabs.forEachIndexed { idx, title ->
                            Tab(
                                selected = selectedDetailTab == idx,
                                onClick = { selectedDetailTab = idx },
                                text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedDetailTab == idx) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }

                    when (selectedDetailTab) {
                        0 -> {
                            // Overview
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    PatientDetailField("Diagnosis / Primary Condition", patient.condition)
                                    PatientDetailField("Known Allergies", patient.allergies)
                                    PatientDetailField("Contact Phone", patient.phone)
                                    PatientDetailField("Email Address", patient.email)
                                    PatientDetailField("Last Consultation", patient.lastVisit)
                                    PatientDetailField("Next Scheduled Visit", patient.nextAppointment)
                                    PatientDetailField("Medication Adherence Score", "${patient.adherencePercent}% (${patient.activeMedicationsCount} Active Rx)")
                                }
                            }
                        }
                        1 -> {
                            // Clinical History
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    Text("Past Clinical Consultations", fontWeight = FontWeight.Bold, color = MedTextPrimary)
                                    Surface(shape = RoundedCornerShape(8.dp), color = MedBackground, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("Sep 18, 2026 — General Cardiology Follow-up", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                            Text("Assessed BP and pulse rhythm. ECG results were normal. Refilled Lisinopril 10mg.", fontSize = 11.sp, color = MedTextSecondary)
                                        }
                                    }
                                    Surface(shape = RoundedCornerShape(8.dp), color = MedBackground, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("Aug 20, 2026 — Routine Triage & Vitals Check", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                            Text("BP elevated at 142/90. Recommended reduction in sodium and prescribed daily monitoring.", fontSize = 11.sp, color = MedTextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Medications
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    Text("Active Prescribed Medications", fontWeight = FontWeight.Bold, color = MedTextPrimary)
                                    Surface(shape = RoundedCornerShape(8.dp), color = MedBackground, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("1. Lisinopril 10 mg • Tablet", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Take 1 tablet daily in the morning with water.", fontSize = 11.sp, color = MedTextSecondary)
                                        }
                                    }
                                    Surface(shape = RoundedCornerShape(8.dp), color = MedBackground, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("2. Atorvastatin 20 mg • Tablet", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Take 1 tablet at bedtime with food.", fontSize = 11.sp, color = MedTextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            // Documents
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    Text("Patient Lab Reports & Documents", fontWeight = FontWeight.Bold, color = MedTextPrimary)
                                    Surface(shape = RoundedCornerShape(8.dp), color = MedBackground, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFE53935))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Comprehensive Metabolic Panel (CMP).pdf", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                                Text("Uploaded Sep 12, 2026 • 1.8 MB", fontSize = 10.sp, color = MedTextSecondary)
                                            }
                                        }
                                    }
                                    Surface(shape = RoundedCornerShape(8.dp), color = MedBackground, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFE53935))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("ECG Electrocardiogram Rhythm Scan.pdf", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                                Text("Uploaded Sep 05, 2026 • 2.4 MB", fontSize = 10.sp, color = MedTextSecondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        4 -> {
                            // Doctor Notes
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(doctorNotesList) { note ->
                                    Surface(shape = RoundedCornerShape(8.dp), color = MedBackground, modifier = Modifier.fillMaxWidth()) {
                                        Text(text = "• $note", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
                                    }
                                }
                                item {
                                    OutlinedTextField(
                                        value = doctorNotesInput,
                                        onValueChange = { doctorNotesInput = it },
                                        placeholder = { Text("Write new clinical note...") },
                                        modifier = Modifier.fillMaxWidth().height(90.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(
                                        onClick = {
                                            if (doctorNotesInput.isNotBlank()) {
                                                doctorNotesList = doctorNotesList + doctorNotesInput.trim()
                                                doctorNotesInput = ""
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.End),
                                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                                    ) {
                                        Text("Add Note")
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val p = patient
                            selectedPatientDetail = null
                            onNavigateToPrescribe(p.id, p.name)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("Write Rx")
                    }
                    Button(
                        onClick = {
                            val p = patient
                            selectedPatientDetail = null
                            onNavigateToChat(p.id, p.name)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                    ) {
                        Text("Send Message")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPatientDetail = null }) {
                    Text("Close")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
            .testTag("doctor_patients_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "My Patients Directory",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                Text(
                    text = "Roster of assigned & connected clinical patients",
                    style = MaterialTheme.typography.bodySmall,
                    color = MedTextSecondary
                )
            }
        }

        // 2. Search & Filter
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, diagnosis, phone...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MedTextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MedSurface,
                    unfocusedContainerColor = MedSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL" to "All Patients (${samplePatients.size})", "HIGH_ADHERENCE" to "High Adherence (>90%)", "CHRONIC" to "Chronic Care").forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }

        // 3. Patients List
        items(filteredPatients, key = { it.id }) { patient ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(16.dp))
                    .clickable { selectedPatientDetail = patient },
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                color = MedBlueLight
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = patient.name.take(1),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MedBluePrimary
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = patient.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MedTextPrimary
                                )
                                Text(
                                    text = "${patient.age}y • ${patient.gender} • Blood: ${patient.bloodGroup}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (patient.adherencePercent >= 90) MedSuccessLight else MedWarningLight
                        ) {
                            Text(
                                text = "${patient.adherencePercent}% Adherence",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (patient.adherencePercent >= 90) MedSuccess else MedWarning
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MedBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Condition", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                Text(patient.condition, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = MedTextPrimary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Next Visit", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                Text(patient.nextAppointment, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = MedBluePrimary)
                            }
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { selectedPatientDetail = patient },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                        ) {
                            Text("View Records", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onNavigateToPrescribe(patient.id, patient.name) },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32))
                        ) {
                            Text("Write Rx", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PatientDetailField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MedTextPrimary)
    }
}
