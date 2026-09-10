package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@Composable
fun CaretakerScreen(
    viewModel: MedTimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isCaretaker = currentUser?.role == "CARETAKER"

    val patientLinks by viewModel.patientCaretakerLinks.collectAsState()
    val caretakerLinks by viewModel.caretakerLinks.collectAsState()

    var linkingCodeInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Patient Mode Header: Linking Code Display
        if (!isCaretaker) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = MedBluePrimary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your Family Caretaker Sync Code",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = currentUser?.caretakerLinkingCode ?: "MED-7842",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    ),
                                    color = Color.White
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Caretaker Code", currentUser?.caretakerLinkingCode ?: "MED-7842")
                                        clipboard.setPrimaryClip(clip)
                                    }
                                ) {
                                    Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = Color.White)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Share this secure code with your family member or caretaker so they can monitor your medication adherence.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // Pending and Linked Caretakers List for Patient
            item {
                Text(
                    text = "Connected Caretakers (${patientLinks.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
            }

            if (patientLinks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MedSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.PeopleOutline, contentDescription = null, tint = MedTextTertiary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Caretakers Linked Yet", fontWeight = FontWeight.Bold)
                            Text("When a caregiver enters your code, their request will appear here for approval.", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        }
                    }
                }
            } else {
                items(patientLinks, key = { it.id }) { link ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MedSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
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
                                            .clip(CircleShape)
                                            .background(MedBlueLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MedBluePrimary)
                                    }
                                    Column {
                                        Text(link.caretakerName, fontWeight = FontWeight.Bold, color = MedTextPrimary)
                                        Text("Status: ${link.status}", style = MaterialTheme.typography.bodySmall, color = if (link.status == "APPROVED") MedSuccess else MedWarning)
                                    }
                                }

                                if (link.status == "PENDING") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { viewModel.updateCaretakerStatus(link.id, "APPROVED") },
                                            colors = ButtonDefaults.buttonColors(containerColor = MedSuccess),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Approve", fontSize = 12.sp)
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.updateCaretakerStatus(link.id, "REJECTED") },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Reject", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            if (link.status == "APPROVED") {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MedBorder)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Sharing Permissions", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MedTextSecondary)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("View Medication Regimen", style = MaterialTheme.typography.bodySmall)
                                    Switch(
                                        checked = link.canViewMedicines,
                                        onCheckedChange = { viewModel.updateCaretakerPermissions(link.id, it, link.canViewAdherence, link.canViewAppointments, link.canReceiveAlerts) }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("View Adherence Statistics", style = MaterialTheme.typography.bodySmall)
                                    Switch(
                                        checked = link.canViewAdherence,
                                        onCheckedChange = { viewModel.updateCaretakerPermissions(link.id, link.canViewMedicines, it, link.canViewAppointments, link.canReceiveAlerts) }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Emergency Missed Dose Alerts", style = MaterialTheme.typography.bodySmall)
                                    Switch(
                                        checked = link.canReceiveAlerts,
                                        onCheckedChange = { viewModel.updateCaretakerPermissions(link.id, link.canViewMedicines, link.canViewAdherence, link.canViewAppointments, it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Caretaker Mode
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MedSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Link to a Patient",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Enter the unique 8-character linking code generated by the patient to request caretaker monitoring access.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = linkingCodeInput,
                                onValueChange = { linkingCodeInput = it },
                                placeholder = { Text("e.g. MED-7842") },
                                modifier = Modifier.weight(1f).testTag("input_caretaker_code"),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    if (linkingCodeInput.isNotBlank()) {
                                        viewModel.requestCaretakerLink(linkingCodeInput.trim())
                                        linkingCodeInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("submit_link_patient")
                            ) {
                                Text("Link Patient")
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Monitored Patients",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
            }

            if (caretakerLinks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MedSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Healing, contentDescription = null, tint = MedTextTertiary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Linked Patients", fontWeight = FontWeight.Bold)
                            Text("Enter a patient's code above to start monitoring their daily regimen.", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        }
                    }
                }
            } else {
                items(caretakerLinks, key = { it.id }) { link ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MedSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(link.patientName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("Linking Code: ${link.linkingCode}", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (link.status == "APPROVED") MedSuccessLight else MedWarningLight
                                ) {
                                    Text(
                                        text = link.status,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (link.status == "APPROVED") MedSuccess else MedWarning
                                    )
                                }
                            }

                            if (link.status == "APPROVED") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Medications: ${if (link.canViewMedicines) "Accessible" else "Hidden"}", style = MaterialTheme.typography.bodySmall)
                                    Text("Adherence: ${if (link.canViewAdherence) "Live Sync" else "Restricted"}", style = MaterialTheme.typography.bodySmall)
                                    Text("Emergency Alerts: ${if (link.canReceiveAlerts) "Enabled" else "Off"}", style = MaterialTheme.typography.bodySmall, color = MedSuccess)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
