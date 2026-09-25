package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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

private data class CaretakerFacility(
    val name: String,
    val type: String,
    val distance: String,
    val address: String,
    val phone: String,
    val isEmergency: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerMapsScreen(
    viewModel: MedTimeViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val caretakerLinks by viewModel.caretakerLinks.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    val approvedLinks = remember(caretakerLinks) {
        caretakerLinks.filter { it.status.equals("APPROVED", ignoreCase = true) }
    }

    var selectedPatientId by remember(approvedLinks) {
        mutableStateOf(approvedLinks.firstOrNull()?.patientId ?: "patient_1")
    }

    val currentLink = remember(approvedLinks, selectedPatientId) {
        approvedLinks.firstOrNull { it.patientId == selectedPatientId }
    }

    val selectedPatientUser = remember(allUsers, selectedPatientId) {
        allUsers.firstOrNull { it.id == selectedPatientId } ?: allUsers.firstOrNull { it.role == "PATIENT" }
    }

    // Toggle for tracking patient live location (OFF by default)
    var isPatientLocationToggled by remember { mutableStateOf(false) }

    val facilities = remember {
        listOf(
            CaretakerFacility("City Central Memorial Hospital", "Trauma & General Hospital", "1.2 km away", "450 Health Sciences Way", "+1 (555) 911-0000", isEmergency = true),
            CaretakerFacility("CVS 24-Hour Pharmacy & Clinic", "Pharmacy / Med Refills", "0.6 km away", "128 Oakridge Ave", "+1 (555) 345-8899"),
            CaretakerFacility("Saint Jude Pediatric & Family Care", "Specialty Medical Center", "2.4 km away", "890 Pine Valley Blvd", "+1 (555) 778-9900"),
            CaretakerFacility("Walgreens Health & Wellness Hub", "Pharmacy & Vaccinations", "1.8 km away", "602 Maple St", "+1 (555) 234-1122")
        )
    }

    var selectedTypeFilter by remember { mutableStateOf("ALL") }

    val filteredFacilities = remember(selectedTypeFilter) {
        when (selectedTypeFilter) {
            "HOSPITAL" -> facilities.filter { it.isEmergency }
            "PHARMACY" -> facilities.filter { it.type.contains("Pharmacy", ignoreCase = true) }
            else -> facilities
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Healthcare Maps", fontWeight = FontWeight.Bold, color = MedTextPrimary)
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
            // Patient Live Location Section
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(MedBlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(20.dp))
                                }
                                Column {
                                    Text("Patient Location Tracker", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text("Patient: ${selectedPatientUser?.name ?: "Vijay Kumar"}", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                }
                            }

                            Switch(
                                checked = isPatientLocationToggled,
                                onCheckedChange = { isPatientLocationToggled = it }
                            )
                        }

                        if (isPatientLocationToggled) {
                            // Check if location permission is granted
                            val hasPermission = currentLink?.canViewLocation == true
                            if (hasPermission) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MedSuccessLight,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Live Coordinates Available", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MedSuccess)
                                            Text("3.4 km away", style = MaterialTheme.typography.labelMedium, color = MedSuccess)
                                        }
                                        Text("Current Address: 1248 Health Science Pkwy, Apt 4B", style = MaterialTheme.typography.bodySmall, color = MedTextPrimary)
                                        Button(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=1248+Health+Science+Pkwy"))
                                                context.startActivity(intent)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MedSuccess)
                                        ) {
                                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Get Driving Directions")
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MedWarningLight,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = MedWarning)
                                        Column {
                                            Text("Location Permission Not Granted", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MedWarning)
                                            Text("The patient has not granted location sharing permission. Caretaker can see patient location ONLY if patient is linked AND grants location permission.", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Patient location tracking is currently disabled (OFF by default). Toggle switch to check authorized live coordinates.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextSecondary
                            )
                        }
                    }
                }
            }

            // Facilities Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTypeFilter == "ALL",
                        onClick = { selectedTypeFilter = "ALL" },
                        label = { Text("All Medical (${facilities.size})") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedTypeFilter == "HOSPITAL",
                        onClick = { selectedTypeFilter = "HOSPITAL" },
                        label = { Text("Emergency / ER") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedTypeFilter == "PHARMACY",
                        onClick = { selectedTypeFilter = "PHARMACY" },
                        label = { Text("Pharmacies") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Facilities List
            items(filteredFacilities) { facility ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (facility.isEmergency) MedErrorLight else MedBlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (facility.isEmergency) Icons.Default.LocalHospital else Icons.Default.LocalPharmacy,
                                        contentDescription = null,
                                        tint = if (facility.isEmergency) MedError else MedBluePrimary
                                    )
                                }
                                Column {
                                    Text(facility.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(facility.type, style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                }
                            }
                            Text(facility.distance, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MedBluePrimary)
                        }

                        Text("Address: ${facility.address}", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${facility.phone}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call", style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(facility.name + " " + facility.address)}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Directions", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
