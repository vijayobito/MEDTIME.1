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
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

data class MedicalRecordEntry(
    val id: String,
    val date: String,
    val title: String,
    val doctorName: String,
    val specialty: String,
    val diagnosis: String,
    val vitals: String,
    val medications: String,
    val notes: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorMedicalRecordsScreen(
    viewModel: MedTimeViewModel,
    modifier: Modifier = Modifier
) {
    val allUsers by viewModel.allUsers.collectAsState()
    val patients = remember(allUsers) { allUsers.filter { it.role == "PATIENT" } }

    var selectedPatientId by remember { mutableStateOf("p_1") }
    var selectedPatientName by remember { mutableStateOf("Vijay Kumar") }
    var showAddRecordDialog by remember { mutableStateOf(false) }

    // New Record form state
    var newDiagnosis by remember { mutableStateOf("") }
    var newVitals by remember { mutableStateOf("BP: 120/80 • HR: 72 bpm") }
    var newMeds by remember { mutableStateOf("") }
    var newNotes by remember { mutableStateOf("") }

    var recordsList by remember {
        mutableStateOf(
            listOf(
                MedicalRecordEntry(
                    id = "rec_1",
                    date = "2026-09-18",
                    title = "Cardiology Assessment & ECG Follow-up",
                    doctorName = "Dr. Sarah Mitchell, MD",
                    specialty = "Cardiology",
                    diagnosis = "Essential Stage 1 Hypertension (Controlled)",
                    vitals = "BP: 122/80 mmHg • HR: 74 bpm • SpO2: 99% • Weight: 74 kg",
                    medications = "Lisinopril 10mg PO Daily, Atorvastatin 20mg PO Daily",
                    notes = "Patient reports good compliance with morning doses. Exercise routine maintained. Heart sounds S1/S2 normal without murmurs."
                ),
                MedicalRecordEntry(
                    id = "rec_2",
                    date = "2026-08-15",
                    title = "Lipid Profile & Metabolic Lab Evaluation",
                    doctorName = "Dr. Sarah Mitchell, MD",
                    specialty = "Cardiology",
                    diagnosis = "Hyperlipidemia (Mild Elevation)",
                    vitals = "BP: 134/84 mmHg • HR: 78 bpm • Total Chol: 210 mg/dL • LDL: 135 mg/dL",
                    medications = "Atorvastatin 20mg PO at bedtime",
                    notes = "Initiated statin therapy due to elevated LDL. Ordered repeat lipid panel in 12 weeks. Recommended Mediterranean diet."
                ),
                MedicalRecordEntry(
                    id = "rec_3",
                    date = "2026-06-20",
                    title = "Annual Comprehensive Health Examination",
                    doctorName = "Dr. James Henderson, MD",
                    specialty = "Internal Medicine",
                    diagnosis = "Routine Health Maintenance",
                    vitals = "BP: 128/82 mmHg • HR: 70 bpm • BMI: 24.2",
                    medications = "Multivitamin 1 Tablet daily",
                    notes = "General wellness evaluation. Vaccinations updated. Recommended annual influenza and booster shots."
                )
            )
        )
    }

    if (showAddRecordDialog) {
        AlertDialog(
            onDismissRequest = { showAddRecordDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PostAdd, contentDescription = null, tint = MedBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Clinical Record Entry", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Patient: $selectedPatientName", fontWeight = FontWeight.Bold, color = MedBluePrimary)
                    OutlinedTextField(
                        value = newDiagnosis,
                        onValueChange = { newDiagnosis = it },
                        label = { Text("Clinical Diagnosis *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newVitals,
                        onValueChange = { newVitals = it },
                        label = { Text("Recorded Vitals (BP, HR, SpO2)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newMeds,
                        onValueChange = { newMeds = it },
                        label = { Text("Prescribed / Adjusted Medications") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newNotes,
                        onValueChange = { newNotes = it },
                        label = { Text("Doctor Findings & Clinical Notes") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDiagnosis.isNotBlank()) {
                            recordsList = listOf(
                                MedicalRecordEntry(
                                    id = "rec_${System.currentTimeMillis()}",
                                    date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                                    title = "Clinical Consultation & Treatment Note",
                                    doctorName = "Dr. Sarah Mitchell, MD",
                                    specialty = "Cardiology",
                                    diagnosis = newDiagnosis.trim(),
                                    vitals = newVitals.trim(),
                                    medications = newMeds.trim(),
                                    notes = newNotes.trim()
                                )
                            ) + recordsList
                            showAddRecordDialog = false
                            newDiagnosis = ""
                            newMeds = ""
                            newNotes = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Text("Save Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRecordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
            .testTag("doctor_records_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Electronic Health Records",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Text(
                        text = "Patient clinical history, vitals & diagnosis logs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                }

                Button(
                    onClick = { showAddRecordDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Entry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Patient Selector Chips
        item {
            Text(
                text = "Select Patient:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MedTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            val patientList = if (patients.isNotEmpty()) patients.map { it.name to it.id }
            else listOf("Vijay Kumar" to "p_1", "Robert Chen" to "p_2", "Maria Garcia" to "p_3", "James Wilson" to "p_4")

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(patientList) { (name, id) ->
                    val isSelected = selectedPatientId == id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedPatientId = id
                            selectedPatientName = name
                        },
                        label = { Text(name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MedBluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // 3. Clinical Summary Banner for Selected Patient
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Patient Health Summary: $selectedPatientName",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        Surface(shape = RoundedCornerShape(8.dp), color = MedBlueLight) {
                            Text(
                                text = "Blood: O+",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MedBluePrimary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MedBackground, modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Allergies", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                Text("Penicillin, Peanuts", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MedError)
                            }
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = MedBackground, modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Chronic Condition", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                Text("Hypertension", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
                            }
                        }
                    }
                }
            }
        }

        // 4. Past Clinical Entries Timeline
        item {
            Text(
                text = "Clinical History & Consultation Notes (${recordsList.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MedTextPrimary
            )
        }

        items(recordsList, key = { it.id }) { rec ->
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rec.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "${rec.date} • ${rec.doctorName} (${rec.specialty})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )
                        }

                        Surface(shape = RoundedCornerShape(8.dp), color = MedBlueLight) {
                            Text(
                                text = "VERIFIED",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MedBluePrimary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MedBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Diagnosis: ${rec.diagnosis}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedBluePrimary
                            )
                            Text(
                                text = "Vitals: ${rec.vitals}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextPrimary
                            )
                            if (rec.medications.isNotBlank()) {
                                Text(
                                    text = "Medications: ${rec.medications}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            HorizontalDivider(color = MedBorder)
                            Text(
                                text = "Clinical Notes: ${rec.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
