package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.data.model.AppointmentEntity
import com.example.data.model.MedicineEntity
import com.example.data.model.MedicineReminderEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Central WorkManager scheduler for managing all medical reminder background tasks:
 * - Medication intake alarms
 * - Snooze timers
 * - Low-stock refill notifications
 * - Doctor appointment reminders
 * - Daily vitals check-in reminders
 */
object MedicalReminderScheduler {

    private const val TAG = "MedicalReminderSched"

    /**
     * Schedules a local notification WorkManager task for a scheduled dose.
     */
    fun scheduleDose(context: Context, reminder: MedicineReminderEntity) {
        try {
            val delayMillis = calculateDelayMillis(reminder.scheduledDate, reminder.scheduledTime)

            val inputData = workDataOf(
                MedicalReminderWorker.KEY_REMINDER_TYPE to MedicalReminderWorker.TYPE_MEDICATION_DOSE,
                MedicalReminderWorker.KEY_REMINDER_ID to reminder.id,
                MedicalReminderWorker.KEY_MEDICINE_ID to reminder.medicineId,
                MedicalReminderWorker.KEY_MEDICINE_NAME to reminder.medicineName,
                MedicalReminderWorker.KEY_DOSAGE to reminder.dosage,
                MedicalReminderWorker.KEY_FORM to reminder.form,
                MedicalReminderWorker.KEY_INSTRUCTIONS to reminder.instructions,
                MedicalReminderWorker.KEY_SCHEDULED_TIME to reminder.scheduledTime
            )

            val workRequest = OneTimeWorkRequestBuilder<MedicalReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("medical_reminder")
                .addTag("reminder_${reminder.id}")
                .addTag("medicine_${reminder.medicineId}")
                .addTag("type_dose")
                .build()

            val uniqueWorkName = "medical_dose_${reminder.id}"
            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            Log.d(TAG, "Scheduled dose for ${reminder.medicineName} at ${reminder.scheduledTime} in ${delayMillis / 1000}s (Work: $uniqueWorkName)")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to schedule dose reminder ${reminder.id}: ${e.message}", e)
        }
    }

    /**
     * Schedules all pending doses in a list.
     */
    fun scheduleDoses(context: Context, reminders: List<MedicineReminderEntity>) {
        try {
            reminders.filter { it.status == "PENDING" || it.status == "SNOOZED" }.forEach { reminder ->
                scheduleDose(context, reminder)
            }
            // Ensure periodic background synchronization is active
            DailyMedicationSyncWorker.enqueuePeriodicSync(context)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to schedule list of doses: ${e.message}", e)
        }
    }

