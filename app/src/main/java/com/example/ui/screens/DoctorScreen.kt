package com.example.ui.screens

<<<<<<< HEAD
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
=======
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
<<<<<<< HEAD
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AppointmentEntity
import com.example.data.model.UserEntity
import com.example.ui.components.AppointmentCard
import com.example.ui.components.StatCard
import com.example.ui.scanner.QrScannerDialog
import com.example.ui.scanner.ShowQrCodeDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
=======
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppointmentEntity
import com.example.ui.components.AppointmentCard
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465

@Composable
fun DoctorScreen(
    viewModel: MedTimeViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
<<<<<<< HEAD
    val isVerified = currentUser?.isDoctorVerified == true || currentUser?.doctorVerificationStatus == "APPROVED"

    if (!isVerified) {
        DoctorPendingVerificationView(
            doctor = currentUser,
            viewModel = viewModel,
            modifier = modifier
        )
    } else {
        DoctorVerifiedDashboardView(
            doctor = currentUser,
            viewModel = viewModel,
            modifier = modifier
        )
    }
}

/**
 * State-based Gate View displayed when doctor verification is PENDING, UNDER_REVIEW, or REJECTED.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorPendingVerificationView(
    doctor: UserEntity?,
    viewModel: MedTimeViewModel,
    modifier: Modifier = Modifier
) {
    var showEditCredentialsDialog by remember { mutableStateOf(false) }
    var showPreviewLicenseDialog by remember { mutableStateOf(false) }

    val status = doctor?.doctorVerificationStatus ?: "PENDING"
    val isRejected = status.equals("REJECTED", ignoreCase = true)

    val licenseImage = doctor?.doctorLicenseImageUrl?.ifBlank {
        "https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=600"
    } ?: "https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=600"

    val profilePhoto = doctor?.doctorProfilePhotoUrl?.ifBlank {
        "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=400"
    } ?: "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=400"

    if (showEditCredentialsDialog && doctor != null) {
        DoctorCredentialsDialog(
            doctor = doctor,
            onDismiss = { showEditCredentialsDialog = false },
            onSubmit = { license, specialty, hospital, licImg, profImg, council, exp ->
                viewModel.submitDoctorCredentials(
                    license = license,
                    specialty = specialty,
                    hospital = hospital,
                    licenseImageUrl = licImg,
                    profilePhotoUrl = profImg,
                    issuingCouncil = council,
                    yearsExperience = exp
                ) { success, _ ->
                    if (success) showEditCredentialsDialog = false
                }
            }
        )
    }

    if (showPreviewLicenseDialog) {
        AlertDialog(
            onDismissRequest = { showPreviewLicenseDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MedBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Medical License Document", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MedBorder, RoundedCornerShape(12.dp)),
                        color = MedSurface
                    ) {
                        AsyncImage(
                            model = licenseImage,
                            contentDescription = "Medical License Scan",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Text(
                        text = "License ID: ${doctor?.doctorLicense ?: "PENDING"}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Text(
                        text = "Issued by ${doctor?.doctorIssuingCouncil ?: "National Medical Board"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPreviewLicenseDialog = false }) {
                    Text("Close Preview")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
            .testTag("doctor_pending_verification_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Status Banner Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = if (isRejected) Color(0xFFC62828) else Color(0xFFE65100)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(54.dp),
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isRejected) Icons.Default.ErrorOutline else Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isRejected) "Verification Rejected" else "Doctor Verification In Review",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = if (isRejected) "Action Required: Resubmit Credentials" else "Application Pending Admin Approval",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (isRejected) "REJECTED" else "PENDING",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                    Text(
                        text = if (isRejected) {
                            "Your submitted medical credentials could not be verified. Please review and resubmit your valid medical license document and professional photo."
                        } else {
                            "Thank you for registering with MedTime Clinical. To ensure patient safety and comply with healthcare laws, our medical administrator board is currently reviewing your medical practitioner license and identity documents."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.95f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 2. Verification Progress Stepper
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
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Verification Progress",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )

                    // Step 1: Account & Profile
                    VerificationStepItem(
                        stepNumber = 1,
                        title = "Registration & Profile Creation",
                        subtitle = "Account created with email & contact details",
                        isCompleted = true,
                        isActive = false
                    )

                    // Step 2: License & Document Review
                    VerificationStepItem(
                        stepNumber = 2,
                        title = "Medical License & Document Review",
                        subtitle = if (isRejected) "Requires resubmission" else "Under active compliance review by Admin",
                        isCompleted = false,
                        isActive = true,
                        isError = isRejected
                    )

                    // Step 3: Clinical Dashboard Access
                    VerificationStepItem(
                        stepNumber = 3,
                        title = "Full Clinical Portal Activation",
                        subtitle = "Access appointments queue, prescriptions, and patient records",
                        isCompleted = false,
                        isActive = false
                    )
                }
            }
        }

        // 3. Submitted Doctor Credentials Review Card
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
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Submitted Practitioner Credentials",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )

                        TextButton(
                            onClick = { showEditCredentialsDialog = true },
                            modifier = Modifier.testTag("btn_edit_credentials")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = MedBluePrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Update", fontSize = 12.sp, color = MedBluePrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Profile Photo & Name Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(2.dp, MedBluePrimary, CircleShape),
                            color = MedBlueLight
                        ) {
                            AsyncImage(
                                model = profilePhoto,
                                contentDescription = "Doctor Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column {
                            Text(
                                text = doctor?.name ?: "Dr. Medical Practitioner",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "${doctor?.doctorSpecialty?.ifBlank { "General Medicine" }} • ${doctor?.doctorHospital?.ifBlank { "Clinical Practice" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )
                            Text(
                                text = "Experience: ${doctor?.doctorYearsExperience ?: 5}+ Years",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedBluePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    HorizontalDivider(color = MedBorder)

                    // License Card Preview
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Medical License Document",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextSecondary
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MedBorder, RoundedCornerShape(12.dp))
                                .clickable { showPreviewLicenseDialog = true },
                            color = MedBackground
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    color = MedSurface
                                ) {
                                    AsyncImage(
                                        model = licenseImage,
                                        contentDescription = "License thumbnail",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "License ID: ${doctor?.doctorLicense?.ifBlank { "MED-LIC-PENDING" }}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MedTextPrimary
                                    )
                                    Text(
                                        text = doctor?.doctorIssuingCouncil?.ifBlank { "National Medical Council" } ?: "National Medical Council",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MedTextSecondary
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Preview License",
                                    tint = MedBluePrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Fast-Track Testing & Admin Simulation
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF7B1FA2))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Admin Simulation / Instant Approval",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF4A148C)
                        )
                    }
                    Text(
                        text = "For demonstration and testing purposes, you can immediately simulate administrator verification to unlock the full clinical dashboard.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6A1B9A)
                    )

                    Button(
                        onClick = {
                            if (doctor != null) {
                                viewModel.verifyDoctor(doctor.id, true)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_fast_track_verify"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Approve Doctor Verification (Instant Demo)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationStepItem(
    stepNumber: Int,
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    isActive: Boolean,
    isError: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = when {
                isError -> MedErrorLight
                isCompleted -> MedSuccessLight
                isActive -> MedWarningLight
                else -> MedBackground
            }
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(16.dp))
                } else if (isError) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = MedError, modifier = Modifier.size(16.dp))
                } else if (isActive) {
                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = MedWarning, modifier = Modifier.size(16.dp))
                } else {
                    Text(
                        text = "$stepNumber",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MedTextTertiary
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isActive || isCompleted) MedTextPrimary else MedTextSecondary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) MedError else MedTextSecondary
            )
        }
    }
}

/**
 * Dialog to edit / upload doctor credentials (photo + license document).
 */
