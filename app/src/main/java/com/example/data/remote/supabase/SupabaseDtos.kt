package com.example.data.remote.supabase

import com.example.data.model.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ------------------------------------------------------------------------------
// 1. User DTO
// ------------------------------------------------------------------------------
@JsonClass(generateAdapter = false)
data class UserDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "role") val role: String,
    @Json(name = "phone") val phone: String? = "",
    @Json(name = "blood_group") val bloodGroup: String? = "O+",
    @Json(name = "allergies") val allergies: String? = "",
    @Json(name = "emergency_contact_name") val emergencyContactName: String? = "",
    @Json(name = "emergency_contact_phone") val emergencyContactPhone: String? = "",
    @Json(name = "emergency_contact_relation") val emergencyContactRelation: String? = "",
    @Json(name = "is_doctor_verified") val isDoctorVerified: Boolean? = false,
    @Json(name = "doctor_verification_status") val doctorVerificationStatus: String? = "APPROVED",
    @Json(name = "doctor_specialty") val doctorSpecialty: String? = "",
    @Json(name = "doctor_hospital") val doctorHospital: String? = "",
    @Json(name = "doctor_license") val doctorLicense: String? = "",
    @Json(name = "doctor_license_image_url") val doctorLicenseImageUrl: String? = "",
    @Json(name = "doctor_profile_photo_url") val doctorProfilePhotoUrl: String? = "",
    @Json(name = "doctor_issuing_council") val doctorIssuingCouncil: String? = "State Medical Council",
    @Json(name = "doctor_years_experience") val doctorYearsExperience: Int? = 8,
    @Json(name = "doctor_verification_submitted_at") val doctorVerificationSubmittedAt: Long? = 0L,
    @Json(name = "doctor_bio") val doctorBio: String? = "",
    @Json(name = "caretaker_linking_code") val caretakerLinkingCode: String? = "",
    @Json(name = "password") val password: String? = "password123",
    @Json(name = "date_of_birth") val dateOfBirth: String? = "1985-05-12",
    @Json(name = "address") val address: String? = "",
    @Json(name = "latitude") val latitude: Double? = 12.9716,
    @Json(name = "longitude") val longitude: Double? = 77.5946,
    @Json(name = "auth_provider") val authProvider: String? = "LOCAL",
    @Json(name = "provider_user_id") val providerUserId: String? = "",
    @Json(name = "profile_photo_url") val profilePhotoUrl: String? = "",
    @Json(name = "is_email_verified") val isEmailVerified: Boolean? = false,
    @Json(name = "account_status") val accountStatus: String? = "ACTIVE",
    @Json(name = "created_at") val createdAt: Long? = 0L
) {
    fun toEntity(): UserEntity = UserEntity(
        id = id,
        name = name,
        email = email,
        role = role,
        phone = phone ?: "",
        bloodGroup = bloodGroup ?: "O+",
        allergies = allergies ?: "",
        emergencyContactName = emergencyContactName ?: "",
        emergencyContactPhone = emergencyContactPhone ?: "",
        emergencyContactRelation = emergencyContactRelation ?: "",
        isDoctorVerified = isDoctorVerified ?: false,
        doctorVerificationStatus = doctorVerificationStatus ?: "APPROVED",
        doctorSpecialty = doctorSpecialty ?: "",
        doctorHospital = doctorHospital ?: "",
        doctorLicense = doctorLicense ?: "",
        doctorLicenseImageUrl = doctorLicenseImageUrl ?: "",
        doctorProfilePhotoUrl = doctorProfilePhotoUrl ?: "",
        doctorIssuingCouncil = doctorIssuingCouncil ?: "State Medical Council",
        doctorYearsExperience = doctorYearsExperience ?: 8,
        doctorVerificationSubmittedAt = doctorVerificationSubmittedAt ?: System.currentTimeMillis(),
        doctorBio = doctorBio ?: "",
        caretakerLinkingCode = caretakerLinkingCode ?: "",
        password = password ?: "password123",
        dateOfBirth = dateOfBirth ?: "1985-05-12",
        address = address ?: "",
        latitude = latitude ?: 12.9716,
        longitude = longitude ?: 77.5946,
        authProvider = authProvider ?: "LOCAL",
        providerUserId = providerUserId ?: "",
        profilePhotoUrl = profilePhotoUrl ?: "",
        isEmailVerified = isEmailVerified ?: false,
        accountStatus = accountStatus ?: "ACTIVE",
        createdAt = createdAt ?: System.currentTimeMillis()
    )
}

