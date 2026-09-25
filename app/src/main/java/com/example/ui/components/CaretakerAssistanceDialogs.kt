package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.data.model.CaretakerAssistanceRequestEntity
import com.example.data.model.CaretakerLinkEntity
import com.example.data.model.PatientAddressEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.util.LocationUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dialog for Caretaker to select an authorized visit address or add a temporary address
 */
@Composable
fun SelectVisitAddressDialog(
    onDismiss: () -> Unit,
    patientName: String,
    patientHomeAddress: String,
    authorizedAddresses: List<PatientAddressEntity>,
    selectedAddressId: String,
    onSelectAddress: (addressId: String, addressTitle: String, addressSnapshot: String, lat: Double?, lon: Double?, isTemporary: Boolean) -> Unit,
    onAddNewAddress: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("select_visit_address_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Place, contentDescription = null, tint = MedBluePrimary)
                Text("Select Visit Address", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Choose from $patientName's authorized visit destinations or specify a temporary address for this visit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MedTextSecondary
                )

                // Option 1: Patient's Home (Default Authorized Address)
                val isHomeSelected = selectedAddressId == "home" || selectedAddressId.isBlank()
                Surface(
                    onClick = {
                        onSelectAddress(
                            "home",
                            "Patient's Home",
                            patientHomeAddress,
                            12.9716,
                            77.5946,
                            false
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isHomeSelected) MedBlueLight else MedSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isHomeSelected) 2.dp else 1.dp,
                        color = if (isHomeSelected) MedBluePrimary else MedBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("option_patient_home_address")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RadioButton(
                            selected = isHomeSelected,
                            onClick = {
                                onSelectAddress(
                                    "home",
                                    "Patient's Home",
                                    patientHomeAddress,
                                    12.9716,
                                    77.5946,
                                    false
                                )
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Patient's Home", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Surface(shape = RoundedCornerShape(4.dp), color = MedSuccessLight) {
                                    Text("🔒 Authorized", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontSize = 10.sp, color = MedSuccess, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(patientHomeAddress, style = MaterialTheme.typography.bodySmall, color = MedTextPrimary)
                            Text("Default Medical Profile Address (~2.2 km from Caretaker)", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                        }
                    }
                }

                // Option 2: Patient's Alternate Address (Koramangala, Bengaluru)
                val isAltSelected = selectedAddressId == "alt_koramangala"
                Surface(
                    onClick = {
                        onSelectAddress(
                            "alt_koramangala",
                            "Patient's Alternate Address",
                            "45 4th Cross, Koramangala 4th Block, Bengaluru, Karnataka 560034",
                            12.9352,
                            77.6245,
                            false
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isAltSelected) MedBlueLight else MedSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isAltSelected) 2.dp else 1.dp,
                        color = if (isAltSelected) MedBluePrimary else MedBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("option_patient_alternate_address")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RadioButton(
                            selected = isAltSelected,
                            onClick = {
                                onSelectAddress(
                                    "alt_koramangala",
                                    "Patient's Alternate Address",
                                    "45 4th Cross, Koramangala 4th Block, Bengaluru, Karnataka 560034",
                                    12.9352,
                                    77.6245,
                                    false
                                )
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Patient's Alternate Address", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Surface(shape = RoundedCornerShape(4.dp), color = MedSuccessLight) {
                                    Text("🔒 Authorized", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontSize = 10.sp, color = MedSuccess, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("45 4th Cross, Koramangala 4th Block, Bengaluru, Karnataka 560034", style = MaterialTheme.typography.bodySmall, color = MedTextPrimary)
                            Text("Authorized Secondary (~5.2 km from Caretaker)", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                        }
                    }
                }

                // Dynamic Authorized addresses from DB
                authorizedAddresses.filter { it.id != "home" && it.id != "alt_koramangala" && it.allowCaretakerVisit }.forEach { addr ->
                    val isSelected = selectedAddressId == addr.id
                    Surface(
                        onClick = {
                            val snapshot = "${addr.addressLine1}, ${addr.addressLine2.ifBlank { "" }}, ${addr.city}, ${addr.state} ${addr.pincode}".replace(", ,", ",")
                            onSelectAddress(addr.id, addr.title, snapshot, addr.latitude, addr.longitude, false)
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MedBlueLight else MedSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MedBluePrimary else MedBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    val snapshot = "${addr.addressLine1}, ${addr.addressLine2.ifBlank { "" }}, ${addr.city}, ${addr.state} ${addr.pincode}".replace(", ,", ",")
                                    onSelectAddress(addr.id, addr.title, snapshot, addr.latitude, addr.longitude, false)
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(addr.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Surface(shape = RoundedCornerShape(4.dp), color = MedSuccessLight) {
                                        Text("🔒 Authorized", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontSize = 10.sp, color = MedSuccess, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${addr.addressLine1}, ${addr.city} ${addr.pincode}", style = MaterialTheme.typography.bodySmall, color = MedTextPrimary)
                            }
                        }
                    }
                }

                HorizontalDivider(color = MedDivider)

                // Option 3: Add New Visit Address (Temporary)
                OutlinedButton(
                    onClick = onAddNewAddress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_add_new_visit_address"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MedBluePrimary)
                ) {
                    Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add New Visit Address (Temporary)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
            ) {
                Text("Confirm Selection")
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
 * Dialog for Caretaker to add a temporary visit address
 */
@Composable
fun AddVisitAddressDialog(
    onDismiss: () -> Unit,
    onConfirmTemporaryAddress: (snapshot: String, lat: Double, lon: Double) -> Unit
) {
    var addressLine1 by remember { mutableStateOf("") }
    var addressLine2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Bengaluru") }
    var state by remember { mutableStateOf("Karnataka") }
    var pinCode by remember { mutableStateOf("560001") }
    var landmark by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("add_visit_address_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AddLocation, contentDescription = null, tint = MedBluePrimary)
                Text("Add Visit Address", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Temporary Address Notice Banner
                Surface(
                    color = MedWarningLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MedWarning, modifier = Modifier.size(18.dp))
                        Column {
                            Text(
                                text = "Temporary Visit Address",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedWarning
                            )
                            Text(
                                text = "This address is used for this specific visit request only. It will NOT overwrite the patient's permanent medical profile address.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextPrimary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = addressLine1,
                    onValueChange = { addressLine1 = it; errorMessage = null },
                    label = { Text("Address Line 1 *") },
                    placeholder = { Text("Flat / House No., Building Name") },
                    modifier = Modifier.fillMaxWidth().testTag("temp_address_line1"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = addressLine2,
                    onValueChange = { addressLine2 = it },
                    label = { Text("Address Line 2") },
                    placeholder = { Text("Street, Area, Sector") },
                    modifier = Modifier.fillMaxWidth().testTag("temp_address_line2"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City *") },
                        modifier = Modifier.weight(1f).testTag("temp_address_city"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State *") },
                        modifier = Modifier.weight(1f).testTag("temp_address_state"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pinCode,
                        onValueChange = { pinCode = it },
                        label = { Text("PIN Code *") },
                        modifier = Modifier.weight(1f).testTag("temp_address_pin"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = landmark,
                        onValueChange = { landmark = it },
                        label = { Text("Landmark (Optional)") },
                        modifier = Modifier.weight(1f).testTag("temp_address_landmark"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = MedError,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (addressLine1.isBlank()) {
                        errorMessage = "Address Line 1 is required."
                        return@Button
                    }
                    if (city.isBlank() || pinCode.isBlank()) {
                        errorMessage = "City and PIN Code are required."
                        return@Button
                    }
                    val formatted = buildString {
                        append(addressLine1.trim())
                        if (addressLine2.isNotBlank()) append(", ${addressLine2.trim()}")
                        append(", ${city.trim()}")
                        if (state.isNotBlank()) append(", ${state.trim()}")
                        append(" - ${pinCode.trim()}")
                        if (landmark.isNotBlank()) append(" (Near: ${landmark.trim()})")
                    }
                    // Realistic geocoordinates simulation for Bangalore destinations
                    val simulatedLat = 12.9300 + (Math.abs(formatted.hashCode() % 1000) / 10000.0)
                    val simulatedLon = 77.5800 + (Math.abs((formatted.hashCode() / 7) % 1000) / 10000.0)

                    onConfirmTemporaryAddress(formatted, simulatedLat, simulatedLon)
                },
                modifier = Modifier.testTag("btn_use_temp_address"),
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
            ) {
                Text("Use This Address")
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
 * Small Map Preview Component for Visit Location
 */
@Composable
fun VisitAddressMapPreview(
    originLabel: String,
    destinationLabel: String,
    distanceKm: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("visit_address_map_preview"),
        colors = CardDefaults.cardColors(containerColor = MedSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(16.dp))
                    Text(
                        "MAP PREVIEW & ROUTE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedBluePrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MedBlueLight
                ) {
                    Text(
                        text = "$distanceKm km route",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedBluePrimary
                    )
                }
            }

            // Stylized Route Map Canvas Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MedBackground)
                    .border(1.dp, MedBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MedSuccess),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                        Column {
                            Text("Current Location (Caretaker)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MedTextSecondary)
                            Text(originLabel, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
                        }
                    }

                    Row(
                        modifier = Modifier.padding(start = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(16.dp)
                                .background(MedBluePrimary)
                        )
                        Text(
                            text = "• • •  $distanceKm km direct travel route  • • •",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MedTextTertiary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MedError),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                        Column {
                            Text("Visit Destination (Patient)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MedTextSecondary)
                            Text(destinationLabel, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Main selection modal when Caretaker taps top Phone icon
 */
@Composable
fun PatientAssistanceChoiceModal(
    onDismiss: () -> Unit,
    onSelectCallPatient: () -> Unit,
    onSelectVisitPatient: () -> Unit,
    onViewActiveRequests: () -> Unit,
    activeRequestsCount: Int = 0
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("caretaker_assistance_choice_modal"),
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MedBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = MedBluePrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Patient Assistance",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Coordinate emergency or routine care via Admin",
                    style = MaterialTheme.typography.bodySmall,
                    color = MedTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Call Patient Option
                Surface(
                    onClick = {
                        onDismiss()
                        onSelectCallPatient()
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = MedSurface,
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("choice_call_patient_option")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MedSuccessLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = MedSuccess)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "📞 Call Patient",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MedTextPrimary
                            )
                            Text(
                                "Submit call request for Admin triage & dialing",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MedTextSecondary)
                    }
                }

                // Visit Patient Option
                Surface(
                    onClick = {
                        onDismiss()
                        onSelectVisitPatient()
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = MedSurface,
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("choice_visit_patient_option")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MedWarningLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HomeWork, contentDescription = null, tint = MedWarning)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "🏠 Visit Patient",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MedTextPrimary
                            )
                            Text(
                                "Request authorized home visit & calculate fare",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MedTextSecondary)
                    }
                }

                if (activeRequestsCount > 0) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onViewActiveRequests()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("choice_view_active_requests_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Active Requests ($activeRequestsCount)")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("choice_modal_cancel_btn")
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog for submitting a Call Patient Assistance Request to Admin
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallPatientRequestDialog(
    onDismiss: () -> Unit,
    caretakerLinks: List<CaretakerLinkEntity>,
    allUsers: List<UserEntity>,
    onSubmit: (patientId: String, patientName: String, reason: String, notes: String, isEmergency: Boolean, locationName: String, lat: Double?, lon: Double?) -> Unit
) {
    val context = LocalContext.current
    val approvedLinks = remember(caretakerLinks) {
        caretakerLinks.filter { it.status.equals("APPROVED", ignoreCase = true) }
    }

    var selectedPatientId by remember(approvedLinks) {
        mutableStateOf(approvedLinks.firstOrNull()?.patientId ?: "patient_1")
    }

    val selectedPatientUser = remember(allUsers, selectedPatientId) {
        allUsers.firstOrNull { it.id == selectedPatientId } ?: allUsers.firstOrNull { it.role == "PATIENT" }
    }

    val reasons = listOf(
        "Medicine Assistance",
        "Patient Check-in",
        "Emergency",
        "Appointment",
        "Other"
    )
    var selectedReason by remember { mutableStateOf("Medicine Assistance") }
    var notes by remember { mutableStateOf("") }
    val isEmergency = selectedReason == "Emergency"

    // Location info (if permission allowed)
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("call_patient_request_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = MedBluePrimary)
                Text("Call Patient Assistance", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info note
                item {
                    Text(
                        text = "MedTime coordinates calls through the Admin Desk to ensure safety and clinical protocol compliance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                }

                // Select Patient Section
                item {
                    Text("Select Linked Patient", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))

                    if (approvedLinks.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MedSurface),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "No linked patients found. Please link with a patient first.",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            approvedLinks.forEach { link ->
                                val patientUser = allUsers.firstOrNull { it.id == link.patientId }
                                val isSelected = selectedPatientId == link.patientId

                                Surface(
                                    onClick = { selectedPatientId = link.patientId },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MedBlueLight else MedSurface,
                                    border = CardDefaults.outlinedCardBorder(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedPatientId = link.patientId }
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(link.patientName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                "Connected • Phone: ${patientUser?.phone ?: "Available"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MedSuccess
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Reason for Call
                item {
                    Text("Reason for Call", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        reasons.forEach { reason ->
                            val isSelected = selectedReason == reason
                            Surface(
                                onClick = { selectedReason = reason },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) {
                                    if (reason == "Emergency") MedErrorLight else MedBlueLight
                                } else MedSurface,
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedReason = reason },
                                        colors = if (reason == "Emergency") RadioButtonDefaults.colors(selectedColor = MedError) else RadioButtonDefaults.colors()
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = reason,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (reason == "Emergency" && isSelected) MedError else MedTextPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Emergency Warning Banner
                if (isEmergency) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MedErrorLight),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MedError)
                                Text(
                                    "For a life-threatening emergency, contact local emergency services immediately (911).",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MedError
                                )
                            }
                        }
                    }
                }

                // Optional Notes
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        placeholder = { Text("e.g., Patient has not responded to medicine reminder.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("call_notes_input"),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2,
                        maxLines = 4
                    )
                }

                // Caretaker Location Telemetry
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MedBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (hasLocationPermission) MedBluePrimary else MedTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Caretaker Location",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    if (hasLocationPermission) "Indiranagar, Bengaluru (Attached)" else "Location unavailable (Permission not granted)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MedTextSecondary
                                )
                            }
                            if (!hasLocationPermission) {
                                TextButton(
                                    onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Allow", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val patientName = selectedPatientUser?.name ?: "Patient"
                    val locName = if (hasLocationPermission) "Indiranagar, Bengaluru" else "Location unavailable"
                    val lat = if (hasLocationPermission) 12.9780 else null
                    val lon = if (hasLocationPermission) 77.6400 else null

                    onSubmit(
                        selectedPatientId,
                        patientName,
                        selectedReason,
                        notes,
                        isEmergency,
                        locName,
                        lat,
                        lon
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("send_call_request_to_admin_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEmergency) MedError else MedBluePrimary
                )
            ) {
                Text("SEND REQUEST TO ADMIN")
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
 * Dialog for submitting a Visit Patient Request to Admin with location permission, dynamic Address selection, & Fare calculation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitPatientRequestDialog(
    onDismiss: () -> Unit,
    caretakerLinks: List<CaretakerLinkEntity>,
    allUsers: List<UserEntity>,
    authorizedAddresses: List<PatientAddressEntity> = emptyList(),
    walletBalance: Double = 450.0,
    onOpenAddMoney: () -> Unit = {},
    onSubmit: (
        patientId: String,
        patientName: String,
        reason: String,
        notes: String,
        isEmergency: Boolean,
        distanceKm: Double,
        baseCharge: Double,
        additionalCharge: Double,
        totalCharge: Double,
        patientAddress: String,
        patientLat: Double?,
        patientLon: Double?,
        caretakerLocationName: String,
        caretakerLat: Double?,
        caretakerLon: Double?,
        selectedAddressId: String,
        addressSnapshot: String
    ) -> Unit
) {
    val context = LocalContext.current
    val approvedLinks = remember(caretakerLinks) {
        caretakerLinks.filter { it.status.equals("APPROVED", ignoreCase = true) }
    }

    var selectedPatientId by remember(approvedLinks) {
        mutableStateOf(approvedLinks.firstOrNull()?.patientId ?: "patient_1")
    }

    val selectedPatientUser = remember(allUsers, selectedPatientId) {
        allUsers.firstOrNull { it.id == selectedPatientId } ?: allUsers.firstOrNull { it.role == "PATIENT" }
    }

    val defaultHomeAddress = selectedPatientUser?.address?.ifBlank {
        "124 Indiranagar 100ft Rd, Bengaluru, Karnataka 560038"
    } ?: "124 Indiranagar 100ft Rd, Bengaluru, Karnataka 560038"

    // Address selection state
    var selectedAddressId by remember(selectedPatientId) { mutableStateOf("home") }
    var selectedAddressTitle by remember(selectedPatientId) { mutableStateOf("Patient's Home") }
    var selectedAddressSnapshot by remember(selectedPatientId, defaultHomeAddress) { mutableStateOf(defaultHomeAddress) }
    var destinationLat by remember(selectedPatientId) { mutableStateOf(selectedPatientUser?.latitude ?: 12.9716) }
    var destinationLon by remember(selectedPatientId) { mutableStateOf(selectedPatientUser?.longitude ?: 77.5946) }
    var isTemporaryAddress by remember(selectedPatientId) { mutableStateOf(false) }

    var showSelectAddressModal by remember { mutableStateOf(false) }
    var showAddTempAddressModal by remember { mutableStateOf(false) }

    // Permission state
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showLocationPromptDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        showLocationPromptDialog = false
    }

    val reasons = listOf(
        "Patient Check",
        "Medicine Assistance",
        "Missed Medicine",
        "Medical Support",
        "Appointment Assistance",
        "Emergency",
        "Other"
    )
    var selectedReason by remember { mutableStateOf("Medicine Assistance") }
    var notes by remember { mutableStateOf("") }
    val isEmergency = selectedReason == "Emergency"

    // Coordinates: Caretaker Location (Indiranagar 12.9780, 77.6400)
    val caretakerLat = if (hasLocationPermission) 12.9780 else 12.9780
    val caretakerLon = if (hasLocationPermission) 77.6400 else 77.6400

    // Dynamic Distance calculation using Haversine formula based on chosen destination
    val calculatedDistance = remember(caretakerLat, caretakerLon, destinationLat, destinationLon, selectedAddressSnapshot) {
        LocationUtils.calculateDistanceKm(caretakerLat, caretakerLon, destinationLat, destinationLon)
    }

    // Authoritative visit pricing with ceiling for billable distance
    val feeResult = remember(calculatedDistance) {
        LocationUtils.calculateVisitFee(calculatedDistance)
    }

    val hasSufficientBalance = walletBalance >= feeResult.totalFee
    val shortage = (feeResult.totalFee - walletBalance).coerceAtLeast(0.0)

    if (showLocationPromptDialog) {
        AlertDialog(
            onDismissRequest = { showLocationPromptDialog = false },
            icon = { Icon(Icons.Default.MyLocation, contentDescription = null, tint = MedBluePrimary) },
            title = { Text("Location Permission") },
            text = {
                Text(
                    "MedTime needs your current location to calculate the visit distance and help Admin coordinate this request.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationPromptDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    // Address Picker Sub-Dialog
    if (showSelectAddressModal) {
        SelectVisitAddressDialog(
            onDismiss = { showSelectAddressModal = false },
            patientName = selectedPatientUser?.name ?: "Patient",
            patientHomeAddress = defaultHomeAddress,
            authorizedAddresses = authorizedAddresses,
            selectedAddressId = selectedAddressId,
            onSelectAddress = { addrId, title, snapshot, lat, lon, isTemp ->
                selectedAddressId = addrId
                selectedAddressTitle = title
                selectedAddressSnapshot = snapshot
                destinationLat = lat ?: 12.9716
                destinationLon = lon ?: 77.5946
                isTemporaryAddress = isTemp
                showSelectAddressModal = false
            },
            onAddNewAddress = {
                showSelectAddressModal = false
                showAddTempAddressModal = true
            }
        )
    }

    // Add Temporary Address Sub-Dialog
    if (showAddTempAddressModal) {
        AddVisitAddressDialog(
            onDismiss = { showAddTempAddressModal = false },
            onConfirmTemporaryAddress = { snapshot, lat, lon ->
                selectedAddressId = "temp_${System.currentTimeMillis()}"
                selectedAddressTitle = "Temporary Visit Address"
                selectedAddressSnapshot = snapshot
                destinationLat = lat
                destinationLon = lon
                isTemporaryAddress = true
                showAddTempAddressModal = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("visit_patient_request_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MedWarning)
                Text("Visit Patient Request", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Select Patient
                item {
                    Text("Select Linked Patient", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        approvedLinks.forEach { link ->
                            val isSelected = selectedPatientId == link.patientId
                            Surface(
                                onClick = {
                                    selectedPatientId = link.patientId
                                    selectedAddressId = "home"
                                    selectedAddressTitle = "Patient's Home"
                                    selectedAddressSnapshot = defaultHomeAddress
                                    destinationLat = 12.9716
                                    destinationLon = 77.5946
                                    isTemporaryAddress = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MedBlueLight else MedSurface,
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            selectedPatientId = link.patientId
                                            selectedAddressId = "home"
                                            selectedAddressTitle = "Patient's Home"
                                            selectedAddressSnapshot = defaultHomeAddress
                                            destinationLat = 12.9716
                                            destinationLon = 77.5946
                                            isTemporaryAddress = false
                                        }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(link.patientName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "Authorized Caregiver Link",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MedSuccess
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // VISIT ADDRESS SECTION with Dynamic Selection
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MedSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder()
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isTemporaryAddress) {
                                        Icon(Icons.Default.LocationSearching, contentDescription = null, tint = MedWarning, modifier = Modifier.size(16.dp))
                                        Text(
                                            "Temporary Visit Address",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MedWarning
                                        )
                                    } else {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(16.dp))
                                        Text(
                                            "Patient's Authorized Address",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MedTextPrimary
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = { showSelectAddressModal = true },
                                    modifier = Modifier.testTag("btn_change_visit_address"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.EditLocationAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Change Address", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isTemporaryAddress) MedWarningLight else MedBackground,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = selectedAddressTitle,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isTemporaryAddress) MedWarning else MedBluePrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = selectedAddressSnapshot,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MedTextPrimary
                                    )
                                }
                            }

                            HorizontalDivider(color = MedDivider)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Estimated Distance", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                    Text(
                                        "${calculatedDistance} km",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MedBluePrimary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Estimated Visit Fee", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                    Text(
                                        "₹${feeResult.totalFee.toInt()}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MedSuccess
                                    )
                                }
                            }

                            // Caretaker location status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (hasLocationPermission) "Caretaker: Indiranagar, Bengaluru (Live)" else "Caretaker: Location GPS Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MedSuccess
                                )
                                if (!hasLocationPermission) {
                                    TextButton(
                                        onClick = { showLocationPromptDialog = true },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Enable High Precision GPS", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }

                // Map Preview Box
                item {
                    VisitAddressMapPreview(
                        originLabel = "Indiranagar (Caretaker)",
                        destinationLabel = selectedAddressSnapshot,
                        distanceKm = calculatedDistance
                    )
                }

                // Visit Charge Breakdown Box
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MedBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Visit Fare Breakdown (Ceiling for Billable Distance)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Distance", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                Text("${feeResult.distanceKm} km (${feeResult.billableKm} km billable)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Base Fare (1–3 km)", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                Text("₹${feeResult.baseFee.toInt()}", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Additional Distance", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                Text("₹${feeResult.additionalFee.toInt()}", style = MaterialTheme.typography.labelSmall)
                            }
                            HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 2.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Required Payment", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                Text("₹${feeResult.totalFee.toInt()}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MedSuccess)
                            }
                        }
                    }
                }

                // Caretaker Wallet Accounting Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (hasSufficientBalance) MedBlueLight else MedErrorLight
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Caretaker Wallet Balance", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                Text(
                                    "₹${walletBalance.toInt()}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (hasSufficientBalance) MedSuccess else MedError
                                )
                                if (!hasSufficientBalance) {
                                    Text(
                                        "Short by ₹${shortage.toInt()}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MedError
                                    )
                                }
                            }

                            if (!hasSufficientBalance) {
                                Button(
                                    onClick = onOpenAddMoney,
                                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Add Money", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Reason for Visit
                item {
                    Text("Reason for Visit", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        reasons.forEach { reason ->
                            val isSelected = selectedReason == reason
                            Surface(
                                onClick = { selectedReason = reason },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) {
                                    if (reason == "Emergency") MedErrorLight else MedBlueLight
                                } else MedSurface,
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedReason = reason },
                                        colors = if (reason == "Emergency") RadioButtonDefaults.colors(selectedColor = MedError) else RadioButtonDefaults.colors()
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = reason,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (reason == "Emergency" && isSelected) MedError else MedTextPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (isEmergency) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MedErrorLight),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MedError)
                                Text(
                                    "For a life-threatening emergency, contact local emergency services immediately (911).",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MedError
                                )
                            }
                        }
                    }
                }

                // Optional Notes
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Visit Notes (Optional)") },
                        placeholder = { Text("e.g., In-person dose check & pillbox refill assistance.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("visit_notes_input"),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!hasSufficientBalance) {
                        onOpenAddMoney()
                        return@Button
                    }
                    val patientName = selectedPatientUser?.name ?: "Patient"
                    val locName = if (hasLocationPermission) "Indiranagar, Bengaluru" else "Indiranagar, Bengaluru"

                    onSubmit(
                        selectedPatientId,
                        patientName,
                        selectedReason,
                        notes,
                        isEmergency,
                        calculatedDistance,
                        feeResult.baseFee,
                        feeResult.additionalFee,
                        feeResult.totalFee,
                        selectedAddressSnapshot,
                        destinationLat,
                        destinationLon,
                        locName,
                        caretakerLat,
                        caretakerLon,
                        selectedAddressId,
                        selectedAddressSnapshot
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("send_visit_request_to_admin_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEmergency) MedError else MedBluePrimary
                )
            ) {
                if (hasSufficientBalance) {
                    Text("REVIEW & PAY (₹${feeResult.totalFee.toInt()})")
                } else {
                    Text("ADD MONEY (₹${feeResult.totalFee.toInt()})")
                }
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
 * Caretaker Assistance Requests Tracking Sheet / Screen Component
 */
@Composable
fun CaretakerAssistanceTrackerSheet(
    onDismiss: () -> Unit,
    requests: List<CaretakerAssistanceRequestEntity>,
    onUpdateProgress: (requestId: String, status: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("caretaker_assistance_tracker_sheet"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.FormatListBulleted, contentDescription = null, tint = MedBluePrimary)
                    Text("Assistance Requests", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            if (requests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No assistance requests yet.\nUse the top phone button to request a Call or Visit.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MedTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(requests) { req ->
                        CaretakerAssistanceRequestCard(
                            request = req,
                            onUpdateProgress = onUpdateProgress
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Individual Assistance Request Card for Caretaker View
 */
@Composable
fun CaretakerAssistanceRequestCard(
    request: CaretakerAssistanceRequestEntity,
    onUpdateProgress: (requestId: String, status: String) -> Unit
) {
    val statusColor = when (request.status) {
        "PENDING" -> MedWarning
        "ACCEPTED", "VISIT APPROVED" -> MedSuccess
        "CARETAKER_ON_THE_WAY" -> MedBluePrimary
        "ARRIVED" -> MedInfo
        "COMPLETED" -> MedSuccess
        "REJECTED" -> MedError
        "CANCELLED" -> MedTextSecondary
        else -> MedTextPrimary
    }

    val statusLabel = when (request.status) {
        "PENDING" -> "Waiting for Admin"
        "ACCEPTED" -> if (request.requestType == "CALL") "Admin Accepted" else "Visit Approved"
        "CARETAKER_ON_THE_WAY" -> "On the Way"
        "ARRIVED" -> "Arrived at Patient"
        "COMPLETED" -> "Completed"
        "REJECTED" -> "Admin Rejected"
        "CANCELLED" -> "Cancelled"
        else -> request.status
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("caretaker_req_card_${request.id}"),
        colors = CardDefaults.cardColors(containerColor = MedSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Type Badge + Patient Name + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (request.requestType == "CALL") MedSuccessLight else MedWarningLight
                    ) {
                        Text(
                            text = if (request.requestType == "CALL") "📞 CALL" else "🏠 VISIT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (request.requestType == "CALL") MedSuccess else MedWarning,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        request.patientName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Reason & Notes
            Text(
                text = "Reason: ${request.reason}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MedTextPrimary
            )

            if (request.notes.isNotBlank()) {
                Text(
                    text = "Note: ${request.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MedTextSecondary
                )
            }

            // Visit Details (Distance & Charge)
            if (request.requestType == "VISIT") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Distance: ${request.distanceKm} km",
                        style = MaterialTheme.typography.labelSmall,
                        color = MedTextSecondary
                    )
                    Text(
                        "Visit Charge: ₹${String.format(Locale.US, "%.2f", request.totalVisitCharge)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedSuccess
                    )
                }
            }

            // Admin response or Rejection notes
            if (request.adminNotes.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MedBlueLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Admin Note: ${request.adminNotes}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MedBluePrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (request.rejectionReason.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MedErrorLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Rejection Reason: ${request.rejectionReason}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MedError,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Timestamp
            val dateStr = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(request.createdAt))
            Text(
                text = "Requested $dateStr",
                style = MaterialTheme.typography.labelSmall,
                color = MedTextSecondary
            )

            // Step Progress Action Buttons for Visit (if Approved or In-Progress)
            if (request.requestType == "VISIT" && (request.status == "ACCEPTED" || request.status == "CARETAKER_ON_THE_WAY" || request.status == "ARRIVED")) {
                HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (request.status == "ACCEPTED") {
                        Button(
                            onClick = { onUpdateProgress(request.id, "CARETAKER_ON_THE_WAY") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_on_the_way_${request.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("On The Way", style = MaterialTheme.typography.labelSmall)
                        }
                    } else if (request.status == "CARETAKER_ON_THE_WAY") {
                        Button(
                            onClick = { onUpdateProgress(request.id, "ARRIVED") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_arrived_${request.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = MedInfo),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Arrived", style = MaterialTheme.typography.labelSmall)
                        }
                    } else if (request.status == "ARRIVED") {
                        Button(
                            onClick = { onUpdateProgress(request.id, "COMPLETED") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_completed_${request.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = MedSuccess),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Complete Visit", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
