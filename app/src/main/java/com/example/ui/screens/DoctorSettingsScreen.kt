package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorSettingsScreen(
    viewModel: MedTimeViewModel,
    onNavigateToSecurity: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onOpenHelpSupport: () -> Unit,
    onLogoutRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var autoAcceptBookings by remember { mutableStateOf(true) }
    var onlineConsultationEnabled by remember { mutableStateOf(true) }
    var slotDurationMinutes by remember { mutableIntStateOf(30) }
    var bufferTimeMinutes by remember { mutableIntStateOf(5) }
    var maxDailyPatients by remember { mutableIntStateOf(20) }

    var instantBookingAlerts by remember { mutableStateOf(true) }
    var patientMessageAlerts by remember { mutableStateOf(true) }
    var labReportUploadAlerts by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
            .testTag("doctor_settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "Doctor Settings",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                Text(
                    text = "Clinical workflow, schedule, notifications & security",
                    style = MaterialTheme.typography.bodySmall,
                    color = MedTextSecondary
                )
            }
        }

        // 2. Section: Clinic Schedule & Availability
        item {
            SettingsSectionCard(title = "Clinic Schedule & Availability", icon = Icons.Default.Schedule) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Accept Online Teleconsultations", fontWeight = FontWeight.SemiBold, color = MedTextPrimary)
                            Text("Enable video / virtual consultation bookings from patients", fontSize = 11.sp, color = MedTextSecondary)
                        }
                        Switch(
                            checked = onlineConsultationEnabled,
                            onCheckedChange = { onlineConsultationEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MedBluePrimary)
                        )
                    }

                    HorizontalDivider(color = MedBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Consultation Slot Duration", fontWeight = FontWeight.SemiBold, color = MedTextPrimary)
                            Text("Standard time reserved per patient", fontSize = 11.sp, color = MedTextSecondary)
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = MedBlueLight) {
                            Text(
                                text = "$slotDurationMinutes mins",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                color = MedBluePrimary
                            )
                        }
                    }

                    HorizontalDivider(color = MedBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Max Daily Patient Limit", fontWeight = FontWeight.SemiBold, color = MedTextPrimary)
                            Text("Cap incoming bookings to prevent overbooking", fontSize = 11.sp, color = MedTextSecondary)
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = MedBackground) {
                            Text(
                                text = "$maxDailyPatients Patients/day",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                color = MedTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 3. Section: Appointment Automation
        item {
            SettingsSectionCard(title = "Appointment Automation", icon = Icons.Default.EventAvailable) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Confirm Bookings", fontWeight = FontWeight.SemiBold, color = MedTextPrimary)
                        Text("Automatically approve appointments for verified patients", fontSize = 11.sp, color = MedTextSecondary)
                    }
                    Switch(
                        checked = autoAcceptBookings,
                        onCheckedChange = { autoAcceptBookings = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MedBluePrimary)
                    )
                }
            }
        }

        // 4. Section: Clinical Notification Alerts
        item {
            SettingsSectionCard(title = "Clinical Alerts & Notifications", icon = Icons.Default.NotificationsActive) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Instant Appointment Alerts", fontWeight = FontWeight.SemiBold, color = MedTextPrimary)
                            Text("Receive push notifications when a new patient books", fontSize = 11.sp, color = MedTextSecondary)
                        }
                        Switch(
                            checked = instantBookingAlerts,
                            onCheckedChange = { instantBookingAlerts = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MedBluePrimary)
                        )
                    }

                    HorizontalDivider(color = MedBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Patient Messages & Inquiries", fontWeight = FontWeight.SemiBold, color = MedTextPrimary)
                            Text("Alerts for patient chat messages and refill requests", fontSize = 11.sp, color = MedTextSecondary)
                        }
                        Switch(
                            checked = patientMessageAlerts,
                            onCheckedChange = { patientMessageAlerts = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MedBluePrimary)
                        )
                    }

                    HorizontalDivider(color = MedBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Lab Reports & Scans Uploads", fontWeight = FontWeight.SemiBold, color = MedTextPrimary)
                            Text("Notification when patient attaches diagnostic reports", fontSize = 11.sp, color = MedTextSecondary)
                        }
                        Switch(
                            checked = labReportUploadAlerts,
                            onCheckedChange = { labReportUploadAlerts = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MedBluePrimary)
                        )
                    }
                }
            }
        }

        // 5. Section: Security & Account
        item {
            SettingsSectionCard(title = "Security & Account", icon = Icons.Default.Security) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsNavigationRow(
                        title = "Security & Privacy",
                        subtitle = "Password, 2-Factor Authentication & active login sessions",
                        icon = Icons.Default.Lock,
                        onClick = onNavigateToSecurity
                    )
                    HorizontalDivider(color = MedBorder)
                    SettingsNavigationRow(
                        title = "Doctor Profile & Credentials",
                        subtitle = "License, specialty, bio & fee settings",
                        icon = Icons.Default.AccountCircle,
                        onClick = onNavigateToProfile
                    )
                    HorizontalDivider(color = MedBorder)
                    SettingsNavigationRow(
                        title = "Help & Clinical Support",
                        subtitle = "Clinical FAQs, support desk & guides",
                        icon = Icons.Default.HelpOutline,
                        onClick = onOpenHelpSupport
                    )
                }
            }
        }

        // 6. Logout Button
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(16.dp))
                    .clickable { onLogoutRequest() },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEA))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout", tint = MedError)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sign Out of Doctor Portal", fontWeight = FontWeight.Bold, color = MedError)
                        Text("Securely close your active clinical practitioner session", fontSize = 11.sp, color = MedError.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(20.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
            }
            HorizontalDivider(color = MedBorder)
            content()
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(modifier = Modifier.size(36.dp), shape = RoundedCornerShape(8.dp), color = MedBlueLight) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(18.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MedTextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MedTextSecondary, modifier = Modifier.size(20.dp))
    }
}
