package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.data.model.MedicineReminderEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object MedicationReminderScheduler {

    private const val TAG = "MedTimeScheduler"

    /**
     * Schedules a local notification alert using WorkManager for a specific scheduled medication reminder.
     */
    fun scheduleReminder(context: Context, reminder: MedicineReminderEntity) {
        try {
<<<<<<< HEAD
            // 1. Schedule Native Android Exact Alarm
            com.example.alarm.MedicineAlarmScheduler.scheduleExactAlarm(context, reminder)

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
            val delayMillis = calculateDelayMillis(reminder.scheduledDate, reminder.scheduledTime)

            val inputData = workDataOf(
                MedicationReminderWorker.KEY_REMINDER_ID to reminder.id,
                MedicationReminderWorker.KEY_MEDICINE_ID to reminder.medicineId,
                MedicationReminderWorker.KEY_MEDICINE_NAME to reminder.medicineName,
                MedicationReminderWorker.KEY_DOSAGE to reminder.dosage,
                MedicationReminderWorker.KEY_INSTRUCTIONS to reminder.instructions,
                MedicationReminderWorker.KEY_SCHEDULED_TIME to reminder.scheduledTime,
                MedicationReminderWorker.KEY_FORM to reminder.form
            )

            val workRequest = OneTimeWorkRequestBuilder<MedicationReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("reminder_${reminder.id}")
                .addTag("medicine_${reminder.medicineId}")
                .addTag("medication_reminders")
                .build()

            val uniqueWorkName = "reminder_${reminder.id}"
            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            Log.d(TAG, "Scheduled reminder for ${reminder.medicineName} at ${reminder.scheduledTime} in ${delayMillis / 1000}s (Work: $uniqueWorkName)")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to schedule reminder ${reminder.id}: ${e.message}", e)
        }
    }

    /**
     * Schedules a list of reminders (e.g. all pending reminders for today).
     */
    fun scheduleReminders(context: Context, reminders: List<MedicineReminderEntity>) {
        try {
            reminders.filter { it.status == "PENDING" || it.status == "SNOOZED" }.forEach { reminder ->
                scheduleReminder(context, reminder)
            }
            // Ensure background periodic sync is also active
            DailyMedicationSyncWorker.enqueuePeriodicSync(context)
<<<<<<< HEAD
            SupabaseSyncWorker.enqueuePeriodicSync(context)
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to schedule reminders list: ${e.message}", e)
        }
    }

    /**
     * Schedules all reminder timings for a specific medicine directly.
     */
    fun scheduleMedicineDoses(context: Context, medicineName: String, medicineId: String, dosage: String, form: String, instructions: String, timesString: String) {
        try {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val times = timesString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            for (time in times) {
                val sanitizedTime = time.replace(":", "_").replace(" ", "_")
                val reminder = MedicineReminderEntity(
                    id = "rem_${medicineId}_$sanitizedTime",
                    medicineId = medicineId,
                    patientId = "patient-1",
                    medicineName = medicineName,
                    dosage = dosage,
                    form = form,
                    instructions = instructions,
                    scheduledDate = todayStr,
                    scheduledTime = time,
                    status = "PENDING"
                )
                scheduleReminder(context, reminder)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to schedule medicine doses for $medicineName: ${e.message}", e)
        }
    }

    /**
     * Schedules a snoozed reminder for N minutes in the future.
     */
<<<<<<< HEAD
    fun scheduleSnooze(context: Context, reminder: MedicineReminderEntity, snoozeMinutes: Int = 10) {
        try {
            // Schedule real Android exact alarm for snooze
            com.example.alarm.MedicineAlarmScheduler.scheduleSnoozeAlarm(context, reminder, snoozeMinutes)

=======
    fun scheduleSnooze(context: Context, reminder: MedicineReminderEntity, snoozeMinutes: Int = 15) {
        try {
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
            val delayMillis = TimeUnit.MINUTES.toMillis(snoozeMinutes.toLong())

            val inputData = workDataOf(
                MedicationReminderWorker.KEY_REMINDER_ID to reminder.id,
                MedicationReminderWorker.KEY_MEDICINE_ID to reminder.medicineId,
                MedicationReminderWorker.KEY_MEDICINE_NAME to "${reminder.medicineName} (Snoozed)",
                MedicationReminderWorker.KEY_DOSAGE to reminder.dosage,
                MedicationReminderWorker.KEY_INSTRUCTIONS to "${reminder.instructions} • Snoozed dose",
                MedicationReminderWorker.KEY_SCHEDULED_TIME to reminder.scheduledTime,
                MedicationReminderWorker.KEY_FORM to reminder.form
            )

            val workRequest = OneTimeWorkRequestBuilder<MedicationReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("reminder_${reminder.id}")
                .addTag("medicine_${reminder.medicineId}")
                .addTag("medication_reminders")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "reminder_${reminder.id}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            Log.d(TAG, "Snoozed reminder for ${reminder.medicineName} for $snoozeMinutes minutes")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to snooze reminder: ${e.message}", e)
        }
    }

    /**
     * Cancels the scheduled alert for a single reminder.
     */
    fun cancelReminder(context: Context, reminderId: String) {
        try {
<<<<<<< HEAD
            com.example.alarm.MedicineAlarmScheduler.cancelAlarm(context, reminderId)
            WorkManager.getInstance(context).cancelUniqueWork("reminder_$reminderId")
            Log.d(TAG, "Cancelled WorkManager & Exact Alarm for reminder: reminder_$reminderId")
=======
            WorkManager.getInstance(context).cancelUniqueWork("reminder_$reminderId")
            Log.d(TAG, "Cancelled WorkManager task for reminder: reminder_$reminderId")
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to cancel reminder: ${e.message}", e)
        }
    }

    /**
     * Cancels all scheduled alerts for a specific medicine.
     */
    fun cancelMedicineReminders(context: Context, medicineId: String) {
        try {
            WorkManager.getInstance(context).cancelAllWorkByTag("medicine_$medicineId")
            Log.d(TAG, "Cancelled all WorkManager tasks for medicine: medicine_$medicineId")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to cancel medicine reminders: ${e.message}", e)
        }
    }

    /**
     * Triggers an immediate test notification using WorkManager to confirm that local alerts are functional.
     */
    fun triggerInstantTestAlert(
        context: Context,
        medicineName: String = "Amoxicillin",
        dosage: String = "500 mg",
        instructions: String = "After meals with water"
    ) {
        try {
            val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            val inputData = workDataOf(
                MedicationReminderWorker.KEY_REMINDER_ID to "test_alert_${System.currentTimeMillis()}",
                MedicationReminderWorker.KEY_MEDICINE_ID to "test_medicine",
                MedicationReminderWorker.KEY_MEDICINE_NAME to medicineName,
                MedicationReminderWorker.KEY_DOSAGE to dosage,
                MedicationReminderWorker.KEY_INSTRUCTIONS to instructions,
                MedicationReminderWorker.KEY_SCHEDULED_TIME to currentTime,
                MedicationReminderWorker.KEY_FORM to "Capsule"
            )

            val workRequest = OneTimeWorkRequestBuilder<MedicationReminderWorker>()
                .setInitialDelay(500, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("test_alert")
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
            Log.d(TAG, "Enqueued immediate test alert with WorkManager")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to trigger test alert: ${e.message}", e)
        }
    }

    /**
     * Computes the initial delay in milliseconds until the scheduled date and time.
     */
    private fun calculateDelayMillis(dateString: String, timeString: String): Long {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        // Try parsing full date + time (e.g. 2026-09-10 and 08:30 AM)
        val combinedFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
        val combined24Format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        val parsedTarget: Date? = try {
            combinedFormat.parse("$dateString $timeString")
        } catch (_: Exception) {
            try {
                combined24Format.parse("$dateString $timeString")
            } catch (_: Exception) {
                null
            }
        }

        if (parsedTarget != null) {
            val diff = parsedTarget.time - now
            if (diff > 0) {
                return diff
            }
        }

        // Fallback: parse time only and compare with today's calendar
        val normalizedTime = timeString.trim().uppercase(Locale.getDefault())
        val timeFormats = listOf("hh:mm a", "h:mm a", "hh:mma", "h:mma", "HH:mm", "H:mm")
        for (format in timeFormats) {
            try {
                val timeDate = SimpleDateFormat(format, Locale.getDefault()).parse(normalizedTime)
                if (timeDate != null) {
                    val timeCal = Calendar.getInstance().apply { time = timeDate }
                    val targetCal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    var diff = targetCal.timeInMillis - now
                    if (diff <= 0) {
                        // If already passed for today, schedule for tomorrow at the same time
                        targetCal.add(Calendar.DAY_OF_YEAR, 1)
                        diff = targetCal.timeInMillis - now
                    }
                    if (diff > 0) {
                        return diff
                    }
                }
            } catch (_: Exception) {
                // Try next format
            }
        }

        // Default to a 30-second reminder delay if unparseable
        return 30_000L
    }
}
