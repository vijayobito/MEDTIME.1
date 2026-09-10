package com.example.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.d("BootCompletedReceiver", "Device rebooted or app updated. Rescheduling WorkManager medication reminders.")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val pendingReminders = db.reminderDao().getPendingRemindersForDateDirect(today)
                    Log.d("BootCompletedReceiver", "Found ${pendingReminders.size} pending reminders to reschedule.")
                    MedicalReminderScheduler.scheduleDoses(context, pendingReminders)
                    MedicationReminderScheduler.scheduleReminders(context, pendingReminders)

                    // Reschedule daily periodic sync
                    DailyMedicationSyncWorker.enqueuePeriodicSync(context)
                } catch (e: Exception) {
                    Log.e("BootCompletedReceiver", "Failed to reschedule on boot: ${e.message}")
                }
            }
        }
    }
}
