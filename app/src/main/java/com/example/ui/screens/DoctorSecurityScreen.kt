package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorSecurityScreen(
    viewModel: MedTimeViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordChangeMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(false) }

    var twoFactorEnabled by remember { mutableStateOf(true) }
    var biometricLoginEnabled by remember { mutableStateOf(true) }
    var sessionTimeoutMinutes by remember { mutableIntStateOf(15) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
            .testTag("doctor_security_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "Security & Privacy",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                Text(
                    text = "Protect clinical patient records and practitioner credentials",
                    style = MaterialTheme.typography.bodySmall,
                    color = MedTextSecondary
                )
            }
        }

        // 2. Verified License Badge Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBluePrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MedBlueLight.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = MedBluePrimary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Licensed Medical Practitioner",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                        Text(
                            text = "License ID: ${currentUser?.doctorLicense?.ifBlank { "MED-88192" }} • Status: VERIFIED & ACTIVE",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedBluePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 3. Two-Factor Authentication & Biometrics
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Authentication & Access Controls",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )

                    HorizontalDivider(color = MedBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Two-Factor Authentication (2FA)", fontWeight = FontWeight.SemiBold, color = MedTextPrimary)
                            Text("Require authenticator code when signing into doctor portal", fontSize = 11.sp, color = MedTextSecondary)
                        }
                        Switch(
                            checked = twoFactorEnabled,
                            onCheckedChange = { twoFactorEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MedBluePrimary)
                        )
                    }

                    HorizontalDivider(color = MedBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Biometric Fingerprint / Face Unlock", fontWeight = FontWeight.SemiBold, color = MedTextPrimary)
                            Text("Quick and secure access to prescription signing", fontSize = 11.sp, color = MedTextSecondary)
                        }
                        Switch(
                            checked = biometricLoginEnabled,
                            onCheckedChange = { biometricLoginEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MedBluePrimary)
                        )
                    }
                }
            }
        }

        // 4. Change Password Form
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Change Account Password",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )

                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (passwordChangeMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSuccessMessage) MedSuccessLight else MedErrorLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = passwordChangeMessage!!,
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSuccessMessage) MedSuccess else MedError
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (newPassword.isBlank()) {
                                passwordChangeMessage = "Please enter a new password"
                                isSuccessMessage = false
                            } else if (newPassword != confirmPassword) {
                                passwordChangeMessage = "New passwords do not match"
                                isSuccessMessage = false
                            } else {
                                val user = currentUser
                                if (user != null) {
                                    viewModel.updateUserProfile(user.copy(password = newPassword.trim()))
                                }
                                isSuccessMessage = true
                                passwordChangeMessage = "Password successfully updated!"
                                currentPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Update Password", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. Active Login Sessions
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
                        text = "Active Practitioner Sessions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )

                    HorizontalDivider(color = MedBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MedBluePrimary)
                            Column {
                                Text("This Android Phone", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MedTextPrimary)
                                Text("Active now • IP: 192.168.1.42", fontSize = 11.sp, color = MedTextSecondary)
                            }
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = MedSuccessLight) {
                            Text("CURRENT", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MedSuccess)
                        }
                    }

                    HorizontalDivider(color = MedBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.LaptopMac, contentDescription = null, tint = MedTextSecondary)
                            Column {
                                Text("MedTime Doctor Web Portal", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MedTextPrimary)
                                Text("Chrome MacOS • Last active 2h ago", fontSize = 11.sp, color = MedTextSecondary)
                            }
                        }
                        TextButton(onClick = {}) {
                            Text("Revoke", color = MedError, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
