package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppointmentEntity
import com.example.data.model.CloudBackupSummary
import com.example.data.model.MedicineEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.util.ClinicalReportPdfGenerator
import java.io.File

// =============================================================================
// 1. Emergency Contact & SOS Dashboard Card (Phone Call & SMS Triggers)
// =============================================================================

@Composable
fun EmergencyContactDashboardCard(
    user: UserEntity?,
    onUpdateContact: (name: String, phone: String, relation: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contactName = user?.emergencyContactName?.ifBlank { "Primary Caretaker" } ?: "Primary Caretaker"
    val contactPhone = user?.emergencyContactPhone?.ifBlank { "911" } ?: "911"
    val contactRelation = user?.emergencyContactRelation?.ifBlank { "Emergency Contact" } ?: "Emergency Contact"

    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        var editName by remember { mutableStateOf(user?.emergencyContactName ?: "") }
        var editPhone by remember { mutableStateOf(user?.emergencyContactPhone ?: "") }
        var editRelation by remember { mutableStateOf(user?.emergencyContactRelation ?: "Caretaker") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ContactEmergency, contentDescription = null, tint = MedError)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Emergency Contact Info", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Specify your primary caretaker or emergency responder for instant 1-tap dial and SMS medical alerts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Contact / Caretaker Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emergency_contact_name_input")
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emergency_contact_phone_input")
                    )
                    OutlinedTextField(
                        value = editRelation,
                        onValueChange = { editRelation = it },
                        label = { Text("Relationship (e.g. Spouse, Caretaker, Doctor)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editPhone.isNotBlank()) {
                            onUpdateContact(editName.ifBlank { "Caretaker" }, editPhone, editRelation)
                            showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                    modifier = Modifier.testTag("save_emergency_contact_button")
                ) {
                    Text("Save Contact")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MedError.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .testTag("emergency_contact_card"),
        colors = CardDefaults.cardColors(containerColor = MedError.copy(alpha = 0.04f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MedError.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Emergency,
                            contentDescription = "Emergency SOS",
                            tint = MedError,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Emergency Caretaker SOS",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = "Instant phone call & SMS alert dispatch",
                            style = MaterialTheme.typography.labelSmall,
                            color = MedTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.testTag("emergency_edit_action")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Contact",
                        tint = MedTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Contact Info Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MedSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
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
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MedBluePrimary
                            )
                        }
                        Column {
                            Text(
                                text = contactName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "$contactRelation • $contactPhone",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MedSuccessLight
                    ) {
                        Text(
                            text = "ACTIVE LINE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MedSuccess,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // Action Buttons: Direct Phone Call & Instant SMS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Call Button
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$contactPhone")
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("emergency_call_action"),
                    colors = ButtonDefaults.buttonColors(containerColor = MedError),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneInTalk,
                        contentDescription = "Call Caretaker",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Call SOS",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }

                // SMS Button
                Button(
                    onClick = {
                        val patientName = user?.name ?: "Patient"
                        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("smsto:$contactPhone")
                            putExtra(
                                "sms_body",
                                "🚨 MEDTIME URGENT ALERT from $patientName: I need medical assistance or a health checkup. Please contact me immediately."
                            )
                        }
                        smsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try {
                            context.startActivity(smsIntent)
                        } catch (e: Exception) {
                            // Fallback to general intent
                            val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("sms:$contactPhone")
                                putExtra(
                                    "sms_body",
                                    "🚨 MEDTIME URGENT ALERT from $patientName: I need medical assistance. Please contact me."
                                )
                            }
                            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(fallbackIntent)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("emergency_sms_action"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2185B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "Send SMS",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Send SMS Alert",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// =============================================================================
// 2. Doctor Clinical Report PDF Export Dialog & Banner
// =============================================================================

@Composable
fun ExportDoctorPdfDialog(
    user: UserEntity,
    adherencePercent: Int,
    totalScheduled: Int,
    takenCount: Int,
    missedCount: Int,
    medicines: List<MedicineEntity>,
    appointments: List<AppointmentEntity>,
    onDismiss: () -> Unit,
    onGenerateAndShare: (Context) -> Unit
) {
    val context = LocalContext.current
    var isGenerating by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MedBlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MedBluePrimary)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Doctor Clinical Report", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Compiled PDF Document", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Compiles your complete medication adherence rate, active prescriptions, and appointment history into a verified clinical PDF document.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MedTextSecondary,
                    lineHeight = 18.sp
                )

                // Report Preview Summary Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MedBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Report Contents Overview",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Patient Name:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            Text(user.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Adherence Score:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            Text("$adherencePercent% ($takenCount taken / $totalScheduled scheduled)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MedSuccess)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Active Medications:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            Text("${medicines.size} Prescriptions", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MedBluePrimary)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Appointment History:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            Text("${appointments.size} Consultations", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MedBluePrimary)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Format & Standard:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            Text("A4 Clinical Summary • PDF", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MedBlueLight.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Ready to share directly with your physician via WhatsApp, Email, or Print.",
                            fontSize = 11.sp,
                            color = MedBlueDark
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isGenerating = true
                    onGenerateAndShare(context)
                    isGenerating = false
                    onDismiss()
                },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("generate_share_pdf_button")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generating...")
                } else {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compile & Share PDF")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// =============================================================================
// 3. Cloud Database Backup & Restore Card / Dialog
// =============================================================================

@Composable
fun CloudBackupCard(
    backupSummary: CloudBackupSummary?,
    isBackingUp: Boolean,
    onTriggerBackup: () -> Unit,
    onTriggerRestore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MedBorder, RoundedCornerShape(16.dp))
            .testTag("cloud_backup_card"),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MedBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Cloud Storage",
                            tint = MedBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Room Database Cloud Backup",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = "Secure encrypted backup to healthcare cloud",
                            style = MaterialTheme.typography.labelSmall,
                            color = MedTextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MedSuccessLight
                ) {
                    Text(
                        text = "256-bit AES",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedSuccess,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        fontSize = 9.sp
                    )
                }
            }

            Text(
                text = "Back up all your local Room database tables (medications, dose adherence logs, appointments, medical files metadata, and audit trails) to your secure encrypted cloud vault.",
                style = MaterialTheme.typography.bodySmall,
                color = MedTextSecondary,
                lineHeight = 18.sp
            )

            // Status strip
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MedBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Last Backup Sync:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        Text(
                            text = backupSummary?.formattedDate ?: "Not yet backed up",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (backupSummary != null) MedSuccess else MedWarning
                        )
                    }

                    if (backupSummary != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Backed Up:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            Text(
                                text = "${backupSummary.totalRecords} records (${String.format(java.util.Locale.US, "%.1f", backupSummary.sizeKb)} KB)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cloud Checksum:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            Text(
                                text = backupSummary.checksum,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MedBluePrimary
                            )
                        }
                    }
                }
            }

            // Action Buttons: Manual Cloud Backup & Restore
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTriggerBackup,
                    enabled = !isBackingUp,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(44.dp)
                        .testTag("manual_cloud_backup_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isBackingUp) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Backing up...")
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Backup to Cloud Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onTriggerRestore,
                    enabled = !isBackingUp,
                    modifier = Modifier
                        .weight(0.8f)
                        .height(44.dp)
                        .testTag("restore_cloud_backup_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = MedBluePrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restore", fontSize = 12.sp, color = MedBluePrimary)
                }
            }
        }
    }
}

