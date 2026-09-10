package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String, // "PATIENT", "DOCTOR", "CARETAKER", "ADMIN"
    val phone: String = "",
    val bloodGroup: String = "O+",
    val allergies: String = "Penicillin, Peanuts",
    val emergencyContactName: String = "Sarah Doe",
    val emergencyContactPhone: String = "911",
    val emergencyContactRelation: String = "Spouse",
    val isDoctorVerified: Boolean = false,
    val doctorVerificationStatus: String = "APPROVED", // "PENDING", "UNDER_REVIEW", "APPROVED", "REJECTED"
    val doctorSpecialty: String = "",
    val doctorHospital: String = "",
    val doctorLicense: String = "",
    val doctorBio: String = "",
    val caretakerLinkingCode: String = "MED-7842",
    val password: String = "password123",
    val dateOfBirth: String = "1985-05-12",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey val id: String,
    val patientId: String,
    val name: String,
    val dosage: String, // e.g. "500 mg"
    val form: String, // "Tablet", "Capsule", "Syrup", "Injection", "Drops", "Inhaler"
    val instructions: String, // "After food", "Before food", "With water", "Empty stomach"
    val frequency: String, // "Daily", "Twice a day", "Three times a day", "As needed"
    val reminderTimes: String, // e.g. "08:00 AM,02:00 PM,08:00 PM"
    val startDate: String,
    val endDate: String,
    val stockQuantity: Int = 30,
    val notes: String = "",
    val status: String = "ACTIVE", // "ACTIVE", "PAUSED", "COMPLETED"
    val colorHex: Long = 0xFF1565C0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "medicine_reminders")
data class MedicineReminderEntity(
    @PrimaryKey val id: String,
    val medicineId: String,
    val patientId: String,
    val medicineName: String,
    val dosage: String,
    val form: String,
    val instructions: String,
    val scheduledDate: String, // YYYY-MM-DD
    val scheduledTime: String, // e.g. "08:00 AM"
    val status: String = "PENDING", // "PENDING", "TAKEN", "MISSED", "SKIPPED", "SNOOZED"
    val takenAtTimestamp: Long? = null,
    val snoozeUntilTime: String? = null
)

@Entity(tableName = "medicine_history")
data class MedicineHistoryEntity(
    @PrimaryKey val id: String,
    val medicineId: String,
    val patientId: String,
    val medicineName: String,
    val dosage: String,
    val scheduledTime: String,
    val action: String, // "TAKEN", "SKIPPED", "MISSED", "SNOOZED"
    val actionTimestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey val id: String,
    val patientId: String,
    val patientName: String,
    val doctorId: String,
    val doctorName: String,
    val doctorSpecialty: String,
    val appointmentDate: String, // YYYY-MM-DD
    val appointmentTime: String, // e.g. "10:30 AM"
    val reason: String,
    val status: String = "PENDING", // "PENDING", "ACCEPTED", "REJECTED", "COMPLETED", "CANCELLED"
    val doctorNotes: String = "",
    val reminderEnabled: Boolean = true,
    val reminderMinutesBefore: Int = 60,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val senderRole: String,
    val receiverId: String,
    val receiverName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "caretaker_links")
data class CaretakerLinkEntity(
    @PrimaryKey val id: String,
    val patientId: String,
    val patientName: String,
    val caretakerId: String,
    val caretakerName: String,
    val linkingCode: String,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val canViewMedicines: Boolean = true,
    val canViewAdherence: Boolean = true,
    val canViewAppointments: Boolean = true,
    val canReceiveAlerts: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "medical_documents")
data class MedicalDocumentEntity(
    @PrimaryKey val id: String,
    val patientId: String,
    val title: String,
    val type: String, // "Prescription", "Lab Report", "Scan", "Insurance", "Doctor Notes", "Discharge Summary"
    val doctorOrClinic: String,
    val dateAdded: String,
    val fileSize: String, // e.g. "1.4 MB"
    val fileSizeBytes: Long = 0L, // Exact size in bytes
    val fileName: String = "", // e.g. "lipid_panel_2026.pdf"
    val fileFormat: String = "PDF", // "PDF" or "IMAGE"
    val mimeType: String = "application/pdf", // e.g. "application/pdf", "image/jpeg", "image/png"
    val fileUri: String = "", // Content URI or storage path
    val pageCount: Int = 1, // Number of pages (for PDF)
    val resolution: String = "", // e.g. "2048x1536 px" or "A4"
    val tags: String = "", // Comma-separated tags
    val isFavorite: Boolean = false,
    val uploadStatus: String = "COMPLETED", // "COMPLETED", "PROCESSING", "FAILED"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // "MEDICINE", "APPOINTMENT", "CARETAKER", "DOCTOR", "SYSTEM"
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val action: String,
    val performedBy: String,
    val targetResource: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
