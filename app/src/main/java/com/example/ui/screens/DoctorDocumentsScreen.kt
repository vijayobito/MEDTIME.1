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

data class DoctorDocumentItem(
    val id: String,
    val title: String,
    val patientName: String,
    val patientId: String,
    val category: String, // "Lab Report", "Prescription", "Scan / Imaging", "Discharge Summary"
    val dateAdded: String,
    val fileSize: String,
    val isReviewed: Boolean = true,
    val reviewNotes: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDocumentsScreen(
    viewModel: MedTimeViewModel,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPreviewDoc by remember { mutableStateOf<DoctorDocumentItem?>(null) }
    var reviewNoteInput by remember { mutableStateOf("") }

    val categories = listOf("ALL", "Lab Report", "Prescription", "Scan / Imaging", "Discharge Summary")

    var documentList by remember {
        mutableStateOf(
            listOf(
                DoctorDocumentItem(
                    id = "doc_1",
                    title = "Comprehensive Metabolic Panel (CMP).pdf",
                    patientName = "Vijay Kumar",
                    patientId = "patient_1",
                    category = "Lab Report",
                    dateAdded = "2026-09-18",
                    fileSize = "1.8 MB",
                    isReviewed = true,
                    reviewNotes = "Kidney and liver function tests within normal range. Electrolytes stable."
                ),
                DoctorDocumentItem(
                    id = "doc_2",
                    title = "ECG Electrocardiogram Rhythm 12-Lead Scan.pdf",
                    patientName = "Vijay Kumar",
                    patientId = "patient_1",
                    category = "Scan / Imaging",
                    dateAdded = "2026-09-12",
                    fileSize = "2.4 MB",
                    isReviewed = true,
                    reviewNotes = "Normal sinus rhythm at 72 bpm. PR interval 160ms. No ST-segment deviations."
                ),
                DoctorDocumentItem(
                    id = "doc_3",
                    title = "HbA1c & Fasting Blood Glucose Report.pdf",
                    patientName = "Robert Chen",
                    patientId = "patient_2",
                    category = "Lab Report",
                    dateAdded = "2026-09-10",
                    fileSize = "1.2 MB",
                    isReviewed = false,
                    reviewNotes = "HbA1c at 7.1%. Needs dosage adjustment for Metformin."
                ),
                DoctorDocumentItem(
                    id = "doc_4",
                    title = "Cardiac Echo Doppler Ultrasound Imaging.pdf",
                    patientName = "Robert Chen",
                    patientId = "patient_2",
                    category = "Scan / Imaging",
                    dateAdded = "2026-08-25",
                    fileSize = "4.6 MB",
                    isReviewed = true,
                    reviewNotes = "Left ventricular ejection fraction (LVEF) 60%. Mild concentric LVH."
                ),
                DoctorDocumentItem(
                    id = "doc_5",
                    title = "Hospital Discharge & Cardiology Transition Plan.pdf",
                    patientName = "James Wilson",
                    patientId = "patient_4",
                    category = "Discharge Summary",
                    dateAdded = "2026-08-15",
                    fileSize = "3.1 MB",
                    isReviewed = true,
                    reviewNotes = "Post-PCI discharge instructions verified. Anticoagulation protocol active."
                )
            )
        )
    }

    val filteredDocs = remember(documentList, selectedCategory, searchQuery) {
        var list = if (selectedCategory == "ALL") documentList else documentList.filter { it.category == selectedCategory }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter { it.title.lowercase().contains(q) || it.patientName.lowercase().contains(q) }
        }
        list
    }

    if (selectedPreviewDoc != null) {
        val doc = selectedPreviewDoc!!
        AlertDialog(
            onDismissRequest = { selectedPreviewDoc = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFE53935))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Document Preview & Clinical Review", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(10.dp)),
                        color = MedBackground
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(40.dp))
                                Text(doc.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTextPrimary)
                                Text("Patient: ${doc.patientName} • Size: ${doc.fileSize}", fontSize = 11.sp, color = MedTextSecondary)
                            }
                        }
                    }

                    Text("Doctor Clinical Review Notes:", fontWeight = FontWeight.Bold, color = MedTextPrimary)
                    OutlinedTextField(
                        value = reviewNoteInput.ifBlank { doc.reviewNotes },
                        onValueChange = { reviewNoteInput = it },
                        placeholder = { Text("Add doctor review notes & instructions for patient...") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        documentList = documentList.map {
                            if (it.id == doc.id) it.copy(isReviewed = true, reviewNotes = if (reviewNoteInput.isNotBlank()) reviewNoteInput else it.reviewNotes) else it
                        }
                        selectedPreviewDoc = null
                        reviewNoteInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Text("Save Review & Sign Off")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPreviewDoc = null }) {
                    Text("Close")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
            .testTag("doctor_documents_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Patient Shared Documents",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                Text(
                    text = "Review shared lab reports, prescriptions, scans and discharge records",
                    style = MaterialTheme.typography.bodySmall,
                    color = MedTextSecondary
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by document title or patient name...") },
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

        // Categories Row
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MedBluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Documents list
        items(filteredDocs, key = { it.id }) { doc ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(16.dp))
                    .clickable {
                        reviewNoteInput = doc.reviewNotes
                        selectedPreviewDoc = doc
                    },
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = when (doc.category) {
                            "Lab Report" -> Color(0xFFE8F5E9)
                            "Scan / Imaging" -> Color(0xFFE3F2FD)
                            else -> Color(0xFFEDE7F6)
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (doc.category) {
                                    "Lab Report" -> Icons.Default.Science
                                    "Scan / Imaging" -> Icons.Default.CameraAlt
                                    else -> Icons.Default.Description
                                },
                                contentDescription = null,
                                tint = when (doc.category) {
                                    "Lab Report" -> Color(0xFF2E7D32)
                                    "Scan / Imaging" -> MedBluePrimary
                                    else -> Color(0xFF673AB7)
                                },
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = doc.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Patient: ${doc.patientName} • ${doc.dateAdded} • ${doc.fileSize}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                        if (doc.reviewNotes.isNotBlank()) {
                            Text(
                                text = "Notes: ${doc.reviewNotes}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedBluePrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (doc.isReviewed) MedSuccessLight else MedWarningLight
                    ) {
                        Text(
                            text = if (doc.isReviewed) "REVIEWED" else "PENDING",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (doc.isReviewed) MedSuccess else MedWarning,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
