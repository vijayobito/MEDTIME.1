package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
