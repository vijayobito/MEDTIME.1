package com.example.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
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
        private const val TAG = "MedicationActionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(MedicationReminderWorker.KEY_REMINDER_ID) ?: return
        val medicineId = intent.getStringExtra(MedicationReminderWorker.KEY_MEDICINE_ID) ?: ""
        val medicineName = intent.getStringExtra(MedicationReminderWorker.KEY_MEDICINE_NAME) ?: "Medication"
        val dosage = intent.getStringExtra(MedicationReminderWorker.KEY_DOSAGE) ?: ""
        val scheduledTime = intent.getStringExtra(MedicationReminderWorker.KEY_SCHEDULED_TIME) ?: ""
        val notifId = intent.getIntExtra("notification_id", reminderId.hashCode())

        // Dismiss the active notification immediately
        try {
            NotificationManagerCompat.from(context).cancel(notifId)
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
                                notes = "Taken via background notification quick action"
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

                        // Cancel any pending work for this reminder
                        MedicationReminderScheduler.cancelReminder(context, reminderId)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error executing take action: ${e.message}")
                    }
                }
            }

            ACTION_SNOOZE_DOSE -> {
                Log.d(TAG, "Action SNOOZE_DOSE received for reminder: $reminderId")
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getInstance(context)
                        val reminder = db.reminderDao().getReminderById(reminderId)
                        val snoozeMinutes = 15
                        val snoozeTarget = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
                        val snoozeTimeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(snoozeTarget))

                        db.reminderDao().snoozeReminder(reminderId, snoozeTimeStr)

                        if (reminder != null) {
                            MedicationReminderScheduler.scheduleSnooze(context, reminder, snoozeMinutes)
                        }

                        db.notificationDao().insertNotification(
                            NotificationEntity(
                                id = UUID.randomUUID().toString(),
                                userId = reminder?.patientId ?: "patient-1",
                                title = "Dose Snoozed: $medicineName",
                                message = "Snoozed $medicineName ($dosage) for $snoozeMinutes minutes (alert at $snoozeTimeStr).",
                                type = "MEDICINE"
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error executing snooze action: ${e.message}")
                    }
                }
            }

            ACTION_DISMISS_DOSE -> {
                Log.d(TAG, "Action DISMISS_DOSE received for reminder: $reminderId")
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getInstance(context)
                        val now = System.currentTimeMillis()

                        db.reminderDao().updateReminderStatus(reminderId, "MISSED", now)

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
                                notes = "Skipped or dismissed directly from notification"
                            )
                        )

                        db.notificationDao().insertNotification(
                            NotificationEntity(
                                id = UUID.randomUUID().toString(),
                                userId = "patient-1",
                                title = "Dose Skipped: $medicineName",
                                message = "Marked scheduled dose ($dosage) at $scheduledTime as skipped.",
                                type = "MEDICINE"
                            )
                        )

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
