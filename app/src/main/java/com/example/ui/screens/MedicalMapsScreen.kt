package com.example.ui.screens

import android.content.Intent
import android.net.Uri
<<<<<<< HEAD
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
=======
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
<<<<<<< HEAD
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
=======
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
<<<<<<< HEAD
import com.example.data.model.AppointmentEntity
import com.example.ui.theme.MedBackground
import com.example.ui.theme.MedBlueLight
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedBorder
import com.example.ui.theme.MedError
import com.example.ui.theme.MedErrorLight
import com.example.ui.theme.MedSuccess
import com.example.ui.theme.MedSuccessLight
import com.example.ui.theme.MedSurface
import com.example.ui.theme.MedTextPrimary
import com.example.ui.theme.MedTextSecondary
import com.example.ui.theme.MedWarning
import com.example.ui.theme.MedWarningLight
import com.example.ui.viewmodel.MedTimeViewModel

data class MapRouteStep(
    val instruction: String,
    val distance: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

=======
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
data class MedicalFacility(
    val id: String,
    val name: String,
    val type: String, // "Hospital", "Pharmacy", "Urgent Care", "Trauma Center"
    val address: String,
    val distance: String,
    val openStatus: String,
    val phone: String,
<<<<<<< HEAD
    val emergencyReady: Boolean = true,
    val travelTimeDriving: String = "12 min",
    val travelTimeTransit: String = "24 min",
    val travelTimeWalking: String = "45 min"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
=======
    val emergencyReady: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
@Composable
fun MedicalMapsScreen(
    viewModel: MedTimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
<<<<<<< HEAD
    val appointments by viewModel.patientAppointments.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedViewMode by remember { mutableStateOf(0) } // 0: Upcoming Appointment Route, 1: Nearby Facilities
    var selectedTravelMode by remember { mutableStateOf("DRIVE") } // DRIVE, TRANSIT, WALK
    var showTurnByTurn by remember { mutableStateOf(false) }

    val upcomingAppointments = remember(appointments) {
        appointments.filter { it.status != "CANCELLED" }
    }

    var selectedAppointmentIndex by remember { mutableStateOf(0) }
    val activeAppointment = upcomingAppointments.getOrNull(selectedAppointmentIndex) ?: AppointmentEntity(
        id = "demo_appt",
        patientId = currentUser?.id ?: "patient_1",
        patientName = currentUser?.name ?: "Vijay Kumar",
        doctorId = "doctor_1",
        doctorName = "Dr. Sarah Mitchell, MD",
        doctorSpecialty = "Cardiology Specialist",
        appointmentDate = "Tomorrow",
        appointmentTime = "10:30 AM",
        reason = "Hypertension follow-up and ECG check"
    )

    val clinicHospitalName = remember(activeAppointment) {
        if (activeAppointment.doctorSpecialty.contains("Cardio", ignoreCase = true)) {
            "St. Jude Heart & Vascular Hospital • Clinic Suite 4B"
        } else if (activeAppointment.doctorSpecialty.contains("Endo", ignoreCase = true)) {
            "Metropolitan Endocrine & Diabetes Center"
        } else {
            "Memorial General Hospital & Specialty Care"
        }
    }

    val clinicAddress = remember(activeAppointment) {
        "1200 Healthcare Parkway, Medical District"
    }

    // Dynamic Route Details
    val travelDistance = "3.8 miles"
    val travelTime = when (selectedTravelMode) {
        "TRANSIT" -> "26 min"
        "WALK" -> "54 min"
        else -> "14 min"
    }

    val leaveByTime = when (selectedTravelMode) {
        "TRANSIT" -> "09:40 AM"
        "WALK" -> "09:15 AM"
        else -> "09:55 AM"
    }

    val turnByTurnSteps = remember(activeAppointment) {
        listOf(
            MapRouteStep("Head North on Pine Street toward Healthcare Pkwy", "0.4 mi", Icons.Default.Navigation),
            MapRouteStep("Turn right onto Healthcare Parkway (Traffic is light)", "2.6 mi", Icons.Default.DirectionsCar),
            MapRouteStep("Take the Medical Center West exit toward Patient Parking", "0.6 mi", Icons.Default.Directions),
            MapRouteStep("Turn left into $clinicHospitalName (Clinic Suite 4B)", "0.2 mi", Icons.Default.Place)
        )
    }

    // Static Medical Facilities List
=======
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedType by remember { mutableStateOf("All") }
    val facilityTypes = listOf("All", "Hospital", "Pharmacy", "Urgent Care", "Trauma Center")

>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
    val facilities = remember {
        listOf(
            MedicalFacility(
                id = "fac_1",
                name = "St. Jude Heart & Vascular Hospital",
                type = "Hospital",
                address = "1200 Healthcare Parkway, Medical District",
                distance = "1.2 miles",
                openStatus = "Open 24/7 • Emergency Ready",
                phone = "555-0199",
<<<<<<< HEAD
                emergencyReady = true,
                travelTimeDriving = "8 min"
            ),
            MedicalFacility(
                id = "fac_2",
                name = "Walgreens 24-Hour Pharmacy & Clinic",
=======
                emergencyReady = true
            ),
            MedicalFacility(
                id = "fac_2",
                name = "Walgreens 24-Hour Pharmacy",
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                type = "Pharmacy",
                address = "455 North Grand Avenue",
                distance = "0.6 miles",
                openStatus = "Open 24 Hours • Drive-Thru Refill",
                phone = "555-0143",
<<<<<<< HEAD
                emergencyReady = false,
                travelTimeDriving = "4 min"
=======
                emergencyReady = false
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
            ),
            MedicalFacility(
                id = "fac_3",
                name = "Memorial Emergency Trauma Center (Level 1)",
                type = "Trauma Center",
                address = "800 Memorial Drive",
                distance = "2.4 miles",
                openStatus = "Open 24/7 • Pediatric & Adult Trauma",
                phone = "911",
<<<<<<< HEAD
                emergencyReady = true,
                travelTimeDriving = "12 min"
=======
                emergencyReady = true
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
            ),
            MedicalFacility(
                id = "fac_4",
                name = "CareNow Immediate Urgent Care",
                type = "Urgent Care",
                address = "210 West End Boulevard",
                distance = "1.8 miles",
                openStatus = "Open Daily 8:00 AM - 10:00 PM",
                phone = "555-0182",
<<<<<<< HEAD
                emergencyReady = false,
                travelTimeDriving = "9 min"
=======
                emergencyReady = false
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
            ),
            MedicalFacility(
                id = "fac_5",
                name = "CVS Pharmacy & MinuteClinic",
                type = "Pharmacy",
                address = "901 Oak Ridge Way",
                distance = "1.1 miles",
                openStatus = "Open until 11:00 PM",
                phone = "555-0177",
<<<<<<< HEAD
                emergencyReady = false,
                travelTimeDriving = "6 min"
=======
                emergencyReady = false
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
            )
        )
    }

<<<<<<< HEAD
    var selectedFacilityType by remember { mutableStateOf("All") }
    val facilityTypes = listOf("All", "Hospital", "Pharmacy", "Urgent Care", "Trauma Center")
    val filteredFacilities = if (selectedFacilityType == "All") facilities else facilities.filter { it.type.equals(selectedFacilityType, ignoreCase = true) }

    // Pulsing Animation for User GPS Marker on Map Canvas
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_map")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
=======
    val filtered = if (selectedType == "All") facilities else facilities.filter { it.type.equals(selectedType, ignoreCase = true) }
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
    ) {
<<<<<<< HEAD
        // Top View Selector Tab Row
        TabRow(
            selectedTabIndex = selectedViewMode,
            containerColor = MedSurface,
            contentColor = MedBluePrimary
        ) {
            Tab(
                selected = selectedViewMode == 0,
                onClick = { selectedViewMode = 0 },
                text = { Text("Appointment Route", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_appointment_route")
            )
            Tab(
                selected = selectedViewMode == 1,
                onClick = { selectedViewMode = 1 },
                text = { Text("Nearby Medical Centers", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.LocalHospital, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_nearby_facilities")
            )
        }

        if (selectedViewMode == 0) {
            // ----------------------------------------------------
            // APPOINTMENT ROUTE & TRAVEL TIME VIEW
            // ----------------------------------------------------
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // If patient has multiple appointments, show selector carousel
                if (upcomingAppointments.size > 1) {
                    item {
                        Text(
                            text = "Select Appointment to Route:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(upcomingAppointments.indices.toList()) { index ->
                                val appt = upcomingAppointments[index]
                                val isSelected = selectedAppointmentIndex == index
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MedBlueLight else MedSurface
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) MedBluePrimary else MedBorder
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .clickable { selectedAppointmentIndex = index }
                                        .testTag("select_appointment_map_$index")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = if (isSelected) MedBluePrimary else MedTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Column {
                                            Text(
                                                text = appt.doctorName,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) MedBluePrimary else MedTextPrimary
                                            )
                                            Text(
                                                text = "${appt.appointmentDate} • ${appt.appointmentTime}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MedTextSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Vector Map Component
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8ECEF)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .testTag("interactive_map_canvas_card")
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Canvas vector map rendering
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Background roads / street grid
                                drawLine(
                                    color = Color(0xFFD0D7DE),
                                    start = Offset(0f, h * 0.35f),
                                    end = Offset(w, h * 0.35f),
                                    strokeWidth = 14f
                                )
                                drawLine(
                                    color = Color(0xFFD0D7DE),
                                    start = Offset(0f, h * 0.72f),
                                    end = Offset(w, h * 0.72f),
                                    strokeWidth = 18f
                                )
                                drawLine(
                                    color = Color(0xFFD0D7DE),
                                    start = Offset(w * 0.3f, 0f),
                                    end = Offset(w * 0.3f, h),
                                    strokeWidth = 12f
                                )
                                drawLine(
                                    color = Color(0xFFD0D7DE),
                                    start = Offset(w * 0.75f, 0f),
                                    end = Offset(w * 0.75f, h),
                                    strokeWidth = 20f
                                )

                                // Park / Green Zone
                                drawCircle(
                                    color = Color(0xFFC8E6C9),
                                    radius = 70f,
                                    center = Offset(w * 0.5f, h * 0.45f)
                                )

                                // Active Route Path (Blue Polyline with glowing accent)
                                val routePath = Path().apply {
                                    moveTo(w * 0.2f, h * 0.75f) // User location
                                    lineTo(w * 0.3f, h * 0.72f)
                                    lineTo(w * 0.3f, h * 0.35f)
                                    lineTo(w * 0.75f, h * 0.35f)
                                    lineTo(w * 0.75f, h * 0.22f) // Clinic location
                                }

                                // Outer glow stroke
                                drawPath(
                                    path = routePath,
                                    color = Color(0x661565C0),
                                    style = Stroke(width = 16f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )

                                // Inner solid route stroke with dash pattern
                                drawPath(
                                    path = routePath,
                                    color = Color(0xFF1565C0),
                                    style = Stroke(
                                        width = 8f,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )

                                // User Location (Home/Current GPS): Pulsing blue beacon
                                val userPos = Offset(w * 0.2f, h * 0.75f)
                                drawCircle(
                                    color = Color(0x442196F3),
                                    radius = pulseRadius,
                                    center = userPos
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 10f,
                                    center = userPos
                                )
                                drawCircle(
                                    color = Color(0xFF1976D2),
                                    radius = 6f,
                                    center = userPos
                                )

                                // Destination Pin (Hospital / Clinic): Red Pin
                                val clinicPos = Offset(w * 0.75f, h * 0.22f)
                                drawCircle(
                                    color = Color(0x44D32F2F),
                                    radius = 18f,
                                    center = clinicPos
                                )
                                drawCircle(
                                    color = Color(0xFFD32F2F),
                                    radius = 9f,
                                    center = clinicPos
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 4f,
                                    center = clinicPos
                                )
                            }

                            // User Origin Overlay Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                shadowElevation = 3.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MedBluePrimary)
                                    )
                                    Text(
                                        text = "Your Location",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MedTextPrimary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Destination Hospital Badge Overlay
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                shadowElevation = 3.dp,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = activeAppointment.doctorName.take(18),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFD32F2F),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Route Summary & Travel Mode Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MedSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        text = clinicHospitalName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MedTextPrimary
                                    )
                                    Text(
                                        text = clinicAddress,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MedTextSecondary
                                    )
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = MedBlueLight) {
                                    Text(
                                        text = travelDistance,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MedBluePrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Travel Mode Selector (Driving, Transit, Walking)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TravelModeChip(
                                    label = "Drive",
                                    time = "14 min",
                                    icon = Icons.Default.DirectionsCar,
                                    isSelected = selectedTravelMode == "DRIVE",
                                    onClick = { selectedTravelMode = "DRIVE" },
                                    modifier = Modifier.weight(1f)
                                )
                                TravelModeChip(
                                    label = "Transit",
                                    time = "26 min",
                                    icon = Icons.Default.DirectionsBus,
                                    isSelected = selectedTravelMode == "TRANSIT",
                                    onClick = { selectedTravelMode = "TRANSIT" },
                                    modifier = Modifier.weight(1f)
                                )
                                TravelModeChip(
                                    label = "Walk",
                                    time = "54 min",
                                    icon = Icons.Default.DirectionsWalk,
                                    isSelected = selectedTravelMode == "WALK",
                                    onClick = { selectedTravelMode = "WALK" },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Departure Time Recommendation Banner
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MedSuccessLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = MedSuccess,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Recommended Departure: Leave by $leaveByTime",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MedSuccess
                                        )
                                        Text(
                                            text = "Arrives 15 min early for ${activeAppointment.appointmentTime} check-in with ${activeAppointment.doctorName}.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MedTextPrimary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val geoUri = Uri.parse("geo:0,0?q=${Uri.encode("$clinicHospitalName, $clinicAddress")}")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                                        context.startActivity(mapIntent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("start_live_navigation_button")
                                ) {
                                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Start Navigation", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showTurnByTurn = !showTurnByTurn },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("toggle_turn_by_turn_button")
                                ) {
                                    Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (showTurnByTurn) "Hide Directions" else "Turn-by-Turn")
                                }
                            }

                            // Turn-by-Turn Directions Expansion
                            AnimatedVisibility(visible = showTurnByTurn) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Route Directions ($travelDistance • $travelTime):",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MedTextPrimary
                                    )
                                    turnByTurnSteps.forEachIndexed { i, step ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MedBlueLight,
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("${i + 1}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MedBluePrimary)
                                                }
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(step.instruction, style = MaterialTheme.typography.bodySmall, color = MedTextPrimary)
                                                Text(step.distance, style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ----------------------------------------------------
            // NEARBY FACILITIES VIEW
            // ----------------------------------------------------
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MedSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(facilityTypes) { type ->
                    val isSelected = selectedFacilityType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFacilityType = type },
                        label = { Text(type, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MedBluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredFacilities, key = { it.id }) { fac ->
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
                                .padding(16.dp)
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
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (fac.emergencyReady) MedErrorLight else MedBlueLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (fac.type) {
                                                "Pharmacy" -> Icons.Default.LocalPharmacy
                                                "Urgent Care" -> Icons.Default.MedicalServices
                                                else -> Icons.Default.LocalHospital
                                            },
                                            contentDescription = null,
                                            tint = if (fac.emergencyReady) MedError else MedBluePrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column {
                                        Text(fac.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MedTextPrimary)
                                        Text("${fac.type} • ${fac.distance} away (${fac.travelTimeDriving} drive)", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(fac.address, style = MaterialTheme.typography.bodySmall, color = MedTextPrimary)
                            Text(fac.openStatus, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = MedSuccess))

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val uri = Uri.parse("geo:0,0?q=${Uri.encode(fac.name + " " + fac.address)}")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Navigation, contentDescription = "Directions", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Directions", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${fac.phone}")
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call Facility", fontSize = 12.sp)
                                }
=======
        // Emergency Call SOS Banner
        Surface(
            color = Color(0xFFD32F2F),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                    Column {
                        Text("Emergency Trauma Helpline", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("One-tap connection to immediate dispatch", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                    }
                }
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:911")
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("dial_911_button")
                ) {
                    Text("Call 911", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Filter chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MedSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(facilityTypes) { type ->
                val isSelected = selectedType == type
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedType = type },
                    label = { Text(type, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MedBluePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Facilities List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered, key = { it.id }) { fac ->
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
                            .padding(16.dp)
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
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (fac.emergencyReady) MedErrorLight else MedBlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (fac.type) {
                                            "Pharmacy" -> Icons.Default.LocalPharmacy
                                            "Urgent Care" -> Icons.Default.MedicalServices
                                            else -> Icons.Default.LocalHospital
                                        },
                                        contentDescription = null,
                                        tint = if (fac.emergencyReady) MedError else MedBluePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(fac.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MedTextPrimary)
                                    Text("${fac.type} • ${fac.distance} away", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(fac.address, style = MaterialTheme.typography.bodySmall, color = MedTextPrimary)
                        Text(fac.openStatus, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = MedSuccess))

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val uri = Uri.parse("geo:0,0?q=${Uri.encode(fac.name + " " + fac.address)}")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = "Directions", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Directions", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${fac.phone}")
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call Facility", fontSize = 12.sp)
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                            }
                        }
                    }
                }
            }
        }
    }
}
<<<<<<< HEAD

@Composable
fun TravelModeChip(
    label: String,
    time: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MedBluePrimary else MedSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MedBluePrimary else MedBorder),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else MedTextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) Color.White else MedTextPrimary,
                fontSize = 11.sp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color.White.copy(alpha = 0.85f) else MedTextSecondary,
                fontSize = 9.sp
            )
        }
    }
}
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
