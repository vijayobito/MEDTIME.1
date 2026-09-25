package com.example.ui.screens

import android.Manifest
<<<<<<< HEAD
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
=======
import android.content.pm.PackageManager
import android.os.Build
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
<<<<<<< HEAD
import androidx.compose.foundation.lazy.items
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
<<<<<<< HEAD
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.CaretakerLinkEntity
import com.example.data.model.UserEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel
import com.example.ui.viewmodel.PatientLoginSession
import com.example.ui.viewmodel.WebConnectedSession
=======
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel
import com.example.worker.MedicalReminderWorker
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465

@Composable
fun SettingsScreen(
    viewModel: MedTimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
<<<<<<< HEAD
    val doctors by viewModel.doctors.collectAsState()
    val caretakerLinks by viewModel.patientCaretakerLinks.collectAsState()

    // Preferences & State from ViewModel
    val is2FaEnabled by viewModel.isTwoFactorEnabled.collectAsState()
    val backupCodes by viewModel.backupCodes.collectAsState()
    val activeSessions by viewModel.activeSessions.collectAsState()
    val webSessions by viewModel.connectedWebSessions.collectAsState()

    val notifMeds by viewModel.notifMedicineReminders.collectAsState()
    val notifAppts by viewModel.notifAppointmentReminders.collectAsState()
    val notifMsgs by viewModel.notifMessageAlerts.collectAsState()
    val notifNews by viewModel.notifMedicalNews.collectAsState()
    val notifSec by viewModel.notifSecurityAlerts.collectAsState()

    val reminderSound by viewModel.reminderSoundEnabled.collectAsState()
    val reminderTone by viewModel.reminderSoundTone.collectAsState()
    val customSoundUri by viewModel.customSoundUri.collectAsState()
    val customSoundName by viewModel.customSoundName.collectAsState()
    val reminderVib by viewModel.reminderVibrationEnabled.collectAsState()
    val reminderBehavior by viewModel.reminderBehavior.collectAsState()
    val spokenReminderEnabled by viewModel.spokenReminderEnabled.collectAsState()
    val ttsVoice by viewModel.ttsVoice.collectAsState()
    val ttsLanguage by viewModel.ttsLanguage.collectAsState()
    val speechVolume by viewModel.speechVolume.collectAsState()

    val locationSharing by viewModel.locationSharingEnabled.collectAsState()
    val docAccessMode by viewModel.medicalDocumentAccess.collectAsState()

    // Supabase PostgreSQL Sync State
    val isSupabaseSyncing by viewModel.isSupabaseSyncing.collectAsState()
    val isSupabaseConfigured by viewModel.isSupabaseConfigured.collectAsState()
    val supabaseConnStatus by viewModel.supabaseConnectionStatus.collectAsState()
    val lastSupabaseSync by viewModel.lastSupabaseSyncResult.collectAsState()
    var showConfigureSupabaseDialog by remember { mutableStateOf(false) }

    // Dialog State Trackers
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showBackupCodesDialog by remember { mutableStateOf(false) }
    var showActiveSessionsDialog by remember { mutableStateOf(false) }
    var showSoundToneDialog by remember { mutableStateOf(false) }
    var showVoiceConfigDialog by remember { mutableStateOf(false) }
    var showReminderBehaviorDialog by remember { mutableStateOf(false) }
    var showConnectWebsiteDialog by remember { mutableStateOf(false) }
    var showDisableAccountDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }

    // Doctor & Caretaker dialogs
    var doctorToRevoke by remember { mutableStateOf<UserEntity?>(null) }
    var caretakerToRevoke by remember { mutableStateOf<CaretakerLinkEntity?>(null) }
    var caretakerToManage by remember { mutableStateOf<CaretakerLinkEntity?>(null) }

    // Custom Audio Picker Launcher (supports MP3, WAV, M4A, OGG)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (ignored: Exception) {}

            var fileName = "Custom Sound"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            } catch (ignored: Exception) {}

            viewModel.setCustomSound(uri, fileName)
            Toast.makeText(context, "Selected audio: $fileName", Toast.LENGTH_SHORT).show()
        }
    }

    // System Notification Permission Launcher
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setNotifMedicineReminders(isGranted)
=======
    val allUsers by viewModel.allUsers.collectAsState()

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
        if (isGranted) {
            viewModel.rescheduleAllReminders()
        }
    }

