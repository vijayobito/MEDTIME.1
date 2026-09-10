package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.model.AppointmentEntity
import com.example.ui.components.AppointmentCard
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@Composable
fun DoctorScreen(
    viewModel: MedTimeViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val appointments by viewModel.doctorAppointments.collectAsState()

    var appointmentNotesDialog by remember { mutableStateOf<AppointmentEntity?>(null) }
    var clinicalNotesText by remember { mutableStateOf("") }

    if (appointmentNotesDialog != null) {
        val appt = appointmentNotesDialog!!
        AlertDialog(
            onDismissRequest = { appointmentNotesDialog = null },
            title = { Text("Complete Consultation with ${appt.patientName}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter post-consultation doctor recommendations, diagnostic notes, or prescription adjustments:")
                    OutlinedTextField(
                        value = clinicalNotesText,
                        onValueChange = { clinicalNotesText = it },
                        label = { Text("Doctor Clinical Notes") },
                        placeholder = { Text("e.g. Continue Lisinopril 10mg daily. Patient BP controlled.") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAppointmentStatus(appt.id, "COMPLETED", clinicalNotesText)
                        appointmentNotesDialog = null
                        clinicalNotesText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedInfo)
                ) {
                    Text("Save & Complete")
                }
            },
            dismissButton = {
                TextButton(onClick = { appointmentNotesDialog = null }) {
                    Text("Cancel")
                }
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
        // Doctor Verification Profile Banner
        item {
            val isVerified = currentUser?.isDoctorVerified == true
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = if (isVerified) MedBluePrimary else Color(0xFFE65100)
                )
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
                                text = "Doctor Clinical Portal",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = currentUser?.name ?: "Dr. Specialist",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "${currentUser?.doctorSpecialty} • ${currentUser?.doctorHospital}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isVerified) Icons.Default.Verified else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isVerified) "Verified MD" else "Pending Review",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Medical License: ${currentUser?.doctorLicense ?: "LIC-348912"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Stats Row
        item {
            val pendingCount = appointments.count { it.status == "PENDING" }
            val acceptedCount = appointments.count { it.status == "ACCEPTED" }
            val completedCount = appointments.count { it.status == "COMPLETED" }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Pending",
                    value = pendingCount.toString(),
                    subtitle = "Needs review",
                    icon = Icons.Default.Schedule,
                    containerColor = MedWarningLight,
                    contentColor = MedWarning,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Scheduled",
                    value = acceptedCount.toString(),
                    subtitle = "Upcoming",
                    icon = Icons.Default.CalendarToday,
                    containerColor = MedBlueLight,
                    contentColor = MedBlueDark,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Done",
                    value = completedCount.toString(),
                    subtitle = "Completed",
                    icon = Icons.Default.CheckCircle,
                    containerColor = MedSuccessLight,
                    contentColor = MedSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Consultation Requests
        item {
            Text(
                text = "Patient Consultation Queue (${appointments.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MedTextPrimary
            )
        }

        if (appointments.isEmpty()) {
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
                        Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MedTextTertiary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No Consultation Requests", fontWeight = FontWeight.Bold)
                        Text("When patients book appointments with you, they will appear here.", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                    }
                }
            }
        } else {
            items(appointments, key = { it.id }) { appt ->
                AppointmentCard(
                    appointment = appt,
                    isDoctorView = true,
                    onAccept = {
                        viewModel.updateAppointmentStatus(appt.id, "ACCEPTED")
                    },
                    onReject = {
                        viewModel.updateAppointmentStatus(appt.id, "REJECTED", "Doctor unavailable")
                    },
                    onComplete = {
                        clinicalNotesText = ""
                        appointmentNotesDialog = appt
                    }
                )
            }
        }
    }
}