fun UserEntity.toDto(): UserDto = UserDto(
    id = id,
    name = name,
    email = email,
    role = role,
    phone = phone,
    bloodGroup = bloodGroup,
    allergies = allergies,
    emergencyContactName = emergencyContactName,
    emergencyContactPhone = emergencyContactPhone,
    emergencyContactRelation = emergencyContactRelation,
    isDoctorVerified = isDoctorVerified,
    doctorVerificationStatus = doctorVerificationStatus,
    doctorSpecialty = doctorSpecialty,
    doctorHospital = doctorHospital,
    doctorLicense = doctorLicense,
    doctorLicenseImageUrl = doctorLicenseImageUrl,
    doctorProfilePhotoUrl = doctorProfilePhotoUrl,
    doctorIssuingCouncil = doctorIssuingCouncil,
    doctorYearsExperience = doctorYearsExperience,
    doctorVerificationSubmittedAt = doctorVerificationSubmittedAt,
    doctorBio = doctorBio,
    caretakerLinkingCode = caretakerLinkingCode,
    password = password,
    dateOfBirth = dateOfBirth,
    address = address,
    latitude = latitude,
    longitude = longitude,
    authProvider = authProvider,
    providerUserId = providerUserId,
    profilePhotoUrl = profilePhotoUrl,
    isEmailVerified = isEmailVerified,
    accountStatus = accountStatus,
    createdAt = createdAt
)

// ------------------------------------------------------------------------------
// 2. Medicine DTO
// ------------------------------------------------------------------------------
@JsonClass(generateAdapter = false)
data class MedicineDto(
    @Json(name = "id") val id: String,
    @Json(name = "patient_id") val patientId: String,
    @Json(name = "name") val name: String,
    @Json(name = "dosage") val dosage: String,
    @Json(name = "form") val form: String? = "Tablet",
    @Json(name = "instructions") val instructions: String? = "Take with water",
    @Json(name = "frequency") val frequency: String? = "Daily",
    @Json(name = "reminder_times") val reminderTimes: String? = "08:00 AM",
    @Json(name = "start_date") val startDate: String,
    @Json(name = "end_date") val endDate: String,
    @Json(name = "stock_quantity") val stockQuantity: Int? = 30,
    @Json(name = "low_stock_threshold") val lowStockThreshold: Int? = 6,
    @Json(name = "notes") val notes: String? = "",
    @Json(name = "status") val status: String? = "ACTIVE",
    @Json(name = "color_hex") val colorHex: Long? = 1402304L,
    @Json(name = "created_at") val createdAt: Long? = 0L
) {
    fun toEntity(): MedicineEntity = MedicineEntity(
        id = id,
        patientId = patientId,
        name = name,
        dosage = dosage,
        form = form ?: "Tablet",
        instructions = instructions ?: "Take with water",
        frequency = frequency ?: "Daily",
        reminderTimes = reminderTimes ?: "08:00 AM",
        startDate = startDate,
        endDate = endDate,
        stockQuantity = stockQuantity ?: 30,
        lowStockThreshold = lowStockThreshold ?: 6,
        notes = notes ?: "",
        status = status ?: "ACTIVE",
        colorHex = colorHex ?: 0xFF1565C0,
        createdAt = createdAt ?: System.currentTimeMillis()
    )
}

fun MedicineEntity.toDto(): MedicineDto = MedicineDto(
    id = id,
    patientId = patientId,
    name = name,
    dosage = dosage,
    form = form,
    instructions = instructions,
    frequency = frequency,
    reminderTimes = reminderTimes,
    startDate = startDate,
    endDate = endDate,
    stockQuantity = stockQuantity,
    lowStockThreshold = lowStockThreshold,
    notes = notes,
    status = status,
    colorHex = colorHex,
    createdAt = createdAt
)

// ------------------------------------------------------------------------------
// 3. Medication DTO
// ------------------------------------------------------------------------------
@JsonClass(generateAdapter = false)
data class MedicationDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "dosage") val dosage: String,
    @Json(name = "frequency") val frequency: String,
    @Json(name = "time_slots") val timeSlots: String,
    @Json(name = "form") val form: String? = "Tablet",
    @Json(name = "instructions") val instructions: String? = "Take with water",
    @Json(name = "start_date") val startDate: String? = "",
    @Json(name = "end_date") val endDate: String? = "",
    @Json(name = "stock_quantity") val stockQuantity: Int? = 30,
    @Json(name = "notes") val notes: String? = "",
    @Json(name = "is_active") val isActive: Boolean? = true,
    @Json(name = "patient_id") val patientId: String? = "",
    @Json(name = "created_at") val createdAt: Long? = 0L
) {
    fun toEntity(): Medication = Medication(
        id = id,
        name = name,
        dosage = dosage,
        frequency = frequency,
        timeSlots = timeSlots,
        form = form ?: "Tablet",
        instructions = instructions ?: "Take with water",
        startDate = startDate ?: "",
        endDate = endDate ?: "",
        stockQuantity = stockQuantity ?: 30,
        notes = notes ?: "",
        isActive = isActive ?: true,
        patientId = patientId ?: "",
        createdAt = createdAt ?: System.currentTimeMillis()
    )
}

fun Medication.toDto(): MedicationDto = MedicationDto(
    id = id,
    name = name,
    dosage = dosage,
    frequency = frequency,
    timeSlots = timeSlots,
    form = form,
    instructions = instructions,
    startDate = startDate,
    endDate = endDate,
    stockQuantity = stockQuantity,
    notes = notes,
    isActive = isActive,
    patientId = patientId,
    createdAt = createdAt
)

