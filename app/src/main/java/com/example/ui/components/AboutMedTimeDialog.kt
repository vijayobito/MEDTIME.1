package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun AboutMedTimeDialog(
    onDismiss: () -> Unit
) {
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
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "About MedTime",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "Smart Healthcare Companion",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("about_medtime_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MedTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // App Identity Card
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MedBlueLight.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MedBluePrimary.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MedBluePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("MedTime", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MedBlueDark)
                                Text("Version 1.0.0", fontSize = 12.sp, color = MedTextSecondary, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Patient Health, Medication Adherence & Caregiver Sync", fontSize = 11.sp, color = MedTextSecondary)
                            }
                        }
                    }

                    // Key Features List
                    item {
                        Text(
                            text = "CORE CAPABILITIES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = MedTextSecondary
                            )
                        )
                    }

                    item {
                        val features = listOf(
                            Pair("Smart Reminders", "Reliable scheduled dosage alarms, custom refill alerts, and voice confirmation logging."),
                            Pair("Caregiver Live Sync", "Automatic missed dose escalation to loved ones and verified caretakers."),
                            Pair("AI Clinical Assistant", "Instant pill analysis, medication interactions, and lifestyle guidance."),
                            Pair("Doctor Appointments & Chat", "Consultations with certified specialists and real-time medical inquiries."),
                            Pair("Encrypted Health Vault", "Offline-first Room database with AES-256 local encrypted cache for prescriptions and records.")
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            features.forEach { (title, desc) ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = MedSurfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(16.dp))
                                        Column {
                                            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = MedTextPrimary)
                                            Text(desc, fontSize = 11.sp, color = MedTextSecondary, lineHeight = 16.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Medical Disclaimer
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MedWarningLight.copy(alpha = 0.7f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MedWarning.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = MedWarningDark, modifier = Modifier.size(16.dp))
                                    Text("Medical Advisory Notice", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MedWarningDark)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "MedTime is designed for health management and dose organization. It does not provide medical diagnosis. Always consult a licensed physician or pharmacist regarding prescription regimens.",
                                    fontSize = 11.sp,
                                    color = MedTextPrimary,
                                    lineHeight = 15.sp
                                )
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
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
