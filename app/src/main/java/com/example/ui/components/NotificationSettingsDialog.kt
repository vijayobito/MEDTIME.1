package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun NotificationSettingsDialog(
    onDismiss: () -> Unit
) {
    var doseReminders by remember { mutableStateOf(true) }
    var refillAlerts by remember { mutableStateOf(true) }
    var appointmentAlerts by remember { mutableStateOf(true) }
    var caretakerSyncAlerts by remember { mutableStateOf(true) }
    var healthTipsBroadcasts by remember { mutableStateOf(false) }
    var quietHoursEnabled by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(MedBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Notification Settings",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "Customize Alerts & Delivery Channels",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("notification_settings_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MedTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "MEDICATION REMINDERS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = MedTextSecondary
                            )
                        )
                    }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MedSurfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Daily Medication Dose Alarms", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTextPrimary)
                                        Text("Receive popups and alerts at scheduled times", fontSize = 11.5.sp, color = MedTextSecondary)
                                    }
                                    Switch(
                                        checked = doseReminders,
                                        onCheckedChange = { doseReminders = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MedBluePrimary)
                                    )
                                }

                                HorizontalDivider(color = MedDivider)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Prescription Refill Warning", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTextPrimary)
                                        Text("Warn when medication stock drops below 5 pills", fontSize = 11.5.sp, color = MedTextSecondary)
                                    }
                                    Switch(
                                        checked = refillAlerts,
                                        onCheckedChange = { refillAlerts = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MedBluePrimary)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "APPOINTMENTS & CAREGIVERS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = MedTextSecondary
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MedSurfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Doctor Appointment Reminders", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTextPrimary)
                                        Text("Alert 1 hour and 24 hours prior to visits", fontSize = 11.5.sp, color = MedTextSecondary)
                                    }
                                    Switch(
                                        checked = appointmentAlerts,
                                        onCheckedChange = { appointmentAlerts = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MedBluePrimary)
                                    )
                                }

                                HorizontalDivider(color = MedDivider)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Caretaker Sync Escalations", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTextPrimary)
                                        Text("Notify assigned family caretakers when a dose is missed", fontSize = 11.5.sp, color = MedTextSecondary)
                                    }
                                    Switch(
                                        checked = caretakerSyncAlerts,
                                        onCheckedChange = { caretakerSyncAlerts = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MedBluePrimary)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "PREFERENCES & BROADCASTS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = MedTextSecondary
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MedSurfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Clinical News & Health Tips", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTextPrimary)
                                        Text("Weekly verified articles and healthy habit insights", fontSize = 11.5.sp, color = MedTextSecondary)
                                    }
                                    Switch(
                                        checked = healthTipsBroadcasts,
                                        onCheckedChange = { healthTipsBroadcasts = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MedBluePrimary)
                                    )
                                }

                                HorizontalDivider(color = MedDivider)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Quiet Hours (10:00 PM – 07:00 AM)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTextPrimary)
                                        Text("Silence non-critical announcements during sleep", fontSize = 11.5.sp, color = MedTextSecondary)
                                    }
                                    Switch(
                                        checked = quietHoursEnabled,
                                        onCheckedChange = { quietHoursEnabled = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MedBluePrimary)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Notification Preferences", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
