package com.example.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.db.AppDatabase
import com.example.data.model.MedicineHistoryEntity
import com.example.data.model.NotificationEntity
import com.example.ui.alarm.MedicineAlarmActivity
import com.example.worker.MedicationActionReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MedicineAlarmService : Service() {

    companion object {
        private const val TAG = "MedicineAlarmService"
        const val CHANNEL_ID = "medicine_alarm_channel_high_priority"
        const val CHANNEL_NAME = "Medicine Alarms & Spoken Reminders"
        const val NOTIFICATION_ID = 2001

        const val ACTION_STOP_ALARM = "com.example.action.STOP_ALARM"
        const val ACTION_TAKE_ALARM = "com.example.action.TAKE_ALARM"
        const val ACTION_TAKE_ALL_ALARM = "com.example.action.TAKE_ALL_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.example.action.SNOOZE_ALARM"
        const val ACTION_SKIP_ALARM = "com.example.action.SKIP_ALARM"

        // Live broadcast action for active alarm countdown updates
        const val ACTION_ALARM_COUNTDOWN_TICK = "com.example.action.ALARM_COUNTDOWN_TICK"
        const val EXTRA_SECONDS_LEFT = "extra_seconds_left"

        var isAlarmActive: Boolean = false
            private set
        var currentReminderId: String? = null
            private set
    }

    private var soundPlayer: AlarmSoundPlayer? = null
    private var ttsEngine: MedicineTtsEngine? = null
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    private val handler = Handler(Looper.getMainLooper())
    private var secondsRemaining = 60
    private var countdownRunnable: Runnable? = null
    private var autoTimeoutRunnable: Runnable? = null

    private var currentReminderIdVal: String = ""
    private var currentMedicineIdVal: String = ""
    private var currentMedicineNameVal: String = "Medication"
    private var currentDosageVal: String = ""
    private var currentFormVal: String = "Tablet"
    private var currentInstructionsVal: String = ""
    private var currentScheduledTimeVal: String = ""
    private var currentAttemptCount: Int = 1
    private var currentSnoozeCount: Int = 0
    private var currentIsTest: Boolean = false
    private var currentPatientName: String = "Vijay"
    private var groupedReminderIds: ArrayList<String> = arrayListOf()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        soundPlayer = AlarmSoundPlayer(applicationContext)
        ttsEngine = MedicineTtsEngine(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val action = intent.action

        if (action == ACTION_STOP_ALARM) {
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }

        if (action == ACTION_TAKE_ALARM || action == ACTION_TAKE_ALL_ALARM) {
            handleTakeDose()
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }

        if (action == ACTION_SNOOZE_ALARM) {
            val mins = intent.getIntExtra("snooze_minutes", 10)
            handleSnoozeDose(mins)
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }

        if (action == ACTION_SKIP_ALARM) {
            val reason = intent.getStringExtra("skip_reason") ?: "Patient skipped"
            handleSkipDose(reason)
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }

        // Initialize alarm parameters
        currentReminderIdVal = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_REMINDER_ID) ?: "rem_${System.currentTimeMillis()}"
        currentMedicineIdVal = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_MEDICINE_ID) ?: ""
        currentMedicineNameVal = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_MEDICINE_NAME) ?: "Medication"
        currentDosageVal = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_DOSAGE) ?: ""
        currentFormVal = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_FORM) ?: "Tablet"
        currentInstructionsVal = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_INSTRUCTIONS) ?: ""
        currentScheduledTimeVal = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_SCHEDULED_TIME) ?: SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        currentAttemptCount = intent.getIntExtra(MedicineAlarmScheduler.EXTRA_ATTEMPT_COUNT, 1)
        currentSnoozeCount = intent.getIntExtra(MedicineAlarmScheduler.EXTRA_SNOOZE_COUNT, 0)
        currentIsTest = intent.getBooleanExtra(MedicineAlarmScheduler.EXTRA_IS_TEST, false)
        groupedReminderIds = intent.getStringArrayListExtra("extra_grouped_reminder_ids") ?: arrayListOf()

        isAlarmActive = true
        currentReminderId = currentReminderIdVal

        // Build and display foreground notification with full-screen intent
        val notification = buildAlarmNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Fetch patient profile name and start audio/voice/vibration engine
        loadPatientNameAndTriggerAlert()

        // Start 60-second countdown
        start60SecondTimer()

        return START_NOT_STICKY
    }

    private fun loadPatientNameAndTriggerAlert() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val users = db.userDao().getAllUsersList()
                val patientUser = users.firstOrNull { it.role == "PATIENT" } ?: users.firstOrNull()
                val name = patientUser?.name?.trim() ?: "Vijay"
                currentPatientName = name

                // Check if there are other simultaneous pending reminders at this exact time
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val simultaneousReminders = if (!currentIsTest) {
                    db.reminderDao().getPendingRemindersForDateDirect(today).filter {
                        it.scheduledTime.equals(currentScheduledTimeVal, ignoreCase = true) &&
                                (it.status == "PENDING" || it.status == "SNOOZED")
                    }
                } else {
                    emptyList()
                }

                withContext(Dispatchers.Main) {
                    startAlarmSoundAndVibration(simultaneousReminders)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching patient profile: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    startAlarmSoundAndVibration(emptyList())
                }
            }
        }
    }

    private fun startAlarmSoundAndVibration(simultaneousReminders: List<com.example.data.model.MedicineReminderEntity>) {
        requestAudioFocus()

        // 1. Start Vibration
        if (AlarmSettingsManager.isVibrationEnabled(this)) {
            try {
                vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }

                val pattern = longArrayOf(0, 800, 400, 800, 400, 800, 1000)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(pattern, 0)
                    vibrator?.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, 0)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start vibration: ${e.message}")
            }
        }

        // 2. Start Selected Sound Tone
        val selectedSound = AlarmSettingsManager.getSelectedSound(this)
        soundPlayer?.playSound(
            soundName = selectedSound,
            looping = true,
            onFallback = { msg ->
                Log.w(TAG, "Sound fallback: $msg")
            }
        )

        // 3. Play Spoken Medicine Reminder once
        if (AlarmSettingsManager.isSpokenReminderEnabled(this)) {
            if (currentIsTest) {
                ttsEngine?.speakTestReminder(currentPatientName)
            } else if (simultaneousReminders.size > 1) {
                val speechItems = simultaneousReminders.map {
                    GroupedMedicineSpeechItem(
                        name = it.medicineName,
                        dosage = it.dosage,
                        form = it.form,
                        instructions = it.instructions
                    )
                }
                ttsEngine?.speakGroupedMedicineReminder(currentPatientName, speechItems)
            } else {
                ttsEngine?.speakMedicineReminder(
                    patientName = currentPatientName,
                    medicineName = currentMedicineNameVal,
                    dosage = currentDosageVal,
                    form = currentFormVal,
                    instructions = currentInstructionsVal
                )
            }
        }
    }

    private fun requestAudioFocus() {
        try {
            audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .build()

                audioFocusRequest?.let { audioManager?.requestAudioFocus(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting audio focus: ${e.message}")
        }
    }

    private fun releaseAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error abandoning audio focus: ${e.message}")
        }
    }

    private fun start60SecondTimer() {
        secondsRemaining = 60
        countdownRunnable = object : Runnable {
            override fun run() {
                if (secondsRemaining > 0) {
                    secondsRemaining--
                    val tickIntent = Intent(ACTION_ALARM_COUNTDOWN_TICK).apply {
                        putExtra(EXTRA_SECONDS_LEFT, secondsRemaining)
                        putExtra(MedicineAlarmScheduler.EXTRA_REMINDER_ID, currentReminderIdVal)
                    }
                    sendBroadcast(tickIntent)
                    handler.postDelayed(this, 1000L)
                }
            }
        }
        handler.post(countdownRunnable!!)

        // Auto-timeout after 60 seconds (1 minute max sound duration)
        autoTimeoutRunnable = Runnable {
            Log.d(TAG, "60-second sound limit reached with no user action. Stopping sound and handling unresolved dose.")
            handleUnansweredAutoTimeout()
        }
        autoTimeoutRunnable?.let { handler.postDelayed(it, 60_000L) }
    }

    private fun handleUnansweredAutoTimeout() {
        stopAlarmSoundAndVibration()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)

                if (currentAttemptCount < 3 && !currentIsTest) {
                    db.reminderDao().updateAttemptCount(currentReminderIdVal, currentAttemptCount)

                    MedicineAlarmScheduler.scheduleRetryAlarm(
                        context = applicationContext,
                        reminderId = currentReminderIdVal,
                        medicineId = currentMedicineIdVal,
                        medicineName = currentMedicineNameVal,
                        dosage = currentDosageVal,
                        form = currentFormVal,
                        instructions = currentInstructionsVal,
                        scheduledTime = currentScheduledTimeVal,
                        nextAttemptCount = currentAttemptCount + 1,
                        snoozeCount = currentSnoozeCount,
                        delayMinutes = 5
                    )
                    Log.d(TAG, "Scheduled repeat reminder attempt ${currentAttemptCount + 1} of 3 in 5 minutes.")
                } else if (!currentIsTest) {
                    val now = System.currentTimeMillis()
                    db.reminderDao().updateReminderStatus(currentReminderIdVal, "MISSED", now)

                    db.historyDao().insertHistory(
                        MedicineHistoryEntity(
                            id = UUID.randomUUID().toString(),
                            medicineId = currentMedicineIdVal,
                            patientId = "patient-1",
                            medicineName = currentMedicineNameVal,
                            dosage = currentDosageVal,
                            scheduledTime = currentScheduledTimeVal,
                            action = "MISSED",
                            actionTimestamp = now,
                            notes = "Unanswered after 3 scheduled reminder attempts (1m sound each, 5m intervals)."
                        )
                    )

                    db.notificationDao().insertNotification(
                        NotificationEntity(
                            id = UUID.randomUUID().toString(),
                            userId = "patient-1",
                            title = "Missed Dose: $currentMedicineNameVal",
                            message = "The scheduled dose for $currentMedicineNameVal ($currentDosageVal) at $currentScheduledTimeVal was marked as MISSED after 3 unanswered attempts.",
                            type = "MISSED_DOSE",
                            severity = "WARNING"
                        )
                    )

                    try {
                        val links = db.caretakerDao().getAllLinksList().filter { it.status == "ACCEPTED" && it.canReceiveAlerts }
                        if (links.isNotEmpty()) {
                            val caretaker = links.first()
                            val reminderEntity = db.reminderDao().getReminderById(currentReminderIdVal)
                            if (reminderEntity != null) {
                                db.notificationDao().insertNotification(
                                    NotificationEntity(
                                        id = UUID.randomUUID().toString(),
                                        userId = "patient-1",
                                        title = "🚨 Caretaker Missed Dose Alert",
                                        message = "Patient missed scheduled dose for ${reminderEntity.medicineName} (${reminderEntity.dosage}). Sent alert to caregiver ${caretaker.caretakerName}.",
                                        type = "MISSED_DOSE",
                                        recipientRole = "CARETAKER",
                                        recipientName = caretaker.caretakerName,
                                        severity = "CRITICAL"
                                    )
                                )
                            }
                        }
                    } catch (ignored: Exception) {}

                    Log.d(TAG, "Dose marked as MISSED after 3 unanswered attempts. Cancelled further alarms.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling auto-timeout: ${e.message}", e)
            } finally {
                stopSelf()
            }
        }
    }

    private fun handleTakeDose() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val now = System.currentTimeMillis()

                // Mark current reminder taken
                db.reminderDao().updateReminderStatus(currentReminderIdVal, "TAKEN", now)

                db.historyDao().insertHistory(
                    MedicineHistoryEntity(
                        id = UUID.randomUUID().toString(),
                        medicineId = currentMedicineIdVal,
                        patientId = "patient-1",
                        medicineName = currentMedicineNameVal,
                        dosage = currentDosageVal,
                        scheduledTime = currentScheduledTimeVal,
                        action = "TAKEN",
                        actionTimestamp = now,
                        notes = "Taken via MedTime Real-Time Alarm"
                    )
                )

                // Decrement stock
                val med = db.medicineDao().getMedicineById(currentMedicineIdVal)
                if (med != null && med.stockQuantity > 0) {
                    val updated = med.copy(stockQuantity = med.stockQuantity - 1)
                    db.medicineDao().updateMedicine(updated)
                }

                // If there are simultaneous grouped medicines, also mark them taken
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val otherPending = db.reminderDao().getPendingRemindersForDateDirect(today).filter {
                    it.scheduledTime.equals(currentScheduledTimeVal, ignoreCase = true) &&
                            it.id != currentReminderIdVal &&
                            (it.status == "PENDING" || it.status == "SNOOZED")
                }
                for (other in otherPending) {
                    db.reminderDao().updateReminderStatus(other.id, "TAKEN", now)
                    db.historyDao().insertHistory(
                        MedicineHistoryEntity(
                            id = UUID.randomUUID().toString(),
                            medicineId = other.medicineId,
                            patientId = "patient-1",
                            medicineName = other.medicineName,
                            dosage = other.dosage,
                            scheduledTime = other.scheduledTime,
                            action = "TAKEN",
                            actionTimestamp = now,
                            notes = "Taken via Grouped Medicine Alarm"
                        )
                    )
                    val otherMed = db.medicineDao().getMedicineById(other.medicineId)
                    if (otherMed != null && otherMed.stockQuantity > 0) {
                        db.medicineDao().updateMedicine(otherMed.copy(stockQuantity = otherMed.stockQuantity - 1))
                    }
                    MedicineAlarmScheduler.cancelAlarm(applicationContext, other.id)
                }

                db.notificationDao().insertNotification(
                    NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        userId = "patient-1",
                        title = "Dose Taken: $currentMedicineNameVal",
                        message = "Logged $currentMedicineNameVal ($currentDosageVal) as TAKEN at ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(now))}.",
                        type = "MEDICINE"
                    )
                )

                MedicineAlarmScheduler.cancelAlarm(applicationContext, currentReminderIdVal)
                Log.d(TAG, "Dose $currentMedicineNameVal logged as TAKEN. Alarm cancelled.")
            } catch (e: Exception) {
                Log.e(TAG, "Error recording take dose: ${e.message}", e)
            }
        }
    }

    private fun handleSnoozeDose(minutes: Int = 10) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val now = System.currentTimeMillis()
                val snoozeTimeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(now + minutes * 60 * 1000L))

                db.reminderDao().snoozeReminderWithCount(currentReminderIdVal, snoozeTimeStr, now)

                db.historyDao().insertHistory(
                    MedicineHistoryEntity(
                        id = UUID.randomUUID().toString(),
                        medicineId = currentMedicineIdVal,
                        patientId = "patient-1",
                        medicineName = currentMedicineNameVal,
                        dosage = currentDosageVal,
                        scheduledTime = currentScheduledTimeVal,
                        action = "SNOOZED",
                        actionTimestamp = now,
                        notes = "Snoozed for $minutes minutes until $snoozeTimeStr (Snooze #${currentSnoozeCount + 1})"
                    )
                )

                val reminder = db.reminderDao().getReminderById(currentReminderIdVal)
                if (reminder != null) {
                    MedicineAlarmScheduler.scheduleSnoozeAlarm(applicationContext, reminder, minutes)
                }
                Log.d(TAG, "Dose $currentMedicineNameVal snoozed for $minutes min until $snoozeTimeStr.")
            } catch (e: Exception) {
                Log.e(TAG, "Error snoozing dose: ${e.message}", e)
            }
        }
    }

    private fun handleSkipDose(reason: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val now = System.currentTimeMillis()

                db.reminderDao().skipReminderWithReason(currentReminderIdVal, reason, now)

                db.historyDao().insertHistory(
                    MedicineHistoryEntity(
                        id = UUID.randomUUID().toString(),
                        medicineId = currentMedicineIdVal,
                        patientId = "patient-1",
                        medicineName = currentMedicineNameVal,
                        dosage = currentDosageVal,
                        scheduledTime = currentScheduledTimeVal,
                        action = "SKIPPED",
                        actionTimestamp = now,
                        notes = "Reason: $reason"
                    )
                )

                db.notificationDao().insertNotification(
                    NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        userId = "patient-1",
                        title = "Dose Skipped: $currentMedicineNameVal",
                        message = "Marked $currentMedicineNameVal as SKIPPED. Reason: $reason.",
                        type = "MEDICINE",
                        severity = "WARNING"
                    )
                )

                MedicineAlarmScheduler.cancelAlarm(applicationContext, currentReminderIdVal)
                Log.d(TAG, "Dose $currentMedicineNameVal skipped ($reason). Alarm cancelled.")
            } catch (e: Exception) {
                Log.e(TAG, "Error skipping dose: ${e.message}", e)
            }
        }
    }

    private fun stopAlarmSoundAndVibration() {
        soundPlayer?.stop()
        ttsEngine?.stop()

        try {
            vibrator?.cancel()
            vibrator = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping vibrator: ${e.message}")
        }

        releaseAudioFocus()
    }

    private fun stopAlarm() {
        countdownRunnable?.let { handler.removeCallbacks(it) }
        autoTimeoutRunnable?.let { handler.removeCallbacks(it) }
        stopAlarmSoundAndVibration()
        isAlarmActive = false
        currentReminderId = null
    }

    private fun buildAlarmNotification(): Notification {
        val fullScreenIntent = Intent(this, MedicineAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MedicineAlarmScheduler.EXTRA_REMINDER_ID, currentReminderIdVal)
            putExtra(MedicineAlarmScheduler.EXTRA_MEDICINE_ID, currentMedicineIdVal)
            putExtra(MedicineAlarmScheduler.EXTRA_MEDICINE_NAME, currentMedicineNameVal)
            putExtra(MedicineAlarmScheduler.EXTRA_DOSAGE, currentDosageVal)
            putExtra(MedicineAlarmScheduler.EXTRA_FORM, currentFormVal)
            putExtra(MedicineAlarmScheduler.EXTRA_INSTRUCTIONS, currentInstructionsVal)
            putExtra(MedicineAlarmScheduler.EXTRA_SCHEDULED_TIME, currentScheduledTimeVal)
            putExtra(MedicineAlarmScheduler.EXTRA_ATTEMPT_COUNT, currentAttemptCount)
            putExtra(MedicineAlarmScheduler.EXTRA_SNOOZE_COUNT, currentSnoozeCount)
            putExtra(MedicineAlarmScheduler.EXTRA_IS_TEST, currentIsTest)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_ID + 100,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takeIntent = Intent(this, MedicationActionReceiver::class.java).apply {
            action = MedicationActionReceiver.ACTION_TAKE_DOSE
            putExtra(MedicationActionReceiver.KEY_REMINDER_ID, currentReminderIdVal)
            putExtra(MedicationActionReceiver.KEY_MEDICINE_ID, currentMedicineIdVal)
            putExtra(MedicationActionReceiver.KEY_MEDICINE_NAME, currentMedicineNameVal)
            putExtra(MedicationActionReceiver.KEY_DOSAGE, currentDosageVal)
            putExtra(MedicationActionReceiver.KEY_SCHEDULED_TIME, currentScheduledTimeVal)
            putExtra("notification_id", NOTIFICATION_ID)
        }
        val takePendingIntent = PendingIntent.getBroadcast(
            this,
            NOTIFICATION_ID + 101,
            takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, MedicationActionReceiver::class.java).apply {
            action = MedicationActionReceiver.ACTION_SNOOZE_DOSE
            putExtra(MedicationActionReceiver.KEY_REMINDER_ID, currentReminderIdVal)
            putExtra(MedicationActionReceiver.KEY_MEDICINE_ID, currentMedicineIdVal)
            putExtra(MedicationActionReceiver.KEY_MEDICINE_NAME, currentMedicineNameVal)
            putExtra(MedicationActionReceiver.KEY_DOSAGE, currentDosageVal)
            putExtra(MedicationActionReceiver.KEY_SCHEDULED_TIME, currentScheduledTimeVal)
            putExtra("snooze_minutes", 10)
            putExtra("notification_id", NOTIFICATION_ID)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            NOTIFICATION_ID + 102,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val skipIntent = Intent(this, MedicationActionReceiver::class.java).apply {
            action = MedicationActionReceiver.ACTION_SKIP_DOSE
            putExtra(MedicationActionReceiver.KEY_REMINDER_ID, currentReminderIdVal)
            putExtra(MedicationActionReceiver.KEY_MEDICINE_ID, currentMedicineIdVal)
            putExtra(MedicationActionReceiver.KEY_MEDICINE_NAME, currentMedicineNameVal)
            putExtra(MedicationActionReceiver.KEY_DOSAGE, currentDosageVal)
            putExtra(MedicationActionReceiver.KEY_SCHEDULED_TIME, currentScheduledTimeVal)
            putExtra("skip_reason", "Skipped via notification quick action")
            putExtra("notification_id", NOTIFICATION_ID)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            this,
            NOTIFICATION_ID + 103,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "💊 Medicine Alarm: $currentMedicineNameVal"
        val subtitle = "$currentDosageVal • $currentFormVal"
        val content = if (currentInstructionsVal.isNotBlank()) "$currentInstructionsVal • Scheduled for $currentScheduledTimeVal" else "Scheduled for $currentScheduledTimeVal"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText("$subtitle — $content")
            .setSubText(if (currentAttemptCount > 1) "Attempt $currentAttemptCount of 3" else "Scheduled Dose")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, "TAKE MEDICINE", takePendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, "SNOOZE (10m)", snoozePendingIntent)
            .addAction(android.R.drawable.ic_delete, "STOP / SKIP", skipPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alarm notifications for scheduled medicine doses with custom sound, speech, and full screen alert"
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopAlarm()
        ttsEngine?.shutdown()
        ttsEngine = null
        soundPlayer = null
        super.onDestroy()
    }
}
