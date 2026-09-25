package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppointmentEntity
import com.example.ui.components.AppointmentCard
import com.example.ui.components.BookAppointmentDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    viewModel: MedTimeViewModel,
    onOpenChatWithDoctor: (doctorId: String, doctorName: String) -> Unit,
    onNavigateToMaps: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val appointments by viewModel.patientAppointments.collectAsState()
    val doctors by viewModel.doctors.collectAsState()

    var selectedFilterIndex by remember { mutableIntStateOf(0) } // 0 = Upcoming, 1 = All, 2 = Past, 3 = Calendar View
    var showBookDialog by remember { mutableStateOf(false) }
    var preselectedDateForBooking by remember { mutableStateOf<String?>(null) }
    var calendarSelectedDate by remember { mutableStateOf<String?>(null) }
    var currentCalendarMonth by remember { mutableStateOf(Calendar.getInstance()) }

    // Dialog for scheduling appointment
    if (showBookDialog) {
        BookAppointmentDialog(
            doctors = doctors,
            initialDate = preselectedDateForBooking,
            onDismiss = {
                showBookDialog = false
                preselectedDateForBooking = null
            },
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
                preselectedDateForBooking = null
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
        3 -> {
            if (calendarSelectedDate != null) {
                appointments.filter { it.appointmentDate == calendarSelectedDate }
            } else {
                upcomingAppointments
            }
        }
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

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = onNavigateToMaps,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("button_view_appointment_maps")
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Map & Routes", fontSize = 11.sp, color = MedBluePrimary, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = { showBookDialog = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("button_schedule_consultation")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Book", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
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
            ScrollableTabRow(
                selectedTabIndex = selectedFilterIndex,
                containerColor = MedSurface,
                contentColor = MedBluePrimary,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedFilterIndex == 0,
                    onClick = { selectedFilterIndex = 0 },
                    text = { Text("Calendar View", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("tab_calendar_view")
                )
                Tab(
                    selected = selectedFilterIndex == 1,
                    onClick = { selectedFilterIndex = 1 },
                    text = { Text("Upcoming (${upcomingAppointments.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_upcoming_appointments")
                )
                Tab(
                    selected = selectedFilterIndex == 2,
                    onClick = { selectedFilterIndex = 2 },
                    text = { Text("All (${appointments.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_all_appointments")
                )
                Tab(
                    selected = selectedFilterIndex == 3,
                    onClick = { selectedFilterIndex = 3 },
                    text = { Text("Past (${pastAppointments.size})", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_past_appointments")
                )
            }

            if (selectedFilterIndex == 0) {
                // Calendar Layout Mode
                AppointmentsCalendarSection(
                    appointments = appointments,
                    currentMonth = currentCalendarMonth,
                    selectedDate = calendarSelectedDate,
                    onDateSelected = { date ->
                        calendarSelectedDate = if (calendarSelectedDate == date) null else date
                    },
                    onMonthChange = { newMonth ->
                        currentCalendarMonth = newMonth
                    },
                    onBookForDate = { date ->
                        preselectedDateForBooking = date
                        showBookDialog = true
                    },
                    onOpenChatWithDoctor = onOpenChatWithDoctor,
                    onNavigateToMaps = onNavigateToMaps,
                    onCancelAppointment = { id ->
                        viewModel.updateAppointmentStatus(id, "CANCELLED", "Cancelled by patient")
                    },
                    onToggleReminder = { appt ->
                        viewModel.toggleAppointmentReminder(appt)
                    }
                )
            } else {
                // List Mode
                val listToDisplay = when (selectedFilterIndex) {
                    1 -> upcomingAppointments
                    2 -> appointments
                    3 -> pastAppointments
                    else -> appointments
                }

                if (listToDisplay.isEmpty()) {
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
                                    text = if (selectedFilterIndex == 1) "No Upcoming Appointments" else "No Appointments Found",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MedTextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Schedule consultations with verified specialists and get automatic reminder notifications.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary,
                                    textAlign = TextAlign.Center
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
                        items(listToDisplay, key = { it.id }) { appt ->
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
                                onViewRoute = onNavigateToMaps,
                                modifier = Modifier.testTag("appointment_card_${appt.id}")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentsCalendarSection(
    appointments: List<AppointmentEntity>,
    currentMonth: Calendar,
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    onMonthChange: (Calendar) -> Unit,
    onBookForDate: (String) -> Unit,
    onOpenChatWithDoctor: (doctorId: String, doctorName: String) -> Unit,
    onNavigateToMaps: () -> Unit,
    onCancelAppointment: (String) -> Unit,
    onToggleReminder: (AppointmentEntity) -> Unit
) {
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayStr = remember { dayFormat.format(Date()) }

    // Compute month grid days
    val cal = remember(currentMonth) {
        (currentMonth.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday...

    // Count appointments by date
    val appointmentCountsByDate = remember(appointments) {
        appointments.groupBy { it.appointmentDate }
    }

    val selectedDayAppointments = remember(selectedDate, appointments) {
        if (selectedDate != null) {
            appointments.filter { it.appointmentDate == selectedDate }
        } else {
            appointments.filter { it.status == "PENDING" || it.status == "ACCEPTED" }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Calendar Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MedSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val prev = (currentMonth.clone() as Calendar).apply {
                                    add(Calendar.MONTH, -1)
                                }
                                onMonthChange(prev)
                            },
                            modifier = Modifier.testTag("btn_prev_month")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month", tint = MedBluePrimary)
                        }

                        Text(
                            text = monthYearFormat.format(currentMonth.time),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )

                        IconButton(
                            onClick = {
                                val next = (currentMonth.clone() as Calendar).apply {
                                    add(Calendar.MONTH, 1)
                                }
                                onMonthChange(next)
                            },
                            modifier = Modifier.testTag("btn_next_month")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", tint = MedBluePrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Day of Week Header Row (Sun to Sat)
                    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        daysOfWeek.forEach { dayName ->
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextSecondary,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MedDivider)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Grid
                    val totalCells = (firstDayOfWeek - 1) + daysInMonth
                    val numRows = (totalCells + 6) / 7

                    for (row in 0 until numRows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (col in 0 until 7) {
                                val cellIndex = row * 7 + col
                                val dayNum = cellIndex - (firstDayOfWeek - 1) + 1

                                if (dayNum in 1..daysInMonth) {
                                    val cellCal = (currentMonth.clone() as Calendar).apply {
                                        set(Calendar.DAY_OF_MONTH, dayNum)
                                    }
                                    val cellDateStr = dayFormat.format(cellCal.time)
                                    val isSelected = cellDateStr == selectedDate
                                    val isToday = cellDateStr == todayStr
                                    val dayAppointments = appointmentCountsByDate[cellDateStr].orEmpty()
                                    val hasAppointments = dayAppointments.isNotEmpty()

                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> MedBluePrimary
                                                    isToday -> MedBlueLight
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                                color = if (isToday && !isSelected) MedBluePrimary else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                onDateSelected(cellDateStr)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$dayNum",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal),
                                                color = when {
                                                    isSelected -> Color.White
                                                    isToday -> MedBluePrimary
                                                    else -> MedTextPrimary
                                                },
                                                fontSize = 12.sp
                                            )

                                            if (hasAppointments) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color.White else MedWarning)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Empty padding cell
                                    Spacer(modifier = Modifier.size(38.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section header for selected date or upcoming
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (selectedDate != null) "Appointments for $selectedDate" else "Active Doctor Visits",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Text(
                        text = if (selectedDate != null) "${selectedDayAppointments.size} scheduled consultation(s)" else "Tap any calendar date to filter or book",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                }

                if (selectedDate != null) {
                    FilledTonalButton(
                        onClick = { onBookForDate(selectedDate) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_book_selected_date")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Book Date", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Day appointment list or empty card
        if (selectedDayAppointments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = MedBluePrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (selectedDate != null) "No visits on $selectedDate" else "No upcoming appointments",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap below to schedule a medical visit with your specialist doctor.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (selectedDate != null) {
                                    onBookForDate(selectedDate)
                                } else {
                                    onBookForDate(todayStr)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Book Consultation")
                        }
                    }
                }
            }
        } else {
            items(selectedDayAppointments, key = { it.id }) { appt ->
                AppointmentCard(
                    appointment = appt,
                    isDoctorView = false,
                    onCancel = { onCancelAppointment(appt.id) },
                    onToggleReminder = { onToggleReminder(appt) },
                    onChat = { onOpenChatWithDoctor(appt.doctorId, appt.doctorName) },
                    onViewRoute = onNavigateToMaps,
                    modifier = Modifier.testTag("calendar_appointment_card_${appt.id}")
                )
            }
        }
    }
}
