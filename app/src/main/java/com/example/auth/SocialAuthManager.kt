package com.example.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.UserEntity
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * Supported Social Authentication Providers in MedTime
 */
enum class SocialAuthProvider(val displayName: String, val brandColorHex: Long) {
    GOOGLE("Google", 0xFF4285F4),
    FACEBOOK("Facebook", 0xFF1877F2),
    APPLE("Apple", 0xFF000000)
}

/**
 * Verified identity returned after OAuth token exchange and backend verification.
 */
data class SocialUserIdentity(
    val provider: SocialAuthProvider,
    val providerUserId: String,
    val email: String,
    val name: String,
    val photoUrl: String = "",
    val idToken: String = "",
    val isEmailVerified: Boolean = true,
    val isApplePrivateRelay: Boolean = false
)

/**
 * Detailed result of a social authentication attempt.
 */
sealed class SocialAuthResult {
    data class Success(val identity: SocialUserIdentity, val user: UserEntity, val isNewUser: Boolean) : SocialAuthResult()
    data class AccountCollision(val existingUser: UserEntity, val socialIdentity: SocialUserIdentity) : SocialAuthResult()
    data class MissingConfiguration(
        val provider: SocialAuthProvider,
        val missingKeys: List<String>,
        val setupInstructions: String,
        val suggestedIdentity: SocialUserIdentity
    ) : SocialAuthResult()
    data class Cancelled(val provider: SocialAuthProvider, val message: String = "Sign-in was cancelled.") : SocialAuthResult()
    data class Error(val provider: SocialAuthProvider, val message: String) : SocialAuthResult()
}

/**
 * Configuration descriptor for an OAuth provider
 */
data class OAuthProviderConfig(
    val provider: SocialAuthProvider,
    val clientId: String,
    val clientSecret: String = "",
    val redirectUri: String,
    val isConfigured: Boolean
)

object SocialAuthManager {

    const val ANDROID_REDIRECT_URI = "medtime://oauth/callback"
    const val WEB_REDIRECT_URI = "https://medtime.health/auth/callback"

    /**
     * Inspects configuration for the requested provider from BuildConfig / environment.
     */
    fun getProviderConfig(context: Context, provider: SocialAuthProvider): OAuthProviderConfig {
        return when (provider) {
            SocialAuthProvider.GOOGLE -> {
                val clientId = getBuildConfigField("GOOGLE_CLIENT_ID")
                OAuthProviderConfig(
                    provider = provider,
                    clientId = clientId,
                    redirectUri = ANDROID_REDIRECT_URI,
                    isConfigured = clientId.isNotBlank() && !clientId.contains("YOUR_")
                )
            }
            SocialAuthProvider.FACEBOOK -> {
                val appId = getBuildConfigField("FACEBOOK_APP_ID")
                OAuthProviderConfig(
                    provider = provider,
                    clientId = appId,
                    redirectUri = ANDROID_REDIRECT_URI,
                    isConfigured = appId.isNotBlank() && !appId.contains("YOUR_")
                )
            }
            SocialAuthProvider.APPLE -> {
                val clientId = getBuildConfigField("APPLE_CLIENT_ID")
                val customRedirect = getBuildConfigField("APPLE_REDIRECT_URI")
                val redirect = if (customRedirect.isNotBlank()) customRedirect else WEB_REDIRECT_URI
                OAuthProviderConfig(
                    provider = provider,
                    clientId = clientId,
                    redirectUri = redirect,
                    isConfigured = clientId.isNotBlank() && !clientId.contains("YOUR_")
                )
            }
        }
    }

