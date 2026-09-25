package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun ReminderSoundDialog(
    onDismiss: () -> Unit
) {
    var selectedTone by remember { mutableStateOf("Gentle Chime (Default)") }
    var selectedVibration by remember { mutableStateOf("Standard Pulse") }
    var alarmVolume by remember { mutableStateOf(0.85f) }
    var fullScreenAlertEnabled by remember { mutableStateOf(true) }
    var voiceReadoutEnabled by remember { mutableStateOf(true) }

    val tones = remember {
        listOf(
            "Gentle Chime (Default)",
            "Clinical Alarm Beep",
            "Morning Radiant Melody",
            "Soft Harp Progression",
            "Urgent Medical Siren"
        )
    }

    val vibrations = remember {
        listOf(
            "Standard Pulse",
            "Double Heartbeat",
            "Continuous Buzz",
            "Vibrate Only",
            "Silent / Off"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MedBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Reminder Sound",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "Auditory & Vibration Preferences",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("reminder_sound_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MedTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Volume Control
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MedSurfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Alert Volume", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTextPrimary)
                                    Text("${(alarmVolume * 100).toInt()}%", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedBluePrimary)
                                }
                                Slider(
                                    value = alarmVolume,
                                    onValueChange = { alarmVolume = it },
                                    colors = SliderDefaults.colors(
                                        thumbColor = MedBluePrimary,
                                        activeTrackColor = MedBluePrimary,
                                        inactiveTrackColor = MedBorder
                                    )
                                )
                            }
                        }
                    }

                    // Tone Selection
                    item {
                        Text(
                            text = "ALARM SOUND TONE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = MedTextSecondary
                            )
                        )
                    }

                    items(tones) { tone ->
                        val isSelected = tone == selectedTone
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedTone = tone }
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MedBluePrimary else MedBorder,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            color = if (isSelected) MedBlueLight.copy(alpha = 0.5f) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.MusicNote else Icons.Default.Audiotrack,
                                        contentDescription = null,
                                        tint = if (isSelected) MedBluePrimary else MedTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = tone,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MedBlueDark else MedTextPrimary
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedTone = tone },
                                    colors = RadioButtonDefaults.colors(selectedColor = MedBluePrimary)
                                )
                            }
                        }
                    }

                    // Vibration Selection
                    item {
                        Text(
                            text = "VIBRATION PATTERN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = MedTextSecondary
                            ),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    items(vibrations) { pattern ->
                        val isSelected = pattern == selectedVibration
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedVibration = pattern }
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MedBluePrimary else MedBorder,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            color = if (isSelected) MedBlueLight.copy(alpha = 0.5f) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Vibration,
                                        contentDescription = null,
                                        tint = if (isSelected) MedBluePrimary else MedTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = pattern,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MedBlueDark else MedTextPrimary
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedVibration = pattern },
                                    colors = RadioButtonDefaults.colors(selectedColor = MedBluePrimary)
                                )
                            }
                        }
                    }

                    // Extra Toggles
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MedSurfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Full-Screen Alarm Modal", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = MedTextPrimary)
                                        Text("Wake screen and show Take/Snooze buttons directly", fontSize = 11.sp, color = MedTextSecondary)
                                    }
                                    Switch(
                                        checked = fullScreenAlertEnabled,
                                        onCheckedChange = { fullScreenAlertEnabled = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MedBluePrimary)
                                    )
                                }

                                HorizontalDivider(color = MedDivider)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Voice Dosage Name Announcement", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = MedTextPrimary)
                                        Text("Read medicine name aloud when reminder rings", fontSize = 11.sp, color = MedTextSecondary)
                                    }
                                    Switch(
                                        checked = voiceReadoutEnabled,
                                        onCheckedChange = { voiceReadoutEnabled = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MedBluePrimary)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Sound Settings", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