// ------------------------------------------------------------------------------
// 4. Medicine Reminder DTO
// ------------------------------------------------------------------------------
@JsonClass(generateAdapter = false)
data class MedicineReminderDto(
    @Json(name = "id") val id: String,
    @Json(name = "medicine_id") val medicineId: String,
    @Json(name = "patient_id") val patientId: String,
    @Json(name = "medicine_name") val medicineName: String,
    @Json(name = "dosage") val dosage: String,
    @Json(name = "form") val form: String? = "Tablet",
    @Json(name = "instructions") val instructions: String? = "",
    @Json(name = "scheduled_date") val scheduledDate: String,
    @Json(name = "scheduled_time") val scheduledTime: String,
    @Json(name = "status") val status: String? = "PENDING",
    @Json(name = "taken_at_timestamp") val takenAtTimestamp: Long? = null,
    @Json(name = "snooze_until_time") val snoozeUntilTime: String? = null,
    @Json(name = "attempt_count") val attemptCount: Int? = 0,
    @Json(name = "snooze_count") val snoozeCount: Int? = 0,
    @Json(name = "last_alarm_timestamp") val lastAlarmTimestamp: Long? = null,
    @Json(name = "skip_reason") val skipReason: String? = null
) {
    fun toEntity(): MedicineReminderEntity = MedicineReminderEntity(
        id = id,
        medicineId = medicineId,
        patientId = patientId,
        medicineName = medicineName,
        dosage = dosage,
        form = form ?: "Tablet",
        instructions = instructions ?: "",
        scheduledDate = scheduledDate,
        scheduledTime = scheduledTime,
        status = status ?: "PENDING",
        takenAtTimestamp = takenAtTimestamp,
        snoozeUntilTime = snoozeUntilTime,
        attemptCount = attemptCount ?: 0,
        snoozeCount = snoozeCount ?: 0,
        lastAlarmTimestamp = lastAlarmTimestamp,
        skipReason = skipReason
    )
}

fun MedicineReminderEntity.toDto(): MedicineReminderDto = MedicineReminderDto(
    id = id,
    medicineId = medicineId,
    patientId = patientId,
    medicineName = medicineName,
    dosage = dosage,
    form = form,
    instructions = instructions,
    scheduledDate = scheduledDate,
    scheduledTime = scheduledTime,
    status = status,
    takenAtTimestamp = takenAtTimestamp,
    snoozeUntilTime = snoozeUntilTime,
    attemptCount = attemptCount,
    snoozeCount = snoozeCount,
    lastAlarmTimestamp = lastAlarmTimestamp,
    skipReason = skipReason
)

// ------------------------------------------------------------------------------
// 5. Medicine History DTO
// ------------------------------------------------------------------------------
@JsonClass(generateAdapter = false)
data class MedicineHistoryDto(
    @Json(name = "id") val id: String,
    @Json(name = "medicine_id") val medicineId: String,
    @Json(name = "patient_id") val patientId: String,
    @Json(name = "medicine_name") val medicineName: String,
    @Json(name = "dosage") val dosage: String,
    @Json(name = "scheduled_time") val scheduledTime: String,
    @Json(name = "action") val action: String,
    @Json(name = "action_timestamp") val actionTimestamp: Long? = 0L,
    @Json(name = "notes") val notes: String? = ""
) {
    fun toEntity(): MedicineHistoryEntity = MedicineHistoryEntity(
        id = id,
        medicineId = medicineId,
        patientId = patientId,
        medicineName = medicineName,
        dosage = dosage,
        scheduledTime = scheduledTime,
        action = action,
        actionTimestamp = actionTimestamp ?: System.currentTimeMillis(),
        notes = notes ?: ""
    )
}

fun MedicineHistoryEntity.toDto(): MedicineHistoryDto = MedicineHistoryDto(
    id = id,
    medicineId = medicineId,
    patientId = patientId,
    medicineName = medicineName,
    dosage = dosage,
    scheduledTime = scheduledTime,
    action = action,
    actionTimestamp = actionTimestamp,
    notes = notes
)

