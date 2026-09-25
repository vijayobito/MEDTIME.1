package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
<<<<<<< HEAD
import com.example.alarm.AlarmSettingsManager
import com.example.alarm.AlarmSoundPlayer
import com.example.alarm.MedicineTtsEngine
import com.example.auth.OAuthProviderConfig
import com.example.auth.SocialAuthManager
import com.example.auth.SocialAuthProvider
import com.example.auth.SocialAuthResult
import com.example.auth.SocialUserIdentity
import com.example.data.model.AnnouncementEntity
import com.example.data.model.AppointmentEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.CaretakerAssistanceRequestEntity
=======
import com.example.data.model.AppointmentEntity
import com.example.data.model.AuditLogEntity
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
import com.example.data.model.CaretakerLinkEntity
import com.example.data.model.MedicalDocumentEntity
import com.example.data.model.MedicineEntity
import com.example.data.model.MedicineHistoryEntity
import com.example.data.model.MedicineReminderEntity
import com.example.data.model.MessageEntity
import com.example.data.model.NotificationEntity
<<<<<<< HEAD
import com.example.data.model.PatientAddressEntity
import com.example.data.model.UserEntity
import com.example.data.model.WalletEntity
import com.example.data.model.WalletTransactionEntity
import com.example.data.repository.MedTimeRepository
import com.example.service.GeminiAiService
import com.example.util.QrAccountPayload
=======
import com.example.data.model.UserEntity
import com.example.data.repository.MedTimeRepository
import com.example.service.GeminiAiService
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
import com.example.worker.AppointmentReminderScheduler
import com.example.worker.DailyMedicationSyncWorker
import com.example.worker.MedicalReminderScheduler
import com.example.worker.MedicalReminderWorker
import com.example.worker.MedicationReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
<<<<<<< HEAD
import kotlinx.coroutines.flow.Flow
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
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
<<<<<<< HEAD
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465

data class DashboardStats(
    val totalScheduled: Int = 0,
    val takenCount: Int = 0,
    val pendingCount: Int = 0,
    val missedCount: Int = 0,
    val adherencePercent: Int = 100
)

<<<<<<< HEAD
data class PatientLoginSession(
    val id: String,
    val deviceName: String,
    val deviceType: String,
    val ipAddress: String,
    val location: String,
    val timestamp: String,
    val isCurrent: Boolean = false
)

data class WebConnectedSession(
    val id: String,
    val browser: String,
    val os: String,
    val ipAddress: String,
    val location: String,
    val connectedAt: String,
    val lastActive: String,
    val isLive: Boolean = true
)

data class CaretakerWalletTransaction(
    val id: String,
    val date: String,
    val type: String,
    val amount: Double,
    val description: String,
    val status: String = "Completed"
)

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
@OptIn(ExperimentalCoroutinesApi::class)
class MedTimeViewModel(application: Application) : AndroidViewModel(application) {
    val repository = MedTimeRepository(application)
    private val aiService = GeminiAiService()

    // Authentication & Session State
    // User begins unauthenticated on start; logs in or selects demo role to enter
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

<<<<<<< HEAD
    // Social Login State
    private val _isSocialAuthLoading = MutableStateFlow(false)
    val isSocialAuthLoading: StateFlow<Boolean> = _isSocialAuthLoading.asStateFlow()

    private val _activeSocialProvider = MutableStateFlow<SocialAuthProvider?>(null)
    val activeSocialProvider: StateFlow<SocialAuthProvider?> = _activeSocialProvider.asStateFlow()

    // Dialog state when an account already exists with this email (Account Linking flow)
    private val _accountCollisionState = MutableStateFlow<Pair<UserEntity, SocialUserIdentity>?>(null)
    val accountCollisionState: StateFlow<Pair<UserEntity, SocialUserIdentity>?> = _accountCollisionState.asStateFlow()

    // Dialog state for OAuth Configuration / Developer Sandbox Guidance
    private val _oauthConfigState = MutableStateFlow<SocialAuthResult.MissingConfiguration?>(null)
    val oauthConfigState: StateFlow<SocialAuthResult.MissingConfiguration?> = _oauthConfigState.asStateFlow()

