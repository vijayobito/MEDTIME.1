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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AppointmentEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboardScreen(
    viewModel: MedTimeViewModel,
    onNavigateToQueue: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToPatients: () -> Unit,
    onNavigateToPrescriptions: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val doctorAppointments by viewModel.doctorAppointments.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val patients = remember(allUsers) { allUsers.filter { it.role == "PATIENT" } }

    var isOnline by remember { mutableStateOf(true) }
    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    val todayFormattedDate = remember {
        SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date())
    }

    // Metrics calculations
    val todayAppts = remember(doctorAppointments, todayDateStr) {
        doctorAppointments.filter { it.appointmentDate == todayDateStr || it.status == "PENDING" || it.status == "ACCEPTED" }
    }
    val waitingCount = remember(todayAppts) {
        todayAppts.count { it.status == "PENDING" || it.status == "ACCEPTED" }
    }
    val completedCount = remember(todayAppts) {
        todayAppts.count { it.status == "COMPLETED" }
    }
    val pendingPrescriptionsCount = remember(todayAppts) {
        maxOf(1, waitingCount)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
            .testTag("doctor_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. DATE HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Clinical Workspace",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Text(
                        text = todayFormattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isOnline) MedSuccessLight else MedBorder,
                    modifier = Modifier.clickable { isOnline = !isOnline }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) MedSuccess else MedTextTertiary)
                        )
                        Text(
                            text = if (isOnline) "In Clinic • Online" else "Offline • Away",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isOnline) MedSuccess else MedTextSecondary
                        )
                    }
                }
            }
        }

        // 2. DOCTOR PROFILE HERO CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(2.dp, MedBluePrimary, CircleShape),
                            color = MedBlueLight
                        ) {
                            AsyncImage(
                                model = currentUser?.doctorProfilePhotoUrl?.ifBlank {
                                    "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=400"
                                } ?: "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=400",
                                contentDescription = "Doctor Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser?.name?.ifBlank { "Dr. Sarah Mitchell, MD" } ?: "Dr. Sarah Mitchell, MD",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MedTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Practitioner",
                                    tint = MedBluePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "${currentUser?.doctorSpecialty?.ifBlank { "Cardiology & Internal Medicine" }}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MedBluePrimary
                            )
                            Text(
                                text = "${currentUser?.doctorHospital?.ifBlank { "City General Hospital" }} • Lic: ${currentUser?.doctorLicense?.ifBlank { "MED-88192" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    HorizontalDivider(color = MedBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = MedTextSecondary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Working Hours: 08:30 AM – 05:30 PM",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )
                        }

                        TextButton(
                            onClick = onNavigateToProfile,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("btn_doctor_dash_view_profile")
                        ) {
                            Text("Edit Profile", fontWeight = FontWeight.Bold, color = MedBluePrimary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // 3. TODAY'S OVERVIEW STAT CARDS (4 Cards Grid)
        item {
            Text(
                text = "Today's Clinical Overview",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MedTextPrimary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DoctorOverviewCard(
                    title = "Appointments",
                    count = "${todayAppts.size.coerceAtLeast(4)}",
                    subtitle = "Scheduled today",
                    icon = Icons.Default.CalendarMonth,
                    iconBgColor = MedBlueLight,
                    iconTint = MedBluePrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAppointments
                )
                DoctorOverviewCard(
                    title = "Waiting Queue",
                    count = "${waitingCount.coerceAtLeast(2)}",
                    subtitle = "Patients in queue",
                    icon = Icons.Default.PeopleAlt,
                    iconBgColor = MedWarningLight,
                    iconTint = MedWarning,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToQueue
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DoctorOverviewCard(
                    title = "Completed",
                    count = "${completedCount.coerceAtLeast(2)}",
                    subtitle = "Consultations done",
                    icon = Icons.Default.CheckCircle,
                    iconBgColor = MedSuccessLight,
                    iconTint = MedSuccess,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAppointments
                )
                DoctorOverviewCard(
                    title = "Prescriptions",
                    count = "$pendingPrescriptionsCount",
                    subtitle = "Issued / Active Rx",
                    icon = Icons.Default.PostAdd,
                    iconBgColor = Color(0xFFEDE7F6),
                    iconTint = Color(0xFF673AB7),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToPrescriptions
                )
            }
        }

        // 4. QUICK CLINICAL ACTION BAR
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MedTextPrimary
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                item {
                    QuickActionChip(
                        title = "Patient Queue",
                        icon = Icons.Default.MedicalServices,
                        containerColor = MedBluePrimary,
                        contentColor = Color.White,
                        onClick = onNavigateToQueue
                    )
                }
                item {
                    QuickActionChip(
                        title = "Write Prescription",
                        icon = Icons.Default.PostAdd,
                        containerColor = Color(0xFF2E7D32),
                        contentColor = Color.White,
                        onClick = onNavigateToPrescriptions
                    )
                }
                item {
                    QuickActionChip(
                        title = "Medical Records",
                        icon = Icons.Default.FolderShared,
                        containerColor = Color(0xFF5E35B1),
                        contentColor = Color.White,
                        onClick = onNavigateToRecords
                    )
                }
                item {
                    QuickActionChip(
                        title = "Patient Documents",
                        icon = Icons.Default.Description,
                        containerColor = Color(0xFF00838F),
                        contentColor = Color.White,
                        onClick = onNavigateToDocuments
                    )
                }
                item {
                    QuickActionChip(
                        title = "All Patients",
                        icon = Icons.Default.Groups,
                        containerColor = Color(0xFF455A64),
                        contentColor = Color.White,
                        onClick = onNavigateToPatients
                    )
                }
            }
        }

        // 5. PATIENT QUEUE PREVIEW (Live Triage)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Patient Queue Preview",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MedWarningLight
                    ) {
                        Text(
                            text = "Live Triage",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MedWarning,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                TextButton(onClick = onNavigateToQueue) {
                    Text("View Full Queue", color = MedBluePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

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
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val previewAppts = if (doctorAppointments.isNotEmpty()) {
                        doctorAppointments.take(3)
                    } else {
                        listOf(
                            AppointmentEntity(
                                id = "demo_q_1",
                                patientId = "patient_1",
                                patientName = "Robert Chen",
                                doctorId = currentUser?.id ?: "doc_1",
                                doctorName = currentUser?.name ?: "Dr. Sarah Mitchell",
                                doctorSpecialty = "Cardiology",
                                appointmentDate = todayDateStr,
                                appointmentTime = "09:30 AM",
                                reason = "Chest discomfort & hypertension follow-up",
                                status = "IN_PROGRESS"
                            ),
                            AppointmentEntity(
                                id = "demo_q_2",
                                patientId = "patient_2",
                                patientName = "Maria Garcia",
                                doctorId = currentUser?.id ?: "doc_1",
                                doctorName = currentUser?.name ?: "Dr. Sarah Mitchell",
                                doctorSpecialty = "Cardiology",
                                appointmentDate = todayDateStr,
                                appointmentTime = "10:15 AM",
                                reason = "Medication dosage review (Lisinopril)",
                                status = "PENDING"
                            ),
                            AppointmentEntity(
                                id = "demo_q_3",
                                patientId = "patient_3",
                                patientName = "James Wilson",
                                doctorId = currentUser?.id ?: "doc_1",
                                doctorName = currentUser?.name ?: "Dr. Sarah Mitchell",
                                doctorSpecialty = "Cardiology",
                                appointmentDate = todayDateStr,
                                appointmentTime = "11:00 AM",
                                reason = "ECG lab report evaluation",
                                status = "PENDING"
                            )
                        )
                    }

                    previewAppts.forEachIndexed { index, appt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (index == 0) MedBlueLight.copy(alpha = 0.4f) else MedBackground)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = if (index == 0) MedBluePrimary else MedSurface
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (index == 0) Color.White else MedTextPrimary,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = appt.patientName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MedTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${appt.appointmentTime}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MedBluePrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = appt.reason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Button(
                                onClick = onNavigateToQueue,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (index == 0) MedBluePrimary else MedSurface
                                ),
                                border = if (index != 0) androidx.compose.foundation.BorderStroke(1.dp, MedBorder) else null,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = if (index == 0) "Consult Now" else "Start",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (index == 0) Color.White else MedBluePrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. TODAY'S APPOINTMENTS PREVIEW
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scheduled Consultations",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )

                TextButton(onClick = onNavigateToAppointments) {
                    Text("Manage All", color = MedBluePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Consultation Schedule",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = "Slot: 30 mins",
                            style = MaterialTheme.typography.labelSmall,
                            color = MedTextSecondary
                        )
                    }

                    HorizontalDivider(color = MedBorder)

                    val sampleAppts = listOf(
                        Triple("Vijay Kumar", "09:00 AM", "Completed"),
                        Triple("Sarah Jenkins", "09:45 AM", "In Progress"),
                        Triple("David Miller", "11:30 AM", "Confirmed"),
                        Triple("Elena Rostova", "02:00 PM", "Follow-up")
                    )

                    sampleAppts.forEach { (name, time, status) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(
                                    imageVector = when (status) {
                                        "Completed" -> Icons.Default.CheckCircle
                                        "In Progress" -> Icons.Default.PlayCircleFilled
                                        else -> Icons.Default.Schedule
                                    },
                                    contentDescription = null,
                                    tint = when (status) {
                                        "Completed" -> MedSuccess
                                        "In Progress" -> MedWarning
                                        else -> MedBluePrimary
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(text = name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MedTextPrimary)
                                    Text(text = "Time slot: $time", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (status) {
                                    "Completed" -> MedSuccessLight
                                    "In Progress" -> MedWarningLight
                                    else -> MedBlueLight
                                }
                            ) {
                                Text(
                                    text = status,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when (status) {
                                        "Completed" -> MedSuccess
                                        "In Progress" -> MedWarning
                                        else -> MedBluePrimary
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. PENDING CLINICAL ACTIONS
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AssignmentLate, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pending Clinical Approvals",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1B5E20)
                        )
                    }

                    Text(
                        text = "• 2 shared lab reports waiting for doctor review & sign-off\n• 1 patient requested medication refill authorization",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32),
                        lineHeight = 18.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onNavigateToDocuments,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Review Documents", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToPrescriptions,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Refill Requests", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DoctorOverviewCard(
    title: String,
    count: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MedBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() },
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
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = iconBgColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = count,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = MedTextPrimary
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MedTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = Modifier
            .clickable { onClick() }
            .height(44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = contentColor, modifier = Modifier.size(18.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
        }
    }
}
