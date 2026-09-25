package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerSecurityScreen(
    viewModel: MedTimeViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var twoFactorEnabled by remember { mutableStateOf(true) }
    var biometricLoginEnabled by remember { mutableStateOf(false) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordChangeMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Security & Privacy", fontWeight = FontWeight.Bold, color = MedTextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MedTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MedSurface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MedBackground)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 2FA Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MedBlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(22.dp))
                                }
                                Column {
                                    Text("Two-Factor Authentication", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Require OTP code upon sign in", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                }
                            }

                            Switch(
                                checked = twoFactorEnabled,
                                onCheckedChange = { twoFactorEnabled = it }
                            )
                        }

                        HorizontalDivider(color = MedBorderLight)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MedSuccessLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(22.dp))
                                }
                                Column {
                                    Text("Biometric Sign In", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Unlock app with Fingerprint / Face ID", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                }
                            }

                            Switch(
                                checked = biometricLoginEnabled,
                                onCheckedChange = { biometricLoginEnabled = it }
                            )
                        }
                    }
                }
            }

            // Change Password Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Change Account Password", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)

                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Current Password") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm New Password") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (currentPassword.isBlank() || newPassword.isBlank()) {
                                    passwordChangeMessage = "Please fill in all password fields."
                                } else if (newPassword != confirmPassword) {
                                    passwordChangeMessage = "New passwords do not match."
                                } else {
                                    passwordChangeMessage = "✓ Password successfully updated!"
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Update Password")
                        }

                        if (passwordChangeMessage.isNotBlank()) {
                            Text(
                                text = passwordChangeMessage,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (passwordChangeMessage.startsWith("✓")) MedSuccess else MedError
                            )
                        }
                    }
                }
            }

            // Active Sessions Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Active Caregiver Sessions", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("This Android Device (Primary)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Online now • IP: 192.168.1.42", style = MaterialTheme.typography.labelSmall, color = MedSuccess)
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = MedSuccessLight) {
                                Text("ACTIVE", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MedSuccess, fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(color = MedBorderLight)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Chrome on macOS (Web Portal)", style = MaterialTheme.typography.bodyMedium)
                                Text("Last active: 2 hours ago • San Jose, US", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                            }
                            TextButton(onClick = {}) {
                                Text("Revoke", color = MedError, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
