package com.example.ui.alarm

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.AlarmSettingsManager
import com.example.alarm.MedicineAlarmScheduler
import com.example.alarm.MedicineAlarmService
import com.example.data.db.AppDatabase
import com.example.data.model.MedicineReminderEntity
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicineAlarmActivity : ComponentActivity() {

    private var tickReceiver: BroadcastReceiver? = null
    private val secondsLeftState = mutableIntStateOf(60)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure activity displays over lock screen and turns the screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val reminderId = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_REMINDER_ID) ?: ""
        val medicineId = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_MEDICINE_ID) ?: ""
        val medicineName = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_MEDICINE_NAME) ?: "Medication"
        val dosage = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_DOSAGE) ?: "500 mg"
        val form = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_FORM) ?: "Tablet"
        val instructions = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_INSTRUCTIONS) ?: "Take with water"
        val scheduledTime = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_SCHEDULED_TIME) ?: "Now"
        val attemptCount = intent.getIntExtra(MedicineAlarmScheduler.EXTRA_ATTEMPT_COUNT, 1)
        val snoozeCount = intent.getIntExtra(MedicineAlarmScheduler.EXTRA_SNOOZE_COUNT, 0)
        val isTest = intent.getBooleanExtra(MedicineAlarmScheduler.EXTRA_IS_TEST, false)

        tickReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val sec = intent?.getIntExtra(MedicineAlarmService.EXTRA_SECONDS_LEFT, 60) ?: 60
                secondsLeftState.intValue = sec
            }
        }
        val filter = IntentFilter(MedicineAlarmService.ACTION_ALARM_COUNTDOWN_TICK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(tickReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(tickReceiver, filter)
        }

        setContent {
            MyApplicationTheme {
                val simultaneousMedsState = remember { mutableStateOf<List<MedicineReminderEntity>>(emptyList()) }

                LaunchedEffect(Unit) {
                    if (!isTest) {
                        withContext(Dispatchers.IO) {
                            try {
                                val db = AppDatabase.getInstance(applicationContext)
                                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                val pending = db.reminderDao().getPendingRemindersForDateDirect(today).filter {
                                    it.scheduledTime.equals(scheduledTime, ignoreCase = true) &&
                                            (it.status == "PENDING" || it.status == "SNOOZED")
                                }
                                simultaneousMedsState.value = pending
                            } catch (ignored: Exception) {}
                        }
                    }
                }

                val currentSelectedSound = remember { AlarmSettingsManager.getSelectedSound(applicationContext) }
                val isSpokenEnabled = remember { AlarmSettingsManager.isSpokenReminderEnabled(applicationContext) }

                MedicineAlarmScreen(
                    primaryMedicineName = medicineName,
                    primaryDosage = dosage,
                    primaryForm = form,
                    primaryInstructions = instructions,
                    scheduledTime = scheduledTime,
                    attemptCount = attemptCount,
                    snoozeCount = snoozeCount,
                    isTest = isTest,
                    selectedSound = currentSelectedSound,
                    spokenEnabled = isSpokenEnabled,
                    simultaneousMeds = simultaneousMedsState.value,
                    secondsRemaining = secondsLeftState.intValue,
                    onTakeAll = {
                        val serviceIntent = Intent(this, MedicineAlarmService::class.java).apply {
                            action = MedicineAlarmService.ACTION_TAKE_ALL_ALARM
                        }
                        startService(serviceIntent)
                        finish()
                    },
                    onSnooze = { minutes ->
                        val serviceIntent = Intent(this, MedicineAlarmService::class.java).apply {
                            action = MedicineAlarmService.ACTION_SNOOZE_ALARM
                            putExtra("snooze_minutes", minutes)
                        }
                        startService(serviceIntent)
                        finish()
                    },
                    onSkip = { reason ->
                        val serviceIntent = Intent(this, MedicineAlarmService::class.java).apply {
                            action = MedicineAlarmService.ACTION_SKIP_ALARM
                            putExtra("skip_reason", reason)
                        }
                        startService(serviceIntent)
                        finish()
                    },
                    onDismiss = {
                        val serviceIntent = Intent(this, MedicineAlarmService::class.java).apply {
                            action = MedicineAlarmService.ACTION_STOP_ALARM
                        }
                        startService(serviceIntent)
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        tickReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (ignored: Exception) {}
        }
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineAlarmScreen(
    primaryMedicineName: String,
    primaryDosage: String,
    primaryForm: String,
    primaryInstructions: String,
    scheduledTime: String,
    attemptCount: Int,
    snoozeCount: Int,
    isTest: Boolean,
    selectedSound: String,
    spokenEnabled: Boolean,
    simultaneousMeds: List<MedicineReminderEntity>,
    secondsRemaining: Int,
    onTakeAll: () -> Unit,
    onSnooze: (Int) -> Unit,
    onSkip: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showSkipDialog by remember { mutableStateOf(false) }
    var selectedSkipReason by remember { mutableStateOf("Feeling unwell") }
    var customSkipReason by remember { mutableStateOf("") }

    val skipReasons = listOf(
        "Feeling unwell",
        "Doctor advised to pause/skip",
        "Already taken earlier",
        "Experiencing side effects",
        "Other reason"
    )

    val isGrouped = simultaneousMeds.size > 1

    // Pulse Animation for alarm icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    if (showSkipDialog) {
        AlertDialog(
            onDismissRequest = { showSkipDialog = false },
            icon = {
                Icon(Icons.Default.Cancel, contentDescription = null, tint = MedError, modifier = Modifier.size(32.dp))
            },
            title = {
                Text(
                    text = if (isGrouped) "Skip Scheduled Doses?" else "Skip This Medicine?",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isGrouped) {
                            "Skipping will record all ${simultaneousMeds.size} scheduled doses as SKIPPED and update your adherence record."
                        } else {
                            "Skipping $primaryMedicineName ($primaryDosage) will record this dose as SKIPPED and update your adherence record."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MedTextSecondary
                    )

                    Text(
                        text = "Reason for skipping:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )

                    skipReasons.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedSkipReason = reason }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSkipReason == reason,
                                onClick = { selectedSkipReason = reason }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = reason, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (selectedSkipReason == "Other reason") {
                        OutlinedTextField(
                            value = customSkipReason,
                            onValueChange = { customSkipReason = it },
                            placeholder = { Text("Specify reason...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalReason = if (selectedSkipReason == "Other reason" && customSkipReason.isNotBlank()) {
                            customSkipReason
                        } else {
                            selectedSkipReason
                        }
                        showSkipDialog = false
                        onSkip(finalReason)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedError)
                ) {
                    Text("Confirm Skip")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSkipDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFF0D1B2A) // Rich deep midnight medical alarm theme
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Header & Badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "MEDTIME • REAL-TIME ALARM",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color(0xFF90CAF9)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Attempt / Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (snoozeCount > 0) Color(0xFFFFB74D).copy(alpha = 0.2f) else Color(0xFF64B5F6).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (snoozeCount > 0) Color(0xFFFFB74D) else Color(0xFF64B5F6)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (snoozeCount > 0) Icons.Default.Snooze else Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (snoozeCount > 0) Color(0xFFFFB74D) else Color(0xFF64B5F6),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isTest) {
                                "Test Alarm • Spoken Medicine Reminder Test"
                            } else if (isGrouped) {
                                "Grouped Dose: ${simultaneousMeds.size} Medicines Scheduled"
                            } else if (snoozeCount > 0) {
                                "Snoozed Dose • Snooze #$snoozeCount"
                            } else if (attemptCount > 1) {
                                "Reminder Attempt $attemptCount of 3 (Repeats in 5m if unanswered)"
                            } else {
                                "Scheduled Dose Alert (Attempt 1 of 3)"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sound & Spoken Badge row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(12.dp))
                            Text(selectedSound, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                    }

                    if (spokenEnabled) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E88E5).copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(12.dp))
                                Text("Voice Reminder Active", style = MaterialTheme.typography.labelSmall, color = Color(0xFF90CAF9), fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Pulsing Center Pill Graphic
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF2196F3).copy(alpha = 0.4f),
                                    Color(0xFF1565C0).copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E88E5), Color(0xFF0D47A1))
                                )
                            )
                            .border(2.dp, Color(0xFF90CAF9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = "Medicine Alarm",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // If multiple medicines at same time, display the list
                if (isGrouped) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A4A))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Medicines to Take Now ($scheduledTime)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )

                            simultaneousMeds.forEach { med ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.07f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Default.Medication, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(20.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(med.medicineName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                            Text("${med.dosage} • ${med.form} ${if (med.instructions.isNotBlank()) "• ${med.instructions}" else ""}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                        }
                                        Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Single Medicine Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A4A))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = primaryMedicineName,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "$primaryDosage • $primaryForm",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF64B5F6),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF90CAF9), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Scheduled Time", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    }
                                    Text(scheduledTime, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color(0xFF90CAF9), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Food Instruction", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    }
                                    Text(
                                        text = if (primaryInstructions.isNotBlank()) primaryInstructions else "With Water",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 60-second Sound Countdown Indicator
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Audible alarm active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Text(
                                text = "${secondsRemaining}s remaining",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (secondsRemaining <= 10) Color(0xFFFF7043) else Color(0xFF81C784)
                            )
                        }

                        LinearProgressIndicator(
                            progress = { secondsRemaining.toFloat() / 60f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (secondsRemaining <= 10) Color(0xFFFF7043) else Color(0xFF81C784),
                            trackColor = Color.White.copy(alpha = 0.15f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Action Section (Take / Snooze / Skip)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. TAKE MEDICINE (Primary Big Action)
                Button(
                    onClick = onTakeAll,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("alarm_take_medicine_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Take Medicine",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isGrouped) "TAKE ALL (${simultaneousMeds.size} MEDICINES)" else "TAKE MEDICINE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }

                // 2. SNOOZE (10 MIN)
                Button(
                    onClick = { onSnooze(10) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("alarm_snooze_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF6C00),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = "Snooze",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SNOOZE (10 MINUTES)",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                // 3. STOP / SKIP
                OutlinedButton(
                    onClick = { showSkipDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("alarm_skip_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFEF5350)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF5350)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Stop or Skip",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isGrouped) "SKIP ALL DOSES" else "STOP / SKIP DOSE",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
