package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CaretakerLinkEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerPermissionsScreen(
    viewModel: MedTimeViewModel,
    initialPatientId: String = "",
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val caretakerLinks by viewModel.caretakerLinks.collectAsState()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Patient Permissions", fontWeight = FontWeight.Bold, color = MedTextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MedTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MedSurface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MedBackground)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Patient selector chips
            if (approvedLinks.size > 1) {
                item {
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

            // Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MedBlueLight), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Authorization Matrix", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Patient: ${currentLink?.patientName ?: "Vijay Kumar"}", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            }
                        }
                        Text(
                            text = "To ensure patient privacy, permission settings are granted directly by the patient. The list below shows the current access granted to your Caretaker account.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                    }
                }
            }

            // Matrix items
            item {
                Text(
                    text = "Current Access Privileges",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
            }

            item {
                PermissionItemCard(
                    title = "Medicine Schedule & Reminders",
                    description = "View daily medication doses, timings, and intake instructions.",
                    isGranted = currentLink?.canViewMedicines ?: true,
                    icon = Icons.Default.Medication
                )
            }

            item {
                PermissionItemCard(
                    title = "Adherence Tracking & History",
                    description = "View real-time taken, missed, and skipped dose adherence statistics.",
                    isGranted = currentLink?.canViewAdherence ?: true,
                    icon = Icons.Default.Assessment
                )
            }

            item {
                PermissionItemCard(
                    title = "Doctor Appointments & Schedules",
                    description = "View upcoming clinic appointments and physician consultation schedules.",
                    isGranted = currentLink?.canViewAppointments ?: true,
                    icon = Icons.Default.CalendarMonth
                )
            }

            item {
                PermissionItemCard(
                    title = "Emergency & Missed Dose Alerts",
                    description = "Receive instant push and SMS alerts when a scheduled dose is missed.",
                    isGranted = currentLink?.canReceiveAlerts ?: true,
                    icon = Icons.Default.NotificationsActive
                )
            }

            item {
                PermissionItemCard(
                    title = "Clinical Prescriptions & Lab Documents",
                    description = "Access uploaded diagnostic PDFs, lab reports, and doctor prescriptions.",
                    isGranted = currentLink?.canViewDocuments ?: true,
                    icon = Icons.Default.PictureAsPdf
                )
            }

            item {
                PermissionItemCard(
                    title = "Patient Live GPS Location",
                    description = "Track patient location for home care visits and emergency dispatch.",
                    isGranted = currentLink?.canViewLocation ?: false,
                    icon = Icons.Default.LocationOn
                )
            }
        }
    }
}

@Composable
private fun PermissionItemCard(
    title: String,
    description: String,
    isGranted: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MedSurface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isGranted) MedSuccessLight else MedSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) MedSuccess else MedTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isGranted) MedSuccessLight else MedSurfaceVariant
            ) {
                Text(
                    text = if (isGranted) "Granted" else "Restricted",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isGranted) MedSuccess else MedTextSecondary
                )
            }
        }
    }
}
