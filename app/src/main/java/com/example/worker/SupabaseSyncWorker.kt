package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.repository.MedTimeRepository
import java.util.concurrent.TimeUnit

/**
 * Background WorkManager worker that automatically synchronizes local Room data
 * with Supabase PostgreSQL whenever internet connectivity is present.
 */
class SupabaseSyncWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val PERIODIC_WORK_NAME = "SupabasePeriodicSyncWork"
        const val ONE_TIME_WORK_NAME = "SupabaseOneTimeSyncWork"
        private const val TAG = "SupabaseSyncWorker"

        /**
         * Enqueues periodic background sync every 15 minutes when connected to network.
         */
        fun enqueuePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<SupabaseSyncWorker>(
                15, TimeUnit.MINUTES
            )
            .setConstraints(constraints)
            .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
            Log.d(TAG, "Periodic Supabase PostgreSQL sync enqueued (every 15 min on CONNECTED network).")
        }

        /**
         * Triggers an immediate one-time sync task when network is connected.
         */
        fun enqueueOneTimeSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<SupabaseSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
            Log.d(TAG, "One-time immediate Supabase PostgreSQL sync requested.")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Executing background Supabase PostgreSQL synchronization.")
        return try {
            val repository = MedTimeRepository(appContext)
            if (!repository.isSupabaseConfigured()) {
                Log.d(TAG, "Supabase not configured; skipping sync.")
                return Result.success()
            }

            val result = repository.syncWithSupabase()
            if (result.isSuccess) {
                Log.d(TAG, "Supabase sync successful: ${result.recordsPulled} pulled, ${result.recordsPushed} pushed.")
                Result.success()
            } else {
                Log.w(TAG, "Supabase sync completed with warning: ${result.message}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in SupabaseSyncWorker: ${e.message}", e)
            Result.retry()
        }
    }
}
