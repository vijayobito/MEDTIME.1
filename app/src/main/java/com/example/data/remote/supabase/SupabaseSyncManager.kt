package com.example.data.remote.supabase

import android.content.Context
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SupabaseSyncResult(
    val isSuccess: Boolean,
    val recordsPushed: Int = 0,
    val recordsPulled: Int = 0,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedTime(): String {
        return SimpleDateFormat("hh:mm:ss a, MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}

/**
 * Orchestrates bidirectional synchronization between the local Room database and Supabase PostgreSQL.
 * Designed with an offline-first architecture: local operations always succeed immediately, and
 * changes sync to Supabase when network connectivity is available.
 */
class SupabaseSyncManager(private val context: Context) {
    private val client = SupabaseClient.getInstance(context)
    private val config = SupabaseConfig.getInstance(context)

    companion object {
        private const val TAG = "SupabaseSyncManager"

        @Volatile
        private var INSTANCE: SupabaseSyncManager? = null

        fun getInstance(context: Context): SupabaseSyncManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SupabaseSyncManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Executes a full two-way synchronization:
     * 1. Pulls the latest remote changes from Supabase PostgreSQL and merges into Room.
     * 2. Pushes local records from Room up to Supabase PostgreSQL via bulk upsert.
     */
    suspend fun syncAll(db: AppDatabase): SupabaseSyncResult = withContext(Dispatchers.IO) {
        if (!config.isConfigured()) {
            return@withContext SupabaseSyncResult(
                isSuccess = false,
                message = "Supabase is not configured. Please add your Supabase URL & Anon Key in Settings."
            )
        }

        try {
            val api = client.getApiService()
            var pulledCount = 0
            var pushedCount = 0

            // ----------------------------------------------------
            // Step 1: PULL remote data from Supabase into Room
            // ----------------------------------------------------
            try {
                val remoteUsers = api.getUsers()
                if (remoteUsers.isNotEmpty()) {
                    db.userDao().insertUsers(remoteUsers.map { it.toEntity() })
                    pulledCount += remoteUsers.size
                }
            } catch (e: Exception) {
                Log.w(TAG, "Pull users failed: ${e.message}")
            }

            try {
                val remoteMedicines = api.getMedicines()
                if (remoteMedicines.isNotEmpty()) {
                    db.medicineDao().insertMedicines(remoteMedicines.map { it.toEntity() })
                    pulledCount += remoteMedicines.size
                }
            } catch (e: Exception) {
                Log.w(TAG, "Pull medicines failed: ${e.message}")
            }

            try {
                val remoteReminders = api.getReminders()
                if (remoteReminders.isNotEmpty()) {
                    db.reminderDao().insertReminders(remoteReminders.map { it.toEntity() })
                    pulledCount += remoteReminders.size
                }
            } catch (e: Exception) {
                Log.w(TAG, "Pull reminders failed: ${e.message}")
            }

            try {
                val remoteHistory = api.getHistory()
                if (remoteHistory.isNotEmpty()) {
                    db.historyDao().insertHistories(remoteHistory.map { it.toEntity() })
                    pulledCount += remoteHistory.size
                }
            } catch (e: Exception) {
                Log.w(TAG, "Pull history failed: ${e.message}")
            }

            try {
                val remoteAppointments = api.getAppointments()
                if (remoteAppointments.isNotEmpty()) {
                    db.appointmentDao().insertAppointments(remoteAppointments.map { it.toEntity() })
                    pulledCount += remoteAppointments.size
                }
            } catch (e: Exception) {
                Log.w(TAG, "Pull appointments failed: ${e.message}")
            }

            try {
                val remoteMessages = api.getMessages()
                if (remoteMessages.isNotEmpty()) {
                    db.messageDao().insertMessages(remoteMessages.map { it.toEntity() })
                    pulledCount += remoteMessages.size
                }
            } catch (e: Exception) {
                Log.w(TAG, "Pull messages failed: ${e.message}")
            }

            try {
                val remoteCaretakerLinks = api.getCaretakerLinks()
                if (remoteCaretakerLinks.isNotEmpty()) {
                    db.caretakerDao().insertLinks(remoteCaretakerLinks.map { it.toEntity() })
                    pulledCount += remoteCaretakerLinks.size
                }
            } catch (e: Exception) {
                Log.w(TAG, "Pull caretaker links failed: ${e.message}")
            }

            try {
                val remoteDocs = api.getDocuments()
                if (remoteDocs.isNotEmpty()) {
                    db.documentDao().insertDocuments(remoteDocs.map { it.toEntity() })
                    pulledCount += remoteDocs.size
                }
            } catch (e: Exception) {
                Log.w(TAG, "Pull documents failed: ${e.message}")
            }

            try {
                val remoteNotifs = api.getNotifications()
                if (remoteNotifs.isNotEmpty()) {
                    db.notificationDao().insertNotifications(remoteNotifs.map { it.toEntity() })
                    pulledCount += remoteNotifs.size
                }
            } catch (e: Exception) {
                Log.w(TAG, "Pull notifications failed: ${e.message}")
            }

            try {
                val remoteAddresses = api.getPatientAddresses()
                if (remoteAddresses.isNotEmpty()) {
                    db.patientAddressDao().insertAddresses(remoteAddresses.map { it.toEntity() })
                    pulledCount += remoteAddresses.size
                }
            } catch (e: Exception) {
                Log.w(TAG, "Pull addresses failed: ${e.message}")
            }

            try {
                val remoteAssistance = api.getAssistanceRequests()
                if (remoteAssistance.isNotEmpty()) {
                    db.caretakerAssistanceDao().insertRequests(remoteAssistance.map { it.toEntity() })
                    pulledCount += remoteAssistance.size
                }
            } catch (e: Exception) {
                Log.w(TAG, "Pull assistance requests failed: ${e.message}")
            }

            try {
                val remoteWallets = api.getWallets()
                if (remoteWallets.isNotEmpty()) {
                    db.walletDao().insertWallets(remoteWallets.map { it.toEntity() })
                    pulledCount += remoteWallets.size
                }
            } catch (e: Exception) {
                Log.w(TAG, "Pull wallets failed: ${e.message}")
            }

            try {
                val remoteTxns = api.getWalletTransactions()
                if (remoteTxns.isNotEmpty()) {
                    db.walletTransactionDao().insertTransactions(remoteTxns.map { it.toEntity() })
                    pulledCount += remoteTxns.size
                }
            } catch (e: Exception) {
                Log.w(TAG, "Pull wallet txns failed: ${e.message}")
            }

            // ----------------------------------------------------
            // Step 2: PUSH local Room records up to Supabase (Upsert)
            // ----------------------------------------------------
            val localUsers = db.userDao().getAllUsersList()
            if (localUsers.isNotEmpty()) {
                val resp = api.upsertUsers(localUsers.map { it.toDto() })
                if (resp.isSuccessful) pushedCount += localUsers.size
            }

            val localMedicines = db.medicineDao().getAllMedicinesList()
            if (localMedicines.isNotEmpty()) {
                val resp = api.upsertMedicines(localMedicines.map { it.toDto() })
                if (resp.isSuccessful) pushedCount += localMedicines.size
            }

            val localReminders = db.reminderDao().getAllRemindersList()
            if (localReminders.isNotEmpty()) {
                val resp = api.upsertReminders(localReminders.map { it.toDto() })
                if (resp.isSuccessful) pushedCount += localReminders.size
            }

            val localHistory = db.historyDao().getAllHistoryList()
            if (localHistory.isNotEmpty()) {
                val resp = api.upsertHistories(localHistory.map { it.toDto() })
                if (resp.isSuccessful) pushedCount += localHistory.size
            }

            val localAppointments = db.appointmentDao().getAllAppointmentsList()
            if (localAppointments.isNotEmpty()) {
                val resp = api.upsertAppointments(localAppointments.map { it.toDto() })
                if (resp.isSuccessful) pushedCount += localAppointments.size
            }

            val localMessages = db.messageDao().getAllMessagesList()
            if (localMessages.isNotEmpty()) {
                val resp = api.upsertMessages(localMessages.map { it.toDto() })
                if (resp.isSuccessful) pushedCount += localMessages.size
            }

            val localCaretakerLinks = db.caretakerDao().getAllLinksList()
            if (localCaretakerLinks.isNotEmpty()) {
                val resp = api.upsertCaretakerLinks(localCaretakerLinks.map { it.toDto() })
                if (resp.isSuccessful) pushedCount += localCaretakerLinks.size
            }

            val localDocs = db.documentDao().getAllDocumentsList()
            if (localDocs.isNotEmpty()) {
                val resp = api.upsertDocuments(localDocs.map { it.toDto() })
                if (resp.isSuccessful) pushedCount += localDocs.size
            }

            val localNotifs = db.notificationDao().getAllNotificationsList()
            if (localNotifs.isNotEmpty()) {
                val resp = api.upsertNotifications(localNotifs.map { it.toDto() })
                if (resp.isSuccessful) pushedCount += localNotifs.size
            }

            val localAddresses = db.patientAddressDao().getAllAddressesList()
            if (localAddresses.isNotEmpty()) {
                val resp = api.upsertPatientAddresses(localAddresses.map { it.toDto() })
                if (resp.isSuccessful) pushedCount += localAddresses.size
            }

            val localAssistance = db.caretakerAssistanceDao().getAllRequestsList()
            if (localAssistance.isNotEmpty()) {
                val resp = api.upsertAssistanceRequests(localAssistance.map { it.toDto() })
                if (resp.isSuccessful) pushedCount += localAssistance.size
            }

            val localWallets = db.walletDao().getAllWalletsList()
            if (localWallets.isNotEmpty()) {
                val resp = api.upsertWallets(localWallets.map { it.toDto() })
                if (resp.isSuccessful) pushedCount += localWallets.size
            }

            val localTxns = db.walletTransactionDao().getAllTransactionsList()
            if (localTxns.isNotEmpty()) {
                val resp = api.upsertWalletTransactions(localTxns.map { it.toDto() })
                if (resp.isSuccessful) pushedCount += localTxns.size
            }

            val now = System.currentTimeMillis()
            config.setLastSyncTimestamp(now)

            SupabaseSyncResult(
                isSuccess = true,
                recordsPushed = pushedCount,
                recordsPulled = pulledCount,
                message = "Synced successfully ($pulledCount pulled, $pushedCount pushed)",
                timestamp = now
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            SupabaseSyncResult(
                isSuccess = false,
                message = "Sync failed: ${e.localizedMessage ?: e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    // ----------------------------------------------------
    // Granular Real-time Asynchronous Push Helpers
    // ----------------------------------------------------

    suspend fun pushUser(user: UserEntity) = withContext(Dispatchers.IO) {
        if (!config.isConfigured()) return@withContext
        try {
            client.getApiService().upsertUser(user.toDto())
        } catch (e: Exception) {
            Log.w(TAG, "Asynchronous push user failed: ${e.message}")
        }
    }

    suspend fun pushMedicine(medicine: MedicineEntity) = withContext(Dispatchers.IO) {
        if (!config.isConfigured()) return@withContext
        try {
            client.getApiService().upsertMedicine(medicine.toDto())
        } catch (e: Exception) {
            Log.w(TAG, "Asynchronous push medicine failed: ${e.message}")
        }
    }

    suspend fun deleteMedicine(medicineId: String) = withContext(Dispatchers.IO) {
        if (!config.isConfigured()) return@withContext
        try {
            client.getApiService().deleteMedicine("eq.$medicineId")
        } catch (e: Exception) {
            Log.w(TAG, "Asynchronous delete medicine failed: ${e.message}")
        }
    }

    suspend fun pushReminder(reminder: MedicineReminderEntity) = withContext(Dispatchers.IO) {
        if (!config.isConfigured()) return@withContext
        try {
            client.getApiService().upsertReminder(reminder.toDto())
        } catch (e: Exception) {
            Log.w(TAG, "Asynchronous push reminder failed: ${e.message}")
        }
    }

    suspend fun pushHistory(history: MedicineHistoryEntity) = withContext(Dispatchers.IO) {
        if (!config.isConfigured()) return@withContext
        try {
            client.getApiService().insertHistory(history.toDto())
        } catch (e: Exception) {
            Log.w(TAG, "Asynchronous push history failed: ${e.message}")
        }
    }

    suspend fun pushAppointment(appointment: AppointmentEntity) = withContext(Dispatchers.IO) {
        if (!config.isConfigured()) return@withContext
        try {
            client.getApiService().upsertAppointment(appointment.toDto())
        } catch (e: Exception) {
            Log.w(TAG, "Asynchronous push appointment failed: ${e.message}")
        }
    }

    suspend fun pushMessage(message: MessageEntity) = withContext(Dispatchers.IO) {
        if (!config.isConfigured()) return@withContext
        try {
            client.getApiService().insertMessage(message.toDto())
        } catch (e: Exception) {
            Log.w(TAG, "Asynchronous push message failed: ${e.message}")
        }
    }

    suspend fun pushCaretakerLink(link: CaretakerLinkEntity) = withContext(Dispatchers.IO) {
        if (!config.isConfigured()) return@withContext
        try {
            client.getApiService().upsertCaretakerLink(link.toDto())
        } catch (e: Exception) {
            Log.w(TAG, "Asynchronous push caretaker link failed: ${e.message}")
        }
    }

    suspend fun pushDocument(doc: MedicalDocumentEntity) = withContext(Dispatchers.IO) {
        if (!config.isConfigured()) return@withContext
        try {
            client.getApiService().upsertDocument(doc.toDto())
        } catch (e: Exception) {
            Log.w(TAG, "Asynchronous push document failed: ${e.message}")
        }
    }

    suspend fun pushNotification(notification: NotificationEntity) = withContext(Dispatchers.IO) {
        if (!config.isConfigured()) return@withContext
        try {
            client.getApiService().insertNotification(notification.toDto())
        } catch (e: Exception) {
            Log.w(TAG, "Asynchronous push notification failed: ${e.message}")
        }
    }

    suspend fun pushAuditLog(log: AuditLogEntity) = withContext(Dispatchers.IO) {
        if (!config.isConfigured()) return@withContext
        try {
            client.getApiService().insertAuditLog(log.toDto())
        } catch (e: Exception) {
            Log.w(TAG, "Asynchronous push audit log failed: ${e.message}")
        }
    }

    suspend fun pushPatientAddress(address: PatientAddressEntity) = withContext(Dispatchers.IO) {
        if (!config.isConfigured()) return@withContext
        try {
            client.getApiService().upsertPatientAddress(address.toDto())
        } catch (e: Exception) {
            Log.w(TAG, "Asynchronous push patient address failed: ${e.message}")
        }
    }

    suspend fun pushAssistanceRequest(request: CaretakerAssistanceRequestEntity) = withContext(Dispatchers.IO) {
        if (!config.isConfigured()) return@withContext
        try {
            client.getApiService().upsertAssistanceRequest(request.toDto())
        } catch (e: Exception) {
            Log.w(TAG, "Asynchronous push assistance request failed: ${e.message}")
        }
    }

    suspend fun pushWallet(wallet: WalletEntity) = withContext(Dispatchers.IO) {
        if (!config.isConfigured()) return@withContext
        try {
            client.getApiService().upsertWallet(wallet.toDto())
        } catch (e: Exception) {
            Log.w(TAG, "Asynchronous push wallet failed: ${e.message}")
        }
    }

    suspend fun pushWalletTransaction(txn: WalletTransactionEntity) = withContext(Dispatchers.IO) {
        if (!config.isConfigured()) return@withContext
        try {
            client.getApiService().insertWalletTransaction(txn.toDto())
        } catch (e: Exception) {
            Log.w(TAG, "Asynchronous push wallet txn failed: ${e.message}")
        }
    }
}