<<<<<<< HEAD
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
            .testTag("patient_settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Section: Title & Patient Subtitle
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = "Patient Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                Text(
                    text = "Manage your account, privacy, reminders, connections, and security.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MedTextSecondary
                )
            }
        }

        // ==========================================
        // SECTION 1: PROFILE & PERSONAL INFORMATION
        // ==========================================
        item {
            SettingsCard(
                title = "Profile & Personal Information",
                icon = Icons.Default.Person,
                iconTint = MedBluePrimary
            ) {
                val user = currentUser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Photo Avatar
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MedBlueLight)
                            .border(2.dp, MedBluePrimary.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Photo",
                            tint = MedBluePrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.name ?: "Vijay Kumar",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = user?.email ?: "patient@medtime.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                        Text(
                            text = user?.phone ?: "+1 (555) 234-5678",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MedDivider)
                Spacer(modifier = Modifier.height(12.dp))

                // Patient Details Row: Blood Group & Emergency Contact
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailChip(label = "Blood Group", value = user?.bloodGroup?.ifBlank { "O+" } ?: "O+")
                    DetailChip(
                        label = "Allergies",
                        value = user?.allergies?.ifBlank { "None" } ?: "None",
                        isAlert = !user?.allergies.isNullOrBlank() && user?.allergies != "None"
                    )
                    DetailChip(
                        label = "Emergency SOS",
                        value = user?.emergencyContactPhone?.ifBlank { "911" } ?: "911"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { showEditProfileDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_edit_profile"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile & Emergency Info", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // ==========================================
        // SECTION 2: SECURITY & PRIVACY
        // ==========================================
        item {
            SettingsCard(
                title = "Security & Privacy",
                icon = Icons.Default.Shield,
                iconTint = Color(0xFF1E88E5)
            ) {
                // Change Password
                SettingsActionRow(
                    title = "Change Password",
                    subtitle = "Update your account login password",
                    icon = Icons.Outlined.Lock,
                    testTag = "row_change_password",
                    onClick = { showChangePasswordDialog = true }
                )

                HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                // Two-Factor Authentication Toggle
                SettingsToggleRow(
                    title = "Two-Factor Authentication (2FA)",
                    subtitle = if (is2FaEnabled) "Active • Requires 6-digit OTP verification" else "Disabled • Recommended for high security",
                    checked = is2FaEnabled,
                    testTag = "toggle_2fa",
                    onCheckedChange = { viewModel.setTwoFactorEnabled(it) }
                )

                HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                // Backup Codes
                SettingsActionRow(
                    title = "Backup Recovery Codes",
                    subtitle = "8 one-time emergency recovery codes",
                    icon = Icons.Outlined.Key,
                    testTag = "row_backup_codes",
                    onClick = { showBackupCodesDialog = true }
                )

                HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                // Active Sessions & Login History
                SettingsActionRow(
                    title = "Active Sessions & Login History",
                    subtitle = "${activeSessions.size} connected devices • Pixel 8 Pro (Active)",
                    icon = Icons.Outlined.Devices,
                    testTag = "row_active_sessions",
                    onClick = { showActiveSessionsDialog = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.logoutFromOtherDevices() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("btn_logout_other_devices"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MedError)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout from Other Devices", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // ==========================================
        // SECTION 3: NOTIFICATIONS
        // ==========================================
        item {
            SettingsCard(
                title = "Notifications",
                icon = Icons.Default.Notifications,
                iconTint = Color(0xFFF57C00)
            ) {
                SettingsToggleRow(
                    title = "Medicine Reminders",
                    subtitle = "Sound and pop-up alarms for daily medication doses",
                    checked = notifMeds,
                    testTag = "toggle_notif_medicine",
                    onCheckedChange = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                return@SettingsToggleRow
                            }
                        }
                        viewModel.setNotifMedicineReminders(enabled)
                    }
                )

                HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                SettingsToggleRow(
                    title = "Appointment Notifications",
                    subtitle = "Upcoming doctor consultations and clinic alerts",
                    checked = notifAppts,
                    testTag = "toggle_notif_appointments",
                    onCheckedChange = { viewModel.setNotifAppointmentReminders(it) }
                )

                HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                SettingsToggleRow(
                    title = "Message Notifications",
                    subtitle = "Secure chats from your doctors and caregivers",
                    checked = notifMsgs,
                    testTag = "toggle_notif_messages",
                    onCheckedChange = { viewModel.setNotifMessageAlerts(it) }
                )

                HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                SettingsToggleRow(
                    title = "Medical News & Health Tips",
                    subtitle = "Curated wellness advisories and medical bulletins",
                    checked = notifNews,
                    testTag = "toggle_notif_news",
                    onCheckedChange = { viewModel.setNotifMedicalNews(it) }
                )

                HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                SettingsToggleRow(
                    title = "Security Notifications",
                    subtitle = "Alerts for new logins and account permission changes",
                    checked = notifSec,
                    testTag = "toggle_notif_security",
                    onCheckedChange = { viewModel.setNotifSecurityAlerts(it) }
                )
            }
        }

        // ==========================================
        // SECTION 4: REMINDER PREFERENCES & ALARM SOUNDS
        // ==========================================
        item {
            SettingsCard(
                title = "Reminder Preferences",
                icon = Icons.Default.Alarm,
                iconTint = Color(0xFF673AB7)
            ) {
                // 1. Reminder Sound Toggle
                SettingsToggleRow(
                    title = "Reminder Sound",
                    subtitle = if (reminderSound) "Audible alarm enabled • $reminderTone" else "Muted (Silent mode)",
                    checked = reminderSound,
                    testTag = "toggle_reminder_sound",
                    onCheckedChange = { viewModel.setReminderSoundEnabled(it) }
                )

                if (reminderSound) {
                    HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                    // Sound Selection Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSoundToneDialog = true }
                            .padding(vertical = 10.dp)
                            .testTag("row_alarm_tone"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MedBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.MusicNote, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Alarm Sound Tone", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MedTextPrimary)
                                Text(
                                    text = if (reminderTone == "Custom Audio File" && !customSoundName.isNullOrBlank()) "Custom: $customSoundName" else reminderTone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalIconButton(
                                onClick = { viewModel.previewSound(reminderTone) },
                                modifier = Modifier.size(36.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MedBlueLight,
                                    contentColor = MedBluePrimary
                                )
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Preview Alarm Sound", modifier = Modifier.size(18.dp))
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MedTextSecondary)
                        }
                    }

                    // Custom Audio File Picker Affordance
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MedBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Outlined.AudioFile, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Custom Audio File",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MedTextPrimary
                                    )
                                }
                                Text("MP3, WAV, M4A, OGG", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                            }

                            if (!customSoundName.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MedBlueLight.copy(alpha = 0.5f))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Selected Sound:", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                        Text(customSoundName ?: "", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MedBluePrimary)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        TextButton(
                                            onClick = { viewModel.previewSound("Custom Audio File") },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Preview", fontSize = 11.sp)
                                        }
                                        TextButton(
                                            onClick = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Change", fontSize = 11.sp)
                                        }
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Choose Custom Sound File", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                // 2. Spoken Medicine Reminder Section
                SettingsToggleRow(
                    title = "Spoken Medicine Reminder",
                    subtitle = if (spokenReminderEnabled) "Speaks patient & medicine name aloud" else "Voice reminders turned off",
                    checked = spokenReminderEnabled,
                    testTag = "toggle_spoken_reminder",
                    onCheckedChange = { viewModel.setSpokenReminderEnabled(it) }
                )

                if (spokenReminderEnabled) {
                    HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE3F2FD),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = MedBluePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Dynamic Spoken Voice Engine",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MedBluePrimary
                                )
                            }

                            Text(
                                text = "Sample announcement:\n\"${currentUser?.name?.ifBlank { "Vijay" } ?: "Vijay"}, it is time to take your Metformin, 500 milligrams, one tablet, after food.\"",
                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                color = MedTextPrimary,
                                lineHeight = 16.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showVoiceConfigDialog = true },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.SettingsVoice, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Voice & Language", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { viewModel.testVoice() },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test Voice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                // 3. Vibration Alert
                SettingsToggleRow(
                    title = "Vibration Alert",
                    subtitle = "Haptic feedback pattern during reminder triggers",
                    checked = reminderVib,
                    testTag = "toggle_reminder_vibration",
                    onCheckedChange = { viewModel.setReminderVibrationEnabled(it) }
                )

                HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                // 4. Alert Behavior
                SettingsActionRow(
                    title = "Reminder Alert Behavior",
                    subtitle = reminderBehavior,
                    icon = Icons.Outlined.Tune,
                    testTag = "row_reminder_behavior",
                    onClick = { showReminderBehaviorDialog = true }
                )

                HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                // 5. Real-Time Native Alarm Testing & Policy Status
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MedBlueLight.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MedBluePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Real-Time Android Alarm Engine",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedBluePrimary
                            )
                        }

                        Text(
                            text = "Uses Android AlarmManager (Exact Alarms) with full-screen lock screen activity, custom sound + spoken reminder, 60s audible alarm with vibration, 5m automatic repeat (3 retry policy), and 10m snooze.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary,
                            lineHeight = 16.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.triggerTestAlarm(5) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("btn_test_real_alarm"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                            ) {
                                Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Alarm (5s)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.rescheduleAllExactAlarms() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("btn_reschedule_alarms"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Resync Alarms", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECTION 5: DOCTOR & CARETAKER CONNECTIONS
        // ==========================================
        item {
            SettingsCard(
                title = "Doctor & Caretaker Connections",
                icon = Icons.Default.PeopleAlt,
                iconTint = Color(0xFF00897B)
            ) {
                // Section 5A: My Doctors
                Text(
                    text = "My Doctors",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (doctors.isEmpty()) {
                    Text(
                        text = "No doctors connected yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                } else {
                    doctors.take(3).forEach { doc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = doc.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MedTextPrimary
                                )
                                Text(
                                    text = "${doc.doctorSpecialty.ifBlank { "Physician" }} • ${doc.doctorHospital.ifBlank { "City General Hospital" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary
                                )
                            }
                            TextButton(
                                onClick = { doctorToRevoke = doc },
                                colors = ButtonDefaults.textButtonColors(contentColor = MedError)
                            ) {
                                Text("Revoke", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MedDivider)
                Spacer(modifier = Modifier.height(10.dp))

                // Section 5B: My Caretakers
                Text(
                    text = "My Caretakers & Family",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (caretakerLinks.isEmpty()) {
                    Text(
                        text = "No family caretaker linked.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                } else {
                    caretakerLinks.forEach { link ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = link.caretakerName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MedTextPrimary
                                )
                                Text(
                                    text = "Status: ${link.status} • Adherence & Meds Access",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (link.status == "APPROVED") MedSuccess else MedWarning
                                )
                            }
                            Row {
                                TextButton(onClick = { caretakerToManage = link }) {
                                    Text("Manage", fontSize = 12.sp)
                                }
                                TextButton(
                                    onClick = { caretakerToRevoke = link },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MedError)
                                ) {
                                    Text("Revoke", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECTION 6: WEBSITE CONNECTION
        // ==========================================
        item {
            SettingsCard(
                title = "Website Connection",
                icon = Icons.Default.QrCodeScanner,
                iconTint = Color(0xFF0288D1)
            ) {
                Text(
                    text = "Connect to MedTime Website",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                Text(
                    text = "Sync your medical charts, prescriptions, and dose logs directly to medtime.health on your desktop browser.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MedTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showConnectWebsiteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_connect_website"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Link Web Browser via QR / Code", fontWeight = FontWeight.SemiBold)
                }

                if (webSessions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Connected Web Sessions (${webSessions.size})",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    webSessions.forEach { session ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MedBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MedSuccess)
                                    )
                                    Column {
                                        Text(
                                            text = session.browser,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MedTextPrimary
                                        )
                                        Text(
                                            text = "${session.location} • ${session.connectedAt}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MedTextSecondary
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.disconnectWebsiteSession(session.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Disconnect", tint = MedError, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.disconnectAllWebSessions() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("btn_disconnect_all_web"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MedError)
                    ) {
                        Text("Disconnect All Web Browsers", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // ==========================================
        // SECTION 7: PRIVACY
        // ==========================================
        item {
            SettingsCard(
                title = "Privacy",
                icon = Icons.Default.Lock,
                iconTint = Color(0xFF388E3C)
            ) {
                SettingsToggleRow(
                    title = "Emergency Location Sharing",
                    subtitle = "Share real-time GPS coordinates with your emergency contact and 911 when SOS is pressed",
                    checked = locationSharing,
                    testTag = "toggle_location_sharing",
                    onCheckedChange = { viewModel.setLocationSharingEnabled(it) }
                )

                HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                // Medical Document Access
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Text(
                        text = "Medical Document Access",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MedTextPrimary
                    )
                    Text(
                        text = "Controls who can view your uploaded test reports and prescriptions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AccessModeChip(
                            label = "Doctors & Caretaker",
                            selected = docAccessMode == "DOCTORS_AND_CARETAKER",
                            onClick = { viewModel.setMedicalDocumentAccess("DOCTORS_AND_CARETAKER") }
                        )
                        AccessModeChip(
                            label = "Doctors Only",
                            selected = docAccessMode == "DOCTORS_ONLY",
                            onClick = { viewModel.setMedicalDocumentAccess("DOCTORS_ONLY") }
                        )
                        AccessModeChip(
                            label = "Private Only",
                            selected = docAccessMode == "PRIVATE_ONLY",
                            onClick = { viewModel.setMedicalDocumentAccess("PRIVATE_ONLY") }
                        )
                    }
                }

                HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                // Caretaker & Doctor permissions summary
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Connected Doctor Permissions",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = "Prescriptions, Consultation Records, and Medication Adherence",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MedSuccessLight
                    ) {
                        Text(
                            text = "HIPAA Secure",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MedSuccess
                        )
                    }
                }
            }
        }

        // ==========================================
        // SECTION: SUPABASE POSTGRESQL DATABASE
        // ==========================================
        item {
            SettingsCard(
                title = "Supabase PostgreSQL Database",
                icon = Icons.Default.CloudSync,
                iconTint = Color(0xFF3ECF8E)
            ) {
                // Header with status indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cloud Database Sync",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = if (isSupabaseConfigured) "Connected to Supabase PostgreSQL" else "Cloud database credentials required",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                    }

                    if (isSupabaseSyncing) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MedBlueLight
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = MedBluePrimary
                                )
                                Text(
                                    text = "Syncing...",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MedBluePrimary
                                )
                            }
                        }
                    } else if (isSupabaseConfigured) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "Active • Ready",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF2E7D32)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MedWarningLight
                        ) {
                            Text(
                                text = "Setup Needed",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MedWarning
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Connection details & latency
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MedBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Database Engine:", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                            Text("PostgreSQL 15+ (PostgREST)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
                        }

                        val activeUrl = viewModel.repository.supabaseConfig.getBaseUrl()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Project URL:", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                            Text(
                                text = if (activeUrl.contains("xyzcompany")) "Not configured" else activeUrl.take(28) + "...",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                        }

                        lastSupabaseSync?.let { sync ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Last Synced:", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                Text(sync.getFormattedTime(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MedSuccess)
                            }
                        }

                        supabaseConnStatus?.let { status ->
                            when (status) {
                                is com.example.data.remote.supabase.SupabaseConnectionResult.Success -> {
                                    Text(
                                        text = "Ping: ${status.latencyMs}ms • PostgreSQL HTTP 200 OK",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                is com.example.data.remote.supabase.SupabaseConnectionResult.Error -> {
                                    Text(
                                        text = "Error: ${status.message.take(60)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MedError
                                    )
                                }
                                is com.example.data.remote.supabase.SupabaseConnectionResult.NotConfigured -> {
                                    Text(
                                        text = "Status: Enter your Supabase URL & Anon Key to connect",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MedWarning
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions: Test Ping, Sync Now, Configure
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.testSupabaseConnection() },
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Ping", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { viewModel.syncWithSupabase() },
                        enabled = !isSupabaseSyncing,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = { showConfigureSupabaseDialog = true },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Configure URL & Anon Key", fontSize = 11.sp)
                }
            }
        }

        // ==========================================
        // SECTION 8: ACCOUNT
        // ==========================================
        item {
            SettingsCard(
                title = "Account",
                icon = Icons.Default.ManageAccounts,
                iconTint = Color(0xFF455A64)
            ) {
                // Account Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Account Status",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = "Patient Account ID: ${currentUser?.id ?: "patient_1"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MedSuccessLight
                    ) {
                        Text(
                            text = "Active • Verified",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MedSuccess
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MedDivider)
                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: Disable, Delete, Logout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDisableAccountDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("btn_disable_account"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MedWarning)
                    ) {
                        Text("Disable Account", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { showDeleteAccountDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("btn_delete_account"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MedError)
                    ) {
                        Text("Delete Account", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_logout_patient"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout from MedTime", fontWeight = FontWeight.Bold)
                }
            }
        }

        // ==========================================
        // SECTION 9: ABOUT MEDTIME & SUPPORT
        // ==========================================
        item {
            SettingsCard(
                title = "About MedTime & Legal",
                icon = Icons.Default.Info,
                iconTint = MedBluePrimary
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "MedTime Patient App",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = "Smart Medical Adherence & Caregiver Network",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MedBlueLight
                    ) {
                        Text(
                            text = "v2.4.0",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MedBluePrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MedDivider)

                SettingsActionRow(
                    title = "Terms of Service",
                    subtitle = "Patient privacy and software use agreement",
                    icon = Icons.Outlined.Description,
                    testTag = "row_terms",
                    onClick = { showTermsDialog = true }
                )

                HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                SettingsActionRow(
                    title = "Privacy Policy",
                    subtitle = "HIPAA compliance & biometric data handling",
                    icon = Icons.Outlined.Policy,
                    testTag = "row_privacy_policy",
                    onClick = { showPrivacyPolicyDialog = true }
                )

                HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                SettingsActionRow(
                    title = "Medical Disclaimer & Licenses",
                    subtitle = "Clinical guidance notices and open-source licenses",
                    icon = Icons.Outlined.VerifiedUser,
                    testTag = "row_disclaimer",
                    onClick = { showDisclaimerDialog = true }
                )
            }
        }
    }

    // ==========================================
    // ALL INTERACTIVE DIALOGS
    // ==========================================

    // 1. Edit Profile Dialog
    if (showEditProfileDialog) {
        val user = currentUser
        var name by remember { mutableStateOf(user?.name ?: "Vijay Kumar") }
        var email by remember { mutableStateOf(user?.email ?: "patient@medtime.com") }
        var phone by remember { mutableStateOf(user?.phone ?: "+1 (555) 234-5678") }
        var bloodGroup by remember { mutableStateOf(user?.bloodGroup ?: "O+") }
        var allergies by remember { mutableStateOf(user?.allergies ?: "Amoxicillin, Pollen") }
        var emergencyPhone by remember { mutableStateOf(user?.emergencyContactPhone ?: "+1 (555) 987-6543") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Patient Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("input_edit_name"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth().testTag("input_edit_email"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Number") },
                        modifier = Modifier.fillMaxWidth().testTag("input_edit_phone"),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = { bloodGroup = it },
                            label = { Text("Blood Group") },
                            modifier = Modifier.weight(1f).testTag("input_edit_blood"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = emergencyPhone,
                            onValueChange = { emergencyPhone = it },
                            label = { Text("Emergency SOS Phone") },
                            modifier = Modifier.weight(1.5f).testTag("input_edit_emergency_phone"),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = allergies,
                        onValueChange = { allergies = it },
                        label = { Text("Known Allergies") },
                        modifier = Modifier.fillMaxWidth().testTag("input_edit_allergies"),
                        singleLine = true
                    )
                }
=======
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MedError)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out of MedTime?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to end your current session? You will return to the MedTime login screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MedTextSecondary
                )
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
            },
            confirmButton = {
                Button(
                    onClick = {
<<<<<<< HEAD
                        val currentUserEntity = user
                        if (currentUserEntity != null) {
                            viewModel.updateUserProfile(
                                currentUserEntity.copy(
                                    name = name.trim(),
                                    email = email.trim(),
                                    phone = phone.trim(),
                                    bloodGroup = bloodGroup.trim(),
                                    allergies = allergies.trim(),
                                    emergencyContactPhone = emergencyPhone.trim()
                                )
                            )
                        }
                        showEditProfileDialog = false
                    },
                    modifier = Modifier.testTag("btn_save_profile")
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
=======
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedError),
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                    Text("Cancel")
                }
            }
        )
    }

<<<<<<< HEAD
    // 2. Change Password Dialog
    if (showChangePasswordDialog) {
        var currentPwd by remember { mutableStateOf("") }
        var newPwd by remember { mutableStateOf("") }
        var confirmPwd by remember { mutableStateOf("") }
=======
    // Change Password Dialog
    if (showChangePasswordDialog && currentUser != null) {
        val user = currentUser!!
        var currentPwdInput by remember { mutableStateOf("") }
        var newPwdInput by remember { mutableStateOf("") }
        var confirmNewPwdInput by remember { mutableStateOf("") }
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
        var pwdError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
<<<<<<< HEAD
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = currentPwd,
                        onValueChange = { currentPwd = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("input_current_pwd"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPwd,
                        onValueChange = { newPwd = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("input_new_pwd"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = confirmPwd,
                        onValueChange = { confirmPwd = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("input_confirm_pwd"),
                        singleLine = true
                    )
                    pwdError?.let {
                        Text(text = it, color = MedError, style = MaterialTheme.typography.bodySmall)
                    }
=======
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MedBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Password", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (pwdError != null) {
                        Text(pwdError!!, color = MedError, style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedTextField(
                        value = currentPwdInput,
                        onValueChange = { currentPwdInput = it },
                        label = { Text("Current Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPwdInput,
                        onValueChange = { newPwdInput = it },
                        label = { Text("New Password (min 6 chars)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmNewPwdInput,
                        onValueChange = { confirmNewPwdInput = it },
                        label = { Text("Confirm New Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                }
            },
            confirmButton = {
                Button(
                    onClick = {
<<<<<<< HEAD
                        if (newPwd.length < 6) {
                            pwdError = "New password must be at least 6 characters."
                            return@Button
                        }
                        if (newPwd != confirmPwd) {
                            pwdError = "Passwords do not match."
                            return@Button
                        }
                        viewModel.changePassword(newPwd) { success: Boolean, msg: String ->
                            if (success) {
                                showChangePasswordDialog = false
                            } else {
                                pwdError = msg
                            }
                        }
                    },
                    modifier = Modifier.testTag("btn_confirm_change_pwd")
=======
                        if (newPwdInput.length < 6) {
                            pwdError = "New password must be at least 6 characters."
                            return@Button
                        }
                        if (newPwdInput != confirmNewPwdInput) {
                            pwdError = "Passwords do not match."
                            return@Button
                        }
                        viewModel.updateUserProfile(user.copy(password = newPwdInput))
                        showChangePasswordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                ) {
                    Text("Update Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

<<<<<<< HEAD
    // 3. Backup Codes Dialog
    if (showBackupCodesDialog) {
        AlertDialog(
            onDismissRequest = { showBackupCodesDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = MedBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Emergency Backup Codes", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Store these one-time recovery codes in a safe place. If you lose your phone or 2FA authenticator, each code can be used once to access your MedTime account.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MedBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            backupCodes.chunked(2).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    row.forEach { code ->
                                        Text(
                                            text = code,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                            ),
                                            color = MedBluePrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    OutlinedButton(
                        onClick = { viewModel.regenerateBackupCodes() },
                        modifier = Modifier.testTag("btn_regenerate_codes")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Regenerate")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("MedTime Backup Codes", backupCodes.joinToString("\n"))
                            cm.setPrimaryClip(clip)
                            Toast.makeText(context, "Backup codes copied to clipboard!", Toast.LENGTH_SHORT).show()
                            showBackupCodesDialog = false
                        },
                        modifier = Modifier.testTag("btn_copy_codes")
                    ) {
                        Text("Copy All & Close")
                    }
                }
            }
        )
    }

    // 4. Active Sessions Dialog
    if (showActiveSessionsDialog) {
        AlertDialog(
            onDismissRequest = { showActiveSessionsDialog = false },
            title = { Text("Active Login Sessions", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    activeSessions.forEach { session ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (session.isCurrent) MedBlueLight.copy(alpha = 0.5f) else MedBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (session.isCurrent) MedBluePrimary else MedBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = session.deviceName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MedTextPrimary
                                    )
                                    if (session.isCurrent) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MedSuccessLight
                                        ) {
                                            Text(
                                                text = "This Device",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MedSuccess
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = session.deviceType,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MedTextSecondary
                                )
                                Text(
                                    text = "IP: ${session.ipAddress} • ${session.location} • ${session.timestamp}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MedTextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showActiveSessionsDialog = false }) {
                    Text("Done")
=======
    if (showEditProfileDialog && currentUser != null) {
        val user = currentUser!!
        var editName by remember { mutableStateOf(user.name) }
        var editPhone by remember { mutableStateOf(user.phone) }
        var editBlood by remember { mutableStateOf(user.bloodGroup) }
        var editAllergies by remember { mutableStateOf(user.allergies) }
        var editContactName by remember { mutableStateOf(user.emergencyContactName) }
        var editContactPhone by remember { mutableStateOf(user.emergencyContactPhone) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Update Medical Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBlood,
                        onValueChange = { editBlood = it },
                        label = { Text("Blood Group") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAllergies,
                        onValueChange = { editAllergies = it },
                        label = { Text("Known Allergies") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editContactName,
                        onValueChange = { editContactName = it },
                        label = { Text("Emergency Contact Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editContactPhone,
                        onValueChange = { editContactPhone = it },
                        label = { Text("Emergency Contact Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = user.copy(
                            name = editName,
                            phone = editPhone,
                            bloodGroup = editBlood,
                            allergies = editAllergies,
                            emergencyContactName = editContactName,
                            emergencyContactPhone = editContactPhone
                        )
                        viewModel.updateUserProfile(updated)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Text("Save Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                }
            }
        )
    }

<<<<<<< HEAD
    // 5. Sound Tone Selection Dialog (Presets + Custom)
    if (showSoundToneDialog) {
        val soundPresets = listOf(
            "MedTime Default" to "Classic resonant chime chime alert",
            "Gentle Chime" to "Soft acoustic multi-tone sequence",
            "Soft Bell" to "Calm hospital medical bell tone",
            "Medication Alert" to "High-visibility double-pulse beep",
            "Digital Alarm" to "Electronic 8-bit digital watch melody",
            "Calm Reminder" to "Relaxing harmonic ambient chime"
        )

        AlertDialog(
            onDismissRequest = {
                viewModel.stopSoundPreview()
                showSoundToneDialog = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = MedBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose Alarm Sound", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Selected sound will play for up to 60 seconds whenever your medicine alarm fires.",
=======
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
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MedBluePrimary)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser?.name?.take(2)?.uppercase() ?: "ME",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                            Column {
                                Text(
                                    text = currentUser?.name ?: "User",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = currentUser?.email ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        IconButton(onClick = { showEditProfileDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Active Clinical Role: ${currentUser?.role}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Quick Role Switcher for Evaluators and Users
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Switch Active Clinical Profile",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Text(
                        text = "Instantly switch roles to test Patient, Doctor, Caretaker, or Admin features:",
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )

<<<<<<< HEAD
                    soundPresets.forEach { (presetName, presetDesc) ->
                        val isSelected = reminderTone == presetName
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MedBlueLight else MedBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MedBluePrimary else MedBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setReminderSoundTone(presetName)
                                }
=======
                    allUsers.forEach { user ->
                        val isSelected = user.id == currentUser?.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.switchUser(user.id) }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MedBluePrimary else MedBorder,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            color = if (isSelected) MedBlueLight.copy(alpha = 0.4f) else MedSurface
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
<<<<<<< HEAD
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.setReminderSoundTone(presetName) }
                                    )
                                    Column {
                                        Text(
                                            text = presetName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) MedBluePrimary else MedTextPrimary
                                        )
                                        Text(
                                            text = presetDesc,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MedTextSecondary
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.previewSound(presetName) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Preview $presetName",
                                        tint = MedBluePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Option: Choose Custom Audio File
                    val isCustomSelected = reminderTone == "Custom Audio File"
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCustomSelected) MedBlueLight else MedBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isCustomSelected) MedBluePrimary else MedBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (customSoundUri != null) {
                                    viewModel.setReminderSoundTone("Custom Audio File")
                                } else {
                                    audioPickerLauncher.launch(arrayOf("audio/*"))
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RadioButton(
                                    selected = isCustomSelected,
                                    onClick = {
                                        if (customSoundUri != null) {
                                            viewModel.setReminderSoundTone("Custom Audio File")
                                        } else {
                                            audioPickerLauncher.launch(arrayOf("audio/*"))
                                        }
                                    }
                                )
                                Column {
                                    Text(
                                        text = "Custom Audio File",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isCustomSelected) MedBluePrimary else MedTextPrimary
                                    )
                                    Text(
                                        text = if (!customSoundName.isNullOrBlank()) "File: $customSoundName" else "Choose MP3, WAV, M4A, OGG",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (!customSoundName.isNullOrBlank()) MedBluePrimary else MedTextSecondary
                                    )
                                }
                            }

                            Row {
                                if (customSoundUri != null) {
                                    IconButton(
                                        onClick = { viewModel.previewSound("Custom Audio File") },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Preview Custom", tint = MedBluePrimary, modifier = Modifier.size(18.dp))
                                    }
                                }
                                IconButton(
                                    onClick = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = "Browse Audio", tint = MedBluePrimary, modifier = Modifier.size(18.dp))
=======
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(user.name, fontWeight = FontWeight.Bold, color = MedTextPrimary)
                                    Text(
                                        text = "${user.role} • ${user.email}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MedTextSecondary
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = MedBluePrimary)
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                                }
                            }
                        }
                    }
                }
<<<<<<< HEAD
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.stopSoundPreview()
                    showSoundToneDialog = false
                }) {
                    Text("Apply Sound")
                }
            }
        )
    }

    // 5B. Voice & Language Configuration Dialog
    if (showVoiceConfigDialog) {
        val languages = listOf(
            "en-US" to "English (United States)",
            "en-GB" to "English (United Kingdom)",
            "en-IN" to "English (India)",
            "es-ES" to "Spanish (Español)",
            "fr-FR" to "French (Français)",
            "de-DE" to "German (Deutsch)"
        )

        val voiceProfiles = listOf(
            "Default Device Voice" to "System standard voice engine",
            "Female (Standard)" to "Clear, natural female reading voice",
            "Male (Standard)" to "Deep, natural male reading voice",
            "Calm Healthcare Voice" to "Soothing clinical cadence"
        )

        AlertDialog(
            onDismissRequest = { showVoiceConfigDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SettingsVoice, contentDescription = null, tint = MedBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Voice & Speech Settings", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Language:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
                    languages.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { viewModel.setTtsLanguage(code) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = ttsLanguage == code,
                                onClick = { viewModel.setTtsLanguage(code) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(label, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Voice Style:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
                    voiceProfiles.forEach { (voiceName, voiceDesc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { viewModel.setTtsVoice(voiceName) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = ttsVoice == voiceName,
                                onClick = { viewModel.setTtsVoice(voiceName) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(voiceName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                                Text(voiceDesc, style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Speech Volume: ${(speechVolume * 100).toInt()}%", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
                    Slider(
                        value = speechVolume,
                        onValueChange = { viewModel.setSpeechVolume(it) },
                        valueRange = 0.3f..1.0f,
                        steps = 7,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showVoiceConfigDialog = false
                    viewModel.testVoice()
                }) {
                    Text("Test & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVoiceConfigDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // 6. Reminder Behavior Dialog
    if (showReminderBehaviorDialog) {
        val behaviors = listOf(
            "Full Screen Alarm" to "Displays persistent full-screen alert over any app with snooze options",
            "Ring Until Dismissed" to "Continuously plays notification tone until taken or snoozed",
            "Gentle Banner Alert" to "Standard non-intrusive notification banner"
        )
        AlertDialog(
            onDismissRequest = { showReminderBehaviorDialog = false },
            title = { Text("Reminder Alert Style", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    behaviors.forEach { (title, desc) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (reminderBehavior == title) MedBlueLight else MedBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (reminderBehavior == title) MedBluePrimary else MedBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setReminderBehavior(title)
                                    showReminderBehaviorDialog = false
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (reminderBehavior == title) MedBluePrimary else MedTextPrimary
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MedTextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReminderBehaviorDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // 7. Connect Website Dialog
    if (showConnectWebsiteDialog) {
        var connectCode by remember { mutableStateOf("MED-WEB-8492") }
        AlertDialog(
            onDismissRequest = { showConnectWebsiteDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MedBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connect to MedTime Website", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "1. Open https://medtime.health on your computer.\n2. Click \"Link Patient Phone\" on the website.\n3. Enter the pairing code below or scan the QR code.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MedBlueLight,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = connectCode,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                letterSpacing = 2.sp
                            ),
                            color = MedBluePrimary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.connectToWebsite(connectCode)
                        showConnectWebsiteDialog = false
                    },
                    modifier = Modifier.testTag("btn_complete_web_link")
                ) {
                    Text("Confirm Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConnectWebsiteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 8. Revoke Doctor Dialog
    doctorToRevoke?.let { doc ->
        AlertDialog(
            onDismissRequest = { doctorToRevoke = null },
            title = { Text("Revoke Doctor Access?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to revoke clinical access for ${doc.name}? They will no longer be able to view your medication adherence or medical documents.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.revokeDoctorConnection(doc.id)
                        doctorToRevoke = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedError)
                ) {
                    Text("Revoke Connection")
                }
            },
            dismissButton = {
                TextButton(onClick = { doctorToRevoke = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 9. Revoke Caretaker Dialog
    caretakerToRevoke?.let { link ->
        AlertDialog(
            onDismissRequest = { caretakerToRevoke = null },
            title = { Text("Revoke Caretaker Access?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to revoke family caregiver permissions for ${link.caretakerName}? They will stop receiving dose miss alerts.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.revokeCaretakerLink(link.id)
                        caretakerToRevoke = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedError)
                ) {
                    Text("Revoke Caretaker")
                }
            },
            dismissButton = {
                TextButton(onClick = { caretakerToRevoke = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 10. Manage Caretaker Permissions Dialog
    caretakerToManage?.let { link ->
        var canMed by remember { mutableStateOf(link.canViewMedicines) }
        var canAdh by remember { mutableStateOf(link.canViewAdherence) }
        var canAppt by remember { mutableStateOf(link.canViewAppointments) }
        var canAlert by remember { mutableStateOf(link.canReceiveAlerts) }

        AlertDialog(
            onDismissRequest = { caretakerToManage = null },
            title = { Text("Caretaker Permissions: ${link.caretakerName}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SettingsToggleRow(
                        title = "View Daily Medicines",
                        subtitle = "Allow viewing active medications list",
                        checked = canMed,
                        testTag = "toggle_perm_med",
                        onCheckedChange = { canMed = it }
                    )
                    SettingsToggleRow(
                        title = "View Adherence Stats",
                        subtitle = "Allow viewing dose taken/missed percentage",
                        checked = canAdh,
                        testTag = "toggle_perm_adh",
                        onCheckedChange = { canAdh = it }
                    )
                    SettingsToggleRow(
                        title = "View Appointments",
                        subtitle = "Allow viewing scheduled doctor visits",
                        checked = canAppt,
                        testTag = "toggle_perm_appt",
                        onCheckedChange = { canAppt = it }
                    )
                    SettingsToggleRow(
                        title = "Missed Dose SOS Alerts",
                        subtitle = "Notify caregiver if doses are missed",
                        checked = canAlert,
                        testTag = "toggle_perm_alert",
                        onCheckedChange = { canAlert = it }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateCaretakerPermissions(link.id, canMed, canAdh, canAppt, canAlert)
                        caretakerToManage = null
                    }
                ) {
                    Text("Save Permissions")
                }
            },
            dismissButton = {
                TextButton(onClick = { caretakerToManage = null }) {
                    Text("Close")
                }
            }
        )
    }

    // 11. Disable Account Dialog
    if (showDisableAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDisableAccountDialog = false },
            title = { Text("Disable Patient Account?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Disabling your account will pause medication push reminders and hide your profile from connected doctors. You can reactivate anytime simply by logging back in.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.disablePatientAccount()
                        showDisableAccountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedWarning)
                ) {
                    Text("Disable Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisableAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 12. Delete Account Dialog
    if (showDeleteAccountDialog) {
        var confirmText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Permanently Delete Account?", fontWeight = FontWeight.Bold, color = MedError) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This action is permanent and cannot be undone. All your medication reminders, medical history, documents, and appointments will be permanently purged.")
                    Text("Type \"DELETE\" below to confirm:", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = confirmText,
                        onValueChange = { confirmText = it },
                        modifier = Modifier.fillMaxWidth().testTag("input_confirm_delete"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (confirmText.trim().uppercase() == "DELETE") {
                            viewModel.deletePatientAccount("CONFIRMED")
                            showDeleteAccountDialog = false
                        }
                    },
                    enabled = confirmText.trim().uppercase() == "DELETE",
                    colors = ButtonDefaults.buttonColors(containerColor = MedError),
                    modifier = Modifier.testTag("btn_confirm_delete_account")
                ) {
                    Text("Delete Forever")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 13. Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out of MedTime?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out of your MedTime patient account?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.testTag("btn_confirm_logout")
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 14. Terms of Service Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms of Service", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "MedTime Patient Agreement (v2.4.0)\n\n" +
                                "1. Medical Assistance: MedTime is an adherence and scheduling aid designed to support personal wellness. It does not provide definitive medical diagnoses or replace emergency medical services.\n\n" +
                                "2. Caregiver Sharing: By connecting family members or physicians, you authorize MedTime to transmit dose adherence reports securely according to your selected permission settings.\n\n" +
                                "3. Data Security: All personal data is encrypted in transit and at rest using industry-standard AES-256 protocols.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextPrimary
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showTermsDialog = false }) {
                    Text("I Understand")
                }
            }
        )
    }

    // 15. Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "HIPAA Compliance & Data Privacy\n\n" +
                                "• Protected Health Information (PHI): Your prescriptions, dosages, and medical documents are stored securely in encrypted local and cloud databases.\n\n" +
                                "• No Third-Party Selling: MedTime never sells patient health data or personal identifiers to third parties or advertisers.\n\n" +
                                "• Account Erasure: You have the right to permanently purge your complete health history at any time via the Delete Account option.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextPrimary
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // 16. Medical Disclaimer Dialog
    if (showDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { showDisclaimerDialog = false },
            title = { Text("Medical Disclaimer", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "IMPORTANT MEDICAL NOTICE:\n\n" +
                                "In case of a medical emergency, call 911 or visit your nearest emergency room immediately.\n\n" +
                                "MedTime reminders and AI Health suggestions are intended for informational guidance only. Always follow the specific instructions of your licensed healthcare provider.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextPrimary
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showDisclaimerDialog = false }) {
                    Text("Acknowledged")
                }
            }
        )
    }

    // 17. Configure Supabase PostgreSQL Dialog
    if (showConfigureSupabaseDialog) {
        val currentUrl = viewModel.repository.supabaseConfig.getBaseUrl().let {
            if (it.contains("xyzcompany")) "" else it
        }
        val currentKey = viewModel.repository.supabaseConfig.getAnonKey()
        var inputUrl by remember { mutableStateOf(currentUrl) }
        var inputKey by remember { mutableStateOf(currentKey) }

        AlertDialog(
            onDismissRequest = { showConfigureSupabaseDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF3ECF8E))
                    Text("Supabase PostgreSQL Config", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Enter your project details from the Supabase Dashboard (Settings -> API):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        label = { Text("Supabase Project URL") },
                        placeholder = { Text("https://your-project.supabase.co") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        label = { Text("Supabase Anon / Public Key") },
                        placeholder = { Text("eyJhbGciOi...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F8E9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Tip: Run supabase_schema.sql in your Supabase SQL Editor to initialize all 16 tables, RLS policies, and seed data.",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF33691E)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSupabaseCredentials(inputUrl.trim(), inputKey.trim())
                        showConfigureSupabaseDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Save & Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigureSupabaseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ==========================================
// REUSABLE SETTINGS COMPONENTS
// ==========================================

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MedSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
            }

            content()
        }
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    testTag: String = "",
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MedTextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MedTextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MedTextSecondary
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MedTextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String = "",
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MedTextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MedTextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MedBluePrimary
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun DetailChip(
    label: String,
    value: String,
    isAlert: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isAlert) MedWarningLight else MedBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isAlert) MedWarning.copy(alpha = 0.4f) else MedBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MedTextSecondary,
                fontSize = 10.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = if (isAlert) MedWarning else MedTextPrimary
            )
        }
    }
}

@Composable
private fun AccessModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MedBluePrimary else MedBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) MedBluePrimary else MedBorder),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (selected) Color.White else MedTextPrimary,
            fontSize = 11.sp
        )
=======
            }
        }

        // Clinical Health Parameters
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Clinical Medical Emergency Info",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Blood Group", color = MedTextSecondary)
                        Text(currentUser?.bloodGroup ?: "O+", fontWeight = FontWeight.Bold, color = MedTextPrimary)
                    }
                    HorizontalDivider(color = MedBorder)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Known Allergies", color = MedTextSecondary)
                        Text(currentUser?.allergies ?: "None reported", fontWeight = FontWeight.Bold, color = MedError)
                    }
                    HorizontalDivider(color = MedBorder)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Emergency Contact", color = MedTextSecondary)
                        Text("${currentUser?.emergencyContactName} (${currentUser?.emergencyContactPhone})", fontWeight = FontWeight.Bold, color = MedTextPrimary)
                    }
                    HorizontalDivider(color = MedBorder)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Caretaker Sync Code", color = MedTextSecondary)
                        Text(currentUser?.caretakerLinkingCode ?: "MED-7842", fontWeight = FontWeight.Bold, color = MedBluePrimary)
                    }
                }
            }
        }

        // Medication Alerts & Local Notifications (WorkManager Engine)
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MedBluePrimary
                            )
                            Text(
                                text = "Local Medication Alerts",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (hasNotificationPermission) MedSuccess.copy(alpha = 0.12f) else MedWarning.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (hasNotificationPermission) "Active • WorkManager" else "Permission Required",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (hasNotificationPermission) MedSuccess else MedWarning
                            )
                        }
                    }

                    Text(
                        text = "MedTime schedules reliable local alarms using Android WorkManager. When medication times arrive, local alerts are triggered with sound and vibration even when the app is closed or offline.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary,
                        lineHeight = 18.sp
                    )

                    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Button(
                            onClick = {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grant Notification Permission")
                        }
                    }

                    // Notification Channel Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MedBlueLight,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Dose Alarms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedBluePrimary)
                                Text("High Priority", fontSize = 9.sp, color = MedTextSecondary)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MedWarningLight,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Refill Alerts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedWarning)
                                Text("Low Stock", fontSize = 9.sp, color = MedTextSecondary)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MedSuccessLight,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Doctor Visits", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedSuccess)
                                Text("Consultation", fontSize = 9.sp, color = MedTextSecondary)
                            }
                        }
                    }

                    Text(
                        text = "Trigger Test WorkManager Notifications:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedTextSecondary
                    )

                    // Test trigger grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.triggerMedicalReminderTest(MedicalReminderWorker.TYPE_MEDICATION_DOSE)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Outlined.Alarm, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dose", fontSize = 11.sp, color = MedBluePrimary)
                        }

                        OutlinedButton(
                            onClick = {
                                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.triggerMedicalReminderTest(MedicalReminderWorker.TYPE_REFILL_ALERT)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Outlined.Warning, contentDescription = null, tint = MedWarning, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Refill", fontSize = 11.sp, color = MedWarning)
                        }

                        OutlinedButton(
                            onClick = {
                                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.triggerMedicalReminderTest(MedicalReminderWorker.TYPE_APPOINTMENT)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Doctor", fontSize = 11.sp, color = MedSuccess)
                        }

                        OutlinedButton(
                            onClick = {
                                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.triggerMedicalReminderTest(MedicalReminderWorker.TYPE_VITALS_CHECK)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Outlined.Favorite, contentDescription = null, tint = MedError, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Vitals", fontSize = 11.sp, color = MedError)
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.rescheduleAllReminders()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Sync, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reschedule All Medical Reminders")
                    }
                }
            }
        }

        // Account Security & Session Controls (Logout & Password)
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Account Security & Preferences",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )

                    OutlinedButton(
                        onClick = { showChangePasswordDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = MedBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Password", color = MedBluePrimary)
                    }

                    OutlinedButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, tint = MedBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Clinical & Emergency Details", color = MedBluePrimary)
                    }

                    HorizontalDivider(color = MedBorder)

                    // Prominent Logout Button
                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("settings_logout_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MedError.copy(alpha = 0.12f),
                            contentColor = MedError
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MedError.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MedError)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign Out / Switch Account",
                            fontWeight = FontWeight.Bold,
                            color = MedError
                        )
                    }
                }
            }
        }
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
    }
}
