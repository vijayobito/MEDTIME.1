package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.model.MedicineReminderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object MedicineAlarmScheduler {

    private const val TAG = "MedAlarmScheduler"

    const val ACTION_ALARM_TRIGGER = "com.example.action.MEDICINE_ALARM_TRIGGER"
    const val ACTION_ALARM_RETRY = "com.example.action.MEDICINE_ALARM_RETRY"
    const val ACTION_ALARM_SNOOZE = "com.example.action.MEDICINE_ALARM_SNOOZE_TRIGGER"
    const val ACTION_ALARM_TEST = "com.example.action.MEDICINE_ALARM_TEST"

    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_MEDICINE_ID = "extra_medicine_id"
    const val EXTRA_MEDICINE_NAME = "extra_medicine_name"
    const val EXTRA_DOSAGE = "extra_dosage"
    const val EXTRA_FORM = "extra_form"
    const val EXTRA_INSTRUCTIONS = "extra_instructions"
    const val EXTRA_SCHEDULED_TIME = "extra_scheduled_time"
    const val EXTRA_ATTEMPT_COUNT = "extra_attempt_count"
    const val EXTRA_SNOOZE_COUNT = "extra_snooze_count"
    const val EXTRA_IS_TEST = "extra_is_test"

    /**
     * Checks if exact alarms are permitted on Android 12+ (API 31+).
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: true
        } else {
            true
        }
    }

    /**
     * Schedules a real Android exact alarm for a medication dose reminder.
     */
    fun scheduleExactAlarm(
        context: Context,
        reminder: MedicineReminderEntity,
        attemptCount: Int = 1,
        isSnooze: Boolean = false
    ) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val triggerTime = calculateTriggerTimeMillis(reminder.scheduledDate, reminder.scheduledTime, isSnooze)

            val now = System.currentTimeMillis()
            if (triggerTime < now - 60_000L) {
                // Time has already passed for today
                Log.d(TAG, "Skipping past reminder ${reminder.medicineName} at ${reminder.scheduledTime}")
                return
            }

            val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
                action = if (isSnooze) ACTION_ALARM_SNOOZE else ACTION_ALARM_TRIGGER
                putExtra(EXTRA_REMINDER_ID, reminder.id)
                putExtra(EXTRA_MEDICINE_ID, reminder.medicineId)
                putExtra(EXTRA_MEDICINE_NAME, reminder.medicineName)
                putExtra(EXTRA_DOSAGE, reminder.dosage)
                putExtra(EXTRA_FORM, reminder.form)
                putExtra(EXTRA_INSTRUCTIONS, reminder.instructions)
                putExtra(EXTRA_SCHEDULED_TIME, reminder.scheduledTime)
                putExtra(EXTRA_ATTEMPT_COUNT, attemptCount)
                putExtra(EXTRA_SNOOZE_COUNT, reminder.snoozeCount)
                putExtra(EXTRA_IS_TEST, false)
            }

            val requestCode = generateRequestCode(reminder.id, attemptCount, isSnooze)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

            // Setup showIntent for AlarmClockInfo
            val showIntent = Intent(context, com.example.ui.alarm.MedicineAlarmActivity::class.java).apply {
                putExtra(EXTRA_REMINDER_ID, reminder.id)
                putExtra(EXTRA_MEDICINE_ID, reminder.medicineId)
                putExtra(EXTRA_MEDICINE_NAME, reminder.medicineName)
                putExtra(EXTRA_DOSAGE, reminder.dosage)
                putExtra(EXTRA_FORM, reminder.form)
                putExtra(EXTRA_INSTRUCTIONS, reminder.instructions)
                putExtra(EXTRA_SCHEDULED_TIME, reminder.scheduledTime)
                putExtra(EXTRA_ATTEMPT_COUNT, attemptCount)
                putExtra(EXTRA_SNOOZE_COUNT, reminder.snoozeCount)
            }
            val showPendingIntent = PendingIntent.getActivity(context, requestCode + 10000, showIntent, flags)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }

            val scheduledDateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(triggerTime))
            Log.d(TAG, "Exact Alarm set for ${reminder.medicineName} (${reminder.dosage}) at $scheduledDateStr (Attempt: $attemptCount, Snooze: $isSnooze, ReqCode: $requestCode)")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to schedule exact alarm for ${reminder.medicineName}: ${e.message}", e)
        }
    }

    /**
     * Schedules a 10-minute snooze alarm.
     */
    fun scheduleSnoozeAlarm(
        context: Context,
        reminder: MedicineReminderEntity,
        snoozeMinutes: Int = 10
    ) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
            val updatedSnoozeCount = reminder.snoozeCount + 1

            val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
                action = ACTION_ALARM_SNOOZE
                putExtra(EXTRA_REMINDER_ID, reminder.id)
                putExtra(EXTRA_MEDICINE_ID, reminder.medicineId)
                putExtra(EXTRA_MEDICINE_NAME, reminder.medicineName)
                putExtra(EXTRA_DOSAGE, reminder.dosage)
                putExtra(EXTRA_FORM, reminder.form)
                putExtra(EXTRA_INSTRUCTIONS, reminder.instructions)
                putExtra(EXTRA_SCHEDULED_TIME, reminder.scheduledTime)
                putExtra(EXTRA_ATTEMPT_COUNT, 1) // Reset attempts for new snooze cycle
                putExtra(EXTRA_SNOOZE_COUNT, updatedSnoozeCount)
                putExtra(EXTRA_IS_TEST, false)
            }

            val requestCode = generateRequestCode(reminder.id, 1, isSnooze = true)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

            val showIntent = Intent(context, com.example.ui.alarm.MedicineAlarmActivity::class.java).apply {
                putExtra(EXTRA_REMINDER_ID, reminder.id)
                putExtra(EXTRA_MEDICINE_ID, reminder.medicineId)
                putExtra(EXTRA_MEDICINE_NAME, reminder.medicineName)
                putExtra(EXTRA_DOSAGE, reminder.dosage)
                putExtra(EXTRA_FORM, reminder.form)
                putExtra(EXTRA_INSTRUCTIONS, reminder.instructions)
                putExtra(EXTRA_SCHEDULED_TIME, reminder.scheduledTime)
                putExtra(EXTRA_ATTEMPT_COUNT, 1)
                putExtra(EXTRA_SNOOZE_COUNT, updatedSnoozeCount)
            }
            val showPendingIntent = PendingIntent.getActivity(context, requestCode + 10000, showIntent, flags)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }

            Log.d(TAG, "Snooze alarm scheduled for ${reminder.medicineName} in $snoozeMinutes minutes (Snooze #$updatedSnoozeCount)")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to schedule snooze alarm: ${e.message}", e)
        }
    }

    /**
     * Schedules a 5-minute retry alarm for an unanswered dose (up to max 3 attempts).
     */
    fun scheduleRetryAlarm(
        context: Context,
        reminderId: String,
        medicineId: String,
        medicineName: String,
        dosage: String,
        form: String,
        instructions: String,
        scheduledTime: String,
        nextAttemptCount: Int,
        snoozeCount: Int,
        delayMinutes: Int = 5
    ) {
        if (nextAttemptCount > 3) {
            Log.d(TAG, "Max attempts reached ($nextAttemptCount > 3) for reminder $reminderId. No more alarms scheduled.")
            return
        }

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val triggerTime = System.currentTimeMillis() + (delayMinutes * 60 * 1000L)

            val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
                action = ACTION_ALARM_RETRY
                putExtra(EXTRA_REMINDER_ID, reminderId)
                putExtra(EXTRA_MEDICINE_ID, medicineId)
                putExtra(EXTRA_MEDICINE_NAME, medicineName)
                putExtra(EXTRA_DOSAGE, dosage)
                putExtra(EXTRA_FORM, form)
                putExtra(EXTRA_INSTRUCTIONS, instructions)
                putExtra(EXTRA_SCHEDULED_TIME, scheduledTime)
                putExtra(EXTRA_ATTEMPT_COUNT, nextAttemptCount)
                putExtra(EXTRA_SNOOZE_COUNT, snoozeCount)
                putExtra(EXTRA_IS_TEST, false)
            }

            val requestCode = generateRequestCode(reminderId, nextAttemptCount, isSnooze = false)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

            val showIntent = Intent(context, com.example.ui.alarm.MedicineAlarmActivity::class.java).apply {
                putExtra(EXTRA_REMINDER_ID, reminderId)
                putExtra(EXTRA_MEDICINE_ID, medicineId)
                putExtra(EXTRA_MEDICINE_NAME, medicineName)
                putExtra(EXTRA_DOSAGE, dosage)
                putExtra(EXTRA_FORM, form)
                putExtra(EXTRA_INSTRUCTIONS, instructions)
                putExtra(EXTRA_SCHEDULED_TIME, scheduledTime)
                putExtra(EXTRA_ATTEMPT_COUNT, nextAttemptCount)
                putExtra(EXTRA_SNOOZE_COUNT, snoozeCount)
            }
            val showPendingIntent = PendingIntent.getActivity(context, requestCode + 10000, showIntent, flags)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }

            Log.d(TAG, "Retry Alarm (Attempt $nextAttemptCount of 3) set for $medicineName in $delayMinutes minutes")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to schedule retry alarm: ${e.message}", e)
        }
    }

    /**
     * Schedules a test alarm to fire in [delaySeconds] seconds (Developer / Testing mode).
     */
    fun triggerTestAlarm(context: Context, delaySeconds: Int = 5) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val triggerTime = System.currentTimeMillis() + (delaySeconds * 1000L)

            val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
                action = ACTION_ALARM_TEST
                putExtra(EXTRA_REMINDER_ID, "test_reminder_" + System.currentTimeMillis())
                putExtra(EXTRA_MEDICINE_ID, "test_med_1")
                putExtra(EXTRA_MEDICINE_NAME, "Metformin HCl")
                putExtra(EXTRA_DOSAGE, "500 mg • 1 Tablet")
                putExtra(EXTRA_FORM, "Tablet")
                putExtra(EXTRA_INSTRUCTIONS, "After Food • Take with water")
                putExtra(EXTRA_SCHEDULED_TIME, SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()))
                putExtra(EXTRA_ATTEMPT_COUNT, 1)
                putExtra(EXTRA_SNOOZE_COUNT, 0)
                putExtra(EXTRA_IS_TEST, true)
            }

            val requestCode = 99999
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

            val showIntent = Intent(context, com.example.ui.alarm.MedicineAlarmActivity::class.java).apply {
                putExtra(EXTRA_REMINDER_ID, "test_reminder")
                putExtra(EXTRA_MEDICINE_NAME, "Metformin HCl")
                putExtra(EXTRA_DOSAGE, "500 mg • 1 Tablet")
                putExtra(EXTRA_FORM, "Tablet")
                putExtra(EXTRA_INSTRUCTIONS, "After Food • Take with water")
                putExtra(EXTRA_SCHEDULED_TIME, SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()))
                putExtra(EXTRA_ATTEMPT_COUNT, 1)
                putExtra(EXTRA_IS_TEST, true)
            }
            val showPendingIntent = PendingIntent.getActivity(context, 99998, showIntent, flags)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }

            Log.d(TAG, "Test alarm scheduled to fire in $delaySeconds seconds")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to schedule test alarm: ${e.message}", e)
        }
    }

    /**
     * Cancels any pending exact alarms for a given reminder ID across all attempts/snoozes.
     */
    fun cancelAlarm(context: Context, reminderId: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE

            // Cancel attempts 1..3 and snooze variants
            for (attempt in 1..3) {
                for (isSnooze in listOf(false, true)) {
                    val reqCode = generateRequestCode(reminderId, attempt, isSnooze)
                    val intent = Intent(context, MedicineAlarmReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(context, reqCode, intent, flags)
                    if (pendingIntent != null) {
                        alarmManager.cancel(pendingIntent)
                        pendingIntent.cancel()
                    }
                }
            }
            Log.d(TAG, "Cancelled exact alarms for reminder $reminderId")
        } catch (e: Throwable) {
            Log.e(TAG, "Error cancelling alarm $reminderId: ${e.message}")
        }
    }

    /**
     * Reschedules all active pending reminders in the database.
     */
    fun rescheduleAllAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val pending = db.reminderDao().getPendingRemindersForDateDirect(today)
                Log.d(TAG, "Rescheduling ${pending.size} pending medicine doses with Exact AlarmManager.")
                for (reminder in pending) {
                    if (reminder.status == "PENDING") {
                        scheduleExactAlarm(context, reminder, attemptCount = maxOf(1, reminder.attemptCount))
                    } else if (reminder.status == "SNOOZED") {
                        scheduleExactAlarm(context, reminder, attemptCount = 1, isSnooze = true)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reschedule all alarms: ${e.message}", e)
            }
        }
    }

    /**
     * Parses the date and time strings and returns the epoch milliseconds for the alarm trigger.
     */
    private fun calculateTriggerTimeMillis(scheduledDate: String, scheduledTime: String, isSnooze: Boolean): Long {
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd H:mm", Locale.getDefault())
        )

        val combined = "$scheduledDate $scheduledTime"
        for (format in formats) {
            try {
                val date = format.parse(combined)
                if (date != null) {
                    val cal = Calendar.getInstance().apply { time = date }
                    return cal.timeInMillis
                }
            } catch (ignored: Exception) {}
        }

        // Fallback: parse time only against today
        val timeFormats = listOf(
            SimpleDateFormat("hh:mm a", Locale.getDefault()),
            SimpleDateFormat("h:mm a", Locale.getDefault()),
            SimpleDateFormat("HH:mm", Locale.getDefault()),
            SimpleDateFormat("H:mm", Locale.getDefault())
        )

        for (tf in timeFormats) {
            try {
                val parsed = tf.parse(scheduledTime)
                if (parsed != null) {
                    val timeCal = Calendar.getInstance().apply { time = parsed }
                    val targetCal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    return targetCal.timeInMillis
                }
            } catch (ignored: Exception) {}
        }

        // Default: 5 minutes from now if unparseable
        return System.currentTimeMillis() + 5 * 60 * 1000L
    }

    private fun generateRequestCode(reminderId: String, attemptCount: Int, isSnooze: Boolean): Int {
        val base = reminderId.hashCode() and 0x7FFFFFFF
        val attemptOffset = attemptCount * 10
        val snoozeOffset = if (isSnooze) 5 else 0
        return (base % 1000000) + attemptOffset + snoozeOffset
    }
}
