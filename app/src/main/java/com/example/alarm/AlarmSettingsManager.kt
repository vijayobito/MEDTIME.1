package com.example.alarm

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri

object AlarmSettingsManager {

    private const val PREFS_NAME = "medtime_alarm_preferences"

    const val SOUND_DEFAULT = "MedTime Default"
    const val SOUND_GENTLE_CHIME = "Gentle Chime"
    const val SOUND_SOFT_BELL = "Soft Bell"
    const val SOUND_MEDICATION_ALERT = "Medication Alert"
    const val SOUND_DIGITAL_ALARM = "Digital Alarm"
    const val SOUND_CALM_REMINDER = "Calm Reminder"
    const val SOUND_CUSTOM = "Custom Sound"

    val PRESET_SOUNDS = listOf(
        SOUND_DEFAULT,
        SOUND_GENTLE_CHIME,
        SOUND_SOFT_BELL,
        SOUND_MEDICATION_ALERT,
        SOUND_DIGITAL_ALARM,
        SOUND_CALM_REMINDER,
        SOUND_CUSTOM
    )

    private const val KEY_SELECTED_SOUND = "selected_sound"
    private const val KEY_CUSTOM_SOUND_URI = "custom_sound_uri"
    private const val KEY_CUSTOM_SOUND_NAME = "custom_sound_name"
    private const val KEY_SOUND_ENABLED = "sound_enabled"
    private const val KEY_SPOKEN_ENABLED = "spoken_reminder_enabled"
    private const val KEY_TTS_VOICE = "tts_voice"
    private const val KEY_TTS_LANGUAGE = "tts_language"
    private const val KEY_SPEECH_VOLUME = "speech_volume"
    private const val KEY_VIBRATION_ENABLED = "vibration_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSelectedSound(context: Context): String {
        return getPrefs(context).getString(KEY_SELECTED_SOUND, SOUND_DEFAULT) ?: SOUND_DEFAULT
    }

    fun setSelectedSound(context: Context, soundName: String) {
        getPrefs(context).edit().putString(KEY_SELECTED_SOUND, soundName).apply()
    }

    fun getCustomSoundUri(context: Context): Uri? {
        val uriStr = getPrefs(context).getString(KEY_CUSTOM_SOUND_URI, null) ?: return null
        return try {
            Uri.parse(uriStr)
        } catch (e: Exception) {
            null
        }
    }

    fun setCustomSound(context: Context, uri: Uri?, displayName: String?) {
        getPrefs(context).edit()
            .putString(KEY_CUSTOM_SOUND_URI, uri?.toString())
            .putString(KEY_CUSTOM_SOUND_NAME, displayName)
            .apply()
    }

    fun getCustomSoundName(context: Context): String? {
        return getPrefs(context).getString(KEY_CUSTOM_SOUND_NAME, null)
    }

    fun isSoundEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    fun isSpokenReminderEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SPOKEN_ENABLED, true)
    }

    fun setSpokenReminderEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SPOKEN_ENABLED, enabled).apply()
    }

    fun getTtsVoice(context: Context): String {
        return getPrefs(context).getString(KEY_TTS_VOICE, "System Default") ?: "System Default"
    }

    fun setTtsVoice(context: Context, voiceName: String) {
        getPrefs(context).edit().putString(KEY_TTS_VOICE, voiceName).apply()
    }

    fun getTtsLanguage(context: Context): String {
        return getPrefs(context).getString(KEY_TTS_LANGUAGE, "en_US") ?: "en_US"
    }

    fun setTtsLanguage(context: Context, languageCode: String) {
        getPrefs(context).edit().putString(KEY_TTS_LANGUAGE, languageCode).apply()
    }

    fun getSpeechVolume(context: Context): Float {
        return getPrefs(context).getFloat(KEY_SPEECH_VOLUME, 1.0f)
    }

    fun setSpeechVolume(context: Context, volume: Float) {
        getPrefs(context).edit().putFloat(KEY_SPEECH_VOLUME, volume.coerceIn(0f, 1f)).apply()
    }

    fun isVibrationEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_VIBRATION_ENABLED, true)
    }

    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
    }
}
