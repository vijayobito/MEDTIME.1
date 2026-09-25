package com.example.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.alarm.MedicineAlarmScheduler
import com.example.alarm.MedicineAlarmService
import com.example.data.db.AppDatabase
import com.example.data.model.MedicineHistoryEntity
import com.example.data.model.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MedicationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TAKE_DOSE = "com.example.action.TAKE_MEDICATION_DOSE"
        const val ACTION_SNOOZE_DOSE = "com.example.action.SNOOZE_MEDICATION_DOSE"
        const val ACTION_DISMISS_DOSE = "com.example.action.DISMISS_MEDICATION_DOSE"
        const val ACTION_SKIP_DOSE = "com.example.action.SKIP_MEDICATION_DOSE"
        const val KEY_REMINDER_ID = "reminder_id"
        const val KEY_MEDICINE_ID = "medicine_id"
        const val KEY_MEDICINE_NAME = "medicine_name"
        const val KEY_DOSAGE = "dosage"
        const val KEY_SCHEDULED_TIME = "scheduled_time"
        private const val TAG = "MedicationActionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(KEY_REMINDER_ID)
            ?: intent.getStringExtra(MedicationReminderWorker.KEY_REMINDER_ID)
            ?: return
        val medicineId = intent.getStringExtra(KEY_MEDICINE_ID)
            ?: intent.getStringExtra(MedicationReminderWorker.KEY_MEDICINE_ID) ?: ""
        val medicineName = intent.getStringExtra(KEY_MEDICINE_NAME)
            ?: intent.getStringExtra(MedicationReminderWorker.KEY_MEDICINE_NAME) ?: "Medication"
        val dosage = intent.getStringExtra(KEY_DOSAGE)
            ?: intent.getStringExtra(MedicationReminderWorker.KEY_DOSAGE) ?: ""
        val scheduledTime = intent.getStringExtra(KEY_SCHEDULED_TIME)
            ?: intent.getStringExtra(MedicationReminderWorker.KEY_SCHEDULED_TIME) ?: ""
        val notifId = intent.getIntExtra("notification_id", reminderId.hashCode())

        // Stop active alarm sound and vibration service
        try {
            val stopIntent = Intent(context, MedicineAlarmService::class.java).apply {
                action = MedicineAlarmService.ACTION_STOP_ALARM
            }
            context.startService(stopIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping MedicineAlarmService: ${e.message}")
        }

        // Dismiss the active notification immediately
        try {
            NotificationManagerCompat.from(context).cancel(notifId)
            NotificationManagerCompat.from(context).cancel(MedicineAlarmService.NOTIFICATION_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Error dismissing notification: ${e.message}")
        }

        when (intent.action) {
            ACTION_TAKE_DOSE -> {
                Log.d(TAG, "Action TAKE_DOSE received for reminder: $reminderId")
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getInstance(context)
                        val now = System.currentTimeMillis()

                        // Update reminder status
                        db.reminderDao().updateReminderStatus(reminderId, "TAKEN", now)

                        // Insert history log
                        db.historyDao().insertHistory(
                            MedicineHistoryEntity(
                                id = UUID.randomUUID().toString(),
                                medicineId = medicineId,
                                patientId = "patient-1",
                                medicineName = medicineName,
                                dosage = dosage,
                                scheduledTime = scheduledTime,
                                action = "TAKEN",
                                actionTimestamp = now,
                                notes = "Taken via notification quick action"
                            )
                        )

                        // Decrement stock if available
                        val med = db.medicineDao().getMedicineById(medicineId)
                        if (med != null && med.stockQuantity > 0) {
                            val updated = med.copy(stockQuantity = med.stockQuantity - 1)
                            db.medicineDao().updateMedicine(updated)
                        }

                        // Add in-app notification record
                        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(now))
                        db.notificationDao().insertNotification(
                            NotificationEntity(
                                id = UUID.randomUUID().toString(),
                                userId = med?.patientId ?: "patient-1",
                                title = "Dose Taken: $medicineName",
                                message = "Logged $medicineName ($dosage) as taken at $timeStr from notification.",
                                type = "MEDICINE"
                            )
                        )

                        // Cancel any pending alarms and work for this reminder
                        MedicineAlarmScheduler.cancelAlarm(context, reminderId)
                        MedicationReminderScheduler.cancelReminder(context, reminderId)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error executing take action: ${e.message}")
                    }
                }
            }

            ACTION_SNOOZE_DOSE -> {
                val snoozeMinutes = intent.getIntExtra("snooze_minutes", 10)
                Log.d(TAG, "Action SNOOZE_DOSE received for reminder: $reminderId for $snoozeMinutes mins")
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getInstance(context)
                        val reminder = db.reminderDao().getReminderById(reminderId)
                        val now = System.currentTimeMillis()
                        val snoozeTarget = now + (snoozeMinutes * 60 * 1000L)
                        val snoozeTimeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(snoozeTarget))

                        db.reminderDao().snoozeReminderWithCount(reminderId, snoozeTimeStr, now)

                        db.historyDao().insertHistory(
                            MedicineHistoryEntity(
                                id = UUID.randomUUID().toString(),
                                medicineId = medicineId,
                                patientId = "patient-1",
                                medicineName = medicineName,
                                dosage = dosage,
                                scheduledTime = scheduledTime,
                                action = "SNOOZED",
                                actionTimestamp = now,
                                notes = "Snoozed for $snoozeMinutes min until $snoozeTimeStr"
                            )
                        )

                        if (reminder != null) {
                            MedicineAlarmScheduler.scheduleSnoozeAlarm(context, reminder, snoozeMinutes)
                        }

                        db.notificationDao().insertNotification(
                            NotificationEntity(
                                id = UUID.randomUUID().toString(),
                                userId = reminder?.patientId ?: "patient-1",
                                title = "Dose Snoozed: $medicineName",
                                message = "Snoozed $medicineName ($dosage) for $snoozeMinutes minutes (alarm set for $snoozeTimeStr).",
                                type = "MEDICINE"
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error executing snooze action: ${e.message}")
                    }
                }
            }

            ACTION_SKIP_DOSE, ACTION_DISMISS_DOSE -> {
                val reason = intent.getStringExtra("skip_reason") ?: "Skipped from notification quick action"
                Log.d(TAG, "Action SKIP_DOSE received for reminder: $reminderId ($reason)")
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getInstance(context)
                        val now = System.currentTimeMillis()

                        db.reminderDao().skipReminderWithReason(reminderId, reason, now)

                        db.historyDao().insertHistory(
                            MedicineHistoryEntity(
                                id = UUID.randomUUID().toString(),
                                medicineId = medicineId,
                                patientId = "patient-1",
                                medicineName = medicineName,
                                dosage = dosage,
                                scheduledTime = scheduledTime,
                                action = "SKIPPED",
                                actionTimestamp = now,
                                notes = reason
                            )
                        )

                        db.notificationDao().insertNotification(
                            NotificationEntity(
                                id = UUID.randomUUID().toString(),
                                userId = "patient-1",
                                title = "Dose Skipped: $medicineName",
                                message = "Marked scheduled dose ($dosage) at $scheduledTime as skipped.",
                                type = "MEDICINE",
                                severity = "WARNING"
                            )
                        )

                        MedicineAlarmScheduler.cancelAlarm(context, reminderId)
                        MedicalReminderScheduler.cancelReminder(context, reminderId)
                        MedicationReminderScheduler.cancelReminder(context, reminderId)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error executing dismiss action: ${e.message}")
                    }
                }
            }
        }
    }
}
