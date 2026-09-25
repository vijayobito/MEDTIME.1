package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.db.AppDatabase
import com.example.data.model.MedicineReminderEntity
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class DailyMedicationSyncWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "DailyMedicationSyncWork"
        private const val TAG = "DailyMedSyncWorker"

        fun enqueuePeriodicSync(context: Context) {
            val periodicRequest = PeriodicWorkRequestBuilder<DailyMedicationSyncWorker>(
                12, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
            Log.d(TAG, "Periodic daily medication sync enqueued.")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Executing daily medication synchronization and reminder scheduling.")
        return try {
            val db = AppDatabase.getInstance(appContext)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Get active medicines
            val allUsers = db.userDao().getAllUsers().first()
            val patientUsers = allUsers.filter { it.role == "PATIENT" }

            for (patient in patientUsers) {
                val medicines = db.medicineDao().getMedicinesByPatient(patient.id).first()
                val activeMedicines = medicines.filter { it.status == "ACTIVE" }

                // Check existing reminders for today
                val existingReminders = db.reminderDao().getRemindersForDate(patient.id, today).first()
                val existingTimesByMedId = existingReminders.groupBy { it.medicineId }

                val newReminders = mutableListOf<MedicineReminderEntity>()

                for (med in activeMedicines) {
                    val scheduledForThisMed = existingTimesByMedId[med.id] ?: emptyList()
                    val scheduledTimes = scheduledForThisMed.map { it.scheduledTime }

                    val requiredTimes = med.reminderTimes.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    for (time in requiredTimes) {
                        if (time !in scheduledTimes) {
                            newReminders.add(
                                MedicineReminderEntity(
                                    id = UUID.randomUUID().toString(),
                                    medicineId = med.id,
                                    patientId = patient.id,
                                    medicineName = med.name,
                                    dosage = med.dosage,
                                    form = med.form,
                                    instructions = med.instructions,
                                    scheduledDate = today,
                                    scheduledTime = time,
                                    status = "PENDING"
                                )
                            )
                        }
                    }
                }

                if (newReminders.isNotEmpty()) {
                    db.reminderDao().insertReminders(newReminders)
                    Log.d(TAG, "Generated ${newReminders.size} daily reminders for patient ${patient.name}")
                }
            }

            // Schedule all pending reminders for today with WorkManager
            val pendingReminders = db.reminderDao().getPendingRemindersForDateDirect(today)
            MedicationReminderScheduler.scheduleReminders(appContext, pendingReminders)
            Log.d(TAG, "Successfully synced and scheduled ${pendingReminders.size} medication reminders.")

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during daily medication sync: ${e.message}", e)
            Result.retry()
        }
    }
}
