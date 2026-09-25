package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@Composable
fun SecurityPrivacyDialog(
    user: UserEntity,
    viewModel: MedTimeViewModel,
    onDismiss: () -> Unit
) {
    var is2FaEnabled by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var locationSharingEnabled by remember { mutableStateOf(true) }
    var doctorAccessEnabled by remember { mutableStateOf(true) }
    var caretakerSyncEnabled by remember { mutableStateOf(true) }
    var documentEncryptionEnabled by remember { mutableStateOf(true) }
    var showBackupCodesDialog by remember { mutableStateOf(false) }

    if (showBackupCodesDialog) {
        AlertDialog(
            onDismissRequest = { showBackupCodesDialog = false },
            title = { Text("2FA Backup Recovery Codes", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Store these single-use recovery codes in a safe place:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                    listOf("8491-3021", "5920-1849", "7730-9182", "3019-4821").forEach { code ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MedSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = code,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold,
                                color = MedTextPrimary,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showBackupCodesDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)) {
                    Text("Done")
                }
            }
        )
    }

    if (showChangePasswordDialog) {
        var currentPwd by remember { mutableStateOf("") }
        var newPwd by remember { mutableStateOf("") }
        var confirmPwd by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Account Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (errorMsg != null) {
                        Text(errorMsg!!, color = MedError, style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedTextField(
                        value = currentPwd,
                        onValueChange = { currentPwd = it },
                        label = { Text("Current Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPwd,
                        onValueChange = { newPwd = it },
                        label = { Text("New Password (min 6 chars)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPwd,
                        onValueChange = { confirmPwd = it },
                        label = { Text("Confirm New Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPwd.length < 6) {
                            errorMsg = "Password must be at least 6 characters"
                            return@Button
                        }
                        if (newPwd != confirmPwd) {
                            errorMsg = "Passwords do not match"
                            return@Button
                        }
                        viewModel.updateUserProfile(user.copy(password = newPwd))
                        showChangePasswordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp)),
            color = MedSurface,
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
                                .background(MedBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text("Security & Privacy", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
                            Text("HIPAA Compliance & Permissions", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MedTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Two-Factor Authentication
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MedSurfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Two-Factor Authentication (2FA)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Require TOTP / SMS code upon login", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                    }
                                    Switch(
                                        checked = is2FaEnabled,
                                        onCheckedChange = {
                                            is2FaEnabled = it
                                        }
                                    )
                                }

                                if (is2FaEnabled) {
                                    OutlinedButton(
                                        onClick = { showBackupCodesDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("View Backup Recovery Codes", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Password Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MedSurfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Account Password", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("Last changed recently", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                }
                                Button(
                                    onClick = { showChangePasswordDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Change", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Privacy Controls
                    item {
                        Text("Clinical Data & Privacy Permissions", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MedTextSecondary)
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MedSurfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Doctor Access
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Doctor Clinical Access", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Allow connected doctors to review medication adherence", fontSize = 11.sp, color = MedTextSecondary)
                                    }
                                    Switch(checked = doctorAccessEnabled, onCheckedChange = { doctorAccessEnabled = it })
                                }
                                HorizontalDivider(color = MedDivider)

                                // Caretaker Sync
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Caretaker Emergency Alerts", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Notify approved family caregivers when doses are missed", fontSize = 11.sp, color = MedTextSecondary)
                                    }
                                    Switch(checked = caretakerSyncEnabled, onCheckedChange = { caretakerSyncEnabled = it })
                                }
                                HorizontalDivider(color = MedDivider)

                                // Document Encryption
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Document Storage Encryption", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("AES-256 local and cloud file encryption for medical reports", fontSize = 11.sp, color = MedTextSecondary)
                                    }
                                    Switch(checked = documentEncryptionEnabled, onCheckedChange = { documentEncryptionEnabled = it })
                                }
                                HorizontalDivider(color = MedDivider)

                                // Location
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Location Services for Healthcare Maps", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Find nearby pharmacies and emergency hospitals", fontSize = 11.sp, color = MedTextSecondary)
                                    }
                                    Switch(checked = locationSharingEnabled, onCheckedChange = { locationSharingEnabled = it })
                                }
                            }
                        }
                    }

                    // Active Sessions
                    item {
                        Text("Active Sessions & Devices", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MedTextSecondary)
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MedSurfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(20.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Current Android Device", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("MedTime Mobile App • Active Now", fontSize = 11.sp, color = MedSuccess)
                                    }
                                }

                                HorizontalDivider(color = MedDivider)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.LaptopMac, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(20.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Web Dashboard Session", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Chrome on MacOS • Synced today", fontSize = 11.sp, color = MedTextSecondary)
                                    }
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
                    Text("Close")
                }
            }
        }
    }
}
