package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity

class AppointmentReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "appointment_reminders_channel"
        const val CHANNEL_NAME = "Doctor Appointment Alerts"

        const val KEY_APPOINTMENT_ID = "appointment_id"
        const val KEY_DOCTOR_NAME = "doctor_name"
        const val KEY_DOCTOR_SPECIALTY = "doctor_specialty"
        const val KEY_APPOINTMENT_DATE = "appointment_date"
        const val KEY_APPOINTMENT_TIME = "appointment_time"
        const val KEY_REASON = "reason"
    }

    override suspend fun doWork(): Result {
        return try {
            val apptId = inputData.getString(KEY_APPOINTMENT_ID) ?: ""
            val doctorName = inputData.getString(KEY_DOCTOR_NAME) ?: "Doctor"
            val specialty = inputData.getString(KEY_DOCTOR_SPECIALTY) ?: "Specialist"
            val apptDate = inputData.getString(KEY_APPOINTMENT_DATE) ?: "Today"
            val apptTime = inputData.getString(KEY_APPOINTMENT_TIME) ?: ""
            val reason = inputData.getString(KEY_REASON) ?: "Medical Consultation"

            createNotificationChannel()

            val intent = Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("appointment_id", apptId)
                putExtra("destination", "appointments")
            }

            val pendingIntent = PendingIntent.getActivity(
                appContext,
                apptId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationTitle = "Doctor Appointment Reminder"
            val notificationText = "$doctorName ($specialty)\nScheduled for $apptDate at $apptTime\nPurpose: $reason"

            val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                .setContentTitle("Upcoming: $doctorName")
                .setContentText("Consultation scheduled for $apptDate at $apptTime ($specialty)")
                .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 300, 200, 300))

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(
                    appContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val notificationManager = NotificationManagerCompat.from(appContext)
                notificationManager.notify("appointment_$apptId".hashCode(), builder.build())
            }

            Result.success()
        } catch (e: Throwable) {
            Log.e("ApptReminderWorker", "Error presenting appointment reminder: ${e.message}", e)
            Result.success()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts and reminders for upcoming doctor consultations"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
