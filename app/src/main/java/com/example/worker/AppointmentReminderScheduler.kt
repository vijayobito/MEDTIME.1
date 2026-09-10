package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.data.model.AppointmentEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object AppointmentReminderScheduler {

    private const val TAG = "ApptReminderScheduler"

    /**
     * Schedules a local notification alert for an appointment using WorkManager.
     */
    fun scheduleAppointmentReminder(context: Context, appointment: AppointmentEntity) {
        if (!appointment.reminderEnabled) {
            cancelAppointmentReminder(context, appointment.id)
            return
        }

        try {
            val delayMillis = calculateDelayMillis(
                dateString = appointment.appointmentDate,
                timeString = appointment.appointmentTime,
                minutesBefore = appointment.reminderMinutesBefore
            )

            val inputData = workDataOf(
                AppointmentReminderWorker.KEY_APPOINTMENT_ID to appointment.id,
                AppointmentReminderWorker.KEY_DOCTOR_NAME to appointment.doctorName,
                AppointmentReminderWorker.KEY_DOCTOR_SPECIALTY to appointment.doctorSpecialty,
                AppointmentReminderWorker.KEY_APPOINTMENT_DATE to appointment.appointmentDate,
                AppointmentReminderWorker.KEY_APPOINTMENT_TIME to appointment.appointmentTime,
                AppointmentReminderWorker.KEY_REASON to appointment.reason
            )

            val workRequest = OneTimeWorkRequestBuilder<AppointmentReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("appointment_${appointment.id}")
                .addTag("doctor_${appointment.doctorId}")
                .addTag("appointment_reminders")
                .build()

            val uniqueWorkName = "appointment_rem_${appointment.id}"
            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            Log.d(TAG, "Scheduled appointment alert for ${appointment.doctorName} on ${appointment.appointmentDate} at ${appointment.appointmentTime} (in ${delayMillis / 1000}s, lead time ${appointment.reminderMinutesBefore}m)")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to schedule appointment reminder: ${e.message}", e)
        }
    }

    /**
     * Cancels an appointment alert in WorkManager.
     */
    fun cancelAppointmentReminder(context: Context, appointmentId: String) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork("appointment_rem_$appointmentId")
            Log.d(TAG, "Cancelled WorkManager task for appointment: appointment_rem_$appointmentId")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to cancel appointment reminder: ${e.message}", e)
        }
    }

    /**
     * Calculates delay in milliseconds before the reminder should trigger.
     */
    fun calculateDelayMillis(dateString: String, timeString: String, minutesBefore: Int = 60): Long {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        // 1. Resolve Date
        val lowerDate = dateString.lowercase(Locale.getDefault()).trim()
        when {
            lowerDate == "today" -> {
                // today
            }
            lowerDate == "tomorrow" -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            lowerDate.startsWith("in ") && lowerDate.endsWith(" days") -> {
                val days = lowerDate.removePrefix("in ").removeSuffix(" days").trim().toIntOrNull() ?: 1
                calendar.add(Calendar.DAY_OF_YEAR, days)
            }
            else -> {
                // Try parsing standard date formats
                val dateFormats = listOf("yyyy-MM-dd", "MM/dd/yyyy", "dd/MM/yyyy", "MMM d, yyyy", "EEE, MMM d")
                var parsedDate: Date? = null
                for (fmt in dateFormats) {
                    try {
                        parsedDate = SimpleDateFormat(fmt, Locale.getDefault()).parse(dateString)
                        if (parsedDate != null) break
                    } catch (_: Exception) {}
                }

                if (parsedDate != null) {
                    val parsedCal = Calendar.getInstance().apply { time = parsedDate }
                    calendar.set(Calendar.YEAR, parsedCal.get(Calendar.YEAR))
                    calendar.set(Calendar.MONTH, parsedCal.get(Calendar.MONTH))
                    calendar.set(Calendar.DAY_OF_MONTH, parsedCal.get(Calendar.DAY_OF_MONTH))
                } else {
                    // Fallback to tomorrow if unrecognized relative string
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }

        // 2. Resolve Time
        val normalizedTime = timeString.trim().uppercase(Locale.getDefault())
        val timeFormats = listOf("hh:mm a", "h:mm a", "hh:mma", "h:mma", "HH:mm", "H:mm")
        var parsedTime: Date? = null
        for (fmt in timeFormats) {
            try {
                parsedTime = SimpleDateFormat(fmt, Locale.getDefault()).parse(normalizedTime)
                if (parsedTime != null) break
            } catch (_: Exception) {}
        }

        if (parsedTime != null) {
            val timeCal = Calendar.getInstance().apply { time = parsedTime }
            calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        } else {
            // Default to 10:00 AM if unparseable
            calendar.set(Calendar.HOUR_OF_DAY, 10)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }

        // Subtract lead time (minutesBefore)
        calendar.add(Calendar.MINUTE, -minutesBefore)

        val targetTime = calendar.timeInMillis
        val delay = targetTime - now
        return if (delay > 0) delay else 5000L // If target is in the past, trigger shortly (5s) for testing
    }
}
