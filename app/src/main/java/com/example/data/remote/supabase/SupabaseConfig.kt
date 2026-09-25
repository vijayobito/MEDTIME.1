package com.example.data.remote.supabase

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

/**
 * Manages Supabase configuration and authentication credentials.
 * Credentials are resolved with priority:
 * 1. User-configured custom values in SharedPreferences (via in-app Settings dialog)
 * 2. Injected values from BuildConfig (.env file / Gradle secrets plugin)
 * 3. Safe fallback defaults
 */
class SupabaseConfig(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "supabase_config_prefs"
        private const val KEY_CUSTOM_URL = "custom_supabase_url"
        private const val KEY_CUSTOM_ANON_KEY = "custom_supabase_anon_key"
        private const val KEY_LAST_SYNC_TIMESTAMP = "last_supabase_sync_timestamp"
        private const val KEY_AUTO_SYNC_ENABLED = "supabase_auto_sync_enabled"

        @Volatile
        private var INSTANCE: SupabaseConfig? = null

        fun getInstance(context: Context): SupabaseConfig {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SupabaseConfig(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Resolves the active Supabase project URL.
     * Ensures trailing slash is present for Retrofit baseUrl.
     */
    fun getBaseUrl(): String {
        val custom = prefs.getString(KEY_CUSTOM_URL, "")?.trim() ?: ""
        if (custom.isNotBlank()) {
            return if (custom.endsWith("/")) custom else "$custom/"
        }

        val buildConfigUrl = try {
            val field = BuildConfig::class.java.getField("SUPABASE_URL")
            field.get(null) as? String ?: ""
        } catch (_: Throwable) {
            ""
        }.trim()

        if (buildConfigUrl.isNotBlank() && !buildConfigUrl.contains("YOUR_SUPABASE_URL")) {
            return if (buildConfigUrl.endsWith("/")) buildConfigUrl else "$buildConfigUrl/"
        }

        return "https://xyzcompany.supabase.co/"
    }

    /**
     * Resolves the active Supabase public / anon key.
     */
    fun getAnonKey(): String {
        val custom = prefs.getString(KEY_CUSTOM_ANON_KEY, "")?.trim() ?: ""
        if (custom.isNotBlank()) return custom

        val buildConfigKey = try {
            val field = BuildConfig::class.java.getField("SUPABASE_ANON_KEY")
            field.get(null) as? String ?: ""
        } catch (_: Throwable) {
            ""
        }.trim()

        if (buildConfigKey.isNotBlank() && !buildConfigKey.contains("YOUR_SUPABASE_ANON_KEY")) {
            return buildConfigKey
        }

        return ""
    }

    /**
     * Checks if Supabase has been configured with a valid project URL and API key.
     */
    fun isConfigured(): Boolean {
        val url = getBaseUrl()
        val key = getAnonKey()
        return url.isNotBlank() &&
                !url.contains("xyzcompany.supabase.co") &&
                key.isNotBlank() &&
                key.length > 20
    }

    /**
     * Saves user-entered credentials from the in-app settings modal.
     */
    fun saveCredentials(url: String, anonKey: String) {
        prefs.edit()
            .putString(KEY_CUSTOM_URL, url.trim())
            .putString(KEY_CUSTOM_ANON_KEY, anonKey.trim())
            .apply()
    }

    /**
     * Clears custom credentials to revert to default BuildConfig.
     */
    fun resetToDefaults() {
        prefs.edit()
            .remove(KEY_CUSTOM_URL)
            .remove(KEY_CUSTOM_ANON_KEY)
            .apply()
    }

    fun getLastSyncTimestamp(): Long = prefs.getLong(KEY_LAST_SYNC_TIMESTAMP, 0L)

    fun setLastSyncTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC_TIMESTAMP, timestamp).apply()
    }

    fun isAutoSyncEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_SYNC_ENABLED, true)

    fun setAutoSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC_ENABLED, enabled).apply()
    }
}
