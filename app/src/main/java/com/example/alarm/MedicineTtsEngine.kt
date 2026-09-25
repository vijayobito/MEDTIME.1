package com.example.alarm

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

class MedicineTtsEngine(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "MedicineTtsEngine"
        private const val UTTERANCE_ID_MEDICINE = "medtime_medicine_speech_utterance"
        private const val UTTERANCE_ID_TEST = "medtime_test_speech_utterance"
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeechText: String? = null
    private var onSpeechDoneCallback: (() -> Unit)? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TextToSpeech: ${e.message}", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            Log.d(TAG, "TextToSpeech successfully initialized.")
            applyUserSettings()

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d(TAG, "Speech started: $utteranceId")
                }

                override fun onDone(utteranceId: String?) {
                    Log.d(TAG, "Speech finished: $utteranceId")
                    onSpeechDoneCallback?.invoke()
                }

                override fun onError(utteranceId: String?) {
                    Log.e(TAG, "Speech error on: $utteranceId")
                    onSpeechDoneCallback?.invoke()
                }
            })

            pendingSpeechText?.let { text ->
                speakText(text, onSpeechDoneCallback)
                pendingSpeechText = null
            }
        } else {
            Log.e(TAG, "TextToSpeech initialization failed with status: $status")
            isInitialized = false
        }
    }

    private fun applyUserSettings() {
        val ttsInstance = tts ?: return
        try {
            val langCode = AlarmSettingsManager.getTtsLanguage(context)
            val selectedLocale = parseLocale(langCode)

            val langResult = ttsInstance.setLanguage(selectedLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Selected language $langCode not supported, falling back to US English.")
                ttsInstance.language = Locale.US
            }

            val voiceName = AlarmSettingsManager.getTtsVoice(context)
            if (voiceName != "System Default" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val matchingVoice = ttsInstance.voices?.find { it.name == voiceName }
                if (matchingVoice != null) {
                    ttsInstance.voice = matchingVoice
                }
            }

            // Set speech attributes to USAGE_ALARM so it is loud and clear
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                ttsInstance.setAudioAttributes(audioAttributes)
            }

            val volume = AlarmSettingsManager.getSpeechVolume(context)
            ttsInstance.setSpeechRate(0.92f) // Slightly relaxed pace for medical clarity
            ttsInstance.setPitch(1.0f)
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring TTS settings: ${e.message}", e)
        }
    }

    /**
     * Speaks the dynamic medicine reminder for a single medication.
     */
    fun speakMedicineReminder(
        patientName: String,
        medicineName: String,
        dosage: String,
        form: String,
        instructions: String,
        onDone: (() -> Unit)? = null
    ) {
        if (!AlarmSettingsManager.isSpokenReminderEnabled(context)) {
            Log.d(TAG, "Spoken reminder is disabled in user preferences. Skipping speech.")
            return
        }

        val speechText = buildDynamicMedicineSpeech(
            patientName = patientName,
            medicineName = medicineName,
            dosage = dosage,
            form = form,
            instructions = instructions
        )

        speakText(speechText, onDone)
    }

    /**
     * Speaks a grouped reminder when multiple medicines are scheduled at the same time.
     */
    fun speakGroupedMedicineReminder(
        patientName: String,
        medicines: List<GroupedMedicineSpeechItem>,
        onDone: (() -> Unit)? = null
    ) {
        if (!AlarmSettingsManager.isSpokenReminderEnabled(context)) {
            return
        }

        val speechText = buildGroupedMedicineSpeech(patientName, medicines)
        speakText(speechText, onDone)
    }

    /**
     * Speaks a sample test reminder.
     */
    fun speakTestReminder(patientName: String, onDone: (() -> Unit)? = null) {
        val name = patientName.trim().ifBlank { "Vijay" }
        val testSpeech = "$name, this is a MedTime medicine reminder. It is time to take your test medicine."
        speakText(testSpeech, onDone)
    }

    private fun speakText(text: String, onDone: (() -> Unit)?) {
        this.onSpeechDoneCallback = onDone

        if (!isInitialized || tts == null) {
            Log.d(TAG, "TTS not ready yet; queuing speech: $text")
            pendingSpeechText = text
            return
        }

        try {
            applyUserSettings()
            tts?.stop() // Ensure no overlap

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val params = Bundle().apply {
                    putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, AlarmSettingsManager.getSpeechVolume(context))
                }
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, UTTERANCE_ID_MEDICINE)
            } else {
                @Suppress("DEPRECATION")
                val params = HashMap<String, String>().apply {
                    put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID_MEDICINE)
                }
                @Suppress("DEPRECATION")
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params)
            }
            Log.d(TAG, "Speaking reminder text: \"$text\"")
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking text: ${e.message}", e)
            onDone?.invoke()
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (ignored: Exception) {}
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (ignored: Exception) {}
    }

    /**
     * Retrieves available languages installed/available on device.
     */
    fun getAvailableLanguages(): List<TtsLanguageOption> {
        val result = mutableListOf<TtsLanguageOption>()
        result.add(TtsLanguageOption("System Default", "default", "System Default"))
        result.add(TtsLanguageOption("English (United States)", "en_US", "English"))
        result.add(TtsLanguageOption("English (India)", "en_IN", "English (India)"))
        result.add(TtsLanguageOption("Hindi (India)", "hi_IN", "हिन्दी (Hindi)"))
        result.add(TtsLanguageOption("Kannada (India)", "kn_IN", "ಕನ್ನಡ (Kannada)"))
        result.add(TtsLanguageOption("Tamil (India)", "ta_IN", "தமிழ் (Tamil)"))
        result.add(TtsLanguageOption("Telugu (India)", "te_IN", "తెలుగు (Telugu)"))
        result.add(TtsLanguageOption("Spanish", "es_ES", "Español"))
        result.add(TtsLanguageOption("French", "fr_FR", "Français"))
        return result
    }

    /**
     * Dynamic sentence construction for a single medicine dose.
     */
    fun buildDynamicMedicineSpeech(
        patientName: String,
        medicineName: String,
        dosage: String,
        form: String,
        instructions: String
    ): String {
        val cleanName = cleanPatientFirstName(patientName)
        val cleanMed = medicineName.trim().ifEmpty { "your medication" }
        val spokenDosage = formatSpokenDosage(dosage)
        val spokenForm = formatSpokenForm(form, dosage)
        val spokenInstructions = formatSpokenInstructions(instructions)

        val parts = mutableListOf<String>()

        if (cleanName.isNotBlank()) {
            parts.add("$cleanName, it is time to take your $cleanMed")
        } else {
            parts.add("It is time to take $cleanMed")
        }

        if (spokenDosage.isNotBlank()) {
            parts.add(spokenDosage)
        }

        if (spokenForm.isNotBlank()) {
            parts.add(spokenForm)
        }

        if (spokenInstructions.isNotBlank()) {
            parts.add(spokenInstructions)
        }

        return parts.joinToString(", ") + "."
    }

    /**
     * Dynamic sentence construction for grouped multi-medicine dose.
     */
    fun buildGroupedMedicineSpeech(
        patientName: String,
        medicines: List<GroupedMedicineSpeechItem>
    ): String {
        val cleanName = cleanPatientFirstName(patientName)
        val count = medicines.size

        val greeting = if (cleanName.isNotBlank()) {
            "$cleanName, you have $count medicines to take now."
        } else {
            "You have $count medicines to take now."
        }

        val medSentences = medicines.map { med ->
            val spokenDosage = formatSpokenDosage(med.dosage)
            val spokenForm = formatSpokenForm(med.form, med.dosage)
            val details = listOf(spokenDosage, spokenForm).filter { it.isNotBlank() }.joinToString(", ")
            if (details.isNotBlank()) "${med.name}, $details." else "${med.name}."
        }

        return "$greeting " + medSentences.joinToString(" ")
    }

    private fun cleanPatientFirstName(fullName: String): String {
        val name = fullName.trim()
        if (name.isBlank() || name.equals("Self", ignoreCase = true) || name.startsWith("User", ignoreCase = true)) {
            return ""
        }
        return name.split(" ").firstOrNull()?.trim() ?: ""
    }

    private fun formatSpokenDosage(dosage: String): String {
        var d = dosage.trim()
        if (d.isBlank()) return ""

        // Remove bullet separators like "500 mg • 1 Tablet" -> process dosage portion
        if (d.contains("•")) {
            d = d.substringBefore("•").trim()
        }

        // Expand common medical abbreviations into spoken phonetics
        d = d.replace(Regex("(?i)\\b(\\d+)\\s*mg\\b"), "$1 milligrams")
            .replace(Regex("(?i)\\b(\\d+)\\s*mcg\\b"), "$1 micrograms")
            .replace(Regex("(?i)\\b(\\d+)\\s*ug\\b"), "$1 micrograms")
            .replace(Regex("(?i)\\b(\\d+)\\s*ml\\b"), "$1 milliliters")
            .replace(Regex("(?i)\\b(\\d+)\\s*iu\\b"), "$1 International Units")
            .replace(Regex("(?i)\\b(\\d+)\\s*g\\b"), "$1 grams")
            .replace(Regex("(?i)\\b(\\d+)\\s*iu\\b"), "$1 International Units")
            .replace(Regex("(?i)\\b(\\d+)\\s*puff\\b"), "$1 puff")
            .replace(Regex("(?i)\\b(\\d+)\\s*puffs\\b"), "$1 puffs")
            .replace(Regex("(?i)\\b(\\d+)\\s*drop\\b"), "$1 drop")
            .replace(Regex("(?i)\\b(\\d+)\\s*drops\\b"), "$1 drops")

        return d
    }

    private fun formatSpokenForm(form: String, dosage: String): String {
        val f = form.trim().lowercase()
        if (f.isBlank()) return ""

        // Detect quantity from dosage or default to 1
        val qty = if (dosage.contains("2", ignoreCase = true) && !dosage.contains("mg", ignoreCase = true)) 2 else 1

        return when {
            f.contains("tablet") || f == "tab" -> if (qty > 1) "$qty tablets" else "one tablet"
            f.contains("capsule") || f == "cap" -> if (qty > 1) "$qty capsules" else "one capsule"
            f.contains("syrup") || f.contains("liquid") -> "syrup"
            f.contains("injection") || f.contains("shot") -> "injection"
            f.contains("inhaler") || f.contains("puff") -> if (qty > 1) "$qty puffs" else "one puff"
            f.contains("drop") -> if (qty > 1) "$qty drops" else "drops"
            f.contains("cream") || f.contains("ointment") || f.contains("gel") -> "topical application"
            f.contains("patch") -> "patch"
            else -> f
        }
    }

    private fun formatSpokenInstructions(instructions: String): String {
        val ins = instructions.trim().lowercase()
        if (ins.isBlank()) return ""

        return when {
            ins.contains("after food") || ins.contains("after meal") || ins.contains("post meal") -> "after food"
            ins.contains("before food") || ins.contains("before meal") || ins.contains("pre meal") -> "before food"
            ins.contains("with food") || ins.contains("with meal") -> "with food"
            ins.contains("empty stomach") -> "on an empty stomach"
            ins.contains("bedtime") || ins.contains("at night") -> "at bedtime"
            ins.contains("water") -> "with water"
            else -> instructions.trim()
        }
    }

    private fun parseLocale(code: String): Locale {
        return when (code) {
            "en_IN" -> Locale("en", "IN")
            "hi_IN" -> Locale("hi", "IN")
            "kn_IN" -> Locale("kn", "IN")
            "ta_IN" -> Locale("ta", "IN")
            "te_IN" -> Locale("te", "IN")
            "es_ES" -> Locale("es", "ES")
            "fr_FR" -> Locale("fr", "FR")
            "default", "System Default" -> Locale.getDefault()
            else -> {
                val parts = code.split("_", "-")
                if (parts.size >= 2) Locale(parts[0], parts[1]) else Locale(parts[0])
            }
        }
    }
}

data class GroupedMedicineSpeechItem(
    val name: String,
    val dosage: String,
    val form: String,
    val instructions: String = ""
)

data class TtsLanguageOption(
    val displayName: String,
    val languageCode: String,
    val nativeName: String
)
