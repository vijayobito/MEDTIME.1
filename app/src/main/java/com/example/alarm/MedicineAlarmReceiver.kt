package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat

class MedicineAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "MedicineAlarmReceiver"
        private const val WAKELOCK_TAG = "MedTime:MedicineAlarmWakeLock"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "Alarm triggered with action: $action")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            WAKELOCK_TAG
        )

        try {
            wakeLock?.acquire(15_000L) // 15 seconds wake lock to ensure service start
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire wake lock: ${e.message}")
        }

        val reminderId = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_REMINDER_ID) ?: "unknown_reminder"
        val medicineId = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_MEDICINE_ID) ?: ""
        val medicineName = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_MEDICINE_NAME) ?: "Medication"
        val dosage = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_DOSAGE) ?: ""
        val form = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_FORM) ?: "Pill"
        val instructions = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_INSTRUCTIONS) ?: ""
        val scheduledTime = intent.getStringExtra(MedicineAlarmScheduler.EXTRA_SCHEDULED_TIME) ?: ""
        val attemptCount = intent.getIntExtra(MedicineAlarmScheduler.EXTRA_ATTEMPT_COUNT, 1)
        val snoozeCount = intent.getIntExtra(MedicineAlarmScheduler.EXTRA_SNOOZE_COUNT, 0)
        val isTest = intent.getBooleanExtra(MedicineAlarmScheduler.EXTRA_IS_TEST, false)

        val serviceIntent = Intent(context, MedicineAlarmService::class.java).apply {
            this.action = action
            putExtra(MedicineAlarmScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra(MedicineAlarmScheduler.EXTRA_MEDICINE_ID, medicineId)
            putExtra(MedicineAlarmScheduler.EXTRA_MEDICINE_NAME, medicineName)
            putExtra(MedicineAlarmScheduler.EXTRA_DOSAGE, dosage)
            putExtra(MedicineAlarmScheduler.EXTRA_FORM, form)
            putExtra(MedicineAlarmScheduler.EXTRA_INSTRUCTIONS, instructions)
            putExtra(MedicineAlarmScheduler.EXTRA_SCHEDULED_TIME, scheduledTime)
            putExtra(MedicineAlarmScheduler.EXTRA_ATTEMPT_COUNT, attemptCount)
            putExtra(MedicineAlarmScheduler.EXTRA_SNOOZE_COUNT, snoozeCount)
            putExtra(MedicineAlarmScheduler.EXTRA_IS_TEST, isTest)
        }

        try {
            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start MedicineAlarmService: ${e.message}", e)
        }

        // Also launch full-screen alarm activity directly if screen is locked / device asleep
        try {
            val fullScreenActivityIntent = Intent(context, com.example.ui.alarm.MedicineAlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                putExtra(MedicineAlarmScheduler.EXTRA_REMINDER_ID, reminderId)
                putExtra(MedicineAlarmScheduler.EXTRA_MEDICINE_ID, medicineId)
                putExtra(MedicineAlarmScheduler.EXTRA_MEDICINE_NAME, medicineName)
                putExtra(MedicineAlarmScheduler.EXTRA_DOSAGE, dosage)
                putExtra(MedicineAlarmScheduler.EXTRA_FORM, form)
                putExtra(MedicineAlarmScheduler.EXTRA_INSTRUCTIONS, instructions)
                putExtra(MedicineAlarmScheduler.EXTRA_SCHEDULED_TIME, scheduledTime)
                putExtra(MedicineAlarmScheduler.EXTRA_ATTEMPT_COUNT, attemptCount)
                putExtra(MedicineAlarmScheduler.EXTRA_SNOOZE_COUNT, snoozeCount)
                putExtra(MedicineAlarmScheduler.EXTRA_IS_TEST, isTest)
            }
            context.startActivity(fullScreenActivityIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start MedicineAlarmActivity: ${e.message}")
        }
    }
}
