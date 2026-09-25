package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AnnouncementEntity
import com.example.data.model.AppointmentEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.CaretakerAssistanceRequestEntity
import com.example.data.model.CaretakerLinkEntity
import com.example.data.model.MedicalDocumentEntity
import com.example.data.model.Medication
import com.example.data.model.MedicineEntity
import com.example.data.model.MedicineHistoryEntity
import com.example.data.model.MedicineReminderEntity
import com.example.data.model.MessageEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.PatientAddressEntity
import com.example.data.model.UserEntity
import com.example.data.model.WalletEntity
import com.example.data.model.WalletTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:identifier) OR phone = :identifier LIMIT 1")
    suspend fun getUserByEmailOrPhone(identifier: String): UserEntity?

    @Query("SELECT * FROM users WHERE authProvider = :provider AND providerUserId = :providerUserId LIMIT 1")
    suspend fun getUserByProvider(provider: String, providerUserId: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersList(): List<UserEntity>

    @Query("SELECT * FROM users WHERE role = 'DOCTOR'")
    fun getAllDoctors(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)
}

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getMedicinesByPatient(patientId: String): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines")
    suspend fun getAllMedicinesList(): List<MedicineEntity>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: String): MedicineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<MedicineEntity>)

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteMedicine(id: String)
}

@Dao
interface MedicineReminderDao {
    @Query("SELECT * FROM medicine_reminders WHERE patientId = :patientId AND scheduledDate = :date ORDER BY scheduledTime ASC")
    fun getRemindersForDate(patientId: String, date: String): Flow<List<MedicineReminderEntity>>

    @Query("SELECT * FROM medicine_reminders WHERE patientId = :patientId ORDER BY scheduledDate DESC, scheduledTime ASC")
    fun getAllRemindersForPatient(patientId: String): Flow<List<MedicineReminderEntity>>

    @Query("SELECT * FROM medicine_reminders")
    suspend fun getAllRemindersList(): List<MedicineReminderEntity>

