package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@Composable
fun EditProfileDialog(
    user: UserEntity,
    viewModel: MedTimeViewModel,
    onDismiss: () -> Unit
) {
    var editName by remember { mutableStateOf(user.name) }
    var editPhone by remember { mutableStateOf(user.phone) }
    var editEmail by remember { mutableStateOf(user.email) }
    var editBlood by remember { mutableStateOf(user.bloodGroup) }
    var editAllergies by remember { mutableStateOf(user.allergies) }
    var editContactName by remember { mutableStateOf(user.emergencyContactName) }
    var editContactPhone by remember { mutableStateOf(user.emergencyContactPhone) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
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
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MedBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Column {
                            Text("Patient Profile", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
                            Text("Account Details & Clinical Info", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MedTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("profile_input_name")
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editBlood,
                            onValueChange = { editBlood = it },
                            label = { Text("Blood Group (e.g. O+, A-, B+)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editAllergies,
                            onValueChange = { editAllergies = it },
                            label = { Text("Known Drug & Food Allergies") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text(
                            text = "Emergency Contact Person",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editContactName,
                            onValueChange = { editContactName = it },
                            label = { Text("Emergency Contact Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editContactPhone,
                            onValueChange = { editContactPhone = it },
                            label = { Text("Emergency Contact Phone") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val updated = user.copy(
                                name = editName.trim().ifBlank { user.name },
                                email = editEmail.trim().ifBlank { user.email },
                                phone = editPhone.trim(),
                                bloodGroup = editBlood.trim(),
                                allergies = editAllergies.trim(),
                                emergencyContactName = editContactName.trim(),
                                emergencyContactPhone = editContactPhone.trim()
                            )
                            viewModel.updateUserProfile(updated)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).testTag("profile_save_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
