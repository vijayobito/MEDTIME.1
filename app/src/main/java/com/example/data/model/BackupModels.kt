package com.example.data.model

data class CloudBackupSummary(
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDate: String = "",
    val usersCount: Int = 0,
    val medicinesCount: Int = 0,
    val remindersCount: Int = 0,
    val appointmentsCount: Int = 0,
    val documentsCount: Int = 0,
    val messagesCount: Int = 0,
    val totalRecords: Int = 0,
    val sizeKb: Double = 0.0,
    val checksum: String = "",
    val cloudStatus: String = "ENCRYPTED_AND_SYNCED"
)

data class DatabaseBackupPayload(
    val version: Int = 1,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val users: List<UserEntity> = emptyList(),
    val medicines: List<MedicineEntity> = emptyList(),
    val reminders: List<MedicineReminderEntity> = emptyList(),
    val history: List<MedicineHistoryEntity> = emptyList(),
    val appointments: List<AppointmentEntity> = emptyList(),
    val messages: List<MessageEntity> = emptyList(),
    val caretakerLinks: List<CaretakerLinkEntity> = emptyList(),
    val medicalDocuments: List<MedicalDocumentEntity> = emptyList(),
    val notifications: List<NotificationEntity> = emptyList(),
    val auditLogs: List<AuditLogEntity> = emptyList()
)
