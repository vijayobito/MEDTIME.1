package com.example.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity

class MedicationReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "medication_reminders_channel"
        const val CHANNEL_NAME = "Medication Alerts & Reminders"

        const val KEY_REMINDER_ID = "reminder_id"
        const val KEY_MEDICINE_ID = "medicine_id"
        const val KEY_MEDICINE_NAME = "medicine_name"
        const val KEY_DOSAGE = "dosage"
        const val KEY_INSTRUCTIONS = "instructions"
        const val KEY_SCHEDULED_TIME = "scheduled_time"
        const val KEY_FORM = "form"
    }

    override suspend fun doWork(): Result {
        val reminderId = inputData.getString(KEY_REMINDER_ID) ?: ""
        val medicineName = inputData.getString(KEY_MEDICINE_NAME) ?: "Medication"
        val dosage = inputData.getString(KEY_DOSAGE) ?: ""
        val instructions = inputData.getString(KEY_INSTRUCTIONS) ?: ""
        val scheduledTime = inputData.getString(KEY_SCHEDULED_TIME) ?: ""
        val form = inputData.getString(KEY_FORM) ?: "Dose"

        createNotificationChannel()

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("reminder_id", reminderId)
            putExtra("destination", "medicines")
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationTitle = "Medication Alert: $medicineName"
        val notificationText = buildString {
            if (dosage.isNotBlank()) append(dosage)
            if (form.isNotBlank()) append(" • $form")
            if (instructions.isNotBlank()) append(" ($instructions)")
            if (scheduledTime.isNotBlank()) append(" • Time: $scheduledTime")
        }

        val notifId = if (reminderId.isNotBlank()) reminderId.hashCode() else System.currentTimeMillis().toInt()

        // Quick action: Take Dose
        val takeIntent = Intent(appContext, MedicationActionReceiver::class.java).apply {
            action = MedicationActionReceiver.ACTION_TAKE_DOSE
            putExtra(KEY_REMINDER_ID, reminderId)
            putExtra(KEY_MEDICINE_ID, inputData.getString(KEY_MEDICINE_ID) ?: "")
            putExtra(KEY_MEDICINE_NAME, medicineName)
            putExtra(KEY_DOSAGE, dosage)
            putExtra(KEY_SCHEDULED_TIME, scheduledTime)
            putExtra("notification_id", notifId)
        }
        val takePendingIntent = PendingIntent.getBroadcast(
            appContext,
            (reminderId + "_take").hashCode(),
            takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Quick action: Snooze 15 minutes
        val snoozeIntent = Intent(appContext, MedicationActionReceiver::class.java).apply {
            action = MedicationActionReceiver.ACTION_SNOOZE_DOSE
            putExtra(KEY_REMINDER_ID, reminderId)
            putExtra(KEY_MEDICINE_ID, inputData.getString(KEY_MEDICINE_ID) ?: "")
            putExtra(KEY_MEDICINE_NAME, medicineName)
            putExtra(KEY_DOSAGE, dosage)
            putExtra(KEY_SCHEDULED_TIME, scheduledTime)
            putExtra("notification_id", notifId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            appContext,
            (reminderId + "_snooze").hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Quick action: Skip / Missed
        val skipIntent = Intent(appContext, MedicationActionReceiver::class.java).apply {
            action = MedicationActionReceiver.ACTION_DISMISS_DOSE
            putExtra(KEY_REMINDER_ID, reminderId)
            putExtra(KEY_MEDICINE_ID, inputData.getString(KEY_MEDICINE_ID) ?: "")
            putExtra(KEY_MEDICINE_NAME, medicineName)
            putExtra(KEY_DOSAGE, dosage)
            putExtra(KEY_SCHEDULED_TIME, scheduledTime)
            putExtra("notification_id", notifId)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            appContext,
            (reminderId + "_skip").hashCode(),
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(notificationTitle)
                    .bigText(
                        "$notificationText\n\nScheduled dose reminder triggered by MedTime. Tap to record dose as TAKEN, SNOOZE for 15 minutes, or SKIP."
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, "Take Dose", takePendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, "Snooze 15m", snoozePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Skip", skipPendingIntent)
            .setVibrate(longArrayOf(0, 450, 200, 450))
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.success()
            }
        }

        NotificationManagerCompat.from(appContext).notify(notifId, notification)

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = "High-priority local notifications for scheduled medication doses"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 450, 200, 450)
            }
            val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
