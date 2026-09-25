package com.example

<<<<<<< HEAD
import com.example.data.model.MedicineReminderEntity
import com.example.data.model.UserEntity
import com.example.ui.viewmodel.DashboardStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
=======
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
class MedTimeLogicTest {

    @Test
    fun testAdherenceCalculation_allTaken() {
        val totalScheduled = 4
        val takenCount = 4
        val percentage = ((takenCount.toFloat() / totalScheduled.toFloat()) * 100).toInt()
        assertEquals(100, percentage)
    }

    @Test
    fun testAdherenceCalculation_halfTaken() {
        val totalScheduled = 4
        val takenCount = 2
        val percentage = ((takenCount.toFloat() / totalScheduled.toFloat()) * 100).toInt()
        assertEquals(50, percentage)
    }

    @Test
    fun testAdherenceCalculation_emptySchedule() {
        val totalScheduled = 0
        val takenCount = 0
        val percentage = if (totalScheduled > 0) ((takenCount.toFloat() / totalScheduled.toFloat()) * 100).toInt() else 100
        assertEquals(100, percentage)
    }

    @Test
    fun testCaretakerLinkingCodeFormat() {
        val code = "MED-" + (1000..9999).random()
        assertTrue(code.startsWith("MED-"))
        assertEquals(8, code.length)
    }

    @Test
    fun testRoleDefaultDashboardMapping() {
        fun getDefaultScreen(role: String): String = when (role) {
            "DOCTOR" -> "doctor_queue"
            "CARETAKER" -> "caretakers"
            "ADMIN" -> "admin"
            else -> "dashboard"
        }
        assertEquals("dashboard", getDefaultScreen("PATIENT"))
        assertEquals("doctor_queue", getDefaultScreen("DOCTOR"))
        assertEquals("caretakers", getDefaultScreen("CARETAKER"))
        assertEquals("admin", getDefaultScreen("ADMIN"))
    }

    @Test
    fun testWorkManagerWorkerKeys() {
        assertEquals("reminder_id", com.example.worker.MedicationReminderWorker.KEY_REMINDER_ID)
        assertEquals("medicine_name", com.example.worker.MedicationReminderWorker.KEY_MEDICINE_NAME)
        assertEquals("dosage", com.example.worker.MedicationReminderWorker.KEY_DOSAGE)
        assertEquals("scheduled_time", com.example.worker.MedicationReminderWorker.KEY_SCHEDULED_TIME)
        assertEquals("medication_reminders_channel", com.example.worker.MedicationReminderWorker.CHANNEL_ID)
    }

    @Test
    fun testMedicationActionReceiverActions() {
        assertEquals("com.example.action.TAKE_MEDICATION_DOSE", com.example.worker.MedicationActionReceiver.ACTION_TAKE_DOSE)
        assertEquals("com.example.action.SNOOZE_MEDICATION_DOSE", com.example.worker.MedicationActionReceiver.ACTION_SNOOZE_DOSE)
        assertEquals("DailyMedicationSyncWork", com.example.worker.DailyMedicationSyncWorker.WORK_NAME)
    }

    @Test
    fun testAppointmentReminderWorkerKeys() {
        assertEquals("appointment_id", com.example.worker.AppointmentReminderWorker.KEY_APPOINTMENT_ID)
        assertEquals("doctor_name", com.example.worker.AppointmentReminderWorker.KEY_DOCTOR_NAME)
        assertEquals("doctor_specialty", com.example.worker.AppointmentReminderWorker.KEY_DOCTOR_SPECIALTY)
        assertEquals("appointment_date", com.example.worker.AppointmentReminderWorker.KEY_APPOINTMENT_DATE)
        assertEquals("appointment_time", com.example.worker.AppointmentReminderWorker.KEY_APPOINTMENT_TIME)
        assertEquals("appointment_reminders_channel", com.example.worker.AppointmentReminderWorker.CHANNEL_ID)
    }

    @Test
    fun testAppointmentReminderSchedulerDelayCalculation() {
        val delay = com.example.worker.AppointmentReminderScheduler.calculateDelayMillis(
            dateString = "Tomorrow",
            timeString = "10:30 AM",
            minutesBefore = 60
        )
        assertTrue("Delay should be positive for tomorrow's appointment", delay > 0)
    }

