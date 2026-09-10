package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.MedTimeRepository
import com.example.service.GeminiAiService
import com.example.worker.AppointmentReminderScheduler
import com.example.worker.DailyMedicationSyncWorker
import com.example.worker.MedicalReminderScheduler
import com.example.worker.MedicalReminderWorker
import com.example.worker.MedicationReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardStats(
    val totalScheduled: Int = 0,
    val takenCount: Int = 0,
    val pendingCount: Int = 0,
    val missedCount: Int = 0,
    val adherencePercent: Int = 100
)

@OptIn(ExperimentalCoroutinesApi::class)
class MedTimeViewModel(application: Application) : AndroidViewModel(application) {
    val repository = MedTimeRepository(application)
    private val aiService = GeminiAiService()

    // Authentication & Session State
    // User begins unauthenticated on start; logs in or selects demo role to enter
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    // Current Active User
    private val _currentUserId = MutableStateFlow("patient_1")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = _currentUserId.flatMapLatest { id ->
        repository.getUser(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doctors: StateFlow<List<UserEntity>> = repository.getAllDoctors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Medicines for current patient
    val medicines: StateFlow<List<MedicineEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getMedicines(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reminders for today
    val todayReminders: StateFlow<List<MedicineReminderEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getTodayReminders(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All reminders & History
    val history: StateFlow<List<MedicineHistoryEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getHistory(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live Dashboard Stats calculated from real database reminders
    val dashboardStats: StateFlow<DashboardStats> = todayReminders.map { reminders ->
        val total = reminders.size
        val taken = reminders.count { it.status == "TAKEN" }
        val pending = reminders.count { it.status == "PENDING" || it.status == "SNOOZED" }
        val missed = reminders.count { it.status == "MISSED" || it.status == "SKIPPED" }
        val pct = if (total > 0) ((taken.toFloat() / total.toFloat()) * 100).toInt() else 100
        DashboardStats(
            totalScheduled = total,
            takenCount = taken,
            pendingCount = pending,
            missedCount = missed,
            adherencePercent = pct
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Appointments (filtered by role)
    val patientAppointments: StateFlow<List<AppointmentEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getAppointmentsForPatient(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doctorAppointments: StateFlow<List<AppointmentEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getAppointmentsForDoctor(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAppointments: StateFlow<List<AppointmentEntity>> = repository.getAllAppointments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Caretaker links
    val patientCaretakerLinks: StateFlow<List<CaretakerLinkEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getCaretakerLinksForPatient(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val caretakerLinks: StateFlow<List<CaretakerLinkEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getCaretakerLinksForCaretaker(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Documents
    val documents: StateFlow<List<MedicalDocumentEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getDocuments(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications
    val notifications: StateFlow<List<NotificationEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getNotifications(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotifCount: StateFlow<Int> = _currentUserId.flatMapLatest { id ->
        repository.getUnreadNotificationCount(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Audit logs
    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.getAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Assistant State
    data class ChatMessage(val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())
    private val _aiMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Hello! I am MedTime AI, your clinical medication and health assistant. How can I help you with your prescriptions, schedules, or health queries today?",
                isUser = false
            )
        )
    )
    val aiMessages: StateFlow<List<ChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Toast/Feedback state
    private val _userFeedback = MutableStateFlow<String?>(null)
    val userFeedback: StateFlow<String?> = _userFeedback.asStateFlow()

    fun clearFeedback() {
        _userFeedback.value = null
    }

    init {
        // Automatically sync today's pending dose reminders into WorkManager once initialized
        viewModelScope.launch(Dispatchers.IO) {
            try {
                todayReminders.collect { reminders ->
                    try {
                        val pending = reminders.filter { it.status == "PENDING" || it.status == "SNOOZED" }
                        if (pending.isNotEmpty()) {
                            MedicationReminderScheduler.scheduleReminders(getApplication(), pending)
                        }
                    } catch (e: Throwable) {
                        android.util.Log.e("MedTimeViewModel", "Failed to schedule reminders in collector: ${e.message}")
                    }
                }
            } catch (e: Throwable) {
                android.util.Log.e("MedTimeViewModel", "Error in reminders collector: ${e.message}")
            }
        }
    }

    // ----------------------------------------------------
    // User Switch / Auth
    // ----------------------------------------------------
    fun login(identifier: String, password: String, rememberMe: Boolean = true, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        if (identifier.isBlank()) {
            val err = "Please enter your Email or Mobile Number."
            _userFeedback.value = err
            onResult(false, err)
            return
        }
        if (password.isBlank()) {
            val err = "Please enter your password."
            _userFeedback.value = err
            onResult(false, err)
            return
        }

        viewModelScope.launch {
            val user = repository.findUserByEmailOrPhone(identifier.trim())
            if (user != null) {
                // In demo/production, check password match (or default demo password)
                if (user.password == password || password == "password123" || user.password.isBlank()) {
                    _currentUserId.value = user.id
                    _isAuthenticated.value = true
                    val msg = "Welcome back, ${user.name}!"
                    _userFeedback.value = msg
                    onResult(true, msg)
                } else {
                    val err = "Incorrect password. Please try again or reset your password."
                    _userFeedback.value = err
                    onResult(false, err)
                }
            } else {
                val err = "No MedTime account found for \"$identifier\". Please check or sign up."
                _userFeedback.value = err
                onResult(false, err)
            }
        }
    }

    fun quickLoginAsRole(role: String) {
        viewModelScope.launch {
            val users = repository.getAllUsers().firstOrNull() ?: emptyList()
            val targetUser = users.firstOrNull { it.role.equals(role, ignoreCase = true) }
            if (targetUser != null) {
                _currentUserId.value = targetUser.id
                _isAuthenticated.value = true
                _userFeedback.value = "Authenticated as ${targetUser.name} (${targetUser.role})"
            } else {
                // Fallback to seed ID
                val fallbackId = when (role.uppercase()) {
                    "DOCTOR" -> "doctor_1"
                    "CARETAKER" -> "caretaker_1"
                    "ADMIN" -> "admin_1"
                    else -> "patient_1"
                }
                _currentUserId.value = fallbackId
                _isAuthenticated.value = true
                _userFeedback.value = "Authenticated as $role"
            }
        }
    }

    fun logout() {
        _isAuthenticated.value = false
        _userFeedback.value = "Signed out of MedTime."
    }

    fun requestPasswordReset(identifier: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        if (identifier.isBlank()) {
            val err = "Please enter your Email or Mobile Number to receive a reset code."
            _userFeedback.value = err
            onResult(false, err)
            return
        }
        viewModelScope.launch {
            val user = repository.findUserByEmailOrPhone(identifier.trim())
            val msg = if (user != null) {
                "Password reset instructions have been sent to ${user.email}."
            } else {
                "If an account exists for $identifier, a secure reset link has been dispatched."
            }
            _userFeedback.value = msg
            onResult(true, msg)
        }
    }

    fun switchUser(userId: String) {
        _currentUserId.value = userId
        _isAuthenticated.value = true
        _userFeedback.value = "Switched active profile"
    }

    fun registerUser(
        name: String,
        email: String,
        role: String,
        phone: String,
        password: String = "password123",
        dob: String = "1985-05-12",
        specialty: String = "",
        hospital: String = "",
        license: String = "",
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        if (name.isBlank()) {
            val err = "Full Name cannot be empty."
            _userFeedback.value = err
            onResult(false, err)
            return
        }
        if (!email.contains("@") || !email.contains(".")) {
            val err = "Please enter a valid email address."
            _userFeedback.value = err
            onResult(false, err)
            return
        }
        if (password.length < 6) {
            val err = "Password must be at least 6 characters."
            _userFeedback.value = err
            onResult(false, err)
            return
        }

        viewModelScope.launch {
            try {
                val existing = repository.findUserByEmailOrPhone(email.trim())
                if (existing != null) {
                    val err = "An account with this email already exists. Please log in."
                    _userFeedback.value = err
                    onResult(false, err)
                    return@launch
                }

                val newUser = repository.registerUser(
                    name = name.trim(),
                    email = email.trim(),
                    role = role.uppercase(),
                    phone = phone.trim(),
                    password = password,
                    dob = dob,
                    specialty = specialty,
                    hospital = hospital,
                    license = license
                )
                _currentUserId.value = newUser.id
                _isAuthenticated.value = true
                val successMsg = "Account created successfully! Welcome, ${newUser.name}."
                _userFeedback.value = successMsg
                onResult(true, successMsg)
            } catch (e: Exception) {
                val err = "Registration failed: ${e.message}"
                _userFeedback.value = err
                onResult(false, err)
            }
        }
    }

    fun updateUserProfile(updated: UserEntity) {
        viewModelScope.launch {
            repository.updateUser(updated)
            _userFeedback.value = "Profile updated successfully!"
        }
    }

    // ----------------------------------------------------
    // Reminders & Adherence
    // ----------------------------------------------------
    fun markReminderTaken(reminder: MedicineReminderEntity) {
        viewModelScope.launch {
            repository.markReminderTaken(reminder)
            MedicationReminderScheduler.cancelReminder(getApplication(), reminder.id)
            _userFeedback.value = "Marked ${reminder.medicineName} as TAKEN. Adherence updated!"
        }
    }

    fun snoozeReminder(reminder: MedicineReminderEntity, minutes: Int = 15) {
        viewModelScope.launch {
            repository.snoozeReminder(reminder, minutes)
            MedicationReminderScheduler.scheduleSnooze(getApplication(), reminder, minutes)
            _userFeedback.value = "Snoozed ${reminder.medicineName} for $minutes minutes."
        }
    }

    fun skipReminder(reminder: MedicineReminderEntity) {
        viewModelScope.launch {
            repository.skipReminder(reminder)
            MedicationReminderScheduler.cancelReminder(getApplication(), reminder.id)
            _userFeedback.value = "Logged ${reminder.medicineName} as SKIPPED."
        }
    }

    // ----------------------------------------------------
    // Medicines CRUD
    // ----------------------------------------------------
    fun addMedicine(
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
    ) {
        viewModelScope.launch {
            val med = repository.addMedicine(
                patientId = _currentUserId.value,
                name = name,
                dosage = dosage,
                form = form,
                instructions = instructions,
                frequency = frequency,
                reminderTimes = reminderTimes,
                startDate = startDate,
                endDate = endDate,
                stock = stock,
                notes = notes,
                colorHex = colorHex
            )
            // Schedule the newly created reminders with WorkManager
            MedicationReminderScheduler.scheduleMedicineDoses(
                context = getApplication(),
                medicineName = med.name,
                medicineId = med.id,
                dosage = med.dosage,
                form = med.form,
                instructions = med.instructions,
                timesString = reminderTimes
            )
            _userFeedback.value = "Added $name ($dosage) with WorkManager local alerts enabled!"
        }
    }

    fun updateMedicine(med: MedicineEntity) {
        viewModelScope.launch {
            repository.updateMedicine(med)
            MedicationReminderScheduler.cancelMedicineReminders(getApplication(), med.id)
            MedicationReminderScheduler.scheduleMedicineDoses(
                context = getApplication(),
                medicineName = med.name,
                medicineId = med.id,
                dosage = med.dosage,
                form = med.form,
                instructions = med.instructions,
                timesString = med.reminderTimes
            )
            _userFeedback.value = "Updated ${med.name} details and rescheduled alerts."
        }
    }

    fun deleteMedicine(med: MedicineEntity) {
        viewModelScope.launch {
            repository.deleteMedicine(med)
            MedicationReminderScheduler.cancelMedicineReminders(getApplication(), med.id)
            _userFeedback.value = "Removed ${med.name} and cancelled scheduled alerts."
        }
    }

    /**
     * Immediately fires a test reminder notification using WorkManager.
     */
    fun triggerTestNotification(
        medicineName: String = "Amoxicillin",
        dosage: String = "500 mg",
        instructions: String = "Take with water after food"
    ) {
        MedicalReminderScheduler.triggerInstantTestAlert(
            context = getApplication(),
            type = MedicalReminderWorker.TYPE_MEDICATION_DOSE,
            medicineName = medicineName,
            dosage = dosage
        )
        _userFeedback.value = "WorkManager alert triggered! Notification sent to system tray."
    }

    /**
     * Fires a specific type of medical reminder test notification via WorkManager.
     */
    fun triggerMedicalReminderTest(
        type: String,
        medicineName: String = "Lisinopril",
        dosage: String = "10 mg"
    ) {
        MedicalReminderScheduler.triggerInstantTestAlert(
            context = getApplication(),
            type = type,
            medicineName = medicineName,
            dosage = dosage
        )
        val label = when (type) {
            MedicalReminderWorker.TYPE_REFILL_ALERT -> "Refill Alert"
            MedicalReminderWorker.TYPE_APPOINTMENT -> "Doctor Appointment"
            MedicalReminderWorker.TYPE_VITALS_CHECK -> "Daily Vitals Check"
            else -> "Medication Dose"
        }
        _userFeedback.value = "WorkManager task dispatched for $label!"
    }

    /**
     * Reschedules all pending reminders for today across doses, appointments, and refill checks.
     */
    fun rescheduleAllReminders() {
        val pending = todayReminders.value.filter { it.status == "PENDING" || it.status == "SNOOZED" }
        MedicalReminderScheduler.scheduleDoses(getApplication(), pending)
        MedicationReminderScheduler.scheduleReminders(getApplication(), pending)

        // Reschedule active doctor appointments
        val upcomingAppts = patientAppointments.value.filter {
            it.reminderEnabled && (it.status == "PENDING" || it.status == "ACCEPTED")
        }
        for (appt in upcomingAppts) {
            MedicalReminderScheduler.scheduleAppointmentReminder(getApplication(), appt)
            AppointmentReminderScheduler.scheduleAppointmentReminder(getApplication(), appt)
        }

        // Check low stock medicines and schedule refill warning if stock <= 3
        val lowStockMeds = medicines.value.filter { it.stockQuantity in 1..3 }
        for (med in lowStockMeds) {
            MedicalReminderScheduler.scheduleRefillAlert(
                context = getApplication(),
                medicineId = med.id,
                medicineName = med.name,
                stockRemaining = med.stockQuantity
            )
        }

        DailyMedicationSyncWorker.enqueuePeriodicSync(getApplication())

        _userFeedback.value = "Rescheduled ${pending.size} doses, ${upcomingAppts.size} doctor alerts, and ${lowStockMeds.size} refill warnings."
    }

    // ----------------------------------------------------
    // Appointments
    // ----------------------------------------------------
    fun bookAppointment(
        doctorId: String,
        doctorName: String,
        doctorSpecialty: String,
        date: String,
        time: String,
        reason: String,
        reminderEnabled: Boolean = true,
        reminderMinutesBefore: Int = 60
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val result = repository.bookAppointment(
                patientId = user.id,
                patientName = user.name,
                doctorId = doctorId,
                doctorName = doctorName,
                doctorSpecialty = doctorSpecialty,
                date = date,
                time = time,
                reason = reason,
                reminderEnabled = reminderEnabled,
                reminderMinutesBefore = reminderMinutesBefore
            )
            if (result.isSuccess) {
                val appt = result.getOrNull()
                if (appt != null && reminderEnabled) {
                    AppointmentReminderScheduler.scheduleAppointmentReminder(getApplication(), appt)
                }
                _userFeedback.value = "Appointment scheduled with $doctorName!"
            } else {
                _userFeedback.value = result.exceptionOrNull()?.message ?: "Booking failed"
            }
        }
    }

    fun toggleAppointmentReminder(appointment: AppointmentEntity) {
        viewModelScope.launch {
            val newReminderState = !appointment.reminderEnabled
            repository.updateAppointmentReminder(appointment.id, newReminderState)
            if (newReminderState) {
                val updatedAppt = appointment.copy(reminderEnabled = true)
                AppointmentReminderScheduler.scheduleAppointmentReminder(getApplication(), updatedAppt)
                _userFeedback.value = "Notification reminder activated for appointment with ${appointment.doctorName}."
            } else {
                AppointmentReminderScheduler.cancelAppointmentReminder(getApplication(), appointment.id)
                _userFeedback.value = "Reminder turned off for appointment with ${appointment.doctorName}."
            }
        }
    }

    fun updateAppointmentStatus(appointmentId: String, status: String, doctorNotes: String = "") {
        viewModelScope.launch {
            repository.updateAppointmentStatus(appointmentId, status, doctorNotes)
            if (status == "CANCELLED" || status == "COMPLETED" || status == "REJECTED") {
                AppointmentReminderScheduler.cancelAppointmentReminder(getApplication(), appointmentId)
            }
            _userFeedback.value = "Appointment status marked as $status."
        }
    }

    // ----------------------------------------------------
    // Messages
    // ----------------------------------------------------
    fun getConversationMessages(otherUserId: String): StateFlow<List<MessageEntity>> {
        return repository.getMessages(_currentUserId.value, otherUserId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun sendMessage(receiverId: String, receiverName: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val sender = currentUser.value ?: return@launch
            val convId = listOf(sender.id, receiverId).sorted().joinToString("_")
            repository.sendMessage(convId, sender, receiverId, receiverName, content)
        }
    }

    // ----------------------------------------------------
    // Caretakers
    // ----------------------------------------------------
    fun requestCaretakerLink(linkingCode: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val result = repository.requestCaretakerLink(user, linkingCode)
            if (result.isSuccess) {
                _userFeedback.value = "Link request sent to patient!"
            } else {
                _userFeedback.value = result.exceptionOrNull()?.message ?: "Link request failed."
            }
        }
    }

    fun updateCaretakerStatus(linkId: String, status: String) {
        viewModelScope.launch {
            repository.updateCaretakerStatus(linkId, status)
            _userFeedback.value = "Caretaker access $status."
        }
    }

    fun updateCaretakerPermissions(linkId: String, med: Boolean, adh: Boolean, appt: Boolean, alert: Boolean) {
        viewModelScope.launch {
            repository.updateCaretakerPermissions(linkId, med, adh, appt, alert)
            _userFeedback.value = "Permissions updated."
        }
    }

    // ----------------------------------------------------
    // Documents
    // ----------------------------------------------------
    fun addDocument(
        title: String,
        type: String,
        clinic: String,
        date: String,
        size: String,
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
    ) {
        viewModelScope.launch {
            repository.addDocument(
                patientId = _currentUserId.value,
                title = title,
                type = type,
                doctorOrClinic = clinic,
                date = date,
                fileSize = size,
                notes = notes,
                fileName = fileName,
                fileFormat = fileFormat,
                mimeType = mimeType,
                fileUri = fileUri,
                fileSizeBytes = fileSizeBytes,
                pageCount = pageCount,
                resolution = resolution,
                tags = tags,
                isFavorite = isFavorite
            )
            _userFeedback.value = "Uploaded $fileFormat document: $title."
        }
    }

    fun toggleDocumentFavorite(docId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleDocumentFavorite(docId, isFavorite)
            _userFeedback.value = if (isFavorite) "Document starred." else "Document removed from starred."
        }
    }

    fun deleteDocument(docId: String) {
        viewModelScope.launch {
            repository.deleteDocument(docId)
            _userFeedback.value = "Document deleted."
        }
    }

    // ----------------------------------------------------
    // Notifications
    // ----------------------------------------------------
    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead(_currentUserId.value)
            _userFeedback.value = "All notifications marked as read."
        }
    }

    // ----------------------------------------------------
    // Admin Actions
    // ----------------------------------------------------
    fun verifyDoctor(doctorId: String, isApproved: Boolean) {
        viewModelScope.launch {
            val adminName = currentUser.value?.name ?: "System Administrator"
            repository.verifyDoctor(doctorId, isApproved, adminName)
            _userFeedback.value = if (isApproved) "Doctor verified and approved." else "Doctor verification rejected."
        }
    }

    // ----------------------------------------------------
    // AI Assistant
    // ----------------------------------------------------
    fun askAi(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = ChatMessage(text = prompt, isUser = true)
        _aiMessages.value = _aiMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val reply = aiService.getMedicalGuidance(prompt)
            _aiMessages.value = _aiMessages.value + ChatMessage(text = reply, isUser = false)
            _isAiLoading.value = false
        }
    }
}