// ------------------------------------------------------------------------------
// 6. Appointment DTO
// ------------------------------------------------------------------------------
@JsonClass(generateAdapter = false)
data class AppointmentDto(
    @Json(name = "id") val id: String,
    @Json(name = "patient_id") val patientId: String,
    @Json(name = "patient_name") val patientName: String,
    @Json(name = "doctor_id") val doctorId: String,
    @Json(name = "doctor_name") val doctorName: String,
    @Json(name = "doctor_specialty") val doctorSpecialty: String,
    @Json(name = "appointment_date") val appointmentDate: String,
    @Json(name = "appointment_time") val appointmentTime: String,
    @Json(name = "reason") val reason: String,
    @Json(name = "status") val status: String? = "PENDING",
    @Json(name = "doctor_notes") val doctorNotes: String? = "",
    @Json(name = "reminder_enabled") val reminderEnabled: Boolean? = true,
    @Json(name = "reminder_minutes_before") val reminderMinutesBefore: Int? = 60,
    @Json(name = "created_at") val createdAt: Long? = 0L
) {
    fun toEntity(): AppointmentEntity = AppointmentEntity(
        id = id,
        patientId = patientId,
        patientName = patientName,
        doctorId = doctorId,
        doctorName = doctorName,
        doctorSpecialty = doctorSpecialty,
        appointmentDate = appointmentDate,
        appointmentTime = appointmentTime,
        reason = reason,
        status = status ?: "PENDING",
        doctorNotes = doctorNotes ?: "",
        reminderEnabled = reminderEnabled ?: true,
        reminderMinutesBefore = reminderMinutesBefore ?: 60,
        createdAt = createdAt ?: System.currentTimeMillis()
    )
}

fun AppointmentEntity.toDto(): AppointmentDto = AppointmentDto(
    id = id,
    patientId = patientId,
    patientName = patientName,
    doctorId = doctorId,
    doctorName = doctorName,
    doctorSpecialty = doctorSpecialty,
    appointmentDate = appointmentDate,
    appointmentTime = appointmentTime,
    reason = reason,
    status = status,
    doctorNotes = doctorNotes,
    reminderEnabled = reminderEnabled,
    reminderMinutesBefore = reminderMinutesBefore,
    createdAt = createdAt
)

// ------------------------------------------------------------------------------
// 7. Message DTO
// ------------------------------------------------------------------------------
@JsonClass(generateAdapter = false)
data class MessageDto(
    @Json(name = "id") val id: String,
    @Json(name = "conversation_id") val conversationId: String,
    @Json(name = "sender_id") val senderId: String,
    @Json(name = "sender_name") val senderName: String,
    @Json(name = "sender_role") val senderRole: String,
    @Json(name = "receiver_id") val receiverId: String,
    @Json(name = "receiver_name") val receiverName: String,
    @Json(name = "content") val content: String,
    @Json(name = "timestamp") val timestamp: Long? = 0L,
    @Json(name = "is_read") val isRead: Boolean? = false
) {
    fun toEntity(): MessageEntity = MessageEntity(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        senderName = senderName,
        senderRole = senderRole,
        receiverId = receiverId,
        receiverName = receiverName,
        content = content,
        timestamp = timestamp ?: System.currentTimeMillis(),
        isRead = isRead ?: false
    )
}

fun MessageEntity.toDto(): MessageDto = MessageDto(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    senderName = senderName,
    senderRole = senderRole,
    receiverId = receiverId,
    receiverName = receiverName,
    content = content,
    timestamp = timestamp,
    isRead = isRead
)

// ------------------------------------------------------------------------------
// 8. Caretaker Link DTO
// ------------------------------------------------------------------------------
@JsonClass(generateAdapter = false)
data class CaretakerLinkDto(
    @Json(name = "id") val id: String,
    @Json(name = "patient_id") val patientId: String,
    @Json(name = "patient_name") val patientName: String,
    @Json(name = "caretaker_id") val caretakerId: String,
    @Json(name = "caretaker_name") val caretakerName: String,
    @Json(name = "linking_code") val linkingCode: String,
    @Json(name = "status") val status: String? = "PENDING",
    @Json(name = "can_view_medicines") val canViewMedicines: Boolean? = true,
    @Json(name = "can_view_adherence") val canViewAdherence: Boolean? = true,
    @Json(name = "can_view_appointments") val canViewAppointments: Boolean? = true,
    @Json(name = "can_view_documents") val canViewDocuments: Boolean? = true,
    @Json(name = "can_view_location") val canViewLocation: Boolean? = true,
    @Json(name = "can_receive_alerts") val canReceiveAlerts: Boolean? = true,
    @Json(name = "created_at") val createdAt: Long? = 0L
) {
    fun toEntity(): CaretakerLinkEntity = CaretakerLinkEntity(
        id = id,
        patientId = patientId,
        patientName = patientName,
        caretakerId = caretakerId,
        caretakerName = caretakerName,
        linkingCode = linkingCode,
        status = status ?: "PENDING",
        canViewMedicines = canViewMedicines ?: true,
        canViewAdherence = canViewAdherence ?: true,
        canViewAppointments = canViewAppointments ?: true,
        canViewDocuments = canViewDocuments ?: true,
        canViewLocation = canViewLocation ?: true,
        canReceiveAlerts = canReceiveAlerts ?: true,
        createdAt = createdAt ?: System.currentTimeMillis()
    )
}

fun CaretakerLinkEntity.toDto(): CaretakerLinkDto = CaretakerLinkDto(
    id = id,
    patientId = patientId,
    patientName = patientName,
    caretakerId = caretakerId,
    caretakerName = caretakerName,
    linkingCode = linkingCode,
    status = status,
    canViewMedicines = canViewMedicines,
    canViewAdherence = canViewAdherence,
    canViewAppointments = canViewAppointments,
    canViewDocuments = canViewDocuments,
    canViewLocation = canViewLocation,
    canReceiveAlerts = canReceiveAlerts,
    createdAt = createdAt
)