    @Test
    fun testMedicalReminderWorkerTypesAndChannels() {
        assertEquals("TYPE_DOSE", com.example.worker.MedicalReminderWorker.TYPE_MEDICATION_DOSE)
        assertEquals("TYPE_REFILL", com.example.worker.MedicalReminderWorker.TYPE_REFILL_ALERT)
        assertEquals("TYPE_APPOINTMENT", com.example.worker.MedicalReminderWorker.TYPE_APPOINTMENT)
        assertEquals("TYPE_VITALS", com.example.worker.MedicalReminderWorker.TYPE_VITALS_CHECK)

        assertEquals("medication_reminders_channel", com.example.worker.MedicalReminderWorker.CHANNEL_DOSE)
        assertEquals("medical_refill_channel", com.example.worker.MedicalReminderWorker.CHANNEL_REFILL)
        assertEquals("appointment_reminders_channel", com.example.worker.MedicalReminderWorker.CHANNEL_APPOINTMENT)
        assertEquals("medical_vitals_channel", com.example.worker.MedicalReminderWorker.CHANNEL_VITALS)

        assertEquals("com.example.action.DISMISS_MEDICATION_DOSE", com.example.worker.MedicationActionReceiver.ACTION_DISMISS_DOSE)
    }

    @Test
    fun testMedicalReminderSchedulerDelayCalculations() {
        val delay = com.example.worker.MedicalReminderScheduler.calculateAppointmentDelayMillis(
            dateString = "Tomorrow",
            timeString = "11:00 AM",
            minutesBefore = 30
        )
        assertTrue("Calculated appointment delay for tomorrow should be positive", delay > 0)

        val doseDelay = com.example.worker.MedicalReminderScheduler.calculateDelayMillis(
            dateString = "2099-12-31",
            timeString = "10:00 AM"
        )
        assertTrue("Delay for future date should be positive", doseDelay > 0)
    }
<<<<<<< HEAD

    @Test
    fun testMedicationEntityCreationAndTimeSlots() {
        val medication = com.example.data.model.Medication(
            name = "Amoxicillin",
            dosage = "500 mg",
            frequency = "Three times a day",
            timeSlots = "08:00 AM, 02:00 PM, 08:00 PM",
            form = "Capsule",
            instructions = "Take after meals"
        )

        assertEquals("Amoxicillin", medication.name)
        assertEquals("500 mg", medication.dosage)
        assertEquals("Three times a day", medication.frequency)
        assertEquals("08:00 AM, 02:00 PM, 08:00 PM", medication.timeSlots)
        assertEquals("Capsule", medication.form)
        assertEquals("Take after meals", medication.instructions)

        val slots = medication.getTimeSlotsList()
        assertEquals(3, slots.size)
        assertEquals("08:00 AM", slots[0])
        assertEquals("02:00 PM", slots[1])
        assertEquals("08:00 PM", slots[2])
    }

    @Test
    fun testDoctorVerificationStates() {
        val pendingDoc = com.example.data.model.UserEntity(
            id = "doc_pending",
            name = "Dr. Marcus Thorne",
            email = "doc@example.com",
            role = "DOCTOR",
            isDoctorVerified = false,
            doctorVerificationStatus = "PENDING",
            doctorLicense = "MED-LIC-88192",
            doctorLicenseImageUrl = "https://example.com/license.jpg",
            doctorProfilePhotoUrl = "https://example.com/photo.jpg"
        )

        assertEquals("DOCTOR", pendingDoc.role)
        assertEquals(false, pendingDoc.isDoctorVerified)
        assertEquals("PENDING", pendingDoc.doctorVerificationStatus)
        assertTrue(pendingDoc.doctorLicense.isNotBlank())
        assertTrue(pendingDoc.doctorLicenseImageUrl.isNotBlank())
        assertTrue(pendingDoc.doctorProfilePhotoUrl.isNotBlank())

        val approvedDoc = pendingDoc.copy(
            isDoctorVerified = true,
            doctorVerificationStatus = "APPROVED"
        )
        assertEquals(true, approvedDoc.isDoctorVerified)
        assertEquals("APPROVED", approvedDoc.doctorVerificationStatus)
    }

    @Test
    fun testDailyAdherenceStatsCalculation() {
        val reminders = listOf(
            MedicineReminderEntity(id = "r1", patientId = "p1", medicineId = "m1", medicineName = "Metformin", dosage = "500mg", form = "Tablet", instructions = "With food", scheduledDate = "2026-09-10", scheduledTime = "08:00 AM", status = "TAKEN"),
            MedicineReminderEntity(id = "r2", patientId = "p1", medicineId = "m2", medicineName = "Lisinopril", dosage = "10mg", form = "Tablet", instructions = "With water", scheduledDate = "2026-09-10", scheduledTime = "12:00 PM", status = "MISSED"),
            MedicineReminderEntity(id = "r3", patientId = "p1", medicineId = "m3", medicineName = "Atorvastatin", dosage = "20mg", form = "Tablet", instructions = "At bedtime", scheduledDate = "2026-09-10", scheduledTime = "08:00 PM", status = "PENDING"),
            MedicineReminderEntity(id = "r4", patientId = "p1", medicineId = "m4", medicineName = "Aspirin", dosage = "81mg", form = "Tablet", instructions = "With food", scheduledDate = "2026-09-10", scheduledTime = "09:00 PM", status = "TAKEN")
        )

        val total = reminders.size
        val taken = reminders.count { it.status == "TAKEN" }
        val missed = reminders.count { it.status == "MISSED" || it.status == "SKIPPED" }
        val pending = reminders.count { it.status == "PENDING" || it.status == "SNOOZED" }
        val adherencePct = if (total > 0) ((taken.toFloat() / total) * 100).toInt() else 100

        val stats = DashboardStats(
            totalScheduled = total,
            takenCount = taken,
            pendingCount = pending,
            missedCount = missed,
            adherencePercent = adherencePct
        )

        assertEquals(4, stats.totalScheduled)
        assertEquals(2, stats.takenCount)
        assertEquals(1, stats.missedCount)
        assertEquals(1, stats.pendingCount)
        assertEquals(50, stats.adherencePercent)
    }