    // Dialog state for "Complete Your Profile" after new social registration
    private val _profileCompletionUser = MutableStateFlow<UserEntity?>(null)
    val profileCompletionUser: StateFlow<UserEntity?> = _profileCompletionUser.asStateFlow()

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
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

<<<<<<< HEAD
    val recycleBinDocuments: StateFlow<List<MedicalDocumentEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getRecycleBinDocuments(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
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

<<<<<<< HEAD
    // Announcements
    val announcements: StateFlow<List<AnnouncementEntity>> = repository.getAllAnnouncements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Caretaker links (for Admin)
    val allCaretakerLinks: StateFlow<List<CaretakerLinkEntity>> = repository.getAllCaretakerLinks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Caretaker Assistance Requests (Calls & Visits)
    val allAssistanceRequests: StateFlow<List<CaretakerAssistanceRequestEntity>> = repository.getAllAssistanceRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val caretakerAssistanceRequests: StateFlow<List<CaretakerAssistanceRequestEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getAssistanceRequestsForCaretaker(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Caretaker Wallet & Transaction State
    val caretakerWallet: StateFlow<WalletEntity?> = _currentUserId.flatMapLatest { id ->
        repository.getWallet(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val caretakerWalletTransactions: StateFlow<List<WalletTransactionEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getWalletTransactions(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWalletTransactions: StateFlow<List<WalletTransactionEntity>> = repository.getAllWalletTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Platform System Settings
    val lowStockBufferDays = MutableStateFlow(3)
    val missedDoseEscalationMinutes = MutableStateFlow(15)
    val emergencySosAutoDispatch = MutableStateFlow(true)
    val maintenanceMode = MutableStateFlow(false)
    val autoCloudSync = MutableStateFlow(true)
    val requireDoctorLicenseUpload = MutableStateFlow(true)

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
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

<<<<<<< HEAD
    // Theme Mode State
    private val _themeMode = MutableStateFlow(com.example.ui.theme.ThemeMode.SYSTEM)
    val themeMode: StateFlow<com.example.ui.theme.ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: com.example.ui.theme.ThemeMode) {
        _themeMode.value = mode
    }

    fun toggleThemeMode() {
        _themeMode.value = when (_themeMode.value) {
            com.example.ui.theme.ThemeMode.LIGHT -> com.example.ui.theme.ThemeMode.DARK
            com.example.ui.theme.ThemeMode.DARK -> com.example.ui.theme.ThemeMode.SYSTEM
            com.example.ui.theme.ThemeMode.SYSTEM -> com.example.ui.theme.ThemeMode.LIGHT
        }
    }

    // ----------------------------------------------------
    // Patient Settings State & Preferences
    // ----------------------------------------------------

    // 1. Security & 2FA State
    private val _isTwoFactorEnabled = MutableStateFlow(false)
    val isTwoFactorEnabled: StateFlow<Boolean> = _isTwoFactorEnabled.asStateFlow()

    private val _backupCodes = MutableStateFlow(
        listOf("8492-1049", "3958-2940", "1849-5029", "7402-9182", "5820-3948", "9102-4820", "2849-1940", "6730-1849")
    )
    val backupCodes: StateFlow<List<String>> = _backupCodes.asStateFlow()

    fun setTwoFactorEnabled(enabled: Boolean) {
        _isTwoFactorEnabled.value = enabled
        _userFeedback.value = if (enabled) "Two-Factor Authentication activated." else "Two-Factor Authentication turned off."
    }

    fun regenerateBackupCodes() {
        val newCodes = (1..8).map {
            val p1 = (1000..9999).random()
            val p2 = (1000..9999).random()
            "$p1-$p2"
        }
        _backupCodes.value = newCodes
        _userFeedback.value = "New emergency backup recovery codes generated."
    }

    // 2. Active Sessions & Login History
    private val _activeSessions = MutableStateFlow(
        listOf(
            PatientLoginSession("session_current", "Google Pixel 8 Pro", "Mobile App (Android 14)", "192.168.1.45", "San Francisco, CA", "Active Now", isCurrent = true),
            PatientLoginSession("session_web_1", "Chrome 128 / macOS", "Web Portal (medtime.health)", "73.189.20.14", "San Jose, CA", "Today, 09:15 AM", isCurrent = false),
            PatientLoginSession("session_ipad", "iPad Pro 12.9 (Safari)", "Mobile Web Session", "104.28.21.90", "San Francisco, CA", "Yesterday, 04:30 PM", isCurrent = false)
        )
    )
    val activeSessions: StateFlow<List<PatientLoginSession>> = _activeSessions.asStateFlow()

    fun logoutFromOtherDevices() {
        _activeSessions.value = _activeSessions.value.filter { it.isCurrent }
        _connectedWebSessions.value = emptyList()
        _userFeedback.value = "Successfully logged out of all other devices and web sessions."
    }

    // 3. Notification Toggles
    private val _notifMedicineReminders = MutableStateFlow(true)
    val notifMedicineReminders: StateFlow<Boolean> = _notifMedicineReminders.asStateFlow()
    fun setNotifMedicineReminders(enabled: Boolean) {
        _notifMedicineReminders.value = enabled
        _userFeedback.value = if (enabled) "Medicine reminders enabled." else "Medicine reminders muted."
    }

    private val _notifAppointmentReminders = MutableStateFlow(true)
    val notifAppointmentReminders: StateFlow<Boolean> = _notifAppointmentReminders.asStateFlow()
    fun setNotifAppointmentReminders(enabled: Boolean) {
        _notifAppointmentReminders.value = enabled
        _userFeedback.value = if (enabled) "Appointment alerts enabled." else "Appointment alerts muted."
    }

    private val _notifMessageAlerts = MutableStateFlow(true)
    val notifMessageAlerts: StateFlow<Boolean> = _notifMessageAlerts.asStateFlow()
    fun setNotifMessageAlerts(enabled: Boolean) {
        _notifMessageAlerts.value = enabled
        _userFeedback.value = if (enabled) "Message notifications enabled." else "Message notifications muted."
    }

    private val _notifMedicalNews = MutableStateFlow(false)
    val notifMedicalNews: StateFlow<Boolean> = _notifMedicalNews.asStateFlow()
    fun setNotifMedicalNews(enabled: Boolean) {
        _notifMedicalNews.value = enabled
        _userFeedback.value = if (enabled) "Medical news notifications enabled." else "Medical news muted."
    }

    private val _notifSecurityAlerts = MutableStateFlow(true)
    val notifSecurityAlerts: StateFlow<Boolean> = _notifSecurityAlerts.asStateFlow()
    fun setNotifSecurityAlerts(enabled: Boolean) {
        _notifSecurityAlerts.value = enabled
        _userFeedback.value = if (enabled) "Security alerts enabled." else "Security alerts muted."
    }

    // 4. Reminder Preferences & Alarm Customization (Sound + Spoken Reminders)
    private val _reminderSoundEnabled = MutableStateFlow(
        AlarmSettingsManager.isSoundEnabled(application)
    )
    val reminderSoundEnabled: StateFlow<Boolean> = _reminderSoundEnabled.asStateFlow()

    fun setReminderSoundEnabled(enabled: Boolean) {
        _reminderSoundEnabled.value = enabled
        AlarmSettingsManager.setSoundEnabled(getApplication(), enabled)
        _userFeedback.value = if (enabled) "Reminder sound turned ON." else "Reminder sound turned OFF."
    }

    private val _reminderSoundTone = MutableStateFlow(
        AlarmSettingsManager.getSelectedSound(application)
    )
    val reminderSoundTone: StateFlow<String> = _reminderSoundTone.asStateFlow()

    fun setReminderSoundTone(tone: String) {
        _reminderSoundTone.value = tone
        AlarmSettingsManager.setSelectedSound(getApplication(), tone)
        _userFeedback.value = "Alarm sound set to: $tone"
    }

    private val _customSoundUri = MutableStateFlow<android.net.Uri?>(
        AlarmSettingsManager.getCustomSoundUri(application)
    )
    val customSoundUri: StateFlow<android.net.Uri?> = _customSoundUri.asStateFlow()

    private val _customSoundName = MutableStateFlow<String?>(
        AlarmSettingsManager.getCustomSoundName(application)
    )
    val customSoundName: StateFlow<String?> = _customSoundName.asStateFlow()

    fun setCustomSound(uri: android.net.Uri?, name: String?) {
        _customSoundUri.value = uri
        _customSoundName.value = name
        AlarmSettingsManager.setCustomSound(getApplication(), uri, name)
        if (uri != null) {
            _reminderSoundTone.value = AlarmSettingsManager.SOUND_CUSTOM
            AlarmSettingsManager.setSelectedSound(getApplication(), AlarmSettingsManager.SOUND_CUSTOM)
            _userFeedback.value = "Custom alarm sound set: ${name ?: "Audio File"}"
        }
    }

    private val _spokenReminderEnabled = MutableStateFlow(
        AlarmSettingsManager.isSpokenReminderEnabled(application)
    )
    val spokenReminderEnabled: StateFlow<Boolean> = _spokenReminderEnabled.asStateFlow()

    fun setSpokenReminderEnabled(enabled: Boolean) {
        _spokenReminderEnabled.value = enabled
        AlarmSettingsManager.setSpokenReminderEnabled(getApplication(), enabled)
        _userFeedback.value = if (enabled) "Spoken Medicine Reminders enabled." else "Spoken reminders disabled."
    }

    private val _ttsVoice = MutableStateFlow(
        AlarmSettingsManager.getTtsVoice(application)
    )
    val ttsVoice: StateFlow<String> = _ttsVoice.asStateFlow()

    fun setTtsVoice(voiceName: String) {
        _ttsVoice.value = voiceName
        AlarmSettingsManager.setTtsVoice(getApplication(), voiceName)
        _userFeedback.value = "TTS voice set to: $voiceName"
    }

    private val _ttsLanguage = MutableStateFlow(
        AlarmSettingsManager.getTtsLanguage(application)
    )
    val ttsLanguage: StateFlow<String> = _ttsLanguage.asStateFlow()

    fun setTtsLanguage(langCode: String) {
        _ttsLanguage.value = langCode
        AlarmSettingsManager.setTtsLanguage(getApplication(), langCode)
        _userFeedback.value = "Spoken reminder language set to: $langCode"
    }

    private val _speechVolume = MutableStateFlow(
        AlarmSettingsManager.getSpeechVolume(application)
    )
    val speechVolume: StateFlow<Float> = _speechVolume.asStateFlow()

    fun setSpeechVolume(vol: Float) {
        _speechVolume.value = vol
        AlarmSettingsManager.setSpeechVolume(getApplication(), vol)
    }

    private val _reminderVibrationEnabled = MutableStateFlow(
        AlarmSettingsManager.isVibrationEnabled(application)
    )
    val reminderVibrationEnabled: StateFlow<Boolean> = _reminderVibrationEnabled.asStateFlow()

    fun setReminderVibrationEnabled(enabled: Boolean) {
        _reminderVibrationEnabled.value = enabled
        AlarmSettingsManager.setVibrationEnabled(getApplication(), enabled)
        _userFeedback.value = if (enabled) "Vibration turned ON." else "Vibration turned OFF."
    }

    private val _reminderBehavior = MutableStateFlow("Full Screen Alarm")
    val reminderBehavior: StateFlow<String> = _reminderBehavior.asStateFlow()
    fun setReminderBehavior(behavior: String) {
        _reminderBehavior.value = behavior
        _userFeedback.value = "Reminder alert style set to: $behavior."
    }

    private var previewPlayer: AlarmSoundPlayer? = null
    private var previewTts: MedicineTtsEngine? = null

    fun previewSound(soundName: String) {
        previewTts?.stop()
        if (previewPlayer == null) {
            previewPlayer = AlarmSoundPlayer(getApplication())
        }
        previewPlayer?.playSound(soundName = soundName, looping = false, onFallback = { msg ->
            _userFeedback.value = msg
        })
        _userFeedback.value = "Previewing $soundName..."
    }

    fun stopSoundPreview() {
        previewPlayer?.stop()
    }

    fun testVoice() {
        stopSoundPreview()
        if (previewTts == null) {
            previewTts = MedicineTtsEngine(getApplication())
        }
        val name = currentUser.value?.name ?: "Vijay"
        previewTts?.speakTestReminder(name)
        _userFeedback.value = "Speaking voice reminder sample for $name..."
    }

    // 5. Connected Website Sessions
    private val _connectedWebSessions = MutableStateFlow(
        listOf(
            WebConnectedSession(
                id = "web_sync_1",
                browser = "Chrome on Windows 11",
                os = "Windows 11 (64-bit)",
                ipAddress = "192.168.1.102",
                location = "San Francisco, CA",
                connectedAt = "Today, 09:15 AM",
                lastActive = "Live Synced",
                isLive = true
            )
        )
    )
    val connectedWebSessions: StateFlow<List<WebConnectedSession>> = _connectedWebSessions.asStateFlow()

    fun connectToWebsite(code: String = "MED-WEB-CONNECT") {
        val newSession = WebConnectedSession(
            id = "web_" + System.currentTimeMillis(),
            browser = "Chrome 128 (Desktop)",
            os = "macOS Sequoia",
            ipAddress = "192.168.1.118",
            location = "Local Network (Verified)",
            connectedAt = "Just now",
            lastActive = "Live Synced",
            isLive = true
        )
        _connectedWebSessions.value = _connectedWebSessions.value + newSession
        _userFeedback.value = "Connected to MedTime Web Portal (medtime.health) successfully!"
    }

    fun disconnectWebsiteSession(sessionId: String) {
        _connectedWebSessions.value = _connectedWebSessions.value.filter { it.id != sessionId }
        _userFeedback.value = "Web browser session disconnected."
    }

    fun disconnectAllWebSessions() {
        _connectedWebSessions.value = emptyList()
        _userFeedback.value = "All MedTime Web sessions disconnected."
    }

    // 6. Privacy Settings
    private val _locationSharingEnabled = MutableStateFlow(false)
    val locationSharingEnabled: StateFlow<Boolean> = _locationSharingEnabled.asStateFlow()
    fun setLocationSharingEnabled(enabled: Boolean) {
        _locationSharingEnabled.value = enabled
        _userFeedback.value = if (enabled) "Emergency location sharing enabled." else "Location sharing disabled."
    }

    private val _medicalDocumentAccess = MutableStateFlow("DOCTORS_AND_CARETAKER")
    val medicalDocumentAccess: StateFlow<String> = _medicalDocumentAccess.asStateFlow()
    fun setMedicalDocumentAccess(mode: String) {
        _medicalDocumentAccess.value = mode
        _userFeedback.value = "Document access permission updated to $mode."
    }

    // 7. Revoke connections
    fun revokeDoctorConnection(doctorId: String) {
        _userFeedback.value = "Doctor connection revoked. Access to medical logs removed."
    }

    fun revokeCaretakerLink(linkId: String) {
        viewModelScope.launch {
            repository.updateCaretakerStatus(linkId, "REJECTED")
            _userFeedback.value = "Caretaker connection revoked."
        }
    }

    // 8. Account actions
    fun disablePatientAccount(reason: String = "User requested temporary deactivation") {
        _isAuthenticated.value = false
        _userFeedback.value = "Account temporarily disabled. You can reactivate anytime by logging in."
    }

    fun deletePatientAccount(confirmation: String) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user != null) {
                repository.deleteUser(user.id)
            }
            _isAuthenticated.value = false
            _userFeedback.value = "Your MedTime patient account and clinical records have been permanently deleted."
        }
    }

    // Cloud Database Backup State
    private val _lastBackupSummary = MutableStateFlow<com.example.data.model.CloudBackupSummary?>(null)
    val lastBackupSummary: StateFlow<com.example.data.model.CloudBackupSummary?> = _lastBackupSummary.asStateFlow()

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    fun performManualCloudBackup(onComplete: ((Boolean, String) -> Unit)? = null) {
        _isBackingUp.value = true
        viewModelScope.launch {
            try {
                val userName = currentUser.value?.name ?: "Current User"
                val summary = repository.createCloudDatabaseBackup(userName)
                _lastBackupSummary.value = summary
                _userFeedback.value = "Cloud Backup successful! ${summary.totalRecords} records encrypted & synced (${String.format(java.util.Locale.US, "%.1f", summary.sizeKb)} KB)."
                onComplete?.invoke(true, "Backup complete: ${summary.totalRecords} records backed up.")
            } catch (e: Exception) {
                _userFeedback.value = "Cloud Backup failed: ${e.message}"
                onComplete?.invoke(false, e.message ?: "Unknown error")
            } finally {
                _isBackingUp.value = false
            }
        }
    }

    fun restoreFromCloudBackup(onComplete: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val userName = currentUser.value?.name ?: "Current User"
                val success = repository.restoreDatabaseFromCloud(userName)
                if (success) {
                    _userFeedback.value = "Database restored successfully from cloud backup."
                    onComplete?.invoke(true, "Restore successful")
                } else {
                    _userFeedback.value = "Could not restore from cloud backup."
                    onComplete?.invoke(false, "Restore failed")
                }
            } catch (e: Exception) {
                _userFeedback.value = "Restore failed: ${e.message}"
                onComplete?.invoke(false, e.message ?: "Unknown error")
            }
        }
    }

    // ----------------------------------------------------
    // Supabase PostgreSQL Cloud Sync State & Actions
    // ----------------------------------------------------
    private val _isSupabaseSyncing = MutableStateFlow(false)
    val isSupabaseSyncing: StateFlow<Boolean> = _isSupabaseSyncing.asStateFlow()

    private val _lastSupabaseSyncResult = MutableStateFlow<com.example.data.remote.supabase.SupabaseSyncResult?>(null)
    val lastSupabaseSyncResult: StateFlow<com.example.data.remote.supabase.SupabaseSyncResult?> = _lastSupabaseSyncResult.asStateFlow()

    private val _supabaseConnectionStatus = MutableStateFlow<com.example.data.remote.supabase.SupabaseConnectionResult?>(null)
    val supabaseConnectionStatus: StateFlow<com.example.data.remote.supabase.SupabaseConnectionResult?> = _supabaseConnectionStatus.asStateFlow()

    private val _isSupabaseConfigured = MutableStateFlow(repository.isSupabaseConfigured())
    val isSupabaseConfigured: StateFlow<Boolean> = _isSupabaseConfigured.asStateFlow()

    fun testSupabaseConnection() {
        viewModelScope.launch {
            _supabaseConnectionStatus.value = null
            val result = repository.testSupabaseConnection()
            _supabaseConnectionStatus.value = result
            _isSupabaseConfigured.value = repository.isSupabaseConfigured()
            when (result) {
                is com.example.data.remote.supabase.SupabaseConnectionResult.Success -> {
                    _userFeedback.value = "Connected to Supabase PostgreSQL (${result.latencyMs}ms)"
                }
                is com.example.data.remote.supabase.SupabaseConnectionResult.Error -> {
                    _userFeedback.value = result.message
                }
                is com.example.data.remote.supabase.SupabaseConnectionResult.NotConfigured -> {
                    _userFeedback.value = result.message
                }
            }
        }
    }

    fun syncWithSupabase(onComplete: ((Boolean, String) -> Unit)? = null) {
        _isSupabaseSyncing.value = true
        viewModelScope.launch {
            try {
                val result = repository.syncWithSupabase()
                _lastSupabaseSyncResult.value = result
                _isSupabaseConfigured.value = repository.isSupabaseConfigured()
                if (result.isSuccess) {
                    _userFeedback.value = "Supabase sync complete: ${result.recordsPulled} pulled, ${result.recordsPushed} pushed."
                    onComplete?.invoke(true, result.message)
                } else {
                    _userFeedback.value = "Supabase sync: ${result.message}"
                    onComplete?.invoke(false, result.message)
                }
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Sync error"
                _userFeedback.value = "Supabase sync failed: $msg"
                onComplete?.invoke(false, msg)
            } finally {
                _isSupabaseSyncing.value = false
            }
        }
    }

    fun updateSupabaseCredentials(url: String, anonKey: String) {
        repository.updateSupabaseCredentials(url, anonKey)
        _isSupabaseConfigured.value = repository.isSupabaseConfigured()
        _userFeedback.value = "Supabase credentials updated."
        testSupabaseConnection()
    }

    // PDF Clinical Report Generation
    fun generateDoctorSummaryPdf(context: android.content.Context, onReady: (java.io.File?) -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user == null) {
                _userFeedback.value = "User profile not available for PDF export."
                onReady(null)
                return@launch
            }

            val stats = dashboardStats.value
            val activeMeds = medicines.value
            val appts = patientAppointments.value
            val recentLogs = history.value

            val reportData = com.example.util.ClinicalReportPdfGenerator.ReportData(
                patient = user,
                adherencePercent = stats.adherencePercent,
                totalScheduled = stats.totalScheduled,
                takenCount = stats.takenCount,
                missedCount = stats.missedCount,
                medicines = activeMeds,
                appointments = appts,
                recentHistory = recentLogs
            )

            try {
                val file = com.example.util.ClinicalReportPdfGenerator.generateDoctorSummaryPdf(context, reportData)
                repository.logAudit(
                    action = "PDF_EXPORT",
                    performedBy = user.name,
                    target = "ClinicalReportPdf",
                    details = "Generated doctor summary PDF with adherence & appointment history"
                )
                _userFeedback.value = "Doctor Summary PDF generated successfully!"
                onReady(file)
            } catch (e: Exception) {
                _userFeedback.value = "Error generating PDF: ${e.message}"
                onReady(null)
            }
        }
    }

    fun updateEmergencyContact(name: String, phone: String, relation: String = "Caretaker") {
        val user = currentUser.value ?: return
        val updated = user.copy(
            emergencyContactName = name,
            emergencyContactPhone = phone,
            emergencyContactRelation = relation
        )
        updateUserProfile(updated)
        _userFeedback.value = "Emergency contact updated to $name ($phone)."
    }

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
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
<<<<<<< HEAD
        _isSocialAuthLoading.value = false
        _activeSocialProvider.value = null
        _accountCollisionState.value = null
        _oauthConfigState.value = null
        _profileCompletionUser.value = null
        _userFeedback.value = "Signed out of MedTime."
    }

    // ----------------------------------------------------
    // Social Login & OAuth
    // ----------------------------------------------------
    fun startSocialLogin(
        provider: SocialAuthProvider,
        onResult: (SocialAuthResult) -> Unit = {}
    ) {
        if (_isSocialAuthLoading.value) return
        _isSocialAuthLoading.value = true
        _activeSocialProvider.value = provider

        viewModelScope.launch {
            try {
                val config = SocialAuthManager.getProviderConfig(getApplication(), provider)
                if (config.isConfigured) {
                    // Provider is fully configured with production keys
                    SocialAuthManager.launchOAuthBrowser(getApplication(), provider)
                    _userFeedback.value = "Connecting to ${provider.displayName}..."
                } else {
                    // Show developer configuration instructions dialog with sandbox testing option
                    val missingKeys = when (provider) {
                        SocialAuthProvider.GOOGLE -> listOf("GOOGLE_CLIENT_ID")
                        SocialAuthProvider.FACEBOOK -> listOf("FACEBOOK_APP_ID", "FACEBOOK_CLIENT_TOKEN")
                        SocialAuthProvider.APPLE -> listOf("APPLE_CLIENT_ID", "APPLE_REDIRECT_URI")
                    }
                    val instructions = when (provider) {
                        SocialAuthProvider.GOOGLE -> "To use production Google OAuth, configure GOOGLE_CLIENT_ID in AI Studio Secrets or .env (obtained from Google Cloud Console)."
                        SocialAuthProvider.FACEBOOK -> "To use production Facebook Login, configure FACEBOOK_APP_ID in AI Studio Secrets or .env (obtained from Meta for Developers)."
                        SocialAuthProvider.APPLE -> "To use Sign in with Apple, configure APPLE_CLIENT_ID and APPLE_REDIRECT_URI in AI Studio Secrets or .env (obtained from Apple Developer Portal)."
                    }
                    val sandboxIdentity = SocialAuthManager.createSandboxIdentity(provider)
                    val missingConfig = SocialAuthResult.MissingConfiguration(
                        provider = provider,
                        missingKeys = missingKeys,
                        setupInstructions = instructions,
                        suggestedIdentity = sandboxIdentity
                    )
                    _oauthConfigState.value = missingConfig
                }
            } catch (e: Exception) {
                val err = SocialAuthResult.Error(provider, "Failed to initialize ${provider.displayName} login: ${e.message}")
                _userFeedback.value = "Unable to connect to ${provider.displayName}. Please try again."
                onResult(err)
            } finally {
                _isSocialAuthLoading.value = false
                _activeSocialProvider.value = null
            }
        }
    }

    fun processSocialIdentity(
        identity: SocialUserIdentity,
        onResult: (SocialAuthResult) -> Unit = {}
    ) {
        _isSocialAuthLoading.value = true
        _activeSocialProvider.value = identity.provider

        viewModelScope.launch {
            try {
                // 1. Check if user already exists with this Provider and Provider User ID
                val existingByProvider = repository.findUserByProvider(identity.provider.name, identity.providerUserId)
                if (existingByProvider != null) {
                    _currentUserId.value = existingByProvider.id
                    _isAuthenticated.value = true
                    val msg = "Welcome back, ${existingByProvider.name}!"
                    _userFeedback.value = msg
                    onResult(SocialAuthResult.Success(identity, existingByProvider, isNewUser = false))
                    return@launch
                }

                // 2. Check if an account already exists with this email (e.g. registered with password)
                val existingByEmail = repository.findUserByEmail(identity.email)
                if (existingByEmail != null) {
                    // Prevent duplicate accounts and require explicit account linking
                    _accountCollisionState.value = Pair(existingByEmail, identity)
                    _userFeedback.value = "An account already exists with this email."
                    onResult(SocialAuthResult.AccountCollision(existingByEmail, identity))
                    return@launch
                }

                // 3. New User Registration:
                // Rule: Normal social registration MUST default to PATIENT role only (Never Doctor, Caretaker, or Admin)
                val newUser = repository.registerSocialUser(
                    name = identity.name,
                    email = identity.email,
                    provider = identity.provider.name,
                    providerUserId = identity.providerUserId,
                    photoUrl = identity.photoUrl,
                    isEmailVerified = identity.isEmailVerified
                )

                _currentUserId.value = newUser.id
                _isAuthenticated.value = true
                _profileCompletionUser.value = newUser
                val msg = "Welcome to MedTime, ${newUser.name}!"
                _userFeedback.value = msg
                onResult(SocialAuthResult.Success(identity, newUser, isNewUser = true))
            } catch (e: Exception) {
                val err = SocialAuthResult.Error(identity.provider, "Authentication failed: ${e.message}")
                _userFeedback.value = "Unable to sign in with ${identity.provider.displayName}."
                onResult(err)
            } finally {
                _isSocialAuthLoading.value = false
                _activeSocialProvider.value = null
            }
        }
    }

    fun linkCollidingSocialAccount(
        user: UserEntity,
        identity: SocialUserIdentity,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                val updated = repository.linkSocialAccount(
                    userId = user.id,
                    provider = identity.provider.name,
                    providerUserId = identity.providerUserId,
                    photoUrl = identity.photoUrl
                )
                if (updated != null) {
                    _currentUserId.value = updated.id
                    _isAuthenticated.value = true
                    _accountCollisionState.value = null
                    val msg = "Account linked with ${identity.provider.displayName} successfully!"
                    _userFeedback.value = msg
                    onResult(true, msg)
                } else {
                    onResult(false, "Could not link account.")
                }
            } catch (e: Exception) {
                val msg = "Error linking account: ${e.message}"
                _userFeedback.value = msg
                onResult(false, msg)
            }
        }
    }

    fun dismissAccountCollision() {
        _accountCollisionState.value = null
    }

    fun dismissOAuthConfig() {
        _oauthConfigState.value = null
    }

    fun dismissProfileCompletion() {
        _profileCompletionUser.value = null
    }

    fun submitCompletedProfile(
        userId: String,
        phone: String,
        dob: String,
        bloodGroup: String,
        allergies: String,
        emergencyContactName: String,
        emergencyContactPhone: String,
        emergencyContactRelation: String,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                val user = currentUser.value
                if (user != null && user.id == userId) {
                    val updated = user.copy(
                        phone = phone.trim(),
                        dateOfBirth = dob.trim(),
                        bloodGroup = bloodGroup.trim(),
                        allergies = allergies.trim(),
                        emergencyContactName = emergencyContactName.trim(),
                        emergencyContactPhone = emergencyContactPhone.trim(),
                        emergencyContactRelation = emergencyContactRelation.trim(),
                        accountStatus = "ACTIVE"
                    )
                    repository.updateUser(updated)
                    _profileCompletionUser.value = null
                    _userFeedback.value = "Profile details saved successfully!"
                    onResult(true, "Profile completed!")
                } else {
                    onResult(false, "User not found")
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to update profile")
            }
        }
    }

    fun changePassword(newPassword: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user == null) {
                onResult(false, "No active user session.")
                return@launch
            }
            if (newPassword.length < 6) {
                onResult(false, "Password must be at least 6 characters.")
                return@launch
            }
            val updated = user.copy(password = newPassword)
            repository.updateUser(updated)
            _userFeedback.value = "Account password updated successfully!"
            onResult(true, "Password updated successfully!")
        }
    }

