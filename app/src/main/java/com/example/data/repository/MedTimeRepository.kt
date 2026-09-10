package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MedTimeRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val userDao = db.userDao()
    private val medicineDao = db.medicineDao()
    private val reminderDao = db.reminderDao()
    private val historyDao = db.historyDao()
    private val appointmentDao = db.appointmentDao()
    private val messageDao = db.messageDao()
    private val caretakerDao = db.caretakerDao()
    private val documentDao = db.documentDao()
    private val notificationDao = db.notificationDao()
    private val auditLogDao = db.auditLogDao()

    // ----------------------------------------------------
    // User / Auth
    // ----------------------------------------------------
    fun getUser(userId: String): Flow<UserEntity?> = userDao.getUserById(userId)
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()
    fun getAllDoctors(): Flow<List<UserEntity>> = userDao.getAllDoctors()

    suspend fun findUserByEmailOrPhone(query: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByEmailOrPhone(query.trim())
    }

    suspend fun registerUser(
        name: String,
        email: String,
        role: String,
        phone: String,
        password: String = "password123",
        dob: String = "1985-05-12",
        specialty: String = "",
        hospital: String = "",
        license: String = ""
    ): UserEntity = withContext(Dispatchers.IO) {
        val linkingCode = "MED-" + (1000..9999).random()
        val isDoctor = role == "DOCTOR"
        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            role = role,
            phone = phone,
            password = password,
            dateOfBirth = dob,
            isDoctorVerified = !isDoctor,
            doctorVerificationStatus = if (isDoctor) "PENDING" else "APPROVED",
            doctorSpecialty = specialty,
            doctorHospital = hospital,
            doctorLicense = license,
            caretakerLinkingCode = linkingCode
        )
        userDao.insertUser(user)
        logAudit(
            action = "USER_REGISTER",
            performedBy = user.name,
            target = "User",
            details = "Registered user with role ${user.role} and email $email"
        )

        // Seed initial starter medicine reminders for new patients
        if (role == "PATIENT") {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val med1 = MedicineEntity(
                id = UUID.randomUUID().toString(),
                patientId = user.id,
                name = "Omega-3 Fish Oil",
                dosage = "1000 mg",
                form = "Capsule",
                instructions = "After lunch",
                frequency = "Once daily",
                reminderTimes = "01:30 PM",
                startDate = today,
                endDate = "2026-12-31",
                stockQuantity = 30,
                notes = "Promotes cardiovascular and joint health."
            )
            val med2 = MedicineEntity(
                id = UUID.randomUUID().toString(),
                patientId = user.id,
                name = "Vitamin D3",
                dosage = "2000 IU",
                form = "Tablet",
                instructions = "Morning with water",
                frequency = "Once daily",
                reminderTimes = "08:30 AM",
                startDate = today,
                endDate = "2026-12-31",
                stockQuantity = 60,
                notes = "Bone health support."
            )
            medicineDao.insertMedicine(med1)
            medicineDao.insertMedicine(med2)

            val rem1 = MedicineReminderEntity(
                id = UUID.randomUUID().toString(),
                medicineId = med1.id,
                patientId = user.id,
                medicineName = med1.name,
                dosage = med1.dosage,
                form = med1.form,
                instructions = med1.instructions,
                scheduledDate = today,
                scheduledTime = "01:30 PM",
                status = "PENDING"
            )
            val rem2 = MedicineReminderEntity(
                id = UUID.randomUUID().toString(),
                medicineId = med2.id,
                patientId = user.id,
                medicineName = med2.name,
                dosage = med2.dosage,
                form = med2.form,
                instructions = med2.instructions,
                scheduledDate = today,
                scheduledTime = "08:30 AM",
                status = "PENDING"
            )
            reminderDao.insertReminder(rem1)
            reminderDao.insertReminder(rem2)

            notificationDao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    title = "Welcome to MedTime",
                    message = "Your account has been configured. Start tracking your daily medication schedule now!",
                    type = "WELCOME"
                )
            )
        }

        user
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
        logAudit(
            action = "USER_UPDATE",
            performedBy = user.name,
            target = "User",
            details = "Updated user profile details"
        )
    }

    // ----------------------------------------------------
    // Medicines
    // ----------------------------------------------------
    fun getMedicines(patientId: String): Flow<List<MedicineEntity>> = medicineDao.getMedicinesByPatient(patientId)

    suspend fun addMedicine(
        patientId: String,
        name: String,
        dosage: String,
        form: String,
        instructions: String,
        frequency: String,
        reminderTimes: String,
        startDate: String,
        endDate: String,
        stock: Int,
        notes: String,
        colorHex: Long
    ): MedicineEntity = withContext(Dispatchers.IO) {
        val medId = UUID.randomUUID().toString()
        val med = MedicineEntity(
            id = medId,
            patientId = patientId,
            name = name,
            dosage = dosage,
            form = form,
            instructions = instructions,
            frequency = frequency,
            reminderTimes = reminderTimes,
            startDate = startDate,
            endDate = endDate,
            stockQuantity = stock,
            notes = notes,
            colorHex = colorHex
        )
        medicineDao.insertMedicine(med)

        // Automatically generate today's reminders for this new medicine
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val times = reminderTimes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val reminders = times.map { time ->
            MedicineReminderEntity(
                id = UUID.randomUUID().toString(),
                medicineId = medId,
                patientId = patientId,
                medicineName = name,
                dosage = dosage,
                form = form,
                instructions = instructions,
                scheduledDate = today,
                scheduledTime = time,
                status = "PENDING"
            )
        }
        if (reminders.isNotEmpty()) {
            reminderDao.insertReminders(reminders)
        }

        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = patientId,
                title = "New Medicine Added",
                message = "Scheduled $name ($dosage) with reminders at $reminderTimes.",
                type = "MEDICINE"
            )
        )

        logAudit(
            action = "MEDICINE_CREATE",
            performedBy = patientId,
            target = "Medicine",
            details = "Added medicine $name ($dosage) with frequency $frequency"
        )
        med
    }

    suspend fun updateMedicine(medicine: MedicineEntity) = withContext(Dispatchers.IO) {
        medicineDao.updateMedicine(medicine)
        logAudit(
            action = "MEDICINE_UPDATE",
            performedBy = medicine.patientId,
            target = "Medicine",
            details = "Updated medicine ${medicine.name}"
        )
    }

    suspend fun deleteMedicine(medicine: MedicineEntity) = withContext(Dispatchers.IO) {
        medicineDao.deleteMedicine(medicine.id)
        logAudit(
            action = "MEDICINE_DELETE",
            performedBy = medicine.patientId,
            target = "Medicine",
            details = "Removed medicine ${medicine.name}"
        )
    }

    // ----------------------------------------------------
    // Reminders & Adherence Tracking
    // ----------------------------------------------------
    fun getTodayReminders(patientId: String): Flow<List<MedicineReminderEntity>> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return reminderDao.getRemindersForDate(patientId, today)
    }

    fun getAllReminders(patientId: String): Flow<List<MedicineReminderEntity>> =
        reminderDao.getAllRemindersForPatient(patientId)

    fun getHistory(patientId: String): Flow<List<MedicineHistoryEntity>> =
        historyDao.getHistoryForPatient(patientId)

    suspend fun markReminderTaken(reminder: MedicineReminderEntity) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        reminderDao.updateReminderStatus(reminder.id, "TAKEN", now)

        historyDao.insertHistory(
            MedicineHistoryEntity(
                id = UUID.randomUUID().toString(),
                medicineId = reminder.medicineId,
                patientId = reminder.patientId,
                medicineName = reminder.medicineName,
                dosage = reminder.dosage,
                scheduledTime = reminder.scheduledTime,
                action = "TAKEN",
                actionTimestamp = now,
                notes = "Taken on time via MedTime Smart Tracker"
            )
        )

        // Decrement stock if possible
        val med = medicineDao.getMedicineById(reminder.medicineId)
        if (med != null && med.stockQuantity > 0) {
            val updated = med.copy(stockQuantity = med.stockQuantity - 1)
            medicineDao.updateMedicine(updated)
            if (updated.stockQuantity <= 5) {
                notificationDao.insertNotification(
                    NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        userId = reminder.patientId,
                        title = "Low Medication Stock",
                        message = "Only ${updated.stockQuantity} doses of ${med.name} left. Refill soon.",
                        type = "MEDICINE"
                    )
                )
            }
        }

        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = reminder.patientId,
                title = "Dose Recorded",
                message = "Recorded ${reminder.medicineName} (${reminder.dosage}) as taken at ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())}.",
                type = "MEDICINE"
            )
        )

        logAudit(
            action = "REMINDER_TAKEN",
            performedBy = reminder.patientId,
            target = "Reminder",
            details = "Patient marked ${reminder.medicineName} dose as taken"
        )
    }

    suspend fun snoozeReminder(reminder: MedicineReminderEntity, minutes: Int = 15) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val snoozeTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(now + minutes * 60 * 1000L))
        reminderDao.snoozeReminder(reminder.id, snoozeTime)

        historyDao.insertHistory(
            MedicineHistoryEntity(
                id = UUID.randomUUID().toString(),
                medicineId = reminder.medicineId,
                patientId = reminder.patientId,
                medicineName = reminder.medicineName,
                dosage = reminder.dosage,
                scheduledTime = reminder.scheduledTime,
                action = "SNOOZED",
                actionTimestamp = now,
                notes = "Snoozed for $minutes minutes until $snoozeTime"
            )
        )

        logAudit(
            action = "REMINDER_SNOOZE",
            performedBy = reminder.patientId,
            target = "Reminder",
            details = "Snoozed ${reminder.medicineName} until $snoozeTime"
        )
    }

    suspend fun skipReminder(reminder: MedicineReminderEntity, reason: String = "Patient skipped") = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        reminderDao.updateReminderStatus(reminder.id, "SKIPPED", now)

        historyDao.insertHistory(
            MedicineHistoryEntity(
                id = UUID.randomUUID().toString(),
                medicineId = reminder.medicineId,
                patientId = reminder.patientId,
                medicineName = reminder.medicineName,
                dosage = reminder.dosage,
                scheduledTime = reminder.scheduledTime,
                action = "SKIPPED",
                actionTimestamp = now,
                notes = reason
            )
        )

        logAudit(
            action = "REMINDER_SKIP",
            performedBy = reminder.patientId,
            target = "Reminder",
            details = "Patient skipped ${reminder.medicineName} ($reason)"
        )
    }

    // ----------------------------------------------------
    // Appointments
    // ----------------------------------------------------
    fun getAppointmentsForPatient(patientId: String): Flow<List<AppointmentEntity>> =
        appointmentDao.getAppointmentsForPatient(patientId)

    fun getAppointmentsForDoctor(doctorId: String): Flow<List<AppointmentEntity>> =
        appointmentDao.getAppointmentsForDoctor(doctorId)

    fun getAllAppointments(): Flow<List<AppointmentEntity>> =
        appointmentDao.getAllAppointments()

    suspend fun bookAppointment(
        patientId: String,
        patientName: String,
        doctorId: String,
        doctorName: String,
        doctorSpecialty: String,
        date: String,
        time: String,
        reason: String,
        reminderEnabled: Boolean = true,
        reminderMinutesBefore: Int = 60
    ): Result<AppointmentEntity> = withContext(Dispatchers.IO) {
        // Double booking check!
        val conflict = appointmentDao.findExistingAppointment(doctorId, date, time)
        if (conflict != null) {
            return@withContext Result.failure(Exception("This slot ($date at $time) is already reserved with $doctorName. Please choose another time."))
        }

        val appt = AppointmentEntity(
            id = UUID.randomUUID().toString(),
            patientId = patientId,
            patientName = patientName,
            doctorId = doctorId,
            doctorName = doctorName,
            doctorSpecialty = doctorSpecialty,
            appointmentDate = date,
            appointmentTime = time,
            reason = reason,
            status = "PENDING",
            reminderEnabled = reminderEnabled,
            reminderMinutesBefore = reminderMinutesBefore
        )
        appointmentDao.insertAppointment(appt)

        // Notify Doctor
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = doctorId,
                title = "New Appointment Request",
                message = "$patientName requested a consultation on $date at $time.",
                type = "APPOINTMENT"
            )
        )

        // Notify Patient
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = patientId,
                title = "Appointment Requested",
                message = "Consultation requested with $doctorName on $date at $time.",
                type = "APPOINTMENT"
            )
        )

        logAudit(
            action = "APPOINTMENT_BOOK",
            performedBy = patientName,
            target = "Appointment",
            details = "Booked consultation with $doctorName for $date $time (Reminder: $reminderEnabled)"
        )
        Result.success(appt)
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: String, doctorNotes: String = "") = withContext(Dispatchers.IO) {
        appointmentDao.updateAppointmentStatus(appointmentId, status, doctorNotes)
        logAudit(
            action = "APPOINTMENT_STATUS_CHANGE",
            performedBy = "Doctor/System",
            target = "Appointment",
            details = "Changed appointment $appointmentId status to $status"
        )
    }

    suspend fun updateAppointmentReminder(appointmentId: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        appointmentDao.updateAppointmentReminder(appointmentId, enabled)
        logAudit(
            action = "APPOINTMENT_REMINDER_TOGGLE",
            performedBy = "Patient",
            target = "Appointment",
            details = "Toggled reminder for appointment $appointmentId to $enabled"
        )
    }

    // ----------------------------------------------------
    // Messages
    // ----------------------------------------------------
    fun getMessages(userId1: String, userId2: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesBetweenUsers(userId1, userId2)

    suspend fun sendMessage(
        conversationId: String,
        sender: UserEntity,
        receiverId: String,
        receiverName: String,
        content: String
    ): MessageEntity = withContext(Dispatchers.IO) {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = sender.id,
            senderName = sender.name,
            senderRole = sender.role,
            receiverId = receiverId,
            receiverName = receiverName,
            content = content
        )
        messageDao.insertMessage(message)

        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = receiverId,
                title = "New message from ${sender.name}",
                message = content.take(60),
                type = "DOCTOR"
            )
        )
        message
    }

    // ----------------------------------------------------
    // Caretakers
    // ----------------------------------------------------
    fun getCaretakerLinksForPatient(patientId: String): Flow<List<CaretakerLinkEntity>> =
        caretakerDao.getLinksForPatient(patientId)

    fun getCaretakerLinksForCaretaker(caretakerId: String): Flow<List<CaretakerLinkEntity>> =
        caretakerDao.getLinksForCaretaker(caretakerId)

    suspend fun requestCaretakerLink(
        caretaker: UserEntity,
        linkingCode: String
    ): Result<CaretakerLinkEntity> = withContext(Dispatchers.IO) {
        // Find patient by linking code
        val allUsers = userDao.getAllUsers().firstOrNull() ?: emptyList()
        val targetPatient = allUsers.find { it.role == "PATIENT" && it.caretakerLinkingCode.equals(linkingCode.trim(), ignoreCase = true) }
            ?: return@withContext Result.failure(Exception("No patient found with code '$linkingCode'. Verify the code on the patient's Caretaker screen."))

        val link = CaretakerLinkEntity(
            id = UUID.randomUUID().toString(),
            patientId = targetPatient.id,
            patientName = targetPatient.name,
            caretakerId = caretaker.id,
            caretakerName = caretaker.name,
            linkingCode = linkingCode,
            status = "PENDING"
        )
        caretakerDao.insertLink(link)

        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = targetPatient.id,
                title = "Caretaker Link Request",
                message = "${caretaker.name} requested to link as your healthcare caretaker.",
                type = "CARETAKER"
            )
        )

        logAudit(
            action = "CARETAKER_LINK_REQUEST",
            performedBy = caretaker.name,
            target = "CaretakerLink",
            details = "Requested link to patient ${targetPatient.name}"
        )
        Result.success(link)
    }

    suspend fun updateCaretakerStatus(linkId: String, status: String) = withContext(Dispatchers.IO) {
        caretakerDao.updateLinkStatus(linkId, status)
        logAudit(
            action = "CARETAKER_STATUS_UPDATE",
            performedBy = "Patient",
            target = "CaretakerLink",
            details = "Updated caretaker access status to $status"
        )
    }

    suspend fun updateCaretakerPermissions(
        linkId: String,
        medicines: Boolean,
        adherence: Boolean,
        appointments: Boolean,
        alerts: Boolean
    ) = withContext(Dispatchers.IO) {
        caretakerDao.updatePermissions(linkId, medicines, adherence, appointments, alerts)
    }

    // ----------------------------------------------------
    // Documents
    // ----------------------------------------------------
    fun getDocuments(patientId: String): Flow<List<MedicalDocumentEntity>> =
        documentDao.getDocumentsForPatient(patientId)

    fun getDocumentsByFormat(patientId: String, format: String): Flow<List<MedicalDocumentEntity>> =
        documentDao.getDocumentsByFormat(patientId, format)

    fun getFavoriteDocuments(patientId: String): Flow<List<MedicalDocumentEntity>> =
        documentDao.getFavoriteDocuments(patientId)

    fun searchDocuments(patientId: String, query: String): Flow<List<MedicalDocumentEntity>> =
        documentDao.searchDocuments(patientId, query)

    suspend fun addDocument(
        patientId: String,
        title: String,
        type: String,
        doctorOrClinic: String,
        date: String,
        fileSize: String,
        notes: String,
        fileName: String = "",
        fileFormat: String = "PDF",
        mimeType: String = "application/pdf",
        fileUri: String = "",
        fileSizeBytes: Long = 0L,
        pageCount: Int = 1,
        resolution: String = "",
        tags: String = "",
        isFavorite: Boolean = false
    ): MedicalDocumentEntity = withContext(Dispatchers.IO) {
        val safeFileName = fileName.ifBlank {
            val ext = if (fileFormat.equals("IMAGE", ignoreCase = true)) "jpg" else "pdf"
            val sanitized = title.lowercase().replace("[^a-z0-9]+".toRegex(), "_")
            "${sanitized}_${System.currentTimeMillis()}.$ext"
        }
        val safeMimeType = if (mimeType.isNotBlank()) mimeType else {
            if (fileFormat.equals("IMAGE", ignoreCase = true)) "image/jpeg" else "application/pdf"
        }
        val doc = MedicalDocumentEntity(
            id = UUID.randomUUID().toString(),
            patientId = patientId,
            title = title,
            type = type,
            doctorOrClinic = doctorOrClinic,
            dateAdded = date,
            fileSize = fileSize,
            fileSizeBytes = if (fileSizeBytes > 0L) fileSizeBytes else 1024L * 1024L,
            fileName = safeFileName,
            fileFormat = if (fileFormat.equals("IMAGE", ignoreCase = true)) "IMAGE" else "PDF",
            mimeType = safeMimeType,
            fileUri = fileUri,
            pageCount = pageCount,
            resolution = resolution,
            tags = tags,
            isFavorite = isFavorite,
            uploadStatus = "COMPLETED",
            notes = notes
        )
        documentDao.insertDocument(doc)
        logAudit(
            action = "DOCUMENT_UPLOAD",
            performedBy = patientId,
            target = "MedicalDocument",
            details = "Uploaded $fileFormat document '$title' ($type, $safeFileName)"
        )
        doc
    }

    suspend fun toggleDocumentFavorite(docId: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        documentDao.toggleFavorite(docId, isFavorite)
    }

    suspend fun deleteDocument(docId: String) = withContext(Dispatchers.IO) {
        documentDao.deleteDocument(docId)
    }

    // ----------------------------------------------------
    // Notifications
    // ----------------------------------------------------
    fun getNotifications(userId: String): Flow<List<NotificationEntity>> =
        notificationDao.getNotificationsForUser(userId)

    fun getUnreadNotificationCount(userId: String): Flow<Int> =
        notificationDao.getUnreadCount(userId)

    suspend fun markNotificationRead(id: String) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsRead(userId: String) = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead(userId)
    }

    // ----------------------------------------------------
    // Admin & Audit
    // ----------------------------------------------------
    fun getAuditLogs(): Flow<List<AuditLogEntity>> = auditLogDao.getAllLogs()

    suspend fun verifyDoctor(doctorId: String, isApproved: Boolean, adminName: String) = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(doctorId).firstOrNull()
        if (user != null) {
            val status = if (isApproved) "APPROVED" else "REJECTED"
            val updated = user.copy(
                isDoctorVerified = isApproved,
                doctorVerificationStatus = status
            )
            userDao.updateUser(updated)

            notificationDao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = doctorId,
                    title = "Medical License Verification: $status",
                    message = if (isApproved) "Your credentials have been verified by administration. You can now conduct patient consultations." else "Your credentials were not approved. Please contact support.",
                    type = "DOCTOR"
                )
            )

            logAudit(
                action = "DOCTOR_VERIFICATION",
                performedBy = adminName,
                target = "DoctorVerification",
                details = "$adminName set verification for ${user.name} to $status"
            )
        }
    }

    suspend fun logAudit(action: String, performedBy: String, target: String, details: String) {
        auditLogDao.insertLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                action = action,
                performedBy = performedBy,
                targetResource = target,
                details = details,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