    /**
     * Schedules reminders for all dosage timings of a medicine.
     */
    fun scheduleMedicineDoses(
        context: Context,
        medicineName: String,
        medicineId: String,
        dosage: String,
        form: String,
        instructions: String,
        timesString: String
    ) {
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
                scheduleDose(context, reminder)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to schedule medicine doses for $medicineName: ${e.message}", e)
        }
    }

    /**
     * Schedules a snoozed dose alert for N minutes.
     */
    fun scheduleSnooze(context: Context, reminder: MedicineReminderEntity, snoozeMinutes: Int = 15) {
        try {
            val delayMillis = TimeUnit.MINUTES.toMillis(snoozeMinutes.toLong())

            val inputData = workDataOf(
                MedicalReminderWorker.KEY_REMINDER_TYPE to MedicalReminderWorker.TYPE_MEDICATION_DOSE,
                MedicalReminderWorker.KEY_REMINDER_ID to reminder.id,
                MedicalReminderWorker.KEY_MEDICINE_ID to reminder.medicineId,
                MedicalReminderWorker.KEY_MEDICINE_NAME to "${reminder.medicineName} (Snoozed)",
                MedicalReminderWorker.KEY_DOSAGE to reminder.dosage,
                MedicalReminderWorker.KEY_FORM to reminder.form,
                MedicalReminderWorker.KEY_INSTRUCTIONS to "${reminder.instructions} • Snoozed dose",
                MedicalReminderWorker.KEY_SCHEDULED_TIME to reminder.scheduledTime
            )

            val workRequest = OneTimeWorkRequestBuilder<MedicalReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("medical_reminder")
                .addTag("reminder_${reminder.id}")
                .addTag("medicine_${reminder.medicineId}")
                .addTag("snooze")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "medical_dose_${reminder.id}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            Log.d(TAG, "Snoozed dose for ${reminder.medicineName} for $snoozeMinutes minutes")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to snooze dose: ${e.message}", e)
        }
    }

    /**
     * Schedules a low-stock refill warning for a medication.
     */
    fun scheduleRefillAlert(
        context: Context,
        medicineId: String,
        medicineName: String,
        stockRemaining: Int,
        delayMinutes: Long = 0
    ) {
        try {
            val inputData = workDataOf(
                MedicalReminderWorker.KEY_REMINDER_TYPE to MedicalReminderWorker.TYPE_REFILL_ALERT,
                MedicalReminderWorker.KEY_REMINDER_ID to "refill_$medicineId",
                MedicalReminderWorker.KEY_MEDICINE_ID to medicineId,
                MedicalReminderWorker.KEY_MEDICINE_NAME to medicineName,
                MedicalReminderWorker.KEY_STOCK_REMAINING to stockRemaining
            )

            val delayMillis = TimeUnit.MINUTES.toMillis(delayMinutes)
            val request = OneTimeWorkRequestBuilder<MedicalReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("medical_reminder")
                .addTag("refill_$medicineId")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "refill_$medicineId",
                ExistingWorkPolicy.REPLACE,
                request
            )
            Log.d(TAG, "Scheduled refill warning for $medicineName (Stock: $stockRemaining)")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to schedule refill alert: ${e.message}", e)
        }
    }

    /**
     * Schedules a doctor appointment consultation reminder with lead-time calculation.
     */
    fun scheduleAppointmentReminder(context: Context, appointment: AppointmentEntity) {
        if (!appointment.reminderEnabled) return

        try {
            val delayMillis = calculateAppointmentDelayMillis(
                dateString = appointment.appointmentDate,
                timeString = appointment.appointmentTime,
                minutesBefore = appointment.reminderMinutesBefore
            )

            val inputData = workDataOf(
                MedicalReminderWorker.KEY_REMINDER_TYPE to MedicalReminderWorker.TYPE_APPOINTMENT,
                MedicalReminderWorker.KEY_REMINDER_ID to appointment.id,
                MedicalReminderWorker.KEY_DOCTOR_NAME to appointment.doctorName,
                MedicalReminderWorker.KEY_DOCTOR_SPECIALTY to appointment.doctorSpecialty,
                MedicalReminderWorker.KEY_APPOINTMENT_DATE to appointment.appointmentDate,
                MedicalReminderWorker.KEY_APPOINTMENT_TIME to appointment.appointmentTime
            )

            val request = OneTimeWorkRequestBuilder<MedicalReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("medical_reminder")
                .addTag("appointment_${appointment.id}")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "appointment_${appointment.id}",
                ExistingWorkPolicy.REPLACE,
                request
            )
            Log.d(TAG, "Scheduled appointment reminder for Dr. ${appointment.doctorName} in ${delayMillis / 1000}s")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to schedule appointment reminder: ${e.message}", e)
        }
    }

    /**
     * Fires an immediate test notification using WorkManager to confirm that background tasks and local notifications work.
     */
    fun triggerInstantTestAlert(
        context: Context,
        type: String = MedicalReminderWorker.TYPE_MEDICATION_DOSE,
        medicineName: String = "Amoxicillin",
        dosage: String = "500 mg"
    ) {
        MedicalReminderWorker.enqueueTestReminder(context, type, medicineName, dosage)
    }

    /**
     * Cancels a specific reminder WorkManager task.
     */
    fun cancelReminder(context: Context, reminderId: String) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork("medical_dose_$reminderId")
            WorkManager.getInstance(context).cancelUniqueWork("reminder_$reminderId")
            Log.d(TAG, "Cancelled WorkManager task for reminder: $reminderId")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to cancel reminder: ${e.message}", e)
        }
    }

    /**
     * Cancels all scheduled reminder tasks for a specific medicine.
     */
    fun cancelMedicineReminders(context: Context, medicineId: String) {
        try {
            WorkManager.getInstance(context).cancelAllWorkByTag("medicine_$medicineId")
            WorkManager.getInstance(context).cancelUniqueWork("refill_$medicineId")
            Log.d(TAG, "Cancelled WorkManager tasks for medicine: $medicineId")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to cancel medicine reminders: ${e.message}", e)
        }
    }

    /**
     * Cancels an appointment reminder task.
     */
    fun cancelAppointmentReminder(context: Context, appointmentId: String) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork("appointment_$appointmentId")
            Log.d(TAG, "Cancelled appointment WorkManager task: appointment_$appointmentId")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to cancel appointment reminder: ${e.message}", e)
        }
    }

    /**
     * Computes the initial delay in milliseconds until the scheduled date and time.
     */
    fun calculateDelayMillis(dateString: String, timeString: String): Long {
        val now = System.currentTimeMillis()

        // 1. Try parsing full date + time (e.g. 2026-09-10 and 08:30 AM)
        val formats = listOf(
            "yyyy-MM-dd hh:mm a",
            "yyyy-MM-dd h:mm a",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd H:mm"
        )
        for (pattern in formats) {
            try {
                val parsed = SimpleDateFormat(pattern, Locale.getDefault()).parse("$dateString $timeString")
                if (parsed != null) {
                    val diff = parsed.time - now
                    if (diff > 0) return diff
                }
            } catch (_: Exception) {}
        }

        // 2. Fallback: parse time only and compare with today's clock
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
                        // If already passed for today, schedule for tomorrow
                        targetCal.add(Calendar.DAY_OF_YEAR, 1)
                        diff = targetCal.timeInMillis - now
                    }
                    if (diff > 0) return diff
                }
            } catch (_: Exception) {}
        }

        // Default to a 30-second reminder delay if unparseable
        return 30_000L
    }

    /**
     * Computes delay for appointment notifications taking lead-time into account.
     */
    fun calculateAppointmentDelayMillis(dateString: String, timeString: String, minutesBefore: Int = 30): Long {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        val normalizedDate = dateString.trim().lowercase(Locale.getDefault())
        when {
            normalizedDate.contains("today") -> {
                // keep today
            }
            normalizedDate.contains("tomorrow") -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            normalizedDate.contains("in 3 days") -> {
                calendar.add(Calendar.DAY_OF_YEAR, 3)
            }
            normalizedDate.contains("next week") -> {
                calendar.add(Calendar.DAY_OF_YEAR, 7)
            }
            else -> {
                try {
                    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
                    if (parsed != null) calendar.time = parsed
                } catch (_: Exception) {}
            }
        }

        val timeFormats = listOf("hh:mm a", "h:mm a", "HH:mm")
        for (pattern in timeFormats) {
            try {
                val parsedTime = SimpleDateFormat(pattern, Locale.getDefault()).parse(timeString.trim())
                if (parsedTime != null) {
                    val timeCal = Calendar.getInstance().apply { time = parsedTime }
                    calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                    calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    val targetMillis = calendar.timeInMillis - TimeUnit.MINUTES.toMillis(minutesBefore.toLong())
                    val diff = targetMillis - now
                    return if (diff > 0) diff else 15_000L
                }
            } catch (_: Exception) {}
        }

        return 15_000L
    }
}