    @Test
    fun testQrCodePayloadParsing_rawCode() {
        val payload = com.example.util.QrCodeUtils.parseQrPayload("MED-7842")
        org.junit.Assert.assertNotNull(payload)
        assertEquals("MED-7842", payload?.code)
        assertEquals("PATIENT", payload?.role)
    }

    @Test
    fun testQrCodePayloadParsing_json() {
        val user = UserEntity(
            id = "p1",
            name = "John Doe",
            email = "john@example.com",
            role = "PATIENT",
            caretakerLinkingCode = "MED-1234"
        )
        val jsonPayload = com.example.util.QrCodeUtils.generateUserQrPayload(user)
        assertTrue(jsonPayload.contains("MED-1234"))
        assertTrue(jsonPayload.contains("John Doe"))

        val parsed = com.example.util.QrCodeUtils.parseQrPayload(jsonPayload)
        org.junit.Assert.assertNotNull(parsed)
        assertEquals("MED-1234", parsed?.code)
        assertEquals("PATIENT", parsed?.role)
        assertEquals("John Doe", parsed?.name)
    }

    @Test
    fun testQrCodePayloadParsing_uriScheme() {
        val uri = "medtime://link?code=DOC-9912&role=DOCTOR&name=Dr.+Alice+Smith&specialty=Neurology"
        val parsed = com.example.util.QrCodeUtils.parseQrPayload(uri)
        org.junit.Assert.assertNotNull(parsed)
        assertEquals("DOC-9912", parsed?.code)
        assertEquals("DOCTOR", parsed?.role)
        assertEquals("Dr. Alice Smith", parsed?.name)
        assertEquals("Neurology", parsed?.specialty)
    }

    @Test
    fun testVisitPricingRule_1to3Km() {
        val fee1km = com.example.util.LocationUtils.calculateVisitFee(1.0)
        assertEquals(30.0, fee1km.totalFee, 0.01)
        assertEquals(1, fee1km.billableKm)

        val fee2km = com.example.util.LocationUtils.calculateVisitFee(2.0)
        assertEquals(30.0, fee2km.totalFee, 0.01)
        assertEquals(2, fee2km.billableKm)

        val fee3km = com.example.util.LocationUtils.calculateVisitFee(3.0)
        assertEquals(30.0, fee3km.totalFee, 0.01)
        assertEquals(3, fee3km.billableKm)
    }

    @Test
    fun testVisitPricingRule_above3KmAndDecimalCeiling() {
        val fee3Point1 = com.example.util.LocationUtils.calculateVisitFee(3.1)
        assertEquals(40.0, fee3Point1.totalFee, 0.01)
        assertEquals(4, fee3Point1.billableKm)

        val fee4km = com.example.util.LocationUtils.calculateVisitFee(4.0)
        assertEquals(40.0, fee4km.totalFee, 0.01)
        assertEquals(4, fee4km.billableKm)

        val fee5km = com.example.util.LocationUtils.calculateVisitFee(5.0)
        assertEquals(50.0, fee5km.totalFee, 0.01)
        assertEquals(5, fee5km.billableKm)

        val fee6km = com.example.util.LocationUtils.calculateVisitFee(6.0)
        assertEquals(60.0, fee6km.totalFee, 0.01)
        assertEquals(6, fee6km.billableKm)

        val fee10km = com.example.util.LocationUtils.calculateVisitFee(10.0)
        assertEquals(100.0, fee10km.totalFee, 0.01)
        assertEquals(10, fee10km.billableKm)
    }

    @Test
    fun testWalletMaximumBalanceConstraint() {
        val currentBalance = 700.0
        val maxAllowedBalance = 1000.0
        val requestedTopUp = 500.0
        val maximumAddable = maxOf(0.0, maxAllowedBalance - currentBalance)

        assertEquals(300.0, maximumAddable, 0.01)
        val canAdd = (currentBalance + requestedTopUp) <= maxAllowedBalance
        assertEquals(false, canAdd)
    }
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
}