    @Query("SELECT * FROM medicine_reminders WHERE id = :id")
    suspend fun getReminderById(id: String): MedicineReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<MedicineReminderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: MedicineReminderEntity)

    @Query("UPDATE medicine_reminders SET status = :status, takenAtTimestamp = :timestamp WHERE id = :id")
    suspend fun updateReminderStatus(id: String, status: String, timestamp: Long? = null)

    @Query("UPDATE medicine_reminders SET status = 'SNOOZED', snoozeUntilTime = :snoozeUntil, snoozeCount = snoozeCount + 1, lastAlarmTimestamp = :timestamp WHERE id = :id")
    suspend fun snoozeReminderWithCount(id: String, snoozeUntil: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE medicine_reminders SET status = 'SNOOZED', snoozeUntilTime = :snoozeUntil WHERE id = :id")
    suspend fun snoozeReminder(id: String, snoozeUntil: String)

    @Query("UPDATE medicine_reminders SET status = 'SKIPPED', skipReason = :reason, takenAtTimestamp = :timestamp WHERE id = :id")
    suspend fun skipReminderWithReason(id: String, reason: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE medicine_reminders SET attemptCount = :attempts, lastAlarmTimestamp = :timestamp WHERE id = :id")
    suspend fun updateAttemptCount(id: String, attempts: Int, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM medicine_reminders WHERE (status = 'PENDING' OR status = 'SNOOZED') AND scheduledDate = :date")
    suspend fun getPendingRemindersForDateDirect(date: String): List<MedicineReminderEntity>

    @Query("SELECT * FROM medicine_reminders WHERE status = 'PENDING' OR status = 'SNOOZED'")
    suspend fun getAllActivePendingRemindersDirect(): List<MedicineReminderEntity>

    @Query("SELECT * FROM medicine_reminders WHERE medicineId = :medicineId")
    suspend fun getRemindersByMedicineId(medicineId: String): List<MedicineReminderEntity>

    @Query("DELETE FROM medicine_reminders WHERE medicineId = :medicineId")
    suspend fun deleteRemindersByMedicineId(medicineId: String)
}

@Dao
interface MedicineHistoryDao {
    @Query("SELECT * FROM medicine_history WHERE patientId = :patientId ORDER BY actionTimestamp DESC")
    fun getHistoryForPatient(patientId: String): Flow<List<MedicineHistoryEntity>>

    @Query("SELECT * FROM medicine_history")
    suspend fun getAllHistoryList(): List<MedicineHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: MedicineHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistories(historyList: List<MedicineHistoryEntity>)
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments WHERE patientId = :patientId ORDER BY appointmentDate ASC, appointmentTime ASC")
    fun getAppointmentsForPatient(patientId: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId ORDER BY appointmentDate ASC, appointmentTime ASC")
    fun getAppointmentsForDoctor(doctorId: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments ORDER BY appointmentDate DESC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments")
    suspend fun getAllAppointmentsList(): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId AND appointmentDate = :date AND appointmentTime = :time AND status != 'CANCELLED' LIMIT 1")
    suspend fun findExistingAppointment(doctorId: String, date: String, time: String): AppointmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<AppointmentEntity>)

    @Query("UPDATE appointments SET status = :status, doctorNotes = :notes WHERE id = :id")
    suspend fun updateAppointmentStatus(id: String, status: String, notes: String = "")

    @Query("UPDATE appointments SET reminderEnabled = :enabled WHERE id = :id")
    suspend fun updateAppointmentReminder(id: String, enabled: Boolean)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY timestamp ASC")
    fun getMessagesBetweenUsers(userId1: String, userId2: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getAllMessagesForUser(userId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages")
    suspend fun getAllMessagesList(): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :conversationId AND receiverId = :userId")
    suspend fun markConversationAsRead(conversationId: String, userId: String)
}

@Dao
interface CaretakerLinkDao {
    @Query("SELECT * FROM caretaker_links ORDER BY createdAt DESC")
    fun getAllLinks(): Flow<List<CaretakerLinkEntity>>

    @Query("SELECT * FROM caretaker_links WHERE patientId = :patientId")
    fun getLinksForPatient(patientId: String): Flow<List<CaretakerLinkEntity>>

    @Query("SELECT * FROM caretaker_links WHERE caretakerId = :caretakerId")
    fun getLinksForCaretaker(caretakerId: String): Flow<List<CaretakerLinkEntity>>

    @Query("SELECT * FROM caretaker_links")
    suspend fun getAllLinksList(): List<CaretakerLinkEntity>

    @Query("SELECT * FROM caretaker_links WHERE linkingCode = :code LIMIT 1")
    suspend fun findLinkByCode(code: String): CaretakerLinkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: CaretakerLinkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinks(links: List<CaretakerLinkEntity>)

    @Query("UPDATE caretaker_links SET status = :status WHERE id = :id")
    suspend fun updateLinkStatus(id: String, status: String)

    @Query("UPDATE caretaker_links SET canViewMedicines = :medicines, canViewAdherence = :adherence, canViewAppointments = :appointments, canReceiveAlerts = :alerts WHERE id = :id")
    suspend fun updatePermissions(id: String, medicines: Boolean, adherence: Boolean, appointments: Boolean, alerts: Boolean)

    @Query("DELETE FROM caretaker_links WHERE id = :id")
    suspend fun deleteLink(id: String)
}

@Dao
interface MedicalDocumentDao {
    @Query("SELECT * FROM medical_documents WHERE patientId = :patientId AND isRecycleBin = 0 ORDER BY createdAt DESC")
    fun getDocumentsForPatient(patientId: String): Flow<List<MedicalDocumentEntity>>

    @Query("SELECT * FROM medical_documents WHERE patientId = :patientId AND isRecycleBin = 1 ORDER BY deletedAt DESC")
    fun getRecycleBinDocuments(patientId: String): Flow<List<MedicalDocumentEntity>>

    @Query("SELECT * FROM medical_documents")
    suspend fun getAllDocumentsList(): List<MedicalDocumentEntity>

    @Query("SELECT * FROM medical_documents WHERE patientId = :patientId AND fileFormat = :format AND isRecycleBin = 0 ORDER BY createdAt DESC")
    fun getDocumentsByFormat(patientId: String, format: String): Flow<List<MedicalDocumentEntity>>

    @Query("SELECT * FROM medical_documents WHERE patientId = :patientId AND isFavorite = 1 AND isRecycleBin = 0 ORDER BY createdAt DESC")
    fun getFavoriteDocuments(patientId: String): Flow<List<MedicalDocumentEntity>>

    @Query("SELECT * FROM medical_documents WHERE patientId = :patientId AND isRecycleBin = 0 AND (title LIKE '%' || :query || '%' OR doctorOrClinic LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    fun searchDocuments(patientId: String, query: String): Flow<List<MedicalDocumentEntity>>

    @Query("SELECT * FROM medical_documents WHERE id = :id LIMIT 1")
    fun getDocumentById(id: String): Flow<MedicalDocumentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: MedicalDocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<MedicalDocumentEntity>)

    @Query("UPDATE medical_documents SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE medical_documents SET isRecycleBin = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun moveToRecycleBin(id: String, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE medical_documents SET isRecycleBin = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restoreFromRecycleBin(id: String)

    @Query("DELETE FROM medical_documents WHERE id = :id")
    suspend fun deleteDocument(id: String)

    @Query("DELETE FROM medical_documents WHERE patientId = :patientId AND isRecycleBin = 1")
    suspend fun emptyRecycleBin(patientId: String)

    @Query("SELECT COUNT(*) FROM medical_documents WHERE patientId = :patientId AND isRecycleBin = 0")
    fun getDocumentCount(patientId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM medical_documents WHERE patientId = :patientId AND isRecycleBin = 1")
    fun getRecycleBinCount(patientId: String): Flow<Int>
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND (type = 'CARETAKER' OR type = 'MISSED_DOSE' OR type = 'EMERGENCY_SOS' OR recipientRole = 'CARETAKER' OR recipientRole = 'PATIENT_AND_CARETAKER') ORDER BY timestamp DESC")
    fun getCaretakerAlertsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND type = :type ORDER BY timestamp DESC")
    fun getNotificationsByType(userId: String, type: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications")
    suspend fun getAllNotificationsList(): List<NotificationEntity>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 0 WHERE id = :id")
    suspend fun markAsUnread(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs")
    suspend fun getAllLogsList(): List<AuditLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<AuditLogEntity>)

    @Query("DELETE FROM audit_logs")
    suspend fun clearAllLogs()
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY timestamp DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements WHERE isActive = 1 AND (targetRole = 'ALL' OR targetRole = :role) ORDER BY timestamp DESC")
    fun getActiveAnnouncementsForRole(role: String): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements")
    suspend fun getAllAnnouncementsList(): List<AnnouncementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<AnnouncementEntity>)

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncement(id: String)
}

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY createdAt DESC")
    fun getAllMedications(): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    suspend fun getMedicationById(id: String): Medication?

    @Query("SELECT * FROM medications WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getMedicationsByPatient(patientId: String): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveMedications(): Flow<List<Medication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<Medication>)

    @Update
    suspend fun updateMedication(medication: Medication)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedication(id: String)
}

@Dao
interface CaretakerAssistanceDao {
    @Query("SELECT * FROM caretaker_assistance_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<CaretakerAssistanceRequestEntity>>

    @Query("SELECT * FROM caretaker_assistance_requests")
    suspend fun getAllRequestsList(): List<CaretakerAssistanceRequestEntity>

    @Query("SELECT * FROM caretaker_assistance_requests WHERE caretakerId = :caretakerId ORDER BY createdAt DESC")
    fun getRequestsForCaretaker(caretakerId: String): Flow<List<CaretakerAssistanceRequestEntity>>

    @Query("SELECT * FROM caretaker_assistance_requests WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getRequestsForPatient(patientId: String): Flow<List<CaretakerAssistanceRequestEntity>>

    @Query("SELECT * FROM caretaker_assistance_requests WHERE id = :id LIMIT 1")
    fun getRequestById(id: String): Flow<CaretakerAssistanceRequestEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: CaretakerAssistanceRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<CaretakerAssistanceRequestEntity>)

    @Query("UPDATE caretaker_assistance_requests SET status = :status, adminNotes = :adminNotes, rejectionReason = :rejectionReason, adminId = :adminId, adminName = :adminName, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(
        id: String,
        status: String,
        adminNotes: String,
        rejectionReason: String,
        adminId: String,
        adminName: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE caretaker_assistance_requests SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCaretakerProgress(
        id: String,
        status: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE caretaker_assistance_requests SET caretakerLatitude = :lat, caretakerLongitude = :lon, caretakerLocationName = :locName, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCaretakerLocation(
        id: String,
        lat: Double?,
        lon: Double?,
        locName: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM caretaker_assistance_requests WHERE id = :id")
    suspend fun deleteRequest(id: String)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets WHERE caretakerId = :caretakerId LIMIT 1")
    fun getWalletFlow(caretakerId: String): Flow<WalletEntity?>

    @Query("SELECT * FROM wallets WHERE caretakerId = :caretakerId LIMIT 1")
    suspend fun getWallet(caretakerId: String): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWallet(wallet: WalletEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallets(wallets: List<WalletEntity>)

    @Query("SELECT * FROM wallets")
    suspend fun getAllWalletsList(): List<WalletEntity>

    @Query("UPDATE wallets SET balance = :newBalance, updatedAt = :updatedAt WHERE caretakerId = :caretakerId")
    suspend fun updateBalance(caretakerId: String, newBalance: Double, updatedAt: Long = System.currentTimeMillis())
}

@Dao
interface WalletTransactionDao {
    @Query("SELECT * FROM wallet_transactions WHERE caretakerId = :caretakerId ORDER BY createdAt DESC")
    fun getTransactionsForCaretaker(caretakerId: String): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions ORDER BY createdAt DESC")
    suspend fun getAllTransactionsList(): List<WalletTransactionEntity>

    @Query("SELECT * FROM wallet_transactions WHERE transactionId = :id LIMIT 1")
    suspend fun getTransactionById(id: String): WalletTransactionEntity?

    @Query("SELECT * FROM wallet_transactions WHERE relatedRequestId = :requestId LIMIT 1")
    suspend fun getTransactionByRequestId(requestId: String): WalletTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<WalletTransactionEntity>)

    @Query("UPDATE wallet_transactions SET status = :status WHERE transactionId = :id")
    suspend fun updateStatus(id: String, status: String)
}

@Dao
interface PatientAddressDao {
    @Query("SELECT * FROM patient_saved_addresses WHERE patientId = :patientId ORDER BY isDefault DESC, createdAt ASC")
    fun getAddressesForPatient(patientId: String): Flow<List<PatientAddressEntity>>

    @Query("SELECT * FROM patient_saved_addresses WHERE patientId = :patientId AND allowCaretakerVisit = 1 ORDER BY isDefault DESC, createdAt ASC")
    fun getAuthorizedAddressesForCaretaker(patientId: String): Flow<List<PatientAddressEntity>>

    @Query("SELECT * FROM patient_saved_addresses")
    suspend fun getAllAddressesList(): List<PatientAddressEntity>

    @Query("SELECT * FROM patient_saved_addresses WHERE patientId = :patientId")
    suspend fun getAddressesListForPatient(patientId: String): List<PatientAddressEntity>

    @Query("SELECT * FROM patient_saved_addresses WHERE id = :id LIMIT 1")
    fun getAddressById(id: String): Flow<PatientAddressEntity?>

    @Query("SELECT * FROM patient_saved_addresses WHERE id = :id LIMIT 1")
    suspend fun getAddressByIdDirect(id: String): PatientAddressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: PatientAddressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddresses(addresses: List<PatientAddressEntity>)

    @Update
    suspend fun updateAddress(address: PatientAddressEntity)

    @Query("DELETE FROM patient_saved_addresses WHERE id = :id")
    suspend fun deleteAddress(id: String)
}