    private fun getBuildConfigField(fieldName: String): String {
        return try {
            val field = BuildConfig::class.java.getField(fieldName)
            (field.get(null) as? String)?.trim() ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Builds the authorization URL to start the OAuth consent flow in the browser or Custom Tab.
     */
    fun buildAuthorizationUrl(provider: SocialAuthProvider, state: String = UUID.randomUUID().toString()): String {
        val encodedRedirect = URLEncoder.encode(ANDROID_REDIRECT_URI, StandardCharsets.UTF_8.toString())
        return when (provider) {
            SocialAuthProvider.GOOGLE -> {
                val clientId = getBuildConfigField("GOOGLE_CLIENT_ID").ifBlank { "medtime-google-oauth-client.apps.googleusercontent.com" }
                "https://accounts.google.com/o/oauth2/v2/auth?" +
                        "client_id=${URLEncoder.encode(clientId, "UTF-8")}&" +
                        "response_type=code&" +
                        "scope=openid%20profile%20email&" +
                        "redirect_uri=$encodedRedirect&" +
                        "state=$state&" +
                        "prompt=select_account"
            }
            SocialAuthProvider.FACEBOOK -> {
                val appId = getBuildConfigField("FACEBOOK_APP_ID").ifBlank { "medtime-facebook-app" }
                "https://www.facebook.com/v19.0/dialog/oauth?" +
                        "client_id=${URLEncoder.encode(appId, "UTF-8")}&" +
                        "redirect_uri=$encodedRedirect&" +
                        "state=$state&" +
                        "scope=email,public_profile"
            }
            SocialAuthProvider.APPLE -> {
                val clientId = getBuildConfigField("APPLE_CLIENT_ID").ifBlank { "com.medtime.signin" }
                val customRedirect = getBuildConfigField("APPLE_REDIRECT_URI").ifBlank { WEB_REDIRECT_URI }
                val encodedAppleRedirect = URLEncoder.encode(customRedirect, StandardCharsets.UTF_8.toString())
                "https://appleid.apple.com/auth/authorize?" +
                        "client_id=${URLEncoder.encode(clientId, "UTF-8")}&" +
                        "redirect_uri=$encodedAppleRedirect&" +
                        "response_type=code%20id_token&" +
                        "scope=name%20email&" +
                        "response_mode=form_post&" +
                        "state=$state"
            }
        }
    }

    /**
     * Launches the OAuth web flow via Android Custom Tab / External Browser Intent.
     */
    fun launchOAuthBrowser(context: Context, provider: SocialAuthProvider): Boolean {
        return try {
            val authUrl = buildAuthorizationUrl(provider)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Parses and decodes a simulated or real OpenID Connect / JWT ID token payload.
     */
    fun decodeJwtPayload(jwtToken: String): JSONObject? {
        return try {
            val parts = jwtToken.split(".")
            if (parts.size >= 2) {
                val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
                val jsonString = String(payloadBytes, StandardCharsets.UTF_8)
                JSONObject(jsonString)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generates a verified mock/sandbox identity for testing when developer API keys are missing in the local build.
     */
    fun createSandboxIdentity(provider: SocialAuthProvider, customEmail: String = "", customName: String = ""): SocialUserIdentity {
        val uniqueSuffix = (1000..9999).random()
        return when (provider) {
            SocialAuthProvider.GOOGLE -> {
                val email = if (customEmail.isNotBlank()) customEmail else "alex.chen.google$uniqueSuffix@gmail.com"
                val name = if (customName.isNotBlank()) customName else "Alex Chen"
                SocialUserIdentity(
                    provider = provider,
                    providerUserId = "google_sub_$uniqueSuffix",
                    email = email,
                    name = name,
                    photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80",
                    isEmailVerified = true,
                    isApplePrivateRelay = false
                )
            }
            SocialAuthProvider.FACEBOOK -> {
                val email = if (customEmail.isNotBlank()) customEmail else "sarah.miller.fb$uniqueSuffix@facebook.com"
                val name = if (customName.isNotBlank()) customName else "Sarah Miller"
                SocialUserIdentity(
                    provider = provider,
                    providerUserId = "fb_uid_$uniqueSuffix",
                    email = email,
                    name = name,
                    photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=256&q=80",
                    isEmailVerified = true,
                    isApplePrivateRelay = false
                )
            }
            SocialAuthProvider.APPLE -> {
                // Handle Apple's Private Relay email address
                val isRelay = customEmail.contains("privaterelay.appleid.com") || customEmail.isBlank()
                val email = if (customEmail.isNotBlank()) customEmail else "k7x9m2p4z@privaterelay.appleid.com"
                val name = if (customName.isNotBlank()) customName else "Jordan Taylor"
                SocialUserIdentity(
                    provider = provider,
                    providerUserId = "apple_user_id_$uniqueSuffix",
                    email = email,
                    name = name,
                    photoUrl = "",
                    isEmailVerified = true,
                    isApplePrivateRelay = isRelay
                )
            }
        }
    }
}
