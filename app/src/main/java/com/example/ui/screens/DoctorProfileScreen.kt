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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorProfileScreen(
    viewModel: MedTimeViewModel,
    onNavigateToSecurity: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()

    var name by remember(currentUser) { mutableStateOf(currentUser?.name ?: "Dr. Sarah Mitchell, MD") }
    var specialty by remember(currentUser) { mutableStateOf(currentUser?.doctorSpecialty ?: "Cardiology & Internal Medicine") }
    var hospital by remember(currentUser) { mutableStateOf(currentUser?.doctorHospital ?: "City General Hospital") }
    var license by remember(currentUser) { mutableStateOf(currentUser?.doctorLicense ?: "MED-88192") }
    var experienceYears by remember(currentUser) { mutableIntStateOf(currentUser?.doctorYearsExperience ?: 12) }
    var consultationFee by remember { mutableDoubleStateOf(120.0) }
    var bio by remember(currentUser) {
        mutableStateOf(
            currentUser?.doctorBio?.ifBlank {
                "Board-certified Cardiologist with 12+ years of experience in clinical cardiovascular care, preventive cardiology, hypertension management, and non-invasive diagnostic cardiac imaging."
            } ?: "Board-certified Cardiologist with 12+ years of experience in clinical cardiovascular care."
        )
    }
    var phone by remember(currentUser) { mutableStateOf(currentUser?.phone ?: "+1 (555) 234-5678") }
    var email by remember(currentUser) { mutableStateOf(currentUser?.email ?: "sarah.mitchell@medtime.app") }

    var isSavedNotification by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
            .testTag("doctor_profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Professional Profile",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                Text(
                    text = "Doctor credentials, specialty, clinic affiliation & consultation fees",
                    style = MaterialTheme.typography.bodySmall,
                    color = MedTextSecondary
                )
            }
        }

        // Photo Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.size(90.dp)) {
                        Surface(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .border(3.dp, MedBluePrimary, CircleShape),
                            color = MedBlueLight
                        ) {
                            AsyncImage(
                                model = currentUser?.doctorProfilePhotoUrl?.ifBlank {
                                    "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=400"
                                } ?: "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=400",
                                contentDescription = "Doctor Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .align(Alignment.BottomEnd),
                            color = MedBluePrimary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Change photo", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Verified, contentDescription = "Verified Practitioner", tint = MedBluePrimary, modifier = Modifier.size(20.dp))
                        }
                        Text(text = specialty, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MedBluePrimary)
                        Text(text = "$hospital • License: $license", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                    }
                }
            }
        }

        // Edit Profile Form
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
                        text = "Clinical Information",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name & Title *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = specialty,
                        onValueChange = { specialty = it },
                        label = { Text("Specialty & Field *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = hospital,
                        onValueChange = { hospital = it },
                        label = { Text("Hospital / Clinic Affiliation *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = license,
                            onValueChange = { license = it },
                            label = { Text("Medical License No. *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = "$experienceYears Years",
                            onValueChange = {
                                val v = it.filter { ch -> ch.isDigit() }
                                experienceYears = v.toIntOrNull() ?: 12
                            },
                            label = { Text("Experience") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = "$$consultationFee",
                        onValueChange = {
                            val v = it.replace("$", "").trim()
                            consultationFee = v.toDoubleOrNull() ?: 120.0
                        },
                        label = { Text("Consultation Fee (USD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Professional Biography & Qualifications") },
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                }
            }
        }

        // Contact & Clinic Details
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
                        text = "Contact & Communications",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Official Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Clinic Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val user = currentUser
                    if (user != null) {
                        val updated = user.copy(
                            name = name.trim(),
                            email = email.trim(),
                            phone = phone.trim(),
                            doctorSpecialty = specialty.trim(),
                            doctorHospital = hospital.trim(),
                            doctorLicense = license.trim(),
                            doctorYearsExperience = experienceYears,
                            doctorBio = bio.trim()
                        )
                        viewModel.updateUserProfile(updated)
                    }
                    isSavedNotification = true
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Profile Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        if (isSavedNotification) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MedSuccessLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedSuccess)
                        Text(
                            text = "Professional credentials updated successfully.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MedSuccess
                        )
                    }
                }
            }
        }
    }
}
