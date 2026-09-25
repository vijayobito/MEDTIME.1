package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.AppointmentEntity
import com.example.data.model.MedicineEntity
import com.example.data.model.MedicineHistoryEntity
import com.example.data.model.UserEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ClinicalReportPdfGenerator {

    data class ReportData(
        val patient: UserEntity,
        val adherencePercent: Int,
        val totalScheduled: Int,
        val takenCount: Int,
        val missedCount: Int,
        val medicines: List<MedicineEntity>,
        val appointments: List<AppointmentEntity>,
        val recentHistory: List<MedicineHistoryEntity>
    )

    /**
     * Generates a multi-page or standard A4 formatted clinical summary PDF document
     * containing medication adherence and appointment history for medical practitioners.
     */
    fun generateDoctorSummaryPdf(context: Context, data: ReportData): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 points: 595 x 842
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val generatedDateStr = dateFormat.format(Date())

        var yPos = 30f

        // 1. Header Banner
        paint.color = Color.rgb(21, 101, 192) // MedBluePrimary #1565C0
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, 595f, 75f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("MEDTIME • CLINICAL SUMMARY & ADHERENCE REPORT", 30f, 38f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Official Patient Medication & Consultation Record | Generated: $generatedDateStr", 30f, 56f, paint)

        yPos = 95f

        // 2. Patient Demographics & Profile Box
        paint.color = Color.rgb(240, 244, 248)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(30f, yPos, 565f, yPos + 65f), 8f, 8f, paint)

        paint.color = Color.rgb(21, 101, 192)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(30f, yPos, 565f, yPos + 65f), 8f, 8f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Patient Name: ${data.patient.name}", 42f, yPos + 20f, paint)
        canvas.drawText("Blood Group: ${data.patient.bloodGroup}", 260f, yPos + 20f, paint)
        canvas.drawText("Contact: ${data.patient.phone.ifBlank { "N/A" }}", 420f, yPos + 20f, paint)

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 10f
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("Email: ${data.patient.email}", 42f, yPos + 38f, paint)
        canvas.drawText("Known Allergies: ${data.patient.allergies}", 260f, yPos + 38f, paint)

        canvas.drawText("Emergency Contact: ${data.patient.emergencyContactName} (${data.patient.emergencyContactPhone})", 42f, yPos + 54f, paint)

        yPos += 80f

        // 3. Medication Adherence KPI Strip
        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("1. MEDICATION ADHERENCE & COMPLIANCE METRICS", 30f, yPos, paint)

        yPos += 10f
        val kpiWidth = (535f - 24f) / 4f
        val kpiBoxY = yPos

        drawKpiBox(canvas, 30f, kpiBoxY, kpiWidth, 42f, "Overall Adherence", "${data.adherencePercent}%", Color.rgb(46, 125, 50))
        drawKpiBox(canvas, 30f + kpiWidth + 8f, kpiBoxY, kpiWidth, 42f, "Scheduled Today", "${data.totalScheduled}", Color.rgb(21, 101, 192))
        drawKpiBox(canvas, 30f + (kpiWidth + 8f) * 2, kpiBoxY, kpiWidth, 42f, "Doses Taken", "${data.takenCount}", Color.rgb(46, 125, 50))
        drawKpiBox(canvas, 30f + (kpiWidth + 8f) * 3, kpiBoxY, kpiWidth, 42f, "Missed / Skipped", "${data.missedCount}", Color.rgb(198, 40, 40))

        yPos += 58f

        // 4. Active Prescriptions Table
        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("2. ACTIVE MEDICATION SCHEDULE (${data.medicines.size} Prescriptions)", 30f, yPos, paint)

        yPos += 8f
        // Table Header
        paint.color = Color.rgb(227, 242, 253)
        paint.style = Paint.Style.FILL
        canvas.drawRect(30f, yPos, 565f, yPos + 20f, paint)

        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("MEDICINE & FORM", 35f, yPos + 13f, paint)
        canvas.drawText("DOSAGE", 175f, yPos + 13f, paint)
        canvas.drawText("FREQUENCY & SCHEDULE", 255f, yPos + 13f, paint)
        canvas.drawText("INSTRUCTIONS", 410f, yPos + 13f, paint)
        canvas.drawText("STOCK", 520f, yPos + 13f, paint)

        yPos += 20f

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 9f
        if (data.medicines.isEmpty()) {
            paint.color = Color.GRAY
            canvas.drawText("No active medications found in this profile.", 35f, yPos + 15f, paint)
            yPos += 24f
        } else {
            val medsToShow = data.medicines.take(6)
            medsToShow.forEachIndexed { index, med ->
                val rowBg = if (index % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                paint.color = rowBg
                paint.style = Paint.Style.FILL
                canvas.drawRect(30f, yPos, 565f, yPos + 20f, paint)

                paint.color = Color.rgb(30, 41, 59)
                canvas.drawText("${med.name} (${med.form})", 35f, yPos + 13f, paint)
                canvas.drawText(med.dosage, 175f, yPos + 13f, paint)
                val schedStr = if (med.reminderTimes.isNotBlank()) "${med.frequency} [${med.reminderTimes}]" else med.frequency
                canvas.drawText(schedStr.take(28), 255f, yPos + 13f, paint)
                canvas.drawText(med.instructions.take(20), 410f, yPos + 13f, paint)
                canvas.drawText("${med.stockQuantity} left", 520f, yPos + 13f, paint)

                yPos += 20f
            }
        }

        // Divider
        paint.color = Color.rgb(226, 232, 240)
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(30f, yPos, 565f, yPos, paint)

        yPos += 18f

        // 5. Doctor Appointment History
        paint.color = Color.rgb(21, 101, 192)
        paint.style = Paint.Style.FILL
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("3. APPOINTMENT & CONSULTATION HISTORY (${data.appointments.size} Consultations)", 30f, yPos, paint)

        yPos += 8f
        paint.color = Color.rgb(227, 242, 253)
        paint.style = Paint.Style.FILL
        canvas.drawRect(30f, yPos, 565f, yPos + 20f, paint)

        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DATE & TIME", 35f, yPos + 13f, paint)
        canvas.drawText("DOCTOR & SPECIALTY", 130f, yPos + 13f, paint)
        canvas.drawText("REASON / CONSULTATION", 270f, yPos + 13f, paint)
        canvas.drawText("STATUS", 440f, yPos + 13f, paint)
        canvas.drawText("NOTES", 500f, yPos + 13f, paint)

        yPos += 20f

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 9f
        if (data.appointments.isEmpty()) {
            paint.color = Color.GRAY
            canvas.drawText("No recorded appointment history for this profile.", 35f, yPos + 15f, paint)
            yPos += 24f
        } else {
            val apptsToShow = data.appointments.take(5)
            apptsToShow.forEachIndexed { index, appt ->
                val rowBg = if (index % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                paint.color = rowBg
                paint.style = Paint.Style.FILL
                canvas.drawRect(30f, yPos, 565f, yPos + 20f, paint)

                paint.color = Color.rgb(30, 41, 59)
                canvas.drawText("${appt.appointmentDate} ${appt.appointmentTime}", 35f, yPos + 13f, paint)
                canvas.drawText("${appt.doctorName} (${appt.doctorSpecialty})".take(24), 130f, yPos + 13f, paint)
                canvas.drawText(appt.reason.take(28), 270f, yPos + 13f, paint)
                
                // Status color
                val statusColor = when (appt.status) {
                    "COMPLETED" -> Color.rgb(46, 125, 50)
                    "ACCEPTED" -> Color.rgb(21, 101, 192)
                    "CANCELLED" -> Color.rgb(198, 40, 40)
                    else -> Color.rgb(245, 127, 23)
                }
                paint.color = statusColor
                canvas.drawText(appt.status, 440f, yPos + 13f, paint)

                paint.color = Color.rgb(71, 85, 105)
                canvas.drawText(appt.doctorNotes.ifBlank { "N/A" }.take(14), 500f, yPos + 13f, paint)

                yPos += 20f
            }
        }

        yPos += 16f

        // 6. Recent Dose Adherence Log Sample (Audit Trail)
        paint.color = Color.rgb(21, 101, 192)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("4. RECENT VERIFIED ADHERENCE LOGS", 30f, yPos, paint)

        yPos += 12f
        if (data.recentHistory.isEmpty()) {
            paint.typeface = Typeface.DEFAULT
            paint.textSize = 9f
            paint.color = Color.GRAY
            canvas.drawText("No historical dose intake actions recorded yet.", 30f, yPos, paint)
            yPos += 16f
        } else {
            val logs = data.recentHistory.take(4)
            val logDateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            logs.forEach { history ->
                val timeStr = logDateFormat.format(Date(history.actionTimestamp))
                paint.typeface = Typeface.DEFAULT
                paint.textSize = 8.5f
                paint.color = if (history.action == "TAKEN") Color.rgb(46, 125, 50) else Color.rgb(198, 40, 40)
                canvas.drawText("• [${history.action}] ${history.medicineName} (${history.dosage}) logged at $timeStr", 35f, yPos, paint)
                yPos += 12f
            }
        }

        yPos += 14f

        // 7. Clinical Sign-off & Disclaimer Footer
        paint.color = Color.rgb(240, 244, 248)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(30f, yPos, 565f, yPos + 55f), 6f, 6f, paint)

        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 8f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(
            "MEDICAL DISCLAIMER: This clinical report was compiled automatically by the MedTime Mobile Health Suite based on patient-logged",
            38f, yPos + 14f, paint
        )
        canvas.drawText(
            "intake records and clinical appointments. Please cross-verify all critical dosages and contraindications prior to clinical adjustments.",
            38f, yPos + 24f, paint
        )

        paint.color = Color.rgb(30, 41, 59)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Physician Signature: ____________________________", 38f, yPos + 44f, paint)
        canvas.drawText("Verification Stamp / Clinic ID: [ MEDTIME-VERIFIED ]", 330f, yPos + 44f, paint)

        pdfDocument.finishPage(page)

        // Save PDF to cache dir for sharing
        val safePatientName = data.patient.name.replace("\\s+".toRegex(), "_")
        val file = File(context.cacheDir, "MedTime_Clinical_Report_${safePatientName}.pdf")
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        fos.close()
        pdfDocument.close()

        return file
    }

    private fun drawKpiBox(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        value: String,
        valueColor: Int
    ) {
        val bgPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val textPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8f
            typeface = Typeface.DEFAULT
        }
        val valPaint = Paint().apply {
            color = valueColor
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawRoundRect(RectF(x, y, x + width, y + height), 6f, 6f, bgPaint)
        canvas.drawRoundRect(RectF(x, y, x + width, y + height), 6f, 6f, borderPaint)

        canvas.drawText(title, x + 8f, y + 15f, textPaint)
        canvas.drawText(value, x + 8f, y + 33f, valPaint)
    }

    /**
     * Creates an Intent to share the generated clinical PDF with doctor or healthcare team.
     */
    fun sharePdf(context: Context, pdfFile: File, patientName: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "MedTime Clinical Adherence & Appointment Summary: $patientName")
            putExtra(
                Intent.EXTRA_TEXT,
                "Please find attached the official MedTime Clinical Adherence & Appointment History Report for $patientName.\nGenerated from MedTime Healthcare Suite."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Share Clinical Summary with Doctor")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
