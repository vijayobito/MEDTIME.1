package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
<<<<<<< HEAD
import java.util.UUID

/**
 * Room Entity and data class representing a Medication schedule.
 * Contains medication details including name, dosage, frequency, and scheduled time slots.
 */
@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dosage: String,
    val frequency: String,
    val timeSlots: String, // Comma-separated time slots, e.g. "08:00 AM, 02:00 PM, 08:00 PM"
    val form: String = "Tablet", // e.g. "Tablet", "Capsule", "Syrup", "Drops", "Inhaler"
    val instructions: String = "Take with water",
    val startDate: String = "",
    val endDate: String = "",
    val stockQuantity: Int = 30,
    val notes: String = "",
    val isActive: Boolean = true,
    val patientId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Helper to retrieve time slots as a parsed list of formatted time strings.
     */
    fun getTimeSlotsList(): List<String> {
        return if (timeSlots.isBlank()) emptyList()
        else timeSlots.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465

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
<<<<<<< HEAD
    val doctorLicenseImageUrl: String = "",
    val doctorProfilePhotoUrl: String = "",
    val doctorIssuingCouncil: String = "State Medical Council",
    val doctorYearsExperience: Int = 8,
    val doctorVerificationSubmittedAt: Long = System.currentTimeMillis(),
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
    val doctorBio: String = "",
    val caretakerLinkingCode: String = "MED-7842",
    val password: String = "password123",
    val dateOfBirth: String = "1985-05-12",
<<<<<<< HEAD
    val address: String = "124 Indiranagar 100ft Rd, Bengaluru, Karnataka 560038",
    val latitude: Double = 12.9716,
    val longitude: Double = 77.5946,
    val authProvider: String = "LOCAL", // "LOCAL", "GOOGLE", "FACEBOOK", "APPLE"
    val providerUserId: String = "",
    val profilePhotoUrl: String = "",
    val isEmailVerified: Boolean = false,
    val accountStatus: String = "ACTIVE", // "ACTIVE", "SUSPENDED", "PROFILE_INCOMPLETE"
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
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
<<<<<<< HEAD
    val frequency: String, // "Daily", "Twice a day", "Three times a day", "Every 8 hours", "As needed"
=======
    val frequency: String, // "Daily", "Twice a day", "Three times a day", "As needed"
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
    val reminderTimes: String, // e.g. "08:00 AM,02:00 PM,08:00 PM"
    val startDate: String,
    val endDate: String,
    val stockQuantity: Int = 30,
<<<<<<< HEAD
    val lowStockThreshold: Int = 6, // Dosage-frequency based buffer threshold
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
    val notes: String = "",
    val status: String = "ACTIVE", // "ACTIVE", "PAUSED", "COMPLETED"
    val colorHex: Long = 0xFF1565C0,
    val createdAt: Long = System.currentTimeMillis()
<<<<<<< HEAD
) {
    /**
     * Calculates daily dosage frequency count based on reminder times or frequency label.
     */
    fun getDailyDoseCount(): Int {
        val times = if (reminderTimes.isNotBlank()) reminderTimes.split(",").filter { it.isNotBlank() }.size else 0
        if (times > 0) return times
        return when (frequency.trim().lowercase()) {
            "twice a day", "every 12 hours" -> 2
            "three times a day", "every 8 hours" -> 3
            "four times a day", "every 6 hours" -> 4
            "as needed" -> 1
            else -> 1
        }
    }

    /**
     * Calculates the dosage-frequency dynamic low stock threshold (e.g. 3-day supply buffer).
     */
    fun calculateThreshold(): Int {
        val daily = getDailyDoseCount()
        val calculated = daily * 3 // 3-day buffer
        return maxOf(calculated, lowStockThreshold)
    }

    /**
     * Calculates estimated days of supply remaining.
     */
    fun getEstimatedDaysRemaining(): Double {
        val daily = getDailyDoseCount().toDouble()
        if (daily <= 0.0) return stockQuantity.toDouble()
        return stockQuantity.toDouble() / daily
    }

    fun isLowStock(): Boolean {
        return stockQuantity <= calculateThreshold()
    }
}
=======
)
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465

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
<<<<<<< HEAD
    val snoozeUntilTime: String? = null,
    val attemptCount: Int = 0,
    val snoozeCount: Int = 0,
    val lastAlarmTimestamp: Long? = null,
    val skipReason: String? = null
=======
    val snoozeUntilTime: String? = null
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
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
<<<<<<< HEAD
    val canViewDocuments: Boolean = true,
    val canViewLocation: Boolean = true,
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
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
<<<<<<< HEAD
    val isRecycleBin: Boolean = false,
    val deletedAt: Long? = null,
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
    val uploadStatus: String = "COMPLETED", // "COMPLETED", "PROCESSING", "FAILED"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