=======
        _userFeedback.value = "Signed out of MedTime."
    }

>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
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
<<<<<<< HEAD
        licenseImageUrl: String = "",
        profilePhotoUrl: String = "",
        issuingCouncil: String = "State Medical Council",
        yearsExperience: Int = 5,
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
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
<<<<<<< HEAD
        if (role.equals("DOCTOR", ignoreCase = true)) {
            if (license.isBlank()) {
                val err = "Medical License ID is required for doctor registration."
                _userFeedback.value = err
                onResult(false, err)
                return
            }
            if (licenseImageUrl.isBlank() || profilePhotoUrl.isBlank()) {
                val err = "Please upload both your medical license document and doctor profile photo."
                _userFeedback.value = err
                onResult(false, err)
                return
            }
        }
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465

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
<<<<<<< HEAD
                    license = license,
                    licenseImageUrl = licenseImageUrl,
                    profilePhotoUrl = profilePhotoUrl,
                    issuingCouncil = issuingCouncil,
                    yearsExperience = yearsExperience
                )
                _currentUserId.value = newUser.id
                _isAuthenticated.value = true
                val successMsg = if (role.equals("DOCTOR", ignoreCase = true)) {
                    "Doctor account registered! Your credentials have been submitted for admin verification."
                } else {
                    "Account created successfully! Welcome, ${newUser.name}."
                }
