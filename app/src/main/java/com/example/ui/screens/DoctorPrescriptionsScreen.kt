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
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

data class PrescribedMedicineItem(
    val name: String,
    val dosage: String,
    val form: String,
    val frequency: String,
    val reminderTimes: String,
    val durationDays: Int,
    val instructions: String
)

data class IssuedPrescriptionRecord(
    val id: String,
    val patientName: String,
    val patientId: String,
    val date: String,
    val diagnosis: String,
    val medicines: List<PrescribedMedicineItem>,
    val status: String = "ACTIVE"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorPrescriptionsScreen(
    viewModel: MedTimeViewModel,
    initialPatientId: String = "",
    initialPatientName: String = "",
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val patients = remember(allUsers) { allUsers.filter { it.role == "PATIENT" } }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Create Prescription", "Prescription History")

    // Form state
    var selectedPatientId by remember {
        mutableStateOf(if (initialPatientId.isNotBlank()) initialPatientId else if (patients.isNotEmpty()) patients.first().id else "p_1")
    }
    var selectedPatientName by remember {
        mutableStateOf(if (initialPatientName.isNotBlank()) initialPatientName else if (patients.isNotEmpty()) patients.first().name else "Vijay Kumar")
    }
    var diagnosisText by remember { mutableStateOf("Essential Hypertension & Cardiovascular Prevention") }
    var doctorInstructions by remember { mutableStateOf("Drink plenty of fluids. Avoid high sodium. Return for BP checkup in 30 days.") }

    // Medicine builder
    var medName by remember { mutableStateOf("Lisinopril") }
    var medDosage by remember { mutableStateOf("10 mg") }
    var medForm by remember { mutableStateOf("Tablet") }
    var medFrequency by remember { mutableStateOf("Once a day") }
    var medTimes by remember { mutableStateOf("08:00 AM") }
    var medDurationDays by remember { mutableIntStateOf(30) }
    var medInstructions by remember { mutableStateOf("Take in morning after food") }

    var prescriptionItems by remember {
        mutableStateOf(
            listOf(
                PrescribedMedicineItem(
                    name = "Lisinopril",
                    dosage = "10 mg",
                    form = "Tablet",
                    frequency = "Once a day",
                    reminderTimes = "08:00 AM",
                    durationDays = 30,
                    instructions = "Take with water in morning after breakfast"
                ),
                PrescribedMedicineItem(
                    name = "Atorvastatin",
                    dosage = "20 mg",
                    form = "Tablet",
                    frequency = "Once a day",
                    reminderTimes = "09:00 PM",
                    durationDays = 30,
                    instructions = "Take at bedtime"
                )
            )
        )
    }

    var issuedPrescriptions by remember {
        mutableStateOf(
            listOf(
                IssuedPrescriptionRecord(
                    id = "rx_101",
                    patientName = "Vijay Kumar",
                    patientId = "patient_1",
                    date = "2026-09-18",
                    diagnosis = "Essential Hypertension",
                    medicines = listOf(
                        PrescribedMedicineItem("Lisinopril", "10 mg", "Tablet", "Daily", "08:00 AM", 30, "After food"),
                        PrescribedMedicineItem("Atorvastatin", "20 mg", "Tablet", "Daily", "09:00 PM", 30, "Bedtime")
                    )
                ),
                IssuedPrescriptionRecord(
                    id = "rx_102",
                    patientName = "Robert Chen",
                    patientId = "patient_2",
                    date = "2026-09-12",
                    diagnosis = "Type 2 Diabetes Mellitus",
                    medicines = listOf(
                        PrescribedMedicineItem("Metformin HCl", "500 mg", "Tablet", "Twice a day", "08:00 AM, 08:00 PM", 60, "With meals")
                    )
                ),
                IssuedPrescriptionRecord(
                    id = "rx_103",
                    patientName = "Maria Garcia",
                    patientId = "patient_3",
                    date = "2026-09-05",
                    diagnosis = "Osteoarthritis & Pain Relief",
                    medicines = listOf(
                        PrescribedMedicineItem("Paracetamol", "650 mg", "Tablet", "As needed", "02:00 PM", 10, "After food for pain")
                    )
                )
            )
        )
    }

    val quickMeds = listOf(
        "Lisinopril 10mg",
        "Metformin 500mg",
        "Atorvastatin 20mg",
        "Amlodipine 5mg",
        "Omeprazole 20mg",
        "Amoxicillin 500mg",
        "Losartan 50mg"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
            .testTag("doctor_prescriptions_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Digital Prescription Desk",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                Text(
                    text = "Generate and transmit certified digital prescriptions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MedTextSecondary
                )
            }
        }

        // Tab Selector
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MedSurface,
                contentColor = Color(0xFF2E7D32),
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                }
            }
        }

        if (selectedTab == 0) {
            // TAB 1: CREATE PRESCRIPTION

            // Patient Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MedSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "1. Select Patient",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )

                        val patientOptions = if (patients.isNotEmpty()) patients.map { it.name to it.id }
                        else listOf("Vijay Kumar" to "p_1", "Robert Chen" to "p_2", "Maria Garcia" to "p_3", "James Wilson" to "p_4", "Elena Rostova" to "p_5")

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(patientOptions) { (name, id) ->
                                val isSelected = selectedPatientId == id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedPatientId = id
                                        selectedPatientName = name
                                    },
                                    label = { Text(name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF2E7D32),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = diagnosisText,
                            onValueChange = { diagnosisText = it },
                            label = { Text("Clinical Diagnosis *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Medicine Builder
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MedSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "2. Add Medications",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE8F5E9)) {
                                Text(
                                    text = "${prescriptionItems.size} in Rx",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }

                        // Quick drug chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(quickMeds) { drug ->
                                SuggestionChip(
                                    onClick = {
                                        val parts = drug.split(" ")
                                        medName = parts[0]
                                        if (parts.size > 1) medDosage = parts[1]
                                    },
                                    label = { Text(drug, fontSize = 11.sp) }
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = medName,
                                onValueChange = { medName = it },
                                label = { Text("Drug Name *") },
                                singleLine = true,
                                modifier = Modifier.weight(1.5f)
                            )
                            OutlinedTextField(
                                value = medDosage,
                                onValueChange = { medDosage = it },
                                label = { Text("Strength") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = medFrequency,
                                onValueChange = { medFrequency = it },
                                label = { Text("Frequency") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = medTimes,
                                onValueChange = { medTimes = it },
                                label = { Text("Alarm Time(s)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = medInstructions,
                            onValueChange = { medInstructions = it },
                            label = { Text("Instructions (e.g., Take after food)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (medName.isNotBlank()) {
                                    prescriptionItems = prescriptionItems + PrescribedMedicineItem(
                                        name = medName.trim(),
                                        dosage = medDosage.trim(),
                                        form = medForm,
                                        frequency = medFrequency.trim(),
                                        reminderTimes = medTimes.trim(),
                                        durationDays = medDurationDays,
                                        instructions = medInstructions.trim()
                                    )
                                    medName = ""
                                    medDosage = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Medicine to Prescription", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Current Prescription Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MedSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "3. Prescription Review",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MedBackground,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Patient: $selectedPatientName", fontWeight = FontWeight.Bold, color = MedTextPrimary)
                                Text("Diagnosis: $diagnosisText", fontSize = 12.sp, color = MedBluePrimary)

                                HorizontalDivider(color = MedBorder)

                                if (prescriptionItems.isEmpty()) {
                                    Text("No medications added yet. Please add above.", fontSize = 12.sp, color = MedTextSecondary)
                                } else {
                                    prescriptionItems.forEachIndexed { idx, item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("${idx + 1}. ${item.name} (${item.dosage})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("${item.frequency} • ${item.instructions} • ${item.durationDays} Days", fontSize = 11.sp, color = MedTextSecondary)
                                            }
                                            IconButton(
                                                onClick = {
                                                    prescriptionItems = prescriptionItems.filterIndexed { i, _ -> i != idx }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MedError, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = doctorInstructions,
                            onValueChange = { doctorInstructions = it },
                            label = { Text("Doctor Advice & Lifestyle Instructions") },
                            modifier = Modifier.fillMaxWidth().height(90.dp)
                        )

                        // Issue & Transmit Button
                        Button(
                            onClick = {
                                // Save to DB for each prescribed drug
                                for (item in prescriptionItems) {
                                    viewModel.issuePrescription(
                                        patientId = selectedPatientId,
                                        patientName = selectedPatientName,
                                        medicineName = item.name,
                                        dosage = item.dosage,
                                        frequency = item.frequency,
                                        reminderTimes = item.reminderTimes,
                                        durationDays = item.durationDays,
                                        totalQuantity = 30,
                                        instructions = item.instructions,
                                        diagnosis = diagnosisText
                                    )
                                }

                                // Add to local history list
                                issuedPrescriptions = listOf(
                                    IssuedPrescriptionRecord(
                                        id = "rx_${System.currentTimeMillis()}",
                                        patientName = selectedPatientName,
                                        patientId = selectedPatientId,
                                        date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                                        diagnosis = diagnosisText,
                                        medicines = prescriptionItems
                                    )
                                ) + issuedPrescriptions

                                selectedTab = 1
                            },
                            enabled = prescriptionItems.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Issue & Sign Digital Prescription", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // TAB 2: PRESCRIPTION HISTORY
            items(issuedPrescriptions, key = { it.id }) { rx ->
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
                            Column {
                                Text(
                                    text = rx.patientName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MedTextPrimary
                                )
                                Text(
                                    text = "Issued on ${rx.date} • Rx #${rx.id}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary
                                )
                            }

                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE8F5E9)) {
                                Text(
                                    text = rx.status,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MedBackground,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Diagnosis: ${rx.diagnosis}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = MedBluePrimary)
                                Text("Medications (${rx.medicines.size}):", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                rx.medicines.forEach { med ->
                                    Text("• ${med.name} ${med.dosage} (${med.frequency} - ${med.instructions})", fontSize = 12.sp, color = MedTextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
