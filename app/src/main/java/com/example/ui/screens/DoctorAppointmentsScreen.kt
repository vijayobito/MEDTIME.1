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
import com.example.data.model.AppointmentEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorAppointmentsScreen(
    viewModel: MedTimeViewModel,
    onNavigateToPrescribe: (patientId: String, patientName: String) -> Unit = { _, _ -> },
    onNavigateToChat: (patientId: String, patientName: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val doctorAppointments by viewModel.doctorAppointments.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val tabs = listOf("Today", "Upcoming", "Completed", "Cancelled")

    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Default sample appointments if database has few
    val combinedAppointments = remember(doctorAppointments) {
        if (doctorAppointments.isNotEmpty()) {
            doctorAppointments
        } else {
            listOf(
                AppointmentEntity(
                    id = "appt_doc_1",
                    patientId = "patient_1",
                    patientName = "Robert Chen",
                    doctorId = "doc_current",
                    doctorName = "Dr. Sarah Mitchell",
                    doctorSpecialty = "Cardiology",
                    appointmentDate = todayDateStr,
                    appointmentTime = "09:30 AM",
                    reason = "Hypertension management & chest pain review",
                    status = "ACCEPTED"
                ),
                AppointmentEntity(
                    id = "appt_doc_2",
                    patientId = "patient_2",
                    patientName = "Maria Garcia",
                    doctorId = "doc_current",
                    doctorName = "Dr. Sarah Mitchell",
                    doctorSpecialty = "Cardiology",
                    appointmentDate = todayDateStr,
                    appointmentTime = "11:00 AM",
                    reason = "Diabetes review and lab results evaluation",
                    status = "PENDING"
                ),
                AppointmentEntity(
                    id = "appt_doc_3",
                    patientId = "patient_3",
                    patientName = "James Wilson",
                    doctorId = "doc_current",
                    doctorName = "Dr. Sarah Mitchell",
                    doctorSpecialty = "Cardiology",
                    appointmentDate = "2026-09-25",
                    appointmentTime = "02:00 PM",
                    reason = "Post-op cardiovascular follow up",
                    status = "ACCEPTED"
                ),
                AppointmentEntity(
                    id = "appt_doc_4",
                    patientId = "patient_4",
                    patientName = "Elena Rostova",
                    doctorId = "doc_current",
                    doctorName = "Dr. Sarah Mitchell",
                    doctorSpecialty = "Cardiology",
                    appointmentDate = "2026-09-26",
                    appointmentTime = "04:30 PM",
                    reason = "Sinus tachycardia evaluation",
                    status = "ACCEPTED"
                ),
                AppointmentEntity(
                    id = "appt_doc_5",
                    patientId = "patient_5",
                    patientName = "Vijay Kumar",
                    doctorId = "doc_current",
                    doctorName = "Dr. Sarah Mitchell",
                    doctorSpecialty = "Cardiology",
                    appointmentDate = todayDateStr,
                    appointmentTime = "08:30 AM",
                    reason = "Routine cardiac checkup & ECG",
                    status = "COMPLETED"
                )
            )
        }
    }

    val filteredList = remember(combinedAppointments, selectedTab, searchQuery) {
        val list = when (selectedTab) {
            0 -> combinedAppointments.filter { it.appointmentDate == todayDateStr || it.status == "ACCEPTED" || it.status == "PENDING" }
            1 -> combinedAppointments.filter { it.appointmentDate > todayDateStr || (it.status == "ACCEPTED" && it.appointmentDate != todayDateStr) }
            2 -> combinedAppointments.filter { it.status == "COMPLETED" }
            3 -> combinedAppointments.filter { it.status == "CANCELLED" || it.status == "REJECTED" }
            else -> combinedAppointments
        }
        if (searchQuery.isBlank()) list
        else list.filter { it.patientName.contains(searchQuery, ignoreCase = true) || it.reason.contains(searchQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
            .testTag("doctor_appointments_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Doctor Appointments",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                Text(
                    text = "Manage consultations, schedules & patient requests",
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
                placeholder = { Text("Search appointments by patient name or reason...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MedTextSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MedSurface,
                    unfocusedContainerColor = MedSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Tab Navigation
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MedSurface,
                contentColor = MedBluePrimary,
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

        // Items list
        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MedSurface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, tint = MedTextTertiary, modifier = Modifier.size(48.dp))
                        Text(
                            text = "No appointments in this category",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = "New bookings and patient requests will appear here automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                    }
                }
            }
        } else {
            items(filteredList, key = { it.id }) { appt ->
                DoctorAppointmentCard(
                    appointment = appt,
                    onAccept = {
                        viewModel.updateAppointmentStatus(appt.id, "ACCEPTED")
                    },
                    onComplete = {
                        viewModel.updateAppointmentStatus(appt.id, "COMPLETED")
                    },
                    onCancel = {
                        viewModel.updateAppointmentStatus(appt.id, "CANCELLED")
                    },
                    onPrescribe = {
                        onNavigateToPrescribe(appt.patientId, appt.patientName)
                    },
                    onChat = {
                        onNavigateToChat(appt.patientId, appt.patientName)
                    }
                )
            }
        }
    }
}

@Composable
private fun DoctorAppointmentCard(
    appointment: AppointmentEntity,
    onAccept: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onPrescribe: () -> Unit,
    onChat: () -> Unit
) {
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = MedBlueLight,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = appointment.patientName.take(1),
                                fontWeight = FontWeight.Bold,
                                color = MedBluePrimary,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Column {
                        Text(
                            text = appointment.patientName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = "Date: ${appointment.appointmentDate} • ${appointment.appointmentTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedBluePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (appointment.status) {
                        "ACCEPTED" -> MedSuccessLight
                        "COMPLETED" -> Color(0xFFEDE7F6)
                        "CANCELLED", "REJECTED" -> MedErrorLight
                        else -> MedWarningLight
                    }
                ) {
                    Text(
                        text = appointment.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (appointment.status) {
                            "ACCEPTED" -> MedSuccess
                            "COMPLETED" -> Color(0xFF673AB7)
                            "CANCELLED", "REJECTED" -> MedError
                            else -> MedWarning
                        }
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MedBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Reason for Visit",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedTextSecondary
                    )
                    Text(
                        text = appointment.reason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MedTextPrimary
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (appointment.status) {
                    "PENDING" -> {
                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MedSuccess)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Confirm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MedError),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MedError)
                        ) {
                            Text("Decline", fontSize = 12.sp)
                        }
                    }
                    "ACCEPTED" -> {
                        Button(
                            onClick = onComplete,
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Complete Visit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onPrescribe,
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32))
                        ) {
                            Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Prescribe", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "COMPLETED" -> {
                        OutlinedButton(
                            onClick = onPrescribe,
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View / Add Rx", fontSize = 12.sp)
                        }
                        IconButton(onClick = onChat, modifier = Modifier.size(38.dp)) {
                            Icon(Icons.Default.Chat, contentDescription = "Chat", tint = MedBluePrimary)
                        }
                    }
                    else -> {
                        OutlinedButton(
                            onClick = onAccept,
                            modifier = Modifier.fillMaxWidth().height(38.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Re-open Appointment", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