// =============================================================================
// 4. Theme Provider / Display Appearance Selector
// =============================================================================

@Composable
fun ThemeSelectorCard(
    currentThemeMode: ThemeMode,
    onSelectThemeMode: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MedBorder, RoundedCornerShape(16.dp))
            .testTag("theme_selector_card"),
        colors = CardDefaults.cardColors(containerColor = MedSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MedBlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = "Theme",
                        tint = MedBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Display Theme & Appearance",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Text(
                        text = "Choose between Light, Dark, or System mode",
                        style = MaterialTheme.typography.labelSmall,
                        color = MedTextSecondary
                    )
                }
            }

            // 3 Theme Options (Cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionItem(
                    mode = ThemeMode.SYSTEM,
                    title = "System",
                    icon = Icons.Default.BrightnessAuto,
                    isSelected = currentThemeMode == ThemeMode.SYSTEM,
                    onClick = { onSelectThemeMode(ThemeMode.SYSTEM) },
                    modifier = Modifier.weight(1f)
                )

                ThemeOptionItem(
                    mode = ThemeMode.LIGHT,
                    title = "Light",
                    icon = Icons.Default.LightMode,
                    isSelected = currentThemeMode == ThemeMode.LIGHT,
                    onClick = { onSelectThemeMode(ThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f)
                )

                ThemeOptionItem(
                    mode = ThemeMode.DARK,
                    title = "Dark",
                    icon = Icons.Default.DarkMode,
                    isSelected = currentThemeMode == ThemeMode.DARK,
                    onClick = { onSelectThemeMode(ThemeMode.DARK) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionItem(
    mode: ThemeMode,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MedBluePrimary else MedBorder
    val bgColor = if (isSelected) MedBlueLight else MedBackground

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("theme_option_${mode.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MedBluePrimary else MedTextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) MedBluePrimary else MedTextPrimary
            )
        }
    }
}
