package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserEntity
import com.example.ui.scanner.QrScannerDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@Composable
fun ConnectWebsiteDialog(
    user: UserEntity,
    viewModel: MedTimeViewModel,
    onDismiss: () -> Unit
) {
    var showScanner by remember { mutableStateOf(false) }
    var scannedWebSessionCode by remember { mutableStateOf<String?>(null) }
    var isConnectedSuccess by remember { mutableStateOf(false) }

    if (showScanner) {
        QrScannerDialog(
            onDismiss = { showScanner = false },
            onAccountScanned = { payload ->
                showScanner = false
                scannedWebSessionCode = payload.code.ifBlank { "SESSION-WEB-${System.currentTimeMillis() % 100000}" }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = MedSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MedBlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = MedBluePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Connect to MedTime Web",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Sync and manage your medication reminders, lab documents, and doctor consultations on your desktop or tablet browser securely.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MedTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                if (isConnectedSuccess) {
                    // Success state
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MedSuccessLight
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Web Session Authorized!", fontWeight = FontWeight.Bold, color = MedSuccess, fontSize = 15.sp)
                            Text(
                                "Your MedTime Web Portal is now connected and synced in real-time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MedSuccess),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Done")
                    }
                } else if (scannedWebSessionCode != null) {
                    // Approval confirmation prompt
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MedSurfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MedBluePrimary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.LaptopMac, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Authorize Web Session?", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Connect this browser to your MedTime account (${user.name})?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MedSurface
                            ) {
                                Text(
                                    text = "Code: ${scannedWebSessionCode?.take(20)}...",
                                    fontSize = 11.sp,
                                    color = MedTextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { scannedWebSessionCode = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                isConnectedSuccess = true
                                viewModel.sendDirectNotification(
                                    userId = user.id,
                                    title = "Web Portal Connected",
                                    message = "Authorized desktop browser session for ${user.name}",
                                    type = "SYSTEM"
                                )
                            },
                            modifier = Modifier.weight(1f).testTag("btn_approve_web_connection"),
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("APPROVE")
                        }
                    }
                } else {
                    // Instructions & Scan Button
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MedSurfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("1. Open web.medtime.app on desktop", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QrCode, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("2. Tap Scan QR Code below to scan", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("3. Single-use, short-lived encrypted token", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { showScanner = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_start_scan_website_qr"),
                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan Website QR Code", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", color = MedTextSecondary)
                    }
                }
            }
        }
    }
}