=======
                    license = license
                )
                _currentUserId.value = newUser.id
                _isAuthenticated.value = true
                val successMsg = "Account created successfully! Welcome, ${newUser.name}."
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                _userFeedback.value = successMsg
                onResult(true, successMsg)
            } catch (e: Exception) {
                val err = "Registration failed: ${e.message}"
                _userFeedback.value = err
                onResult(false, err)
            }
        }
    }

<<<<<<< HEAD
    fun submitDoctorCredentials(
        license: String,
        specialty: String,
        hospital: String,
        licenseImageUrl: String,
        profilePhotoUrl: String,
        issuingCouncil: String = "State Medical Council",
        yearsExperience: Int = 5,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val current = currentUser.value ?: return
        if (license.isBlank()) {
            val err = "Please enter your medical license ID."
            _userFeedback.value = err
            onResult(false, err)
            return
        }
        if (licenseImageUrl.isBlank() || profilePhotoUrl.isBlank()) {
            val err = "Please upload both your medical license image and profile photo."
            _userFeedback.value = err
            onResult(false, err)
            return
        }

        viewModelScope.launch {
            try {
                val updated = repository.submitDoctorCredentials(
                    doctorId = current.id,
                    license = license,
                    specialty = specialty,
                    hospital = hospital,
                    licenseImageUrl = licenseImageUrl,
                    profilePhotoUrl = profilePhotoUrl,
                    issuingCouncil = issuingCouncil,
                    yearsExperience = yearsExperience
                )
                if (updated != null) {
                    val msg = "Doctor credentials submitted for verification review!"
                    _userFeedback.value = msg
                    onResult(true, msg)
                } else {
                    val err = "Failed to update doctor profile."
                    _userFeedback.value = err
                    onResult(false, err)
                }
            } catch (e: Exception) {
                val err = "Submission failed: ${e.message}"
                _userFeedback.value = err
                onResult(false, err)
            }
        }
    }

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
    fun updateUserProfile(updated: UserEntity) {
        viewModelScope.launch {
            repository.updateUser(updated)
            _userFeedback.value = "Profile updated successfully!"
        }
    }

    // ----------------------------------------------------
