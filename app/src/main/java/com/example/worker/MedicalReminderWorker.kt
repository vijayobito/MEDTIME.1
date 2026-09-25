package com.example.worker

import android.Manifest
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
import androidx.work.*
import com.example.MainActivity
import java.util.concurrent.TimeUnit

/**
 * Robust WorkManager worker that generates local notifications for all medical reminders:
 * - Scheduled medication doses (with interactive Take / Snooze / Skip actions)
 * - Low-stock prescription refill alerts
 * - Doctor appointment consultation reminders
 * - Daily health vitals logging reminders
 */
class MedicalReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "MedicalReminderWorker"

        // Reminder types
        const val TYPE_MEDICATION_DOSE = "TYPE_DOSE"
        const val TYPE_REFILL_ALERT = "TYPE_REFILL"
        const val TYPE_APPOINTMENT = "TYPE_APPOINTMENT"
        const val TYPE_VITALS_CHECK = "TYPE_VITALS"
        const val TYPE_GENERAL = "TYPE_GENERAL"

        // Input keys
        const val KEY_REMINDER_TYPE = "reminder_type"
        const val KEY_REMINDER_ID = "reminder_id"
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        const val KEY_MEDICINE_ID = "medicine_id"
        const val KEY_MEDICINE_NAME = "medicine_name"
        const val KEY_DOSAGE = "dosage"
        const val KEY_FORM = "form"
        const val KEY_INSTRUCTIONS = "instructions"
        const val KEY_SCHEDULED_TIME = "scheduled_time"
        const val KEY_DOCTOR_NAME = "doctor_name"
        const val KEY_DOCTOR_SPECIALTY = "doctor_specialty"
        const val KEY_APPOINTMENT_DATE = "appointment_date"
        const val KEY_APPOINTMENT_TIME = "appointment_time"
        const val KEY_STOCK_REMAINING = "stock_remaining"

        // Notification Channels
        const val CHANNEL_DOSE = "medication_reminders_channel"
        const val CHANNEL_DOSE_NAME = "Medication Alerts & Reminders"

        const val CHANNEL_REFILL = "medical_refill_channel"
        const val CHANNEL_REFILL_NAME = "Prescription Refill Reminders"

        const val CHANNEL_APPOINTMENT = "appointment_reminders_channel"
        const val CHANNEL_APPOINTMENT_NAME = "Doctor Appointment Alerts"

        const val CHANNEL_VITALS = "medical_vitals_channel"
        const val CHANNEL_VITALS_NAME = "Daily Vitals & Health Checks"

        /**
         * Enqueues an immediate test medical reminder via WorkManager.
         */
        fun enqueueTestReminder(
            context: Context,
            type: String = TYPE_MEDICATION_DOSE,
            medicineName: String = "Amoxicillin",
            dosage: String = "500 mg"
        ) {
            val inputData = workDataOf(
                KEY_REMINDER_TYPE to type,
                KEY_REMINDER_ID to "test_${System.currentTimeMillis()}",
                KEY_MEDICINE_NAME to medicineName,
                KEY_DOSAGE to dosage,
                KEY_FORM to "Capsule",
                KEY_INSTRUCTIONS to "Take with a glass of water after meal",
                KEY_SCHEDULED_TIME to "Now",
                KEY_STOCK_REMAINING to 3,
                KEY_DOCTOR_NAME to "Dr. Sarah Jenkins",
                KEY_DOCTOR_SPECIALTY to "Cardiologist",
                KEY_APPOINTMENT_DATE to "Tomorrow",
                KEY_APPOINTMENT_TIME to "10:30 AM"
            )

            val request = OneTimeWorkRequestBuilder<MedicalReminderWorker>()
                .setInitialDelay(200, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("medical_reminder")
                .addTag("test_alert")
                .build()

            WorkManager.getInstance(context).enqueue(request)
            Log.d(TAG, "Enqueued test medical reminder of type: $type")
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val type = inputData.getString(KEY_REMINDER_TYPE) ?: TYPE_MEDICATION_DOSE
            val reminderId = inputData.getString(KEY_REMINDER_ID) ?: "med_${System.currentTimeMillis()}"

            createNotificationChannels()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        appContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.w(TAG, "Notification permission not granted. Skipping notification.")
                    return Result.success()
                }
            }

            val notificationManager = NotificationManagerCompat.from(appContext)

            when (type) {
                TYPE_REFILL_ALERT -> {
                    val medName = inputData.getString(KEY_MEDICINE_NAME) ?: "Medication"
                    val stock = inputData.getInt(KEY_STOCK_REMAINING, 2)
                    val notifId = ("refill_" + (inputData.getString(KEY_MEDICINE_ID) ?: reminderId)).hashCode()

                    val intent = Intent(appContext, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("destination", "medicines")
                    }
                    val pi = PendingIntent.getActivity(
                        appContext,
                        notifId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val notification = NotificationCompat.Builder(appContext, CHANNEL_REFILL)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("Refill Alert: $medName Low Stock")
                        .setContentText("Only $stock doses remaining. Tap to check prescription or reorder.")
                        .setStyle(
                            NotificationCompat.BigTextStyle()
                                .setBigContentTitle("⚠️ Prescription Refill Needed: $medName")
                                .bigText(
                                    "Your inventory for $medName has reached a low threshold ($stock units left).\n" +
                                    "Order a refill soon to avoid missing any future doses."
                                )
                        )
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setAutoCancel(true)
                        .setContentIntent(pi)
                        .addAction(android.R.drawable.ic_menu_agenda, "View Medicines", pi)
                        .build()

                    notificationManager.notify(notifId, notification)
                    Log.d(TAG, "Delivered refill alert notification for $medName")
                }

                TYPE_APPOINTMENT -> {
                    val docName = inputData.getString(KEY_DOCTOR_NAME) ?: "Doctor"
                    val spec = inputData.getString(KEY_DOCTOR_SPECIALTY) ?: "Specialist"
                    val date = inputData.getString(KEY_APPOINTMENT_DATE) ?: "Upcoming"
                    val time = inputData.getString(KEY_APPOINTMENT_TIME) ?: ""
                    val notifId = ("appt_" + reminderId).hashCode()

                    val intent = Intent(appContext, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("destination", "appointments")
                    }
                    val pi = PendingIntent.getActivity(
                        appContext,
                        notifId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val notification = NotificationCompat.Builder(appContext, CHANNEL_APPOINTMENT)
                        .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                        .setContentTitle("Doctor Visit: $docName")
                        .setContentText("Appointment on $date at $time ($spec)")
                        .setStyle(
                            NotificationCompat.BigTextStyle()
                                .setBigContentTitle("Upcoming Consultation: $docName")
                                .bigText(
                                    "Doctor: $docName\nSpecialty: $spec\nDate & Time: $date at $time\n" +
                                    "Please be prepared 10 minutes before your scheduled slot."
                                )
                        )
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_EVENT)
                        .setAutoCancel(true)
                        .setContentIntent(pi)
                        .setVibrate(longArrayOf(0, 350, 150, 350))
                        .addAction(android.R.drawable.ic_menu_agenda, "View Details", pi)
                        .build()

                    notificationManager.notify(notifId, notification)
                    Log.d(TAG, "Delivered appointment notification for $docName")
                }

                TYPE_VITALS_CHECK -> {
                    val notifId = ("vitals_" + reminderId).hashCode()

                    val intent = Intent(appContext, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("destination", "dashboard")
                    }
                    val pi = PendingIntent.getActivity(
                        appContext,
                        notifId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val notification = NotificationCompat.Builder(appContext, CHANNEL_VITALS)
                        .setSmallIcon(android.R.drawable.ic_menu_compass)
                        .setContentTitle("Daily Health Vitals Check")
                        .setContentText("Time to record your daily blood pressure and heart rate.")
                        .setStyle(
                            NotificationCompat.BigTextStyle()
                                .setBigContentTitle("Daily Health Vitals Check")
                                .bigText("Staying consistent with your vitals logs helps your doctor monitor your cardiovascular and metabolic health.")
                        )
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(pi)
                        .addAction(android.R.drawable.ic_menu_edit, "Log Now", pi)
                        .build()

                    notificationManager.notify(notifId, notification)
                    Log.d(TAG, "Delivered vitals reminder notification")
                }

                else -> {
                    // TYPE_MEDICATION_DOSE or GENERAL
                    val medicineName = inputData.getString(KEY_MEDICINE_NAME) ?: "Medication"
                    val dosage = inputData.getString(KEY_DOSAGE) ?: ""
                    val instructions = inputData.getString(KEY_INSTRUCTIONS) ?: ""
                    val scheduledTime = inputData.getString(KEY_SCHEDULED_TIME) ?: ""
                    val form = inputData.getString(KEY_FORM) ?: "Dose"
                    val medicineId = inputData.getString(KEY_MEDICINE_ID) ?: ""
                    val notifId = reminderId.hashCode()

                    val mainIntent = Intent(appContext, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("reminder_id", reminderId)
                        putExtra("destination", "medicines")
                    }
                    val mainPendingIntent = PendingIntent.getActivity(
                        appContext,
                        notifId,
                        mainIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    // Quick Action 1: Take Dose
                    val takeIntent = Intent(appContext, MedicationActionReceiver::class.java).apply {
                        action = MedicationActionReceiver.ACTION_TAKE_DOSE
                        putExtra(KEY_REMINDER_ID, reminderId)
                        putExtra(KEY_MEDICINE_ID, medicineId)
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

                    // Quick Action 2: Snooze 15m
                    val snoozeIntent = Intent(appContext, MedicationActionReceiver::class.java).apply {
                        action = MedicationActionReceiver.ACTION_SNOOZE_DOSE
                        putExtra(KEY_REMINDER_ID, reminderId)
                        putExtra(KEY_MEDICINE_ID, medicineId)
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

                    // Quick Action 3: Skip Dose
                    val skipIntent = Intent(appContext, MedicationActionReceiver::class.java).apply {
                        action = MedicationActionReceiver.ACTION_DISMISS_DOSE
                        putExtra(KEY_REMINDER_ID, reminderId)
                        putExtra(KEY_MEDICINE_ID, medicineId)
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

                    val title = "Medication Due: $medicineName"
                    val shortText = buildString {
                        if (dosage.isNotBlank()) append(dosage)
                        if (form.isNotBlank()) append(" • $form")
                        if (scheduledTime.isNotBlank()) append(" at $scheduledTime")
                    }

                    val bigText = buildString {
                        append("Scheduled dose for $medicineName ($dosage $form).\n")
                        if (instructions.isNotBlank()) append("Directions: $instructions\n")
                        if (scheduledTime.isNotBlank()) append("Scheduled Time: $scheduledTime\n")
                        append("Tap 'Take Dose' below to record adherence instantly, or 'Snooze' for 15 minutes.")
                    }

                    val notification = NotificationCompat.Builder(appContext, CHANNEL_DOSE)
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .setContentTitle(title)
                        .setContentText(shortText)
                        .setStyle(
                            NotificationCompat.BigTextStyle()
                                .setBigContentTitle(title)
                                .bigText(bigText)
                        )
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setAutoCancel(true)
                        .setContentIntent(mainPendingIntent)
                        .addAction(android.R.drawable.checkbox_on_background, "Take Dose", takePendingIntent)
                        .addAction(android.R.drawable.ic_popup_sync, "Snooze 15m", snoozePendingIntent)
                        .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Skip", skipPendingIntent)
                        .setVibrate(longArrayOf(0, 500, 200, 500))
                        .build()

                    notificationManager.notify(notifId, notification)
                    Log.d(TAG, "Delivered medication dose notification for $medicineName")
                }
            }

            Result.success()
        } catch (e: Throwable) {
            Log.e(TAG, "Error in MedicalReminderWorker: ${e.message}", e)
            Result.success()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Dose Channel
            val doseChannel = NotificationChannel(
                CHANNEL_DOSE,
                CHANNEL_DOSE_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority local notifications for scheduled medication intake"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            notificationManager.createNotificationChannel(doseChannel)

            // Refill Channel
            val refillChannel = NotificationChannel(
                CHANNEL_REFILL,
                CHANNEL_REFILL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders when medicine stock count is low"
            }
            notificationManager.createNotificationChannel(refillChannel)

            // Appointment Channel
            val apptChannel = NotificationChannel(
                CHANNEL_APPOINTMENT,
                CHANNEL_APPOINTMENT_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Doctor consultation and clinic visit reminders"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 350, 150, 350)
            }
            notificationManager.createNotificationChannel(apptChannel)

            // Vitals Channel
            val vitalsChannel = NotificationChannel(
                CHANNEL_VITALS,
                CHANNEL_VITALS_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to log daily vitals like blood pressure and glucose"
            }
            notificationManager.createNotificationChannel(vitalsChannel)
        }
    }
}
