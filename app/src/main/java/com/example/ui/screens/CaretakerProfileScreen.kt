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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerProfileScreen(
    viewModel: MedTimeViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()

    var nameInput by remember(currentUser) { mutableStateOf(currentUser?.name ?: "Emily Watson") }
    var emailInput by remember(currentUser) { mutableStateOf(currentUser?.email ?: "emily.watson@medtime.care") }
    var phoneInput by remember(currentUser) { mutableStateOf(currentUser?.phone ?: "+1 (555) 345-6789") }
    var relationInput by remember(currentUser) { mutableStateOf(currentUser?.emergencyContactRelation ?: "Primary Caregiver / Daughter") }

    var isSavedConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Caretaker Profile", fontWeight = FontWeight.Bold, color = MedTextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MedTextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = onNavigateToSecurity) {
                        Text("Security", color = MedBluePrimary, fontWeight = FontWeight.Bold)
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
            // Profile Avatar Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MedBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                        }

                        Text(nameInput, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MedWarningLight
                        ) {
                            Text(
                                text = "ROLE: CARETAKER (Verified)",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MedWarning
                            )
                        }

                        Text("Unique Caregiver ID: ${currentUser?.id ?: "caretaker_1"}", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                    }
                }
            }

            // Editable profile form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("Personal Information", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Full Legal Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Contact Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = relationInput,
                            onValueChange = { relationInput = it },
                            label = { Text("Caregiver Relationship / Designation") },
                            leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val current = currentUser
                                if (current != null) {
                                    val updated = current.copy(
                                        name = nameInput,
                                        email = emailInput,
                                        phone = phoneInput,
                                        emergencyContactRelation = relationInput
                                    )
                                    viewModel.updateUserProfile(updated)
                                    isSavedConfirmation = true
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("caretaker_save_profile_button")
                        ) {
                            Text("Save Profile Changes")
                        }

                        if (isSavedConfirmation) {
                            Text(
                                text = "✓ Profile details updated successfully!",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MedSuccess
                            )
                        }
                    }
                }
            }
        }
    }
}
