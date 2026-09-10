package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppointmentCard
import com.example.ui.components.BookAppointmentDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    viewModel: MedTimeViewModel,
    onOpenChatWithDoctor: (doctorId: String, doctorName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val appointments by viewModel.patientAppointments.collectAsState()
    val doctors by viewModel.doctors.collectAsState()

    var selectedFilterIndex by remember { mutableIntStateOf(0) } // 0 = Upcoming, 1 = All, 2 = Past
    var showBookDialog by remember { mutableStateOf(false) }

    // Dialog for scheduling appointment
    if (showBookDialog) {
        BookAppointmentDialog(
            doctors = doctors,
            onDismiss = { showBookDialog = false },
            onBook = { docId, docName, spec, date, time, reason, reminderEnabled, reminderMinutesBefore ->
                viewModel.bookAppointment(
                    doctorId = docId,
                    doctorName = docName,
                    doctorSpecialty = spec,
                    date = date,
                    time = time,
                    reason = reason,
                    reminderEnabled = reminderEnabled,
                    reminderMinutesBefore = reminderMinutesBefore
                )
            }
        )
    }

    val upcomingAppointments = appointments.filter { it.status == "PENDING" || it.status == "ACCEPTED" }
    val pastAppointments = appointments.filter { it.status == "COMPLETED" || it.status == "CANCELLED" || it.status == "REJECTED" }
    val reminderActiveCount = upcomingAppointments.count { it.reminderEnabled }

    val displayedAppointments = when (selectedFilterIndex) {
        0 -> upcomingAppointments
        1 -> appointments
        2 -> pastAppointments
        else -> appointments
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showBookDialog = true },
                containerColor = MedBluePrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = "Book Appointment") },
                text = { Text("Book Appointment", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_book_appointment")
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(MedBackground)
        ) {
            // Header Overview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MedSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MedBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MedBluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Doctor Appointments",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MedTextPrimary
                                )
                                Text(
                                    text = "Consultations & timely reminders",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = { showBookDialog = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("button_schedule_consultation")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Book", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Stat Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MedBackground, RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp, horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${upcomingAppointments.size}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedBluePrimary
                            )
                            Text("Upcoming", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                        }
                        Divider(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp),
                            color = MedBorder
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$reminderActiveCount",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedSuccess
                            )
                            Text("Reminders Active", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                        }
                        Divider(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp),
                            color = MedBorder
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${doctors.size}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text("Specialists", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                        }
                    }
                }
            }

            // Tab navigation
            TabRow(
                selectedTabIndex = selectedFilterIndex,
                containerColor = MedSurface,
                contentColor = MedBluePrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedFilterIndex == 0,
                    onClick = { selectedFilterIndex = 0 },
                    text = { Text("Upcoming (${upcomingAppointments.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_upcoming_appointments")
                )
                Tab(
                    selected = selectedFilterIndex == 1,
                    onClick = { selectedFilterIndex = 1 },
                    text = { Text("All (${appointments.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_all_appointments")
                )
                Tab(
                    selected = selectedFilterIndex == 2,
                    onClick = { selectedFilterIndex = 2 },
                    text = { Text("Past (${pastAppointments.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_past_appointments")
                )
            }

            // List or Empty View
            if (displayedAppointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MedSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MedBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventNote,
                                    contentDescription = null,
                                    tint = MedBluePrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (selectedFilterIndex == 0) "No Upcoming Appointments" else "No Appointments Found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Schedule consultations with verified specialists and get automatic reminder notifications.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = { showBookDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("button_empty_book_appointment")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Schedule Consultation Now")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("list_appointments"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(displayedAppointments, key = { it.id }) { appt ->
                        AppointmentCard(
                            appointment = appt,
                            isDoctorView = false,
                            onCancel = {
                                viewModel.updateAppointmentStatus(appt.id, "CANCELLED", "Cancelled by patient")
                            },
                            onToggleReminder = {
                                viewModel.toggleAppointmentReminder(appt)
                            },
                            onChat = {
                                onOpenChatWithDoctor(appt.doctorId, appt.doctorName)
                            },
                            modifier = Modifier.testTag("appointment_card_${appt.id}")
                        )
                    }
                }
            }
        }
    }
}