@Composable
fun DoctorCredentialsDialog(
    doctor: UserEntity,
    onDismiss: () -> Unit,
    onSubmit: (license: String, specialty: String, hospital: String, licImg: String, profImg: String, council: String, exp: Int) -> Unit
) {
    var licenseId by remember { mutableStateOf(doctor.doctorLicense.ifBlank { "MED-LIC-88192" }) }
    var specialty by remember { mutableStateOf(doctor.doctorSpecialty.ifBlank { "Cardiologist" }) }
    var hospital by remember { mutableStateOf(doctor.doctorHospital.ifBlank { "City General Hospital" }) }
    var council by remember { mutableStateOf(doctor.doctorIssuingCouncil.ifBlank { "National Medical Council" }) }
    var experience by remember { mutableIntStateOf(doctor.doctorYearsExperience.takeIf { it > 0 } ?: 6) }

    var selectedProfilePhoto by remember {
        mutableStateOf(doctor.doctorProfilePhotoUrl.ifBlank { "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=400" })
    }
    var selectedLicenseImage by remember {
        mutableStateOf(doctor.doctorLicenseImageUrl.ifBlank { "https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=600" })
    }

    val presetPhotos = listOf(
        "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=400",
        "https://images.unsplash.com/photo-1594824813689-53b0e14a1a6b?w=400",
        "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=400",
        "https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?w=400"
    )

    val presetLicenses = listOf(
        "https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=600",
        "https://images.unsplash.com/photo-1450133064473-71024230f91b?w=600",
        "https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?w=600"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MedBluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update Practitioner Credentials", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "1. Doctor Profile Photo",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(presetPhotos) { photoUrl ->
                            Surface(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (selectedProfilePhoto == photoUrl) 3.dp else 1.dp,
                                        color = if (selectedProfilePhoto == photoUrl) MedBluePrimary else MedBorder,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedProfilePhoto = photoUrl },
                                color = MedSurface
                            ) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Doctor photo option",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "2. Medical License Document / Certificate",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(presetLicenses) { licUrl ->
                            Surface(
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (selectedLicenseImage == licUrl) 3.dp else 1.dp,
                                        color = if (selectedLicenseImage == licUrl) MedBluePrimary else MedBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedLicenseImage = licUrl },
                                color = MedSurface
                            ) {
                                AsyncImage(
                                    model = licUrl,
                                    contentDescription = "License document option",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = licenseId,
                        onValueChange = { licenseId = it },
                        label = { Text("Medical License ID *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_update_license_id")
                    )
                }

                item {
                    OutlinedTextField(
                        value = specialty,
                        onValueChange = { specialty = it },
                        label = { Text("Medical Specialty *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = hospital,
                        onValueChange = { hospital = it },
                        label = { Text("Hospital / Clinic Affiliation *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = council,
                        onValueChange = { council = it },
                        label = { Text("Issuing Medical Council") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(licenseId, specialty, hospital, selectedLicenseImage, selectedProfilePhoto, council, experience)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                modifier = Modifier.testTag("btn_submit_updated_credentials")
            ) {
                Text("Submit for Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Full Verified Doctor Clinical Portal
 */
@Composable
fun DoctorVerifiedDashboardView(
    doctor: UserEntity?,
    viewModel: MedTimeViewModel,
    modifier: Modifier = Modifier
) {
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
    val appointments by viewModel.doctorAppointments.collectAsState()

    var appointmentNotesDialog by remember { mutableStateOf<AppointmentEntity?>(null) }
    var clinicalNotesText by remember { mutableStateOf("") }
<<<<<<< HEAD
    var showQrCodeDialog by remember { mutableStateOf(false) }
    var showQrScannerDialog by remember { mutableStateOf(false) }
    var showIssuePrescriptionDialog by remember { mutableStateOf(false) }
    var targetPatientForPrescription by remember { mutableStateOf<Pair<String, String>?>(null) } // (id, name)
    val allUsers by viewModel.allUsers.collectAsState()
    val patients = remember(allUsers) { allUsers.filter { it.role == "PATIENT" } }

    if (showIssuePrescriptionDialog) {
        IssuePrescriptionDialog(
            patients = patients,
            preselectedPatient = targetPatientForPrescription,
            onDismiss = {
                showIssuePrescriptionDialog = false
                targetPatientForPrescription = null
            },
            onIssue = { patientId, patientName, medicineName, dosage, frequency, times, duration, qty, instructions, diagnosis ->
                viewModel.issuePrescription(
                    patientId = patientId,
                    patientName = patientName,
                    medicineName = medicineName,
                    dosage = dosage,
                    frequency = frequency,
                    reminderTimes = times,
                    durationDays = duration,
                    totalQuantity = qty,
                    instructions = instructions,
                    diagnosis = diagnosis,
                    onSuccess = {
                        showIssuePrescriptionDialog = false
                        targetPatientForPrescription = null
                    }
                )
            }
        )
    }

    if (showQrCodeDialog && doctor != null) {
        ShowQrCodeDialog(
            user = doctor,
            onDismiss = { showQrCodeDialog = false }
        )
    }

    if (showQrScannerDialog) {
        QrScannerDialog(
            onDismiss = { showQrScannerDialog = false },
            onAccountScanned = { payload ->
                viewModel.processScannedQrCode(payload)
            }
        )
    }
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465

    if (appointmentNotesDialog != null) {
        val appt = appointmentNotesDialog!!
        AlertDialog(
            onDismissRequest = { appointmentNotesDialog = null },
            title = { Text("Complete Consultation with ${appt.patientName}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter post-consultation doctor recommendations, diagnostic notes, or prescription adjustments:")
                    OutlinedTextField(
                        value = clinicalNotesText,
                        onValueChange = { clinicalNotesText = it },
                        label = { Text("Doctor Clinical Notes") },
                        placeholder = { Text("e.g. Continue Lisinopril 10mg daily. Patient BP controlled.") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
<<<<<<< HEAD
                    OutlinedButton(
                        onClick = {
                            targetPatientForPrescription = Pair(appt.patientId, appt.patientName)
                            showIssuePrescriptionDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Medication, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Issue Digital Prescription for ${appt.patientName}", fontSize = 12.sp, color = MedBluePrimary)
                    }
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAppointmentStatus(appt.id, "COMPLETED", clinicalNotesText)
                        appointmentNotesDialog = null
                        clinicalNotesText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedInfo)
                ) {
                    Text("Save & Complete")
                }
            },
            dismissButton = {
                TextButton(onClick = { appointmentNotesDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
<<<<<<< HEAD
            .background(MedBackground)
            .testTag("doctor_verified_dashboard"),
=======
            .background(MedBackground),
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Doctor Verification Profile Banner
        item {
<<<<<<< HEAD
=======
            val isVerified = currentUser?.isDoctorVerified == true
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
<<<<<<< HEAD
                colors = CardDefaults.cardColors(containerColor = MedBluePrimary)
=======
                colors = CardDefaults.cardColors(
                    containerColor = if (isVerified) MedBluePrimary else Color(0xFFE65100)
                )
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
<<<<<<< HEAD
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                AsyncImage(
                                    model = doctor?.doctorProfilePhotoUrl?.ifBlank { "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=400" },
                                    contentDescription = "Doctor photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column {
                                Text(
                                    text = "Doctor Clinical Portal",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = doctor?.name ?: "Dr. Specialist",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = "${doctor?.doctorSpecialty} • ${doctor?.doctorHospital}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
=======
                        Column {
                            Text(
                                text = "Doctor Clinical Portal",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = currentUser?.name ?: "Dr. Specialist",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "${currentUser?.doctorSpecialty} • ${currentUser?.doctorHospital}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
<<<<<<< HEAD
                                    imageVector = Icons.Default.Verified,
=======
                                    imageVector = if (isVerified) Icons.Default.Verified else Icons.Default.Warning,
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
<<<<<<< HEAD
                                    text = "Verified MD",
=======
                                    text = if (isVerified) "Verified MD" else "Pending Review",
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
<<<<<<< HEAD
                        text = "Medical License: ${doctor?.doctorLicense ?: "LIC-348912"}",
=======
                        text = "Medical License: ${currentUser?.doctorLicense ?: "LIC-348912"}",
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

<<<<<<< HEAD
        // Doctor QR Fast Connect & Patient Scanner Hub
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MedSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = MedBlueLight
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MedBluePrimary)
                            }
                        }
                        Column {
                            Text(
                                text = "QR Quick Connect",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "Scan patient QR or share doctor code",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showQrCodeDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_doctor_show_qr")
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("My QR", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showQrScannerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_doctor_scan_patient_qr")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan QR", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Prescription Quick Action Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MedBluePrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MedBlueLight.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = MedBluePrimary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Medication, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                        Column {
                            Text(
                                text = "Digital Prescription Desk",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "Issue e-prescriptions that auto-sync to patient reminders",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = {
                            targetPatientForPrescription = null
                            showIssuePrescriptionDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_doctor_issue_rx")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Issue Rx", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
        // Stats Row
        item {
            val pendingCount = appointments.count { it.status == "PENDING" }
            val acceptedCount = appointments.count { it.status == "ACCEPTED" }
            val completedCount = appointments.count { it.status == "COMPLETED" }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Pending",
                    value = pendingCount.toString(),
                    subtitle = "Needs review",
                    icon = Icons.Default.Schedule,
                    containerColor = MedWarningLight,
                    contentColor = MedWarning,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Scheduled",
                    value = acceptedCount.toString(),
                    subtitle = "Upcoming",
                    icon = Icons.Default.CalendarToday,
                    containerColor = MedBlueLight,
                    contentColor = MedBlueDark,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Done",
                    value = completedCount.toString(),
                    subtitle = "Completed",
                    icon = Icons.Default.CheckCircle,
                    containerColor = MedSuccessLight,
                    contentColor = MedSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Consultation Requests
        item {
            Text(
                text = "Patient Consultation Queue (${appointments.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MedTextPrimary
            )
        }

        if (appointments.isEmpty()) {
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
                        Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MedTextTertiary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No Consultation Requests", fontWeight = FontWeight.Bold)
                        Text("When patients book appointments with you, they will appear here.", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                    }
                }
            }
        } else {
            items(appointments, key = { it.id }) { appt ->
                AppointmentCard(
                    appointment = appt,
                    isDoctorView = true,
                    onAccept = {
                        viewModel.updateAppointmentStatus(appt.id, "ACCEPTED")
                    },
                    onReject = {
                        viewModel.updateAppointmentStatus(appt.id, "REJECTED", "Doctor unavailable")
                    },
                    onComplete = {
                        clinicalNotesText = ""
                        appointmentNotesDialog = appt
                    }
                )
            }
        }
    }
}
<<<<<<< HEAD

/**
 * Dialog for doctors to compose and issue a digital prescription for a patient.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssuePrescriptionDialog(
    patients: List<UserEntity>,
    preselectedPatient: Pair<String, String>?,
    onDismiss: () -> Unit,
    onIssue: (
        patientId: String,
        patientName: String,
        medicineName: String,
        dosage: String,
        frequency: String,
        times: String,
        duration: Int,
        qty: Int,
        instructions: String,
        diagnosis: String
    ) -> Unit
) {
    var selectedPatientId by remember {
        mutableStateOf(preselectedPatient?.first ?: patients.firstOrNull()?.id ?: "patient_demo")
    }
    var selectedPatientName by remember {
        mutableStateOf(preselectedPatient?.second ?: patients.firstOrNull()?.name ?: "John Doe")
    }

    var medicineName by remember { mutableStateOf("Amoxicillin") }
    var dosage by remember { mutableStateOf("500 mg") }
    var frequency by remember { mutableStateOf("Twice daily") }
    var reminderTimes by remember { mutableStateOf("08:00 AM, 08:00 PM") }
    var durationDays by remember { mutableStateOf("7") }
    var totalQuantity by remember { mutableStateOf("14") }
    var instructions by remember { mutableStateOf("Take with full glass of water after food.") }
    var diagnosis by remember { mutableStateOf("Acute bacterial bronchitis / upper respiratory infection") }

    val presetMedicines = listOf(
        "Amoxicillin" to "500 mg",
        "Lisinopril" to "10 mg",
        "Metformin" to "500 mg",
        "Paracetamol" to "650 mg",
        "Atorvastatin" to "20 mg",
        "Azithromycin" to "250 mg"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Medication, contentDescription = null, tint = MedBluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Issue Digital Prescription", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Patient Selector
                item {
                    Text("Target Patient", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    if (preselectedPatient != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MedBlueLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "👤 ${preselectedPatient.second}",
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.Bold,
                                color = MedBluePrimary
                            )
                        }
                    } else if (patients.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(patients) { p ->
                                FilterChip(
                                    selected = selectedPatientId == p.id,
                                    onClick = {
                                        selectedPatientId = p.id
                                        selectedPatientName = p.name
                                    },
                                    label = { Text(p.name, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MedBluePrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = selectedPatientName,
                            onValueChange = { selectedPatientName = it },
                            label = { Text("Patient Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Quick Preset Pills
                item {
                    Text("Quick Rx Presets", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(presetMedicines) { (med, dos) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MedSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MedBorder),
                                modifier = Modifier.clickable {
                                    medicineName = med
                                    dosage = dos
                                    if (med == "Amoxicillin") {
                                        frequency = "Twice daily"
                                        reminderTimes = "08:00 AM, 08:00 PM"
                                        durationDays = "7"
                                        totalQuantity = "14"
                                    } else if (med == "Paracetamol") {
                                        frequency = "Every 8 hours"
                                        reminderTimes = "08:00 AM, 02:00 PM, 08:00 PM"
                                        durationDays = "5"
                                        totalQuantity = "15"
                                    } else {
                                        frequency = "Once daily"
                                        reminderTimes = "08:00 AM"
                                        durationDays = "30"
                                        totalQuantity = "30"
                                    }
                                }
                            ) {
                                Text(
                                    text = "$med $dos",
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MedBluePrimary
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = medicineName,
                        onValueChange = { medicineName = it },
                        label = { Text("Medicine Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dosage,
                            onValueChange = { dosage = it },
                            label = { Text("Dosage *") },
                            placeholder = { Text("500 mg") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = frequency,
                            onValueChange = { frequency = it },
                            label = { Text("Frequency *") },
                            placeholder = { Text("Twice daily") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = reminderTimes,
                        onValueChange = { reminderTimes = it },
                        label = { Text("Reminder Times (comma-separated) *") },
                        placeholder = { Text("08:00 AM, 08:00 PM") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = durationDays,
                            onValueChange = { durationDays = it.filter { c -> c.isDigit() } },
                            label = { Text("Duration (Days)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = totalQuantity,
                            onValueChange = { totalQuantity = it.filter { c -> c.isDigit() } },
                            label = { Text("Total Qty") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Clinical Dosing Instructions") },
                        placeholder = { Text("Take after meals with water") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = diagnosis,
                        onValueChange = { diagnosis = it },
                        label = { Text("Clinical Diagnosis / Indication") },
                        placeholder = { Text("Hypertension, Type 2 Diabetes, etc.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = durationDays.toIntOrNull() ?: 7
                    val qty = totalQuantity.toIntOrNull() ?: 14
                    onIssue(
                        selectedPatientId,
                        selectedPatientName,
                        medicineName.trim(),
                        dosage.trim(),
                        frequency.trim(),
                        reminderTimes.trim(),
                        days,
                        qty,
                        instructions.trim(),
                        diagnosis.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                enabled = medicineName.isNotBlank() && dosage.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_issue_rx")
            ) {
                Text("Issue & Dispatch Rx")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
