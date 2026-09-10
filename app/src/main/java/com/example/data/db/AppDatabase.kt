package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppointmentDao
import com.example.data.dao.AuditLogDao
import com.example.data.dao.CaretakerLinkDao
import com.example.data.dao.MedicalDocumentDao
import com.example.data.dao.MedicineDao
import com.example.data.dao.MedicineHistoryDao
import com.example.data.dao.MedicineReminderDao
import com.example.data.dao.MessageDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.UserDao
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        UserEntity::class,
        MedicineEntity::class,
        MedicineReminderEntity::class,
        MedicineHistoryEntity::class,
        AppointmentEntity::class,
        MessageEntity::class,
        CaretakerLinkEntity::class,
        MedicalDocumentEntity::class,
        NotificationEntity::class,
        AuditLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun medicineDao(): MedicineDao
    abstract fun reminderDao(): MedicineReminderDao
    abstract fun historyDao(): MedicineHistoryDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun messageDao(): MessageDao
    abstract fun caretakerDao(): CaretakerLinkDao
    abstract fun documentDao(): MedicalDocumentDao
    abstract fun notificationDao(): NotificationDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medtime_database"
                )
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(database)
                }
            }
        }

        private suspend fun seedDatabase(db: AppDatabase) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // 1. Seed Users (Patient, Doctor, Caretaker, Admin)
            val patient = UserEntity(
                id = "patient_1",
                name = "Vijay Kumar",
                email = "patient@medtime.com",
                role = "PATIENT",
                phone = "+1 (555) 234-5678",
                bloodGroup = "O+",
                allergies = "Amoxicillin, Pollen",
                emergencyContactName = "Ananya Kumar",
                emergencyContactPhone = "+1 (555) 987-6543",
                emergencyContactRelation = "Spouse",
                caretakerLinkingCode = "MED-7842"
            )

            val doctor1 = UserEntity(
                id = "doctor_1",
                name = "Dr. Sarah Mitchell, MD",
                email = "doctor@medtime.com",
                role = "DOCTOR",
                phone = "+1 (555) 876-5432",
                isDoctorVerified = true,
                doctorVerificationStatus = "APPROVED",
                doctorSpecialty = "Cardiologist",
                doctorHospital = "St. Jude Heart & Vascular Hospital",
                doctorLicense = "MED-LIC-94821",
                doctorBio = "Chief of Cardiology with over 15 years treating hypertension, arrhythmias, and cardiovascular preventive care."
            )

            val doctor2 = UserEntity(
                id = "doctor_2",
                name = "Dr. Robert Chen, MD",
                email = "robert.chen@medtime.com",
                role = "DOCTOR",
                phone = "+1 (555) 345-6789",
                isDoctorVerified = true,
                doctorVerificationStatus = "APPROVED",
                doctorSpecialty = "Endocrinologist",
                doctorHospital = "Memorial General Care",
                doctorLicense = "MED-LIC-32910",
                doctorBio = "Specialist in diabetes management, endocrine metabolic disorders, and thyroid therapy."
            )

            val caretaker = UserEntity(
                id = "caretaker_1",
                name = "Emily Davis",
                email = "caretaker@medtime.com",
                role = "CARETAKER",
                phone = "+1 (555) 654-3210"
            )

            val admin = UserEntity(
                id = "admin_1",
                name = "System Administrator",
                email = "admin@medtime.com",
                role = "ADMIN",
                phone = "+1 (555) 000-1111"
            )

            db.userDao().insertUser(patient)
            db.userDao().insertUser(doctor1)
            db.userDao().insertUser(doctor2)
            db.userDao().insertUser(caretaker)
            db.userDao().insertUser(admin)

            // 2. Seed Medicines for Patient
            val med1 = MedicineEntity(
                id = "med_1",
                patientId = "patient_1",
                name = "Atorvastatin",
                dosage = "20 mg",
                form = "Tablet",
                instructions = "After food with water",
                frequency = "Daily",
                reminderTimes = "08:00 AM",
                startDate = today,
                endDate = "2026-12-31",
                stockQuantity = 28,
                notes = "For cholesterol regulation",
                colorHex = 0xFF1565C0
            )

            val med2 = MedicineEntity(
                id = "med_2",
                patientId = "patient_1",
                name = "Metformin HCl",
                dosage = "500 mg",
                form = "Tablet",
                instructions = "With breakfast & dinner",
                frequency = "Twice a day",
                reminderTimes = "08:00 AM,08:00 PM",
                startDate = today,
                endDate = "2026-12-31",
                stockQuantity = 52,
                notes = "Blood glucose stabilization",
                colorHex = 0xFF2E7D32
            )

            val med3 = MedicineEntity(
                id = "med_3",
                patientId = "patient_1",
                name = "Lisinopril",
                dosage = "10 mg",
                form = "Tablet",
                instructions = "Morning before food",
                frequency = "Daily",
                reminderTimes = "07:30 AM",
                startDate = today,
                endDate = "2026-12-31",
                stockQuantity = 14,
                notes = "Blood pressure maintenance",
                colorHex = 0xFF00695C
            )

            val med4 = MedicineEntity(
                id = "med_4",
                patientId = "patient_1",
                name = "Omega-3 Fish Oil",
                dosage = "1000 mg",
                form = "Capsule",
                instructions = "After lunch",
                frequency = "Daily",
                reminderTimes = "01:30 PM",
                startDate = today,
                endDate = "2026-12-31",
                stockQuantity = 45,
                notes = "Cardiovascular dietary supplement",
                colorHex = 0xFFF57F17
            )

            db.medicineDao().insertMedicine(med1)
            db.medicineDao().insertMedicine(med2)
            db.medicineDao().insertMedicine(med3)
            db.medicineDao().insertMedicine(med4)

            // 3. Seed Today's Reminders
            val rem1 = MedicineReminderEntity(
                id = "rem_1",
                medicineId = "med_3",
                patientId = "patient_1",
                medicineName = "Lisinopril",
                dosage = "10 mg",
                form = "Tablet",
                instructions = "Morning before food",
                scheduledDate = today,
                scheduledTime = "07:30 AM",
                status = "TAKEN",
                takenAtTimestamp = System.currentTimeMillis() - 3600000 * 2
            )

            val rem2 = MedicineReminderEntity(
                id = "rem_2",
                medicineId = "med_1",
                patientId = "patient_1",
                medicineName = "Atorvastatin",
                dosage = "20 mg",
                form = "Tablet",
                instructions = "After food with water",
                scheduledDate = today,
                scheduledTime = "08:00 AM",
                status = "TAKEN",
                takenAtTimestamp = System.currentTimeMillis() - 3600000
            )

            val rem3 = MedicineReminderEntity(
                id = "rem_3",
                medicineId = "med_4",
                patientId = "patient_1",
                medicineName = "Omega-3 Fish Oil",
                dosage = "1000 mg",
                form = "Capsule",
                instructions = "After lunch",
                scheduledDate = today,
                scheduledTime = "01:30 PM",
                status = "PENDING"
            )

            val rem4 = MedicineReminderEntity(
                id = "rem_4",
                medicineId = "med_2",
                patientId = "patient_1",
                medicineName = "Metformin HCl",
                dosage = "500 mg",
                form = "Tablet",
                instructions = "With dinner",
                scheduledDate = today,
                scheduledTime = "08:00 PM",
                status = "PENDING"
            )

            db.reminderDao().insertReminders(listOf(rem1, rem2, rem3, rem4))

            // 4. Seed History Records for the taken medications
            db.historyDao().insertHistory(
                MedicineHistoryEntity(
                    id = "hist_1",
                    medicineId = "med_3",
                    patientId = "patient_1",
                    medicineName = "Lisinopril",
                    dosage = "10 mg",
                    scheduledTime = "07:30 AM",
                    action = "TAKEN",
                    actionTimestamp = System.currentTimeMillis() - 3600000 * 2,
                    notes = "Taken on schedule with water"
                )
            )

            db.historyDao().insertHistory(
                MedicineHistoryEntity(
                    id = "hist_2",
                    medicineId = "med_1",
                    patientId = "patient_1",
                    medicineName = "Atorvastatin",
                    dosage = "20 mg",
                    scheduledTime = "08:00 AM",
                    action = "TAKEN",
                    actionTimestamp = System.currentTimeMillis() - 3600000,
                    notes = "Taken after breakfast"
                )
            )

            // 5. Seed Appointments
            val appt1 = AppointmentEntity(
                id = "appt_1",
                patientId = "patient_1",
                patientName = "Vijay Kumar",
                doctorId = "doctor_1",
                doctorName = "Dr. Sarah Mitchell, MD",
                doctorSpecialty = "Cardiologist",
                appointmentDate = "Tomorrow",
                appointmentTime = "10:30 AM",
                reason = "Quarterly cardiovascular evaluation and blood pressure checkup",
                status = "ACCEPTED",
                doctorNotes = "Bring recent lipid panel report.",
                reminderEnabled = true,
                reminderMinutesBefore = 60
            )

            val appt2 = AppointmentEntity(
                id = "appt_2",
                patientId = "patient_1",
                patientName = "Vijay Kumar",
                doctorId = "doctor_2",
                doctorName = "Dr. Robert Chen, MD",
                doctorSpecialty = "Endocrinologist",
                appointmentDate = "In 5 Days",
                appointmentTime = "02:15 PM",
                reason = "Routine HbA1c glucose monitoring review",
                status = "PENDING",
                doctorNotes = "",
                reminderEnabled = true,
                reminderMinutesBefore = 30
            )

            db.appointmentDao().insertAppointment(appt1)
            db.appointmentDao().insertAppointment(appt2)

            // 6. Seed Messages (Patient <-> Doctor)
            val msg1 = MessageEntity(
                id = "msg_1",
                conversationId = "conv_patient1_doctor1",
                senderId = "doctor_1",
                senderName = "Dr. Sarah Mitchell, MD",
                senderRole = "DOCTOR",
                receiverId = "patient_1",
                receiverName = "Vijay Kumar",
                content = "Good morning Vijay. How has your blood pressure been trending since adjusting the Lisinopril dose?",
                timestamp = System.currentTimeMillis() - 86400000,
                isRead = true
            )

            val msg2 = MessageEntity(
                id = "msg_2",
                conversationId = "conv_patient1_doctor1",
                senderId = "patient_1",
                senderName = "Vijay Kumar",
                senderRole = "PATIENT",
                receiverId = "doctor_1",
                receiverName = "Dr. Sarah Mitchell, MD",
                content = "Morning Dr. Mitchell. Readings have remained stable around 122/78 this past week. Feeling great!",
                timestamp = System.currentTimeMillis() - 43200000,
                isRead = true
            )

            val msg3 = MessageEntity(
                id = "msg_3",
                conversationId = "conv_patient1_doctor1",
                senderId = "doctor_1",
                senderName = "Dr. Sarah Mitchell, MD",
                senderRole = "DOCTOR",
                receiverId = "patient_1",
                receiverName = "Vijay Kumar",
                content = "Excellent news. We'll verify during our follow-up consultation tomorrow morning.",
                timestamp = System.currentTimeMillis() - 1800000,
                isRead = true
            )

            db.messageDao().insertMessage(msg1)
            db.messageDao().insertMessage(msg2)
            db.messageDao().insertMessage(msg3)

            // 7. Seed Caretaker Link
            val link1 = CaretakerLinkEntity(
                id = "link_1",
                patientId = "patient_1",
                patientName = "Vijay Kumar",
                caretakerId = "caretaker_1",
                caretakerName = "Emily Davis",
                linkingCode = "MED-7842",
                status = "APPROVED",
                canViewMedicines = true,
                canViewAdherence = true,
                canViewAppointments = true,
                canReceiveAlerts = true
            )
            db.caretakerDao().insertLink(link1)

            // 8. Seed Medical Documents (PDFs & Medical Images)
            val doc1 = MedicalDocumentEntity(
                id = "doc_1",
                patientId = "patient_1",
                title = "Comprehensive Lipid & Metabolic Panel",
                type = "Lab Report",
                doctorOrClinic = "Quest Diagnostics",
                dateAdded = "2026-09-02",
                fileSize = "1.4 MB",
                fileSizeBytes = 1468006L,
                fileName = "lipid_metabolic_panel_sep2026.pdf",
                fileFormat = "PDF",
                mimeType = "application/pdf",
                pageCount = 3,
                resolution = "A4 / 300 DPI",
                tags = "Cardiology, Cholesterol, Fasting, Blood Chemistry",
                isFavorite = true,
                notes = "HDL: 54 mg/dL, LDL: 88 mg/dL, Triglycerides: 140 mg/dL (Normal). Fasting blood glucose: 92 mg/dL."
            )

            val doc2 = MedicalDocumentEntity(
                id = "doc_2",
                patientId = "patient_1",
                title = "Cardiology Prescription Sheet",
                type = "Prescription",
                doctorOrClinic = "Dr. Sarah Mitchell, MD",
                dateAdded = "2026-08-28",
                fileSize = "640 KB",
                fileSizeBytes = 655360L,
                fileName = "prescription_atorvastatin_lisinopril.pdf",
                fileFormat = "PDF",
                mimeType = "application/pdf",
                pageCount = 1,
                resolution = "Letter / 300 DPI",
                tags = "Prescription, Statins, ACE Inhibitor, Pharmacy",
                isFavorite = true,
                notes = "Authorized 90-day supply of Atorvastatin 20mg and Lisinopril 10mg with 3 refills."
            )

            val doc3 = MedicalDocumentEntity(
                id = "doc_3",
                patientId = "patient_1",
                title = "Chest X-Ray Digital Radiograph (PA View)",
                type = "Scan",
                doctorOrClinic = "St. Jude Diagnostic Imaging",
                dateAdded = "2026-08-18",
                fileSize = "3.8 MB",
                fileSizeBytes = 3984588L,
                fileName = "chest_xray_pa_view.png",
                fileFormat = "IMAGE",
                mimeType = "image/png",
                pageCount = 1,
                resolution = "2560x2048 px",
                tags = "Radiology, Lungs, Cardiac Silhouette, Normal",
                isFavorite = false,
                notes = "Normal cardiothoracic ratio, clear lung parenchyma, sharp costophrenic angles. No pulmonary edema or consolidation."
            )

            val doc4 = MedicalDocumentEntity(
                id = "doc_4",
                patientId = "patient_1",
                title = "12-Lead Electrocardiogram (ECG) Strip",
                type = "Scan",
                doctorOrClinic = "Metropolitan Heart Center",
                dateAdded = "2026-08-10",
                fileSize = "2.1 MB",
                fileSizeBytes = 2202009L,
                fileName = "ecg_12lead_telemetry.jpg",
                fileFormat = "IMAGE",
                mimeType = "image/jpeg",
                pageCount = 1,
                resolution = "3000x1200 px",
                tags = "ECG, Sinus Rhythm, Cardiology, Diagnostics",
                isFavorite = false,
                notes = "Normal sinus rhythm at 68 bpm, normal axis, normal PR (154 ms) and QTc (418 ms) intervals. No ST elevation or depression."
            )

            val doc5 = MedicalDocumentEntity(
                id = "doc_5",
                patientId = "patient_1",
                title = "Annual Health & Wellness Examination",
                type = "Doctor Notes",
                doctorOrClinic = "Dr. James Wilson, MD",
                dateAdded = "2026-07-22",
                fileSize = "1.1 MB",
                fileSizeBytes = 1153433L,
                fileName = "annual_wellness_summary.pdf",
                fileFormat = "PDF",
                mimeType = "application/pdf",
                pageCount = 4,
                resolution = "A4 / 300 DPI",
                tags = "Preventive Care, Vitals, Annual Physical, Wellness",
                isFavorite = false,
                notes = "Patient in good overall health. Blood pressure well-controlled on current regimen. Recommended continued moderate cardiovascular exercise."
            )

            db.documentDao().insertDocument(doc1)
            db.documentDao().insertDocument(doc2)
            db.documentDao().insertDocument(doc3)
            db.documentDao().insertDocument(doc4)
            db.documentDao().insertDocument(doc5)

            // 9. Seed Notifications
            val notif1 = NotificationEntity(
                id = "notif_1",
                userId = "patient_1",
                title = "Appointment Confirmed",
                message = "Dr. Sarah Mitchell accepted your appointment for tomorrow at 10:30 AM.",
                type = "APPOINTMENT",
                isRead = false,
                timestamp = System.currentTimeMillis() - 7200000
            )

            val notif2 = NotificationEntity(
                id = "notif_2",
                userId = "patient_1",
                title = "Morning Meds Taken On Time",
                message = "Great job! Lisinopril & Atorvastatin logged. Adherence is at 88%.",
                type = "MEDICINE",
                isRead = false,
                timestamp = System.currentTimeMillis() - 3600000
            )

            val notif3 = NotificationEntity(
                id = "notif_3",
                userId = "patient_1",
                title = "Caretaker Connected",
                message = "Emily Davis has been linked as your authorized healthcare caretaker.",
                type = "CARETAKER",
                isRead = true,
                timestamp = System.currentTimeMillis() - 86400000
            )

            db.notificationDao().insertNotification(notif1)
            db.notificationDao().insertNotification(notif2)
            db.notificationDao().insertNotification(notif3)

            // 10. Seed Audit Log
            db.auditLogDao().insertLog(
                AuditLogEntity(
                    id = "audit_1",
                    action = "INITIAL_SYSTEM_INITIALIZE",
                    performedBy = "System",
                    targetResource = "Database",
                    details = "Initialized MedTime core tables, seed data, and clinical role permissions.",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
