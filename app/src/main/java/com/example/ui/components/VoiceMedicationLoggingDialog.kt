package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MedBlueLight
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedSuccess
import com.example.ui.theme.MedSuccessLight
import com.example.ui.theme.MedSurface
import com.example.ui.theme.MedTextPrimary
import com.example.ui.theme.MedTextSecondary
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceMedicationLoggingDialog(
    onDismiss: () -> Unit,
    onVoiceParsed: (spokenText: String) -> Unit,
    activeMedicines: List<String> = listOf("Lisinopril 10mg", "Metformin 500mg", "Atorvastatin 20mg", "Omega-3 1000mg")
) {
    val context = LocalContext.current
    var spokenTranscript by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var parseStatusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    // Speech Recognition Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull() ?: ""
            if (spoken.isNotBlank()) {
                spokenTranscript = spoken
                onVoiceParsed(spoken)
                isSuccess = true
                parseStatusMessage = "Successfully recognized and parsed verbal intake!"
            }
        }
    }

    // Permission Launcher for Audio Recording
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isListening = true
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Say what medicine you took (e.g., 'I took my 10mg Lisinopril')")
                }
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                isListening = false
                parseStatusMessage = "Speech recognizer not available. You can tap quick voice phrases below."
            }
        } else {
            parseStatusMessage = "Microphone permission required for speech recognition."
        }
    }

    // Mic Pulsing Animation
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MedBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = MedBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Voice Intake Logger",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = "Speak naturally to record doses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_voice_dialog")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MedTextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Microphone Interactive Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(110.dp)
                ) {
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(MedBluePrimary.copy(alpha = 0.2f))
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = if (isListening) Color(0xFFD32F2F) else MedBluePrimary,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(72.dp)
                            .clickable {
                                if (isListening) {
                                    isListening = false
                                } else {
                                    permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                }
                            }
                            .testTag("voice_record_mic_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                                contentDescription = "Microphone",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (isListening) "Listening... Speak your dose now" else "Tap microphone to speak",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isListening) Color(0xFFD32F2F) else MedTextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = "Example: \"I just took my 10mg Lisinopril after lunch\" or \"Took all morning pills\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MedTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Transcript or Input Field
                OutlinedTextField(
                    value = spokenTranscript,
                    onValueChange = { spokenTranscript = it },
                    label = { Text("Spoken Transcript / Dose Command") },
                    placeholder = { Text("Spoken words will appear here...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MicNone,
                            contentDescription = null,
                            tint = MedBluePrimary
                        )
                    },
                    trailingIcon = {
                        if (spokenTranscript.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    onVoiceParsed(spokenTranscript)
                                    isSuccess = true
                                    parseStatusMessage = "Parsed & updated adherence database!"
                                },
                                modifier = Modifier.testTag("submit_voice_text_button")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Parse", tint = MedBluePrimary)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_transcript_input")
                )

                // Status Message Card
                if (parseStatusMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSuccess) MedSuccessLight else Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Medication,
                                contentDescription = null,
                                tint = if (isSuccess) MedSuccess else Color(0xFFD32F2F),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = parseStatusMessage ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = if (isSuccess) MedSuccess else Color(0xFFD32F2F)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Preset Voice Prompts (Useful for rapid simulation & testing)
                Text(
                    text = "Quick Voice Simulation Phrases:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                val samplePhrases = listOf(
                    "I just took my 10mg Lisinopril",
                    "Took 500mg Metformin with water",
                    "Logged morning Atorvastatin",
                    "Took all scheduled morning doses",
                    "Skipped evening dose today"
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    samplePhrases.forEach { phrase ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MedBlueLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MedBluePrimary.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .clickable {
                                    spokenTranscript = phrase
                                    onVoiceParsed(phrase)
                                    isSuccess = true
                                    parseStatusMessage = "Parsed: \"$phrase\" → Adherence updated!"
                                }
                                .testTag("sample_voice_phrase_${phrase.take(10)}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = MedBluePrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = phrase,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MedBluePrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (spokenTranscript.isNotBlank()) {
                        onVoiceParsed(spokenTranscript)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("voice_dialog_done_button")
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MedTextSecondary)
            }
        },
        containerColor = MedSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