// ------------------------------------------------------------------------------
// 9. Medical Document DTO
// ------------------------------------------------------------------------------
@JsonClass(generateAdapter = false)
data class MedicalDocumentDto(
    @Json(name = "id") val id: String,
    @Json(name = "patient_id") val patientId: String,
    @Json(name = "title") val title: String,
    @Json(name = "type") val type: String,
    @Json(name = "doctor_or_clinic") val doctorOrClinic: String,
    @Json(name = "date_added") val dateAdded: String,
    @Json(name = "file_size") val fileSize: String? = "1.0 MB",
    @Json(name = "file_size_bytes") val fileSizeBytes: Long? = 0L,
    @Json(name = "file_name") val fileName: String? = "",
    @Json(name = "file_format") val fileFormat: String? = "PDF",
    @Json(name = "mime_type") val mimeType: String? = "application/pdf",
    @Json(name = "file_uri") val fileUri: String? = "",
    @Json(name = "page_count") val pageCount: Int? = 1,
    @Json(name = "resolution") val resolution: String? = "",
    @Json(name = "tags") val tags: String? = "",
    @Json(name = "is_favorite") val isFavorite: Boolean? = false,
    @Json(name = "is_recycle_bin") val isRecycleBin: Boolean? = false,
    @Json(name = "deleted_at") val deletedAt: Long? = null,
    @Json(name = "upload_status") val uploadStatus: String? = "COMPLETED",
    @Json(name = "notes") val notes: String? = "",
    @Json(name = "created_at") val createdAt: Long? = 0L
) {
    fun toEntity(): MedicalDocumentEntity = MedicalDocumentEntity(
        id = id,
        patientId = patientId,
        title = title,
        type = type,
        doctorOrClinic = doctorOrClinic,
        dateAdded = dateAdded,
        fileSize = fileSize ?: "1.0 MB",
        fileSizeBytes = fileSizeBytes ?: 0L,
        fileName = fileName ?: "",
        fileFormat = fileFormat ?: "PDF",
        mimeType = mimeType ?: "application/pdf",
        fileUri = fileUri ?: "",
        pageCount = pageCount ?: 1,
        resolution = resolution ?: "",
        tags = tags ?: "",
        isFavorite = isFavorite ?: false,
        isRecycleBin = isRecycleBin ?: false,
        deletedAt = deletedAt,
        uploadStatus = uploadStatus ?: "COMPLETED",
        notes = notes ?: "",
        createdAt = createdAt ?: System.currentTimeMillis()
    )
}

fun MedicalDocumentEntity.toDto(): MedicalDocumentDto = MedicalDocumentDto(
    id = id,
    patientId = patientId,
    title = title,
    type = type,
    doctorOrClinic = doctorOrClinic,
    dateAdded = dateAdded,
    fileSize = fileSize,
    fileSizeBytes = fileSizeBytes,
    fileName = fileName,
    fileFormat = fileFormat,
    mimeType = mimeType,
    fileUri = fileUri,
    pageCount = pageCount,
    resolution = resolution,
    tags = tags,
    isFavorite = isFavorite,
    isRecycleBin = isRecycleBin,
    deletedAt = deletedAt,
    uploadStatus = uploadStatus,
    notes = notes,
    createdAt = createdAt
)

// ------------------------------------------------------------------------------
// 10. Notification DTO
// ------------------------------------------------------------------------------
@JsonClass(generateAdapter = false)
data class NotificationDto(
    @Json(name = "id") val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "title") val title: String,
    @Json(name = "message") val message: String,
    @Json(name = "type") val type: String,
    @Json(name = "is_read") val isRead: Boolean? = false,
    @Json(name = "timestamp") val timestamp: Long? = 0L,
    @Json(name = "recipient_role") val recipientRole: String? = "PATIENT_AND_CARETAKER",
    @Json(name = "recipient_name") val recipientName: String? = "",
    @Json(name = "delivery_channels") val deliveryChannels: String? = "PUSH,SMS",
    @Json(name = "severity") val severity: String? = "NORMAL",
    @Json(name = "related_entity_id") val relatedEntityId: String? = "",
    @Json(name = "action_taken") val actionTaken: String? = ""
) {
    fun toEntity(): NotificationEntity = NotificationEntity(
        id = id,
        userId = userId,
        title = title,
        message = message,
        type = type,
        isRead = isRead ?: false,
        timestamp = timestamp ?: System.currentTimeMillis(),
        recipientRole = recipientRole ?: "PATIENT_AND_CARETAKER",
        recipientName = recipientName ?: "",
        deliveryChannels = deliveryChannels ?: "PUSH,SMS",
        severity = severity ?: "NORMAL",
        relatedEntityId = relatedEntityId ?: "",
        actionTaken = actionTaken ?: ""
    )
}

