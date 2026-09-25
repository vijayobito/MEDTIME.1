package com.example.service

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

interface AiAssistantService {
    suspend fun getMedicalGuidance(prompt: String): String
}

class GeminiAiService : AiAssistantService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun getMedicalGuidance(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        // If key is configured and not default placeholder, attempt live Gemini API call
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                
                val systemPrompt = "You are MedTime AI, an intelligent, empathetic, and safety-focused medical assistant. " +
                        "Provide concise, easy-to-understand explanations of medications, dosages, dietary instructions (before/after food), " +
                        "and healthy habits. Emphasize medication adherence. " +
                        "Always conclude with a brief reminder to consult their healthcare provider."

                val jsonPayload = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", "$systemPrompt\n\nUser Question: $prompt"))
                            })
                        })
                    })
                }

                val body = jsonPayload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: ""
                        val root = JSONObject(responseBody)
                        val candidates = root.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val candidate = candidates.getJSONObject(0)
                            val content = candidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val text = parts.getJSONObject(0).optString("text")
                                if (text.isNotBlank()) {
                                    return@withContext text
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback gracefully to offline medical assistant logic below
            }
        }

        // Clinical Safety Offline Intelligence Engine
        return@withContext generateOfflineGuidance(prompt)
    }

    private fun generateOfflineGuidance(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("miss") || q.contains("forgot") -> {
                "If you miss a dose of your medication, the standard medical guideline is to take it as soon as you remember. " +
                        "However, if it is almost time for your next scheduled dose, skip the missed dose and resume your normal schedule. " +
                        "Never double up on doses unless specifically advised by your doctor."
            }
            q.contains("food") || q.contains("empty stomach") || q.contains("after meal") -> {
                "Medications taken 'After Food' (such as NSAIDs, Metformin, or Prednisone) help protect your gastric stomach lining and reduce nausea. " +
                        "Medications taken 'On an Empty Stomach' (such as thyroid hormones or certain antibiotics) absorb best with a full glass of water 1 hour before or 2 hours after meals."
            }
            q.contains("atorvastatin") || q.contains("cholesterol") || q.contains("statin") -> {
                "Atorvastatin is a HMG-CoA reductase inhibitor used to lower LDL ('bad') cholesterol and triglycerides while raising HDL ('good') cholesterol. " +
                        "It is usually taken once daily, with or without food. Avoid excessive grapefruit or grapefruit juice, as it can elevate statin blood levels."
            }
            q.contains("metformin") || q.contains("diabetes") || q.contains("sugar") -> {
                "Metformin helps regulate blood glucose by decreasing hepatic glucose production and improving insulin sensitivity. " +
                        "Taking it with meals significantly mitigates gastrointestinal side effects like nausea or stomach upset."
            }
            q.contains("lisinopril") || q.contains("blood pressure") || q.contains("hypertension") -> {
                "Lisinopril is an ACE inhibitor that relaxes blood vessels to decrease arterial blood pressure. " +
                        "Take it at consistent times daily. If you experience dizziness when standing up quickly, rise slowly. Report persistent dry cough to your prescribing physician."
            }
            q.contains("adherence") || q.contains("schedule") || q.contains("track") -> {
                "Medication adherence is critical for long-term chronic disease management! MedTime helps you stay on track with interactive reminders, " +
                        "taken vs missed tracking, and automatic caretaker notifications when doses are missed."
            }
            q.contains("emergency") || q.contains("chest pain") || q.contains("shortness of breath") || q.contains("bleeding") -> {
                "🚨 CRITICAL WARNING: If you or someone you are caring for is experiencing acute chest pain, severe difficulty breathing, sudden numbness, or heavy bleeding, " +
                        "do not wait! Use the MedTime Emergency dialer button immediately to call 911 or visit the nearest Emergency Room."
            }
            else -> {
                "Here are key tips regarding '$query':\n\n" +
                        "1. **Timing Consistency**: Take medications at fixed hours each day to maintain uniform therapeutic plasma levels.\n" +
                        "2. **Hydration**: Swallow oral solid medications with a full 8 oz glass of water unless fluid-restricted.\n" +
                        "3. **Storage**: Keep medications in a cool, dry place away from bathroom humidity and out of reach of children.\n" +
                        "4. **MedTime Logs**: Remember to tap 'Mark Taken' in MedTime as soon as you ingest your dose to keep your adherence log accurate."
            }
        }
    }
}
