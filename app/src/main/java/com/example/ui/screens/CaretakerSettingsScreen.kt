package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerSettingsScreen(
    viewModel: MedTimeViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToLinkedPatients: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToWebsiteConnection: () -> Unit,
    onOpenHelpSupport: () -> Unit,
    onOpenAboutMedTime: () -> Unit,
    onLogoutRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    var pushAlertsEnabled by remember { mutableStateOf(true) }
    var soundAlertsEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onNavigateToProfile() },
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
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MedBluePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(currentUser?.name ?: "Emily Watson", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(currentUser?.email ?: "emily.watson@medtime.care", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MedWarningLight,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "ROLE: CARETAKER",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MedWarning
                            )
                        }
                    }

                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MedTextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Section: Caretaker Connections
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MedSurface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Patient Links & Privacy", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MedTextSecondary)

                    SettingsRowItem(
                        icon = Icons.Default.PeopleAlt,
                        title = "Linked Patients Management",
                        subtitle = "View and manage active patient connections",
                        onClick = onNavigateToLinkedPatients
                    )

                    HorizontalDivider(color = MedBorderLight)

                    SettingsRowItem(
                        icon = Icons.Default.Shield,
                        title = "Patient Permissions Matrix",
                        subtitle = "Check authorized clinical access permissions",
                        onClick = onNavigateToPermissions
                    )
                }
            }
        }

        // Section: Security & Web Connection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MedSurface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Security & Portal Access", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MedTextSecondary)

                    SettingsRowItem(
                        icon = Icons.Default.Security,
                        title = "Security & Two-Factor Authentication",
                        subtitle = "Password change and 2FA authentication",
                        onClick = onNavigateToSecurity
                    )

                    HorizontalDivider(color = MedBorderLight)

                    SettingsRowItem(
                        icon = Icons.Default.Devices,
                        title = "Connect to MedTime Web",
                        subtitle = "QR Code sync with Caretaker Web Portal",
                        onClick = onNavigateToWebsiteConnection
                    )
                }
            }
        }

        // Section: Notifications & Alerts Preferences
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MedSurface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Alerts & Notification Preferences", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MedTextSecondary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Push Dose Missed Alerts", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Receive immediate alert when a dose is missed", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                        }
                        Switch(checked = pushAlertsEnabled, onCheckedChange = { pushAlertsEnabled = it })
                    }

                    HorizontalDivider(color = MedBorderLight)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Urgent Audio Chime", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Play sound for critical medical alerts", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                        }
                        Switch(checked = soundAlertsEnabled, onCheckedChange = { soundAlertsEnabled = it })
                    }

                    HorizontalDivider(color = MedBorderLight)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Haptic Vibration", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Vibrate device during alarms", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                        }
                        Switch(checked = vibrationEnabled, onCheckedChange = { vibrationEnabled = it })
                    }
                }
            }
        }

        // Section: Appearance & Help
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MedSurface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("App Info & Support", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MedTextSecondary)

                    SettingsRowItem(
                        icon = when (themeMode) {
                            ThemeMode.DARK -> Icons.Default.DarkMode
                            ThemeMode.LIGHT -> Icons.Default.LightMode
                            ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                        },
                        title = "App Theme Mode",
                        subtitle = "Currently set to: ${themeMode.name}",
                        onClick = { viewModel.toggleThemeMode() }
                    )

                    HorizontalDivider(color = MedBorderLight)

                    SettingsRowItem(
                        icon = Icons.Default.HelpOutline,
                        title = "Help & Technical Support",
                        subtitle = "24/7 Caregiver assistance line",
                        onClick = onOpenHelpSupport
                    )

                    HorizontalDivider(color = MedBorderLight)

                    SettingsRowItem(
                        icon = Icons.Default.Info,
                        title = "About MedTime",
                        subtitle = "Version 2.4.0 (Build 2026.09)",
                        onClick = onOpenAboutMedTime
                    )
                }
            }
        }

        // Logout Button
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onLogoutRequest() }
                    .testTag("caretaker_logout_row"),
                colors = CardDefaults.cardColors(containerColor = MedErrorLight),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MedError)
                    Text("Sign Out of MedTime", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MedError)
                }
            }
        }
    }
}

@Composable
private fun SettingsRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MedBlueLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
        }

        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MedTextSecondary, modifier = Modifier.size(14.dp))
    }
}
