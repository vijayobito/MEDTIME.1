package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppointmentEntity
import com.example.data.model.MedicineEntity
import com.example.data.model.MedicineReminderEntity
import com.example.ui.theme.*

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ReminderCard(
    reminder: MedicineReminderEntity,
    onTake: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTaken = reminder.status == "TAKEN"
    val isSkipped = reminder.status == "SKIPPED"
    val isSnoozed = reminder.status == "SNOOZED"

    val statusBg = when {
        isTaken -> MedSuccessLight
        isSkipped -> MedErrorLight
        isSnoozed -> MedWarningLight
        else -> MedBlueLight
    }

    val statusColor = when {
        isTaken -> MedSuccess
        isSkipped -> MedError
        isSnoozed -> MedWarning
        else -> MedBluePrimary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, if (isTaken) MedSuccess.copy(alpha = 0.3f) else MedBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MedSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (reminder.form.lowercase()) {
                                "capsule" -> Icons.Default.MedicalInformation
                                "syrup" -> Icons.Default.Vaccines
                                else -> Icons.Default.Medication
                            },
                            contentDescription = reminder.form,
                            tint = statusColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = reminder.medicineName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = "${reminder.dosage} • ${reminder.instructions}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusBg
                ) {
                    Text(
                        text = if (isSnoozed && reminder.snoozeUntilTime != null) "Snoozed (${reminder.snoozeUntilTime})" else reminder.status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = MedTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Scheduled: ${reminder.scheduledTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (!isTaken && !isSkipped) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onSnooze,
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("snooze_button_${reminder.id}"),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Snooze, contentDescription = "Snooze", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Snooze", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onTake,
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("take_button_${reminder.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = MedSuccess),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Take", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Take Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (isTaken) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Logged",
                            tint = MedSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Logged to Health Records",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedSuccess,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineCard(
    medicine: MedicineEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRefill: (amount: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isLow = medicine.isLowStock()
    val threshold = medicine.calculateThreshold()
    val daysRemaining = medicine.getEstimatedDaysRemaining()
    val dailyDoses = medicine.getDailyDoseCount()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isLow) MedWarning.copy(alpha = 0.6f) else MedBorder,
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MedSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(medicine.colorHex).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = Color(medicine.colorHex),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = medicine.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = "${medicine.dosage} • ${medicine.form}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_med_${medicine.id}")
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = MedTextSecondary)
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_med_${medicine.id}")
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MedError)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MedBorder)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Instructions", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                    Text(medicine.instructions, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Frequency", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                    Text(medicine.frequency, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Times", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                    Text(medicine.reminderTimes, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Stock Remaining", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                    Text(
                        text = "${medicine.stockQuantity} doses",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isLow) MedError else MedSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            if (isLow) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MedWarningLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MedWarning, modifier = Modifier.size(18.dp))
                            Column {
                                Text(
                                    text = "Low Stock Alert (${medicine.stockQuantity} left / threshold: $threshold)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFE65100)
                                )
                                Text(
                                    text = "~${String.format(java.util.Locale.US, "%.1f", daysRemaining)} days supply remaining at $dailyDoses doses/day.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MedTextPrimary
                                )
                            }
                        }

                        Button(
                            onClick = { onRefill(30) },
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("refill_button_${medicine.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Refill +30", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: AppointmentEntity,
    isDoctorView: Boolean = false,
    onAccept: () -> Unit = {},
    onReject: () -> Unit = {},
    onComplete: () -> Unit = {},
    onCancel: () -> Unit = {},
    onToggleReminder: () -> Unit = {},
    onChat: () -> Unit = {},
    onViewRoute: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statusColor = when (appointment.status) {
        "ACCEPTED" -> MedSuccess
        "PENDING" -> MedWarning
        "REJECTED", "CANCELLED" -> MedError
        "COMPLETED" -> MedInfo
        else -> MedBluePrimary
    }

    val statusBg = when (appointment.status) {
        "ACCEPTED" -> MedSuccessLight
        "PENDING" -> MedWarningLight
        "REJECTED", "CANCELLED" -> MedErrorLight
        "COMPLETED" -> MedInfoLight
        else -> MedBlueLight
    }

    val statusLabel = when (appointment.status) {
        "ACCEPTED" -> "CONFIRMED"
        "PENDING" -> "PENDING APPROVAL"
        else -> appointment.status
    }

    val isUpcoming = appointment.status == "ACCEPTED" || appointment.status == "PENDING"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MedSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Avatar, Name & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Avatar circle with specialty icon or initials
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MedBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDoctorView) Icons.Default.Person else Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = MedBluePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isDoctorView) appointment.patientName else appointment.doctorName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isDoctorView) "Patient Consultation" else appointment.doctorSpecialty,
                            style = MaterialTheme.typography.bodySmall,
                            color = MedBluePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date & Time Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MedBackground, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MedBluePrimary
                    )
                    Text(
                        text = appointment.appointmentDate,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MedTextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MedBluePrimary
                    )
                    Text(
                        text = appointment.appointmentTime,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MedTextPrimary
                    )
                }
            }

            if (appointment.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Purpose:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MedTextSecondary
                    )
                    Text(
                        text = appointment.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (appointment.doctorNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Doctor Note:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MedInfo
                    )
                    Text(
                        text = appointment.doctorNotes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MedInfo,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Notification Reminder Toggle Row (For Upcoming Appointments)
            if (!isDoctorView && isUpcoming) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (appointment.reminderEnabled) MedBlueLight.copy(alpha = 0.6f) else MedBackground,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (appointment.reminderEnabled) MedBluePrimary.copy(alpha = 0.3f) else MedBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (appointment.reminderEnabled) Icons.Default.NotificationsActive else Icons.Outlined.NotificationsOff,
                                contentDescription = null,
                                tint = if (appointment.reminderEnabled) MedBluePrimary else MedTextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = if (appointment.reminderEnabled) "Reminder Alert Active" else "Reminder Muted",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (appointment.reminderEnabled) MedBluePrimary else MedTextSecondary
                                )
                                Text(
                                    text = if (appointment.reminderEnabled) "Notify ${appointment.reminderMinutesBefore}m before visit" else "Tap toggle to turn on reminder",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = appointment.reminderEnabled,
                            onCheckedChange = { onToggleReminder() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MedBluePrimary
                            ),
                            modifier = Modifier.testTag("toggle_reminder_${appointment.id}")
                        )
                    }
                }
            }

            // Action Buttons
            if (isDoctorView && appointment.status == "PENDING") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MedError)
                    ) {
                        Text("Decline", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MedSuccess)
                    ) {
                        Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (isDoctorView && appointment.status == "ACCEPTED") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MedInfo)
                    ) {
                        Text("Mark Completed", fontSize = 12.sp)
                    }
                }
            } else if (!isDoctorView && isUpcoming) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onViewRoute,
                        modifier = Modifier.height(36.dp).testTag("view_appointment_route_${appointment.id}"),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MedBluePrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Route & Map", fontSize = 11.sp, color = MedBluePrimary, fontWeight = FontWeight.SemiBold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(
                            onClick = onChat,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = MedBluePrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chat", fontSize = 11.sp, color = MedBluePrimary)
                        }

                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.height(36.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MedError),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MedError
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