fun NotificationEntity.toDto(): NotificationDto = NotificationDto(
    id = id,
    userId = userId,
    title = title,
    message = message,
    type = type,
    isRead = isRead,
    timestamp = timestamp,
    recipientRole = recipientRole,
    recipientName = recipientName,
    deliveryChannels = deliveryChannels,
    severity = severity,
    relatedEntityId = relatedEntityId,
    actionTaken = actionTaken
)

// ------------------------------------------------------------------------------
// 11. Audit Log DTO
// ------------------------------------------------------------------------------
@JsonClass(generateAdapter = false)
data class AuditLogDto(
    @Json(name = "id") val id: String,
    @Json(name = "action") val action: String,
    @Json(name = "performed_by") val performedBy: String,
    @Json(name = "target_resource") val targetResource: String,
    @Json(name = "details") val details: String,
    @Json(name = "timestamp") val timestamp: Long? = 0L
) {
    fun toEntity(): AuditLogEntity = AuditLogEntity(
        id = id,
        action = action,
        performedBy = performedBy,
        targetResource = targetResource,
        details = details,
        timestamp = timestamp ?: System.currentTimeMillis()
    )
}

fun AuditLogEntity.toDto(): AuditLogDto = AuditLogDto(
    id = id,
    action = action,
    performedBy = performedBy,
    targetResource = targetResource,
    details = details,
    timestamp = timestamp
)

// ------------------------------------------------------------------------------
// 12. Announcement DTO
// ------------------------------------------------------------------------------
@JsonClass(generateAdapter = false)
data class AnnouncementDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "target_role") val targetRole: String? = "ALL",
    @Json(name = "priority") val priority: String? = "NORMAL",
    @Json(name = "author_name") val authorName: String? = "System Administrator",
    @Json(name = "is_active") val isActive: Boolean? = true,
    @Json(name = "expires_at") val expiresAt: String? = "",
    @Json(name = "timestamp") val timestamp: Long? = 0L
) {
    fun toEntity(): AnnouncementEntity = AnnouncementEntity(
        id = id,
        title = title,
        content = content,
        targetRole = targetRole ?: "ALL",
        priority = priority ?: "NORMAL",
        authorName = authorName ?: "System Administrator",
        isActive = isActive ?: true,
        expiresAt = expiresAt ?: "",
        timestamp = timestamp ?: System.currentTimeMillis()
    )
}

fun AnnouncementEntity.toDto(): AnnouncementDto = AnnouncementDto(
    id = id,
    title = title,
    content = content,
    targetRole = targetRole,
    priority = priority,
    authorName = authorName,
    isActive = isActive,
    expiresAt = expiresAt,
    timestamp = timestamp
)

// ------------------------------------------------------------------------------
// 13. Patient Address DTO
// ------------------------------------------------------------------------------
@JsonClass(generateAdapter = false)
data class PatientAddressDto(
    @Json(name = "id") val id: String,
    @Json(name = "patient_id") val patientId: String,
    @Json(name = "title") val title: String,
    @Json(name = "address_type") val addressType: String? = "HOME",
    @Json(name = "address_line1") val addressLine1: String,
    @Json(name = "address_line2") val addressLine2: String? = "",
    @Json(name = "city") val city: String? = "Bengaluru",
    @Json(name = "state") val state: String? = "Karnataka",
    @Json(name = "pincode") val pincode: String? = "560038",
    @Json(name = "landmark") val landmark: String? = "",
    @Json(name = "full_address") val fullAddress: String? = "",
    @Json(name = "latitude") val latitude: Double? = 12.9716,
    @Json(name = "longitude") val longitude: Double? = 77.5946,
    @Json(name = "is_default") val isDefault: Boolean? = false,
    @Json(name = "allow_caretaker_visit") val allowCaretakerVisit: Boolean? = true,
    @Json(name = "allow_doctor") val allowDoctor: Boolean? = true,
    @Json(name = "is_private") val isPrivate: Boolean? = false,
    @Json(name = "is_temporary") val isTemporary: Boolean? = false,
    @Json(name = "created_at") val createdAt: Long? = 0L
) {
    fun toEntity(): PatientAddressEntity = PatientAddressEntity(
        id = id,
        patientId = patientId,
        title = title,
        addressType = addressType ?: "HOME",
        addressLine1 = addressLine1,
        addressLine2 = addressLine2 ?: "",
        city = city ?: "Bengaluru",
        state = state ?: "Karnataka",
        pincode = pincode ?: "560038",
        landmark = landmark ?: "",
        fullAddress = fullAddress ?: "",
        latitude = latitude ?: 12.9716,
        longitude = longitude ?: 77.5946,
        isDefault = isDefault ?: false,
        allowCaretakerVisit = allowCaretakerVisit ?: true,
        allowDoctor = allowDoctor ?: true,
        isPrivate = isPrivate ?: false,
        isTemporary = isTemporary ?: false,
        createdAt = createdAt ?: System.currentTimeMillis()
    )
}

