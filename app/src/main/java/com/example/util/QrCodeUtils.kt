package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.model.UserEntity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.json.JSONObject
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Data model representing the payload contained inside a MedTime QR code for account linking.
 */
data class QrAccountPayload(
    val code: String,
    val role: String, // "PATIENT", "DOCTOR", "CARETAKER"
    val userId: String = "",
    val name: String = "",
    val specialty: String = "",
    val hospital: String = "",
    val email: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val displayRole: String
        get() = when (role.uppercase()) {
            "PATIENT" -> "Patient"
            "DOCTOR" -> "Doctor / Physician"
            "CARETAKER" -> "Caregiver / Family"
            else -> "Healthcare Member"
        }
}

object QrCodeUtils {

    /**
     * Generates a structured JSON string payload for a User to embed in their QR code.
     */
    fun generateUserQrPayload(user: UserEntity): String {
        val json = JSONObject()
        json.put("app", "MedTime")
        json.put("action", "LINK_ACCOUNT")
        json.put("code", user.caretakerLinkingCode.ifBlank { "MED-${user.id.takeLast(4)}" })
        json.put("userId", user.id)
        json.put("role", user.role)
        json.put("name", user.name)
        json.put("email", user.email)
        if (user.role == "DOCTOR") {
            json.put("specialty", user.doctorSpecialty)
            json.put("hospital", user.doctorHospital)
        }
        json.put("timestamp", System.currentTimeMillis())
        return json.toString()
    }

    /**
     * Parses raw QR code string content into a structured QrAccountPayload.
     * Supports JSON format, URI scheme (medtime://link?...), and plain linking codes (e.g. MED-7842).
     */
    fun parseQrPayload(rawContent: String): QrAccountPayload? {
        val trimmed = rawContent.trim()
        if (trimmed.isEmpty()) return null

        // 1. Try parsing JSON format
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                val json = JSONObject(trimmed)
                val code = json.optString("code", "")
                val role = json.optString("role", "PATIENT")
                val userId = json.optString("userId", "")
                val name = json.optString("name", "Medical Member")
                val specialty = json.optString("specialty", "")
                val hospital = json.optString("hospital", "")
                val email = json.optString("email", "")

                if (code.isNotBlank() || userId.isNotBlank()) {
                    return QrAccountPayload(
                        code = code.ifBlank { userId },
                        role = role,
                        userId = userId,
                        name = name,
                        specialty = specialty,
                        hospital = hospital,
                        email = email
                    )
                }
            } catch (_: Exception) {
                // Fallback to URI or raw text
            }
        }

        // 2. Try parsing URI scheme (e.g. medtime://link?code=MED-7842&role=PATIENT&name=John)
        if (trimmed.startsWith("medtime://", ignoreCase = true) || trimmed.startsWith("http", ignoreCase = true)) {
            try {
                val queryString = trimmed.substringAfter("?", "")
                if (queryString.isNotEmpty()) {
                    val params = mutableMapOf<String, String>()
                    queryString.split("&").forEach { pair ->
                        val parts = pair.split("=", limit = 2)
                        if (parts.size == 2) {
                            val key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8.name())
                            val value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name())
                            params[key] = value
                        }
                    }

                    val code = params["code"] ?: params["id"] ?: ""
                    val role = params["role"] ?: "PATIENT"
                    val userId = params["userId"] ?: params["id"] ?: ""
                    val name = params["name"] ?: "Healthcare User"
                    val specialty = params["specialty"] ?: ""

                    if (code.isNotBlank() || userId.isNotBlank()) {
                        return QrAccountPayload(
                            code = code.ifBlank { userId },
                            role = role,
                            userId = userId,
                            name = name,
                            specialty = specialty
                        )
                    }
                }
            } catch (_: Exception) {
                // Fallback to raw text
            }
        }

        // 3. Fallback: Parse as a raw alphanumeric linking code (e.g. MED-7842, DOC-1024, or standard code)
        val cleanCode = trimmed.take(32)
        val guessedRole = when {
            cleanCode.startsWith("DOC", ignoreCase = true) -> "DOCTOR"
            cleanCode.startsWith("CARE", ignoreCase = true) -> "CARETAKER"
            else -> "PATIENT"
        }

        return QrAccountPayload(
            code = cleanCode,
            role = guessedRole,
            name = when (guessedRole) {
                "DOCTOR" -> "Dr. Verified Specialist"
                "CARETAKER" -> "Caregiver Member"
                else -> "Patient ($cleanCode)"
            }
        )
    }

    /**
     * Generates a sharp Android Bitmap representation of the QR code using ZXing.
     */
    fun generateQrBitmap(
        content: String,
        width: Int = 512,
        height: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN to 1
            )
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val pixels = IntArray(matrixWidth * matrixHeight)

            for (y in 0 until matrixHeight) {
                val offset = y * matrixWidth
                for (x in 0 until matrixWidth) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) foregroundColor else backgroundColor
                }
            }

            val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