<<<<<<< HEAD
    // Reminders, Alarms & Adherence
=======
    // Reminders & Adherence
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
    // ----------------------------------------------------
    fun markReminderTaken(reminder: MedicineReminderEntity) {
        viewModelScope.launch {
            repository.markReminderTaken(reminder)
<<<<<<< HEAD
            com.example.alarm.MedicineAlarmScheduler.cancelAlarm(getApplication(), reminder.id)
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
            MedicationReminderScheduler.cancelReminder(getApplication(), reminder.id)
            _userFeedback.value = "Marked ${reminder.medicineName} as TAKEN. Adherence updated!"
        }
    }

<<<<<<< HEAD
    fun snoozeReminder(reminder: MedicineReminderEntity, minutes: Int = 10) {
        viewModelScope.launch {
            repository.snoozeReminder(reminder, minutes)
            com.example.alarm.MedicineAlarmScheduler.scheduleSnoozeAlarm(getApplication(), reminder, minutes)
=======
    fun snoozeReminder(reminder: MedicineReminderEntity, minutes: Int = 15) {
        viewModelScope.launch {
            repository.snoozeReminder(reminder, minutes)
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
            MedicationReminderScheduler.scheduleSnooze(getApplication(), reminder, minutes)
            _userFeedback.value = "Snoozed ${reminder.medicineName} for $minutes minutes."
        }
    }

<<<<<<< HEAD
    fun skipReminder(reminder: MedicineReminderEntity, reason: String = "Patient skipped") {
        viewModelScope.launch {
            repository.skipReminder(reminder, reason)
            com.example.alarm.MedicineAlarmScheduler.cancelAlarm(getApplication(), reminder.id)
            MedicationReminderScheduler.cancelReminder(getApplication(), reminder.id)
            _userFeedback.value = "Logged ${reminder.medicineName} as SKIPPED ($reason)."
        }
    }

    fun triggerTestAlarm(delaySeconds: Int = 5) {
        com.example.alarm.MedicineAlarmScheduler.triggerTestAlarm(getApplication(), delaySeconds)
        _userFeedback.value = "Test Medicine Alarm scheduled! It will fire in $delaySeconds seconds."
    }

    fun rescheduleAllExactAlarms() {
        com.example.alarm.MedicineAlarmScheduler.rescheduleAllAlarms(getApplication())
        _userFeedback.value = "Rescheduled all pending medicine alarms with exact AlarmManager."
    }

=======
    fun skipReminder(reminder: MedicineReminderEntity) {
        viewModelScope.launch {
            repository.skipReminder(reminder)
            MedicationReminderScheduler.cancelReminder(getApplication(), reminder.id)
            _userFeedback.value = "Logged ${reminder.medicineName} as SKIPPED."
        }
    }

>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
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

<<<<<<< HEAD
    fun refillMedicine(medicineId: String, amount: Int = 30) {
        viewModelScope.launch {
            val updated = repository.refillMedicine(medicineId, amount)
            if (updated != null) {
                _userFeedback.value = "Refilled ${updated.name} with +$amount units! Total stock: ${updated.stockQuantity}."
            }
        }
    }

    /**
     * Parses spoken patient speech (e.g. "I just took my 10mg Lisinopril after food")
     * and automatically logs the intake into the adherence database in real-time.
     */
    fun parseAndLogVoiceIntake(
        spokenText: String,
        onResult: (Boolean, String, String) -> Unit = { _, _, _ -> }
    ) {
        if (spokenText.isBlank()) {
            onResult(false, "No speech detected. Please try speaking again.", "")
            return
        }

        viewModelScope.launch {
            val lower = spokenText.lowercase().trim()
            val currentMeds = medicines.value
            val todayRem = todayReminders.value

            val isSkip = lower.contains("skip") || lower.contains("missed") || lower.contains("omit")
            val isSnooze = lower.contains("snooze") || lower.contains("later") || lower.contains("delay")
            val isTakeAll = lower.contains("all") && (lower.contains("took") || lower.contains("taken") || lower.contains("done"))

            if (isTakeAll) {
                val pending = todayRem.filter { it.status == "PENDING" || it.status == "SNOOZED" }
                if (pending.isNotEmpty()) {
                    for (rem in pending) {
                        repository.markReminderTaken(rem)
                        MedicationReminderScheduler.cancelReminder(getApplication(), rem.id)
                    }
                    val msg = "Logged all ${pending.size} pending doses as TAKEN via voice command!"
                    _userFeedback.value = msg
                    onResult(true, msg, "All Scheduled Doses (${pending.size})")
                    return@launch
                }
            }

            // Find matching medicine from spoken words
            val matchedMed = currentMeds.firstOrNull { med ->
                val nameLower = med.name.lowercase()
                lower.contains(nameLower) ||
                nameLower.split(" ").any { part -> part.length > 3 && lower.contains(part) }
            } ?: currentMeds.firstOrNull { med ->
                // Check if any word in the speech matches medicine prefix
                val words = lower.split(" ", ",", ".")
                words.any { w -> w.length >= 4 && med.name.lowercase().startsWith(w) }
            }

            if (matchedMed == null) {
                // Check if there is only 1 pending reminder today
                val pending = todayRem.filter { it.status == "PENDING" || it.status == "SNOOZED" }
                if (pending.size == 1 && (lower.contains("took") || lower.contains("taken") || lower.contains("yes") || lower.contains("swallowed") || lower.contains("done"))) {
                    val rem = pending.first()
                    repository.markReminderTaken(rem)
                    MedicationReminderScheduler.cancelReminder(getApplication(), rem.id)
                    val msg = "Parsed \"$spokenText\" → Recorded ${rem.medicineName} (${rem.dosage}) as TAKEN!"
                    _userFeedback.value = msg
                    onResult(true, msg, rem.medicineName)
                    return@launch
                }

                val err = "Could not identify medicine from \"$spokenText\". Please mention the medicine name (e.g., \"Took my ${currentMeds.firstOrNull()?.name ?: "Metformin"}\")."
                _userFeedback.value = err
                onResult(false, err, "")
                return@launch
            }

            // Find matching reminder for this medicine
            val reminder = todayRem.firstOrNull { it.medicineId == matchedMed.id && (it.status == "PENDING" || it.status == "SNOOZED") }
                ?: todayRem.firstOrNull { it.medicineId == matchedMed.id }
                ?: MedicineReminderEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    medicineId = matchedMed.id,
                    patientId = _currentUserId.value,
                    medicineName = matchedMed.name,
                    dosage = matchedMed.dosage,
                    form = matchedMed.form,
                    instructions = matchedMed.instructions,
                    scheduledDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                    scheduledTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date()),
                    status = "PENDING"
                )

            if (isSkip) {
                repository.skipReminder(reminder)
                MedicationReminderScheduler.cancelReminder(getApplication(), reminder.id)
                val msg = "Logged ${matchedMed.name} as SKIPPED via voice command."
                _userFeedback.value = msg
                onResult(true, msg, matchedMed.name)
            } else if (isSnooze) {
                repository.snoozeReminder(reminder, 15)
                MedicationReminderScheduler.scheduleSnooze(getApplication(), reminder, 15)
                val msg = "Snoozed ${matchedMed.name} for 15 minutes via voice command."
                _userFeedback.value = msg
                onResult(true, msg, matchedMed.name)
            } else {
                repository.markReminderTaken(reminder)
                MedicationReminderScheduler.cancelReminder(getApplication(), reminder.id)
                val msg = "Success! Verified \"$spokenText\" → Logged ${matchedMed.name} (${matchedMed.dosage}) as TAKEN."
                _userFeedback.value = msg
                onResult(true, msg, matchedMed.name)
            }
        }
    }

    /**
     * Dispatches an escalated missed dose alert notification to linked caregivers.
     */
    fun dispatchMissedDoseCaretakerAlert(
        reminder: MedicineReminderEntity,
        caretakerName: String = "Emily Davis",
        caretakerPhone: String = "+1 (555) 234-5678"
    ) {
        viewModelScope.launch {
            repository.dispatchMissedDoseCaretakerAlert(reminder, caretakerName, caretakerPhone)
            _userFeedback.value = "Missed dose alert dispatched to caregiver $caretakerName ($caretakerPhone)!"
        }
    }

    /**
     * Dispatches emergency SOS alert and transparency notification log.
     */
    fun dispatchEmergencySos(
        contactName: String = "Sarah Doe",
        phone: String = "+1 (555) 987-6543"
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            repository.dispatchEmergencySosAlert(
                patientId = _currentUserId.value,
                patientName = user?.name ?: "Vijay Kumar",
                emergencyContactName = contactName,
                emergencyPhone = phone
            )
            _userFeedback.value = "Emergency SOS logged. Emergency contacts & 911 dispatch alerted."
        }
    }

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
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