fun PatientAddressEntity.toDto(): PatientAddressDto = PatientAddressDto(
    id = id,
    patientId = patientId,
    title = title,
    addressType = addressType,
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    city = city,
    state = state,
    pincode = pincode,
    landmark = landmark,
    fullAddress = fullAddress,
    latitude = latitude,
    longitude = longitude,
    isDefault = isDefault,
    allowCaretakerVisit = allowCaretakerVisit,
    allowDoctor = allowDoctor,
    isPrivate = isPrivate,
    isTemporary = isTemporary,
    createdAt = createdAt
)

// ------------------------------------------------------------------------------
// 14. Caretaker Assistance Request DTO
// ------------------------------------------------------------------------------
@JsonClass(generateAdapter = false)
data class CaretakerAssistanceRequestDto(
    @Json(name = "id") val id: String,
    @Json(name = "caretaker_id") val caretakerId: String,
    @Json(name = "caretaker_name") val caretakerName: String,
    @Json(name = "caretaker_phone") val caretakerPhone: String? = "",
    @Json(name = "patient_id") val patientId: String,
    @Json(name = "patient_name") val patientName: String,
    @Json(name = "patient_phone") val patientPhone: String? = "",
    @Json(name = "request_type") val requestType: String,
    @Json(name = "reason") val reason: String,
    @Json(name = "notes") val notes: String? = "",
    @Json(name = "status") val status: String? = "PAID_PENDING_ADMIN",
    @Json(name = "caretaker_latitude") val caretakerLatitude: Double? = null,
    @Json(name = "caretaker_longitude") val caretakerLongitude: Double? = null,
    @Json(name = "caretaker_location_name") val caretakerLocationName: String? = "",
    @Json(name = "selected_address_id") val selectedAddressId: String? = "",
    @Json(name = "address_snapshot") val addressSnapshot: String? = "",
    @Json(name = "patient_address") val patientAddress: String? = "",
    @Json(name = "patient_latitude") val patientLatitude: Double? = null,
    @Json(name = "patient_longitude") val patientLongitude: Double? = null,
    @Json(name = "distance_km") val distanceKm: Double? = 0.0,
    @Json(name = "billable_km") val billableKm: Int? = 3,
    @Json(name = "base_charge") val baseCharge: Double? = 30.0,
    @Json(name = "additional_distance_charge") val additionalDistanceCharge: Double? = 0.0,
    @Json(name = "total_visit_charge") val totalVisitCharge: Double? = 30.0,
    @Json(name = "payment_status") val paymentStatus: String? = "UNPAID",
    @Json(name = "payment_id") val paymentId: String? = "",
    @Json(name = "transaction_id") val transactionId: String? = "",
    @Json(name = "paid_amount") val paidAmount: Double? = 0.0,
    @Json(name = "paid_at") val paidAt: Long? = null,
    @Json(name = "refund_transaction_id") val refundTransactionId: String? = "",
    @Json(name = "refunded_at") val refundedAt: Long? = null,
    @Json(name = "admin_notes") val adminNotes: String? = "",
    @Json(name = "rejection_reason") val rejectionReason: String? = "",
    @Json(name = "admin_id") val adminId: String? = "",
    @Json(name = "admin_name") val adminName: String? = "",
    @Json(name = "is_emergency") val isEmergency: Boolean? = false,
    @Json(name = "created_at") val createdAt: Long? = 0L,
    @Json(name = "updated_at") val updatedAt: Long? = 0L
) {
    fun toEntity(): CaretakerAssistanceRequestEntity = CaretakerAssistanceRequestEntity(
        id = id,
        caretakerId = caretakerId,
        caretakerName = caretakerName,
        caretakerPhone = caretakerPhone ?: "",
        patientId = patientId,
        patientName = patientName,
        patientPhone = patientPhone ?: "",
        requestType = requestType,
        reason = reason,
        notes = notes ?: "",
        status = status ?: "PAID_PENDING_ADMIN",
        caretakerLatitude = caretakerLatitude,
        caretakerLongitude = caretakerLongitude,
        caretakerLocationName = caretakerLocationName ?: "",
        selectedAddressId = selectedAddressId ?: "",
        addressSnapshot = addressSnapshot ?: "",
        patientAddress = patientAddress ?: "",
        patientLatitude = patientLatitude,
        patientLongitude = patientLongitude,
        distanceKm = distanceKm ?: 0.0,
        billableKm = billableKm ?: 3,
        baseCharge = baseCharge ?: 30.0,
        additionalDistanceCharge = additionalDistanceCharge ?: 0.0,
        totalVisitCharge = totalVisitCharge ?: 30.0,
        paymentStatus = paymentStatus ?: "UNPAID",
        paymentId = paymentId ?: "",
        transactionId = transactionId ?: "",
        paidAmount = paidAmount ?: 0.0,
        paidAt = paidAt,
        refundTransactionId = refundTransactionId ?: "",
        refundedAt = refundedAt,
        adminNotes = adminNotes ?: "",
        rejectionReason = rejectionReason ?: "",
        adminId = adminId ?: "",
        adminName = adminName ?: "",
        isEmergency = isEmergency ?: false,
        createdAt = createdAt ?: System.currentTimeMillis(),
        updatedAt = updatedAt ?: System.currentTimeMillis()
    )
}

