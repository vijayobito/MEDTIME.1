package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppointmentEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.CaretakerLinkEntity
import com.example.data.model.MedicalDocumentEntity
import com.example.data.model.MedicineEntity
import com.example.data.model.MedicineHistoryEntity
import com.example.data.model.MedicineReminderEntity
import com.example.data.model.MessageEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:identifier) OR phone = :identifier LIMIT 1")
    suspend fun getUserByEmailOrPhone(identifier: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = 'DOCTOR'")
    fun getAllDoctors(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getMedicinesByPatient(patientId: String): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: String): MedicineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity)

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

    @Query("SELECT * FROM medicine_reminders WHERE id = :id")
    suspend fun getReminderById(id: String): MedicineReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<MedicineReminderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: MedicineReminderEntity)

    @Query("UPDATE medicine_reminders SET status = :status, takenAtTimestamp = :timestamp WHERE id = :id")
    suspend fun updateReminderStatus(id: String, status: String, timestamp: Long? = null)

    @Query("UPDATE medicine_reminders SET status = 'SNOOZED', snoozeUntilTime = :snoozeUntil WHERE id = :id")
    suspend fun snoozeReminder(id: String, snoozeUntil: String)

    @Query("SELECT * FROM medicine_reminders WHERE (status = 'PENDING' OR status = 'SNOOZED') AND scheduledDate = :date")
    suspend fun getPendingRemindersForDateDirect(date: String): List<MedicineReminderEntity>
}

@Dao
interface MedicineHistoryDao {
    @Query("SELECT * FROM medicine_history WHERE patientId = :patientId ORDER BY actionTimestamp DESC")
    fun getHistoryForPatient(patientId: String): Flow<List<MedicineHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: MedicineHistoryEntity)
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments WHERE patientId = :patientId ORDER BY appointmentDate ASC, appointmentTime ASC")
    fun getAppointmentsForPatient(patientId: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId ORDER BY appointmentDate ASC, appointmentTime ASC")
    fun getAppointmentsForDoctor(doctorId: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments ORDER BY appointmentDate DESC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId AND appointmentDate = :date AND appointmentTime = :time AND status != 'CANCELLED' LIMIT 1")
    suspend fun findExistingAppointment(doctorId: String, date: String, time: String): AppointmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :conversationId AND receiverId = :userId")
    suspend fun markConversationAsRead(conversationId: String, userId: String)
}

@Dao
interface CaretakerLinkDao {
    @Query("SELECT * FROM caretaker_links WHERE patientId = :patientId")
    fun getLinksForPatient(patientId: String): Flow<List<CaretakerLinkEntity>>

    @Query("SELECT * FROM caretaker_links WHERE caretakerId = :caretakerId")
    fun getLinksForCaretaker(caretakerId: String): Flow<List<CaretakerLinkEntity>>

    @Query("SELECT * FROM caretaker_links WHERE linkingCode = :code LIMIT 1")
    suspend fun findLinkByCode(code: String): CaretakerLinkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: CaretakerLinkEntity)

    @Query("UPDATE caretaker_links SET status = :status WHERE id = :id")
    suspend fun updateLinkStatus(id: String, status: String)

    @Query("UPDATE caretaker_links SET canViewMedicines = :medicines, canViewAdherence = :adherence, canViewAppointments = :appointments, canReceiveAlerts = :alerts WHERE id = :id")
    suspend fun updatePermissions(id: String, medicines: Boolean, adherence: Boolean, appointments: Boolean, alerts: Boolean)

    @Query("DELETE FROM caretaker_links WHERE id = :id")
    suspend fun deleteLink(id: String)
}

@Dao
interface MedicalDocumentDao {
    @Query("SELECT * FROM medical_documents WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getDocumentsForPatient(patientId: String): Flow<List<MedicalDocumentEntity>>

    @Query("SELECT * FROM medical_documents WHERE patientId = :patientId AND fileFormat = :format ORDER BY createdAt DESC")
    fun getDocumentsByFormat(patientId: String, format: String): Flow<List<MedicalDocumentEntity>>

    @Query("SELECT * FROM medical_documents WHERE patientId = :patientId AND isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteDocuments(patientId: String): Flow<List<MedicalDocumentEntity>>

    @Query("SELECT * FROM medical_documents WHERE patientId = :patientId AND (title LIKE '%' || :query || '%' OR doctorOrClinic LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    fun searchDocuments(patientId: String, query: String): Flow<List<MedicalDocumentEntity>>

    @Query("SELECT * FROM medical_documents WHERE id = :id LIMIT 1")
    fun getDocumentById(id: String): Flow<MedicalDocumentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: MedicalDocumentEntity)

    @Query("UPDATE medical_documents SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM medical_documents WHERE id = :id")
    suspend fun deleteDocument(id: String)

    @Query("SELECT COUNT(*) FROM medical_documents WHERE patientId = :patientId")
    fun getDocumentCount(patientId: String): Flow<Int>
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)
}