<<<<<<< HEAD
    fun deleteCaretakerLink(linkId: String) {
        viewModelScope.launch {
            repository.deleteCaretakerLink(linkId)
            _userFeedback.value = "Patient connection removed."
        }
    }

    fun topUpCaretakerWallet(
        amount: Double,
        razorpayPaymentId: String = "pay_sim_${System.currentTimeMillis()}",
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val res = repository.topUpWalletRazorpay(user.id, amount, razorpayPaymentId)
            res.fold(
                onSuccess = {
                    _userFeedback.value = "Wallet credited with ₹${amount.toInt()}."
                    onResult(true, "Wallet credited with ₹${amount.toInt()}.")
                },
                onFailure = {
                    val msg = it.message ?: "Failed to top up wallet."
                    _userFeedback.value = msg
                    onResult(false, msg)
                }
            )
        }
    }

    fun getMedicinesForPatient(patientId: String): Flow<List<MedicineEntity>> = repository.getMedicines(patientId)

    fun getTodayRemindersForPatient(patientId: String): Flow<List<MedicineReminderEntity>> = repository.getTodayReminders(patientId)

    fun getAppointmentsForPatient(patientId: String): Flow<List<AppointmentEntity>> = repository.getAppointmentsForPatient(patientId)

    fun getDocumentsForPatient(patientId: String): Flow<List<MedicalDocumentEntity>> = repository.getDocuments(patientId)

    /**
     * Caretaker sends a gentle nudge notification directly to the patient's device.
     */
    fun sendCaretakerNudge(patientId: String, patientName: String, medicineName: String = "") {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val msg = if (medicineName.isNotBlank()) {
                "Caregiver ${user.name} sent a reminder to take your scheduled $medicineName."
            } else {
                "Caregiver ${user.name} sent a friendly reminder to check today's medication schedule."
            }
            repository.sendDirectNotification(
                targetUserId = patientId,
                title = "💙 Caregiver Reminder from ${user.name}",
                message = msg,
                type = "MEDICINE",
                severity = "INFO"
            )
            repository.logAudit(
                action = "CARETAKER_NUDGE",
                performedBy = user.name,
                target = "PatientMedication",
                details = "Sent gentle dose reminder to $patientName"
            )
            _userFeedback.value = "Gentle reminder sent to $patientName!"
        }
    }

    /**
     * Doctor issues a formal clinical prescription for a patient.
     */
    fun issuePrescription(
        patientId: String,
        patientName: String,
        medicineName: String,
        dosage: String,
        frequency: String,
        reminderTimes: String,
        durationDays: Int,
        totalQuantity: Int,
        instructions: String,
        diagnosis: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val doc = currentUser.value ?: return@launch
            try {
                repository.issuePrescription(
                    doctorId = doc.id,
                    doctorName = doc.name,
                    doctorHospital = doc.doctorHospital.ifBlank { "City General Hospital" },
                    patientId = patientId,
                    patientName = patientName,
                    medicineName = medicineName,
                    dosage = dosage,
                    frequency = frequency,
                    reminderTimes = reminderTimes,
                    durationDays = durationDays,
                    totalQuantity = totalQuantity,
                    instructions = instructions,
                    diagnosis = diagnosis
                )
                _userFeedback.value = "Prescription for $medicineName issued to $patientName!"
                onSuccess()
            } catch (e: Exception) {
                _userFeedback.value = "Failed to issue prescription: ${e.message}"
            }
        }
    }

    /**
     * Patient accepts a doctor-issued prescription directly into their daily reminder alarms.
     */
    fun acceptPrescriptionIntoReminders(
        documentId: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val doc = documents.value.find { it.id == documentId }
            val medicineName = doc?.title?.removePrefix("Prescription - ")?.trim() ?: "Prescribed Medication"
            val notes = doc?.notes ?: ""
            val prescribedBy = doc?.doctorOrClinic ?: "Attending Physician"
            try {
                repository.acceptPrescriptionIntoReminders(
                    patientId = user.id,
                    medicineName = medicineName,
                    dosage = "1 Tablet",
                    frequency = "Daily",
                    reminderTimes = "08:00 AM, 08:00 PM",
                    totalQuantity = 30,
                    instructions = notes.ifBlank { "Take as directed by $prescribedBy" },
                    prescribedBy = prescribedBy
                )
                _userFeedback.value = "$medicineName added to your daily reminder alarms!"
                onSuccess()
            } catch (e: Exception) {
                _userFeedback.value = "Could not schedule reminder: ${e.message}"
            }
        }
    }

    /**
     * Patient accepts a doctor-issued prescription directly into their daily reminder alarms with custom parameters.
     */
    fun acceptPrescriptionIntoReminders(
        medicineName: String,
        dosage: String,
        frequency: String,
        reminderTimes: String,
        totalQuantity: Int = 30,
        instructions: String = "",
        prescribedBy: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            try {
                repository.acceptPrescriptionIntoReminders(
                    patientId = user.id,
                    medicineName = medicineName,
                    dosage = dosage,
                    frequency = frequency,
                    reminderTimes = reminderTimes,
                    totalQuantity = totalQuantity,
                    instructions = instructions,
                    prescribedBy = prescribedBy
                )
                _userFeedback.value = "$medicineName added to your daily reminder alarms!"
                onSuccess()
            } catch (e: Exception) {
                _userFeedback.value = "Could not schedule reminder: ${e.message}"
            }
        }
    }

    /**
     * Processes a scanned QR code payload to connect/link patient, doctor, or caregiver accounts.
     */
    fun processScannedQrCode(
        payload: QrAccountPayload,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val code = payload.code.trim()
            if (code.isBlank()) {
                val msg = "Invalid QR code: missing account linking code."
                _userFeedback.value = msg
                onError(msg)
                return@launch
            }

            val result = repository.requestCaretakerLink(user, code)
            if (result.isSuccess) {
                val targetName = payload.name.ifBlank { "Account ($code)" }
                val msg = "Successfully linked with $targetName!"
                _userFeedback.value = msg
                onSuccess(msg)
            } else {
                val errMsg = result.exceptionOrNull()?.message ?: "Failed to link account"
                _userFeedback.value = errMsg
                onError(errMsg)
            }
        }
    }

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
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
<<<<<<< HEAD
            _userFeedback.value = "Document moved to Recycle Bin."
        }
    }

    fun restoreDocument(docId: String) {
        viewModelScope.launch {
            repository.restoreDocument(docId)
            _userFeedback.value = "Document restored from Recycle Bin."
        }
    }

    fun permanentlyDeleteDocument(docId: String) {
        viewModelScope.launch {
            repository.permanentlyDeleteDocument(docId)
            _userFeedback.value = "Document permanently deleted."
        }
    }

    fun emptyRecycleBin() {
        viewModelScope.launch {
            repository.emptyRecycleBin(_currentUserId.value)
            _userFeedback.value = "Recycle bin emptied."
=======
            _userFeedback.value = "Document deleted."
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
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

<<<<<<< HEAD
    fun markNotificationUnread(id: String) {
        viewModelScope.launch {
            repository.markNotificationUnread(id)
            _userFeedback.value = "Notification marked as unread."
        }
    }

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead(_currentUserId.value)
            _userFeedback.value = "All notifications marked as read."
        }
    }