fun CaretakerAssistanceRequestEntity.toDto(): CaretakerAssistanceRequestDto = CaretakerAssistanceRequestDto(
    id = id,
    caretakerId = caretakerId,
    caretakerName = caretakerName,
    caretakerPhone = caretakerPhone,
    patientId = patientId,
    patientName = patientName,
    patientPhone = patientPhone,
    requestType = requestType,
    reason = reason,
    notes = notes,
    status = status,
    caretakerLatitude = caretakerLatitude,
    caretakerLongitude = caretakerLongitude,
    caretakerLocationName = caretakerLocationName,
    selectedAddressId = selectedAddressId,
    addressSnapshot = addressSnapshot,
    patientAddress = patientAddress,
    patientLatitude = patientLatitude,
    patientLongitude = patientLongitude,
    distanceKm = distanceKm,
    billableKm = billableKm,
    baseCharge = baseCharge,
    additionalDistanceCharge = additionalDistanceCharge,
    totalVisitCharge = totalVisitCharge,
    paymentStatus = paymentStatus,
    paymentId = paymentId,
    transactionId = transactionId,
    paidAmount = paidAmount,
    paidAt = paidAt,
    refundTransactionId = refundTransactionId,
    refundedAt = refundedAt,
    adminNotes = adminNotes,
    rejectionReason = rejectionReason,
    adminId = adminId,
    adminName = adminName,
    isEmergency = isEmergency,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// ------------------------------------------------------------------------------
// 15. Wallet DTO
// ------------------------------------------------------------------------------
@JsonClass(generateAdapter = false)
data class WalletDto(
    @Json(name = "caretaker_id") val caretakerId: String,
    @Json(name = "balance") val balance: Double? = 450.0,
    @Json(name = "max_balance") val maxBalance: Double? = 1000.0,
    @Json(name = "currency") val currency: String? = "INR",
    @Json(name = "updated_at") val updatedAt: Long? = 0L
) {
    fun toEntity(): WalletEntity = WalletEntity(
        caretakerId = caretakerId,
        balance = balance ?: 450.0,
        maxBalance = maxBalance ?: 1000.0,
        currency = currency ?: "INR",
        updatedAt = updatedAt ?: System.currentTimeMillis()
    )
}

fun WalletEntity.toDto(): WalletDto = WalletDto(
    caretakerId = caretakerId,
    balance = balance,
    maxBalance = maxBalance,
    currency = currency,
    updatedAt = updatedAt
)

// ------------------------------------------------------------------------------
// 16. Wallet Transaction DTO
// ------------------------------------------------------------------------------
@JsonClass(generateAdapter = false)
data class WalletTransactionDto(
    @Json(name = "transaction_id") val transactionId: String,
    @Json(name = "caretaker_id") val caretakerId: String,
    @Json(name = "type") val type: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "balance_before") val balanceBefore: Double? = 0.0,
    @Json(name = "balance_after") val balanceAfter: Double? = 0.0,
    @Json(name = "status") val status: String? = "SUCCESS",
    @Json(name = "description") val description: String? = "",
    @Json(name = "payment_provider") val paymentProvider: String? = "Razorpay",
    @Json(name = "provider_payment_id") val providerPaymentId: String? = "",
    @Json(name = "order_id") val orderId: String? = "",
    @Json(name = "related_request_id") val relatedRequestId: String? = "",
    @Json(name = "patient_name") val patientName: String? = "",
    @Json(name = "created_at") val createdAt: Long? = 0L
) {
    fun toEntity(): WalletTransactionEntity = WalletTransactionEntity(
        transactionId = transactionId,
        caretakerId = caretakerId,
        type = type,
        amount = amount,
        balanceBefore = balanceBefore ?: 0.0,
        balanceAfter = balanceAfter ?: 0.0,
        status = status ?: "SUCCESS",
        description = description ?: "",
        paymentProvider = paymentProvider ?: "Razorpay",
        providerPaymentId = providerPaymentId ?: "",
        orderId = orderId ?: "",
        relatedRequestId = relatedRequestId ?: "",
        patientName = patientName ?: "",
        createdAt = createdAt ?: System.currentTimeMillis()
    )
}

fun WalletTransactionEntity.toDto(): WalletTransactionDto = WalletTransactionDto(
    transactionId = transactionId,
    caretakerId = caretakerId,
    type = type,
    amount = amount,
    balanceBefore = balanceBefore,
    balanceAfter = balanceAfter,
    status = status,
    description = description,
    paymentProvider = paymentProvider,
    providerPaymentId = providerPaymentId,
    orderId = orderId,
    relatedRequestId = relatedRequestId,
    patientName = patientName,
    createdAt = createdAt
)