<<<<<<< HEAD
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // "MEDICINE", "LOW_STOCK", "MISSED_DOSE", "EMERGENCY_SOS", "APPOINTMENT", "CARETAKER", "DOCTOR", "SYSTEM"
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val recipientRole: String = "PATIENT_AND_CARETAKER", // "PATIENT", "CARETAKER", "PATIENT_AND_CARETAKER", "EMERGENCY_SERVICES"
    val recipientName: String = "Emily Davis (Caretaker)",
    val deliveryChannels: String = "PUSH,SMS", // "PUSH", "SMS", "PHONE_CALL", "PUSH,SMS"
    val severity: String = "NORMAL", // "INFO", "NORMAL", "WARNING", "CRITICAL"
    val relatedEntityId: String = "",
    val actionTaken: String = ""
=======
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // "MEDICINE", "APPOINTMENT", "CARETAKER", "DOCTOR", "SYSTEM"
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
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
<<<<<<< HEAD

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val targetRole: String = "ALL", // "ALL", "PATIENT", "DOCTOR", "CARETAKER"
    val priority: String = "NORMAL", // "INFO", "NORMAL", "URGENT", "CRITICAL"
    val authorName: String = "System Administrator",
    val isActive: Boolean = true,
    val expiresAt: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "patient_saved_addresses")
data class PatientAddressEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val patientId: String,
    val title: String, // "Patient's Home", "Patient's Alternate Address", "Work", "Clinic"
    val addressType: String = "HOME", // "HOME", "ALTERNATE", "WORK", "OTHER", "TEMPORARY"
    val addressLine1: String,
    val addressLine2: String = "",
    val city: String = "Bengaluru",
    val state: String = "Karnataka",
    val pincode: String = "560038",
    val landmark: String = "",
    val fullAddress: String = "",
    val latitude: Double = 12.9716,
    val longitude: Double = 77.5946,
    val isDefault: Boolean = false,
    val allowCaretakerVisit: Boolean = true,
    val allowDoctor: Boolean = true,
    val isPrivate: Boolean = false,
    val isTemporary: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getFormattedAddress(): String {
        if (fullAddress.isNotBlank()) return fullAddress
        val parts = listOf(
            addressLine1,
            addressLine2,
            landmark.let { if (it.isNotBlank()) "Near $it" else "" },
            city,
            "$state $pincode".trim()
        ).filter { it.isNotBlank() }
        return parts.joinToString(", ")
    }
}

@Entity(tableName = "caretaker_assistance_requests")
data class CaretakerAssistanceRequestEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val caretakerId: String,
    val caretakerName: String,
    val caretakerPhone: String = "",
    val patientId: String,
    val patientName: String,
    val patientPhone: String = "",
    val requestType: String, // "CALL" or "VISIT"
    val reason: String, // "Medicine Assistance", "Patient Check-in", "Emergency", "Missed Medicine", "Medical Support", "Appointment Assistance", "Other"
    val notes: String = "",
    val status: String = "PAID_PENDING_ADMIN", // "PAID_PENDING_ADMIN", "PENDING", "ACCEPTED", "REJECTED", "CARETAKER_ON_THE_WAY", "ARRIVED", "COMPLETED", "CANCELLED"
    val caretakerLatitude: Double? = null,
    val caretakerLongitude: Double? = null,
    val caretakerLocationName: String = "",
    val selectedAddressId: String = "",
    val addressSnapshot: String = "",
    val patientAddress: String = "",
    val patientLatitude: Double? = null,
    val patientLongitude: Double? = null,
    val distanceKm: Double = 0.0,
    val billableKm: Int = 3,
    val baseCharge: Double = 30.0,
    val additionalDistanceCharge: Double = 0.0,
    val totalVisitCharge: Double = 30.0,
    val paymentStatus: String = "UNPAID", // "UNPAID", "PAID", "REFUNDED"
    val paymentId: String = "",
    val transactionId: String = "",
    val paidAmount: Double = 0.0,
    val paidAt: Long? = null,
    val refundTransactionId: String = "",
    val refundedAt: Long? = null,
    val adminNotes: String = "",
    val rejectionReason: String = "",
    val adminId: String = "",
    val adminName: String = "",
    val isEmergency: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val caretakerId: String,
    val balance: Double = 450.0, // Default initialized balance (capped at maxBalance)
    val maxBalance: Double = 1000.0,
    val currency: String = "INR",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey val transactionId: String = "TXN_" + System.currentTimeMillis() + "_" + (1000..9999).random(),
    val caretakerId: String,
    val type: String, // "TOP_UP", "VISIT_PAYMENT", "REFUND"
    val amount: Double,
    val balanceBefore: Double,
    val balanceAfter: Double,
    val status: String, // "PENDING", "SUCCESS", "FAILED", "CANCELLED", "REFUNDED"
    val description: String,
    val paymentProvider: String = "Razorpay",
    val providerPaymentId: String = "", // e.g. "pay_rzp_..."
    val orderId: String = "", // e.g. "order_rzp_..."
    val relatedRequestId: String = "",
    val patientName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