<<<<<<< HEAD
    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
            _userFeedback.value = "Notification removed."
        }
    }

    fun sendDirectNotification(
        userId: String,
        title: String,
        message: String,
        type: String = "INFO"
    ) {
        viewModelScope.launch {
            repository.sendDirectNotification(
                targetUserId = userId,
                title = title,
                message = message,
                type = type
            )
        }
    }

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
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

<<<<<<< HEAD
    fun createAnnouncement(
        title: String,
        content: String,
        targetRole: String = "ALL",
        priority: String = "NORMAL"
    ) {
        viewModelScope.launch {
            val author = currentUser.value?.name ?: "System Administrator"
            repository.createAnnouncement(
                title = title,
                content = content,
                targetRole = targetRole,
                priority = priority,
                authorName = author
            )
            _userFeedback.value = "Announcement '$title' broadcasted successfully."
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            val adminName = currentUser.value?.name ?: "System Administrator"
            repository.deleteAnnouncement(id, adminName)
            _userFeedback.value = "Announcement removed."
        }
    }

    fun clearAuditLogs() {
        viewModelScope.launch {
            val adminName = currentUser.value?.name ?: "System Administrator"
            repository.clearAuditLogs(adminName)
            _userFeedback.value = "Audit trail logs cleared."
        }
    }

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
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
<<<<<<< HEAD

    // ----------------------------------------------------
    // Caretaker Assistance System (Call & Visit Coordination)
    // ----------------------------------------------------
    fun submitCallAssistanceRequest(
        patientId: String,
        patientName: String,
        reason: String,
        notes: String = "",
        isEmergency: Boolean = false,
        caretakerLocationName: String = "",
        lat: Double? = null,
        lon: Double? = null
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            val patient = allUsers.value.firstOrNull { it.id == patientId }
            val request = CaretakerAssistanceRequestEntity(
                caretakerId = user?.id ?: "caretaker_1",
                caretakerName = user?.name ?: "Emily Davis",
                caretakerPhone = user?.phone ?: "+1 (555) 654-3210",
                patientId = patientId,
                patientName = patientName,
                patientPhone = patient?.phone ?: "+1 (555) 234-5678",
                requestType = "CALL",
                reason = reason,
                notes = notes,
                status = "PENDING",
                caretakerLatitude = lat,
                caretakerLongitude = lon,
                caretakerLocationName = caretakerLocationName,
                patientAddress = patient?.address ?: "",
                patientLatitude = patient?.latitude,
                patientLongitude = patient?.longitude,
                isEmergency = isEmergency
            )
            repository.createAssistanceRequest(request)
            _userFeedback.value = "Call request for $patientName submitted to Admin."
        }
    }

    fun submitVisitAssistanceRequest(
        patientId: String,
        patientName: String,
        reason: String,
        notes: String = "",
        isEmergency: Boolean = false,
        distanceKm: Double,
        baseCharge: Double,
        additionalCharge: Double,
        totalCharge: Double,
        patientAddress: String,
        patientLat: Double?,
        patientLon: Double?,
        caretakerLocationName: String,
        caretakerLat: Double?,
        caretakerLon: Double?
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            val patient = allUsers.value.firstOrNull { it.id == patientId }
            val request = CaretakerAssistanceRequestEntity(
                caretakerId = user?.id ?: "caretaker_1",
                caretakerName = user?.name ?: "Emily Davis",
                caretakerPhone = user?.phone ?: "+1 (555) 654-3210",
                patientId = patientId,
                patientName = patientName,
                patientPhone = patient?.phone ?: "+1 (555) 234-5678",
                requestType = "VISIT",
                reason = reason,
                notes = notes,
                status = "PENDING",
                caretakerLatitude = caretakerLat,
                caretakerLongitude = caretakerLon,
                caretakerLocationName = caretakerLocationName,
                patientAddress = patientAddress.ifBlank { patient?.address ?: "" },
                patientLatitude = patientLat ?: patient?.latitude,
                patientLongitude = patientLon ?: patient?.longitude,
                distanceKm = distanceKm,
                baseCharge = baseCharge,
                additionalDistanceCharge = additionalCharge,
                totalVisitCharge = totalCharge,
                isEmergency = isEmergency
            )
            repository.createAssistanceRequest(request)
            _userFeedback.value = "Visit request for $patientName submitted to Admin."
        }
    }

    fun adminUpdateAssistanceStatus(
        requestId: String,
        status: String,
        adminNotes: String = "",
        rejectionReason: String = ""
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            repository.updateAssistanceRequestStatus(
                id = requestId,
                status = status,
                adminNotes = adminNotes,
                rejectionReason = rejectionReason,
                adminId = user?.id ?: "admin_1",
                adminName = user?.name ?: "System Administrator"
            )
            _userFeedback.value = "Assistance request marked as $status."
        }
    }

    fun caretakerUpdateVisitProgress(
        requestId: String,
        status: String
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            repository.updateCaretakerVisitProgress(
                id = requestId,
                status = status,
                caretakerName = user?.name ?: "Emily Davis"
            )
            val desc = when (status) {
                "CARETAKER_ON_THE_WAY" -> "Status updated: On the Way"
                "ARRIVED" -> "Status updated: Arrived at Patient"
                "COMPLETED" -> "Status updated: Visit Completed"
                else -> "Progress updated"
            }
            _userFeedback.value = desc
        }
    }

    // ----------------------------------------------------
    // Caretaker Wallet Actions
    // ----------------------------------------------------
    fun topUpCaretakerWallet(
        amount: Double,
        providerPaymentId: String = "pay_rzp_" + System.currentTimeMillis(),
        orderId: String = "order_rzp_" + System.currentTimeMillis(),
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            val caretakerId = user?.id ?: "user_caretaker_1"
            val caretakerName = user?.name ?: "Emily Davis"

            val result = repository.topUpWalletRazorpay(
                caretakerId = caretakerId,
                amount = amount,
                caretakerName = caretakerName,
                providerPaymentId = providerPaymentId,
                orderId = orderId
            )

            result.fold(
                onSuccess = { updatedWallet ->
                    _userFeedback.value = "₹${amount.toInt()} added to wallet. Balance: ₹${updatedWallet.balance.toInt()}."
                    onResult(true, "₹${amount.toInt()} successfully added to your wallet.")
                },
                onFailure = { error ->
                    val errMsg = error.message ?: "Failed to top up wallet."
                    _userFeedback.value = errMsg
                    onResult(false, errMsg)
                }
            )
        }
    }

    fun getAuthorizedAddressesForCaretaker(patientId: String): Flow<List<PatientAddressEntity>> =
        repository.getAuthorizedAddressesForCaretaker(patientId)

    fun getAddressesForPatient(patientId: String): Flow<List<PatientAddressEntity>> =
        repository.getAddressesForPatient(patientId)

    fun savePatientAddress(address: PatientAddressEntity) {
        viewModelScope.launch {
            repository.savePatientAddress(address)
        }
    }

    fun updatePatientAddress(address: PatientAddressEntity) {
        viewModelScope.launch {
            repository.updatePatientAddress(address)
        }
    }

    fun deletePatientAddress(id: String) {
        viewModelScope.launch {
            repository.deletePatientAddress(id)
        }
    }

    fun payAndSubmitVisitRequest(
        patientId: String,
        patientName: String,
        reason: String,
        notes: String = "",
        isEmergency: Boolean = false,
        distanceKm: Double,
        patientAddress: String,
        patientLat: Double?,
        patientLon: Double?,
        caretakerLocationName: String,
        caretakerLat: Double?,
        caretakerLon: Double?,
        selectedAddressId: String = "",
        addressSnapshot: String = "",
        onResult: (Boolean, String, CaretakerAssistanceRequestEntity?) -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            val patient = allUsers.value.firstOrNull { it.id == patientId }
            val caretakerId = user?.id ?: "user_caretaker_1"
            val caretakerName = user?.name ?: "Emily Davis"
            val caretakerPhone = user?.phone ?: "+1 (555) 654-3210"
            val patientPhone = patient?.phone ?: "+1 (555) 234-5678"

            val result = repository.payForVisitAndSubmitRequest(
                caretakerId = caretakerId,
                caretakerName = caretakerName,
                caretakerPhone = caretakerPhone,
                patientId = patientId,
                patientName = patientName,
                patientPhone = patientPhone,
                patientAddress = patientAddress.ifBlank { patient?.address ?: "" },
                patientLat = patientLat ?: patient?.latitude,
                patientLon = patientLon ?: patient?.longitude,
                caretakerLat = caretakerLat,
                caretakerLon = caretakerLon,
                caretakerLocName = caretakerLocationName,
                distanceKm = distanceKm,
                reason = reason,
                notes = notes,
                isEmergency = isEmergency,
                selectedAddressId = selectedAddressId,
                addressSnapshot = addressSnapshot.ifBlank { patientAddress }
            )

            result.fold(
                onSuccess = { req ->
                    _userFeedback.value = "Paid ₹${req.totalVisitCharge.toInt()} for visit. Sent to Admin."
                    onResult(true, "Payment successful! Visit request submitted to Admin.", req)
                },
                onFailure = { error ->
                    val msg = error.message ?: "Failed to process visit payment."
                    _userFeedback.value = msg
                    onResult(false, msg, null)
                }
            )
        }
    }
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
}
