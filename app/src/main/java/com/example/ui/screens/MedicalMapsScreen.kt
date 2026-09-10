package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

data class MedicalFacility(
    val id: String,
    val name: String,
    val type: String, // "Hospital", "Pharmacy", "Urgent Care", "Trauma Center"
    val address: String,
    val distance: String,
    val openStatus: String,
    val phone: String,
    val emergencyReady: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalMapsScreen(
    viewModel: MedTimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedType by remember { mutableStateOf("All") }
    val facilityTypes = listOf("All", "Hospital", "Pharmacy", "Urgent Care", "Trauma Center")

    val facilities = remember {
        listOf(
            MedicalFacility(
                id = "fac_1",
                name = "St. Jude Heart & Vascular Hospital",
                type = "Hospital",
                address = "1200 Healthcare Parkway, Medical District",
                distance = "1.2 miles",
                openStatus = "Open 24/7 • Emergency Ready",
                phone = "555-0199",
                emergencyReady = true
            ),
            MedicalFacility(
                id = "fac_2",
                name = "Walgreens 24-Hour Pharmacy",
                type = "Pharmacy",
                address = "455 North Grand Avenue",
                distance = "0.6 miles",
                openStatus = "Open 24 Hours • Drive-Thru Refill",
                phone = "555-0143",
                emergencyReady = false
            ),
            MedicalFacility(
                id = "fac_3",
                name = "Memorial Emergency Trauma Center (Level 1)",
                type = "Trauma Center",
                address = "800 Memorial Drive",
                distance = "2.4 miles",
                openStatus = "Open 24/7 • Pediatric & Adult Trauma",
                phone = "911",
                emergencyReady = true
            ),
            MedicalFacility(
                id = "fac_4",
                name = "CareNow Immediate Urgent Care",
                type = "Urgent Care",
                address = "210 West End Boulevard",
                distance = "1.8 miles",
                openStatus = "Open Daily 8:00 AM - 10:00 PM",
                phone = "555-0182",
                emergencyReady = false
            ),
            MedicalFacility(
                id = "fac_5",
                name = "CVS Pharmacy & MinuteClinic",
                type = "Pharmacy",
                address = "901 Oak Ridge Way",
                distance = "1.1 miles",
                openStatus = "Open until 11:00 PM",
                phone = "555-0177",
                emergencyReady = false
            )
        )
    }

    val filtered = if (selectedType == "All") facilities else facilities.filter { it.type.equals(selectedType, ignoreCase = true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground)
    ) {
        // Emergency Call SOS Banner
        Surface(
            color = Color(0xFFD32F2F),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                    Column {
                        Text("Emergency Trauma Helpline", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("One-tap connection to immediate dispatch", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                    }
                }
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:911")
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("dial_911_button")
                ) {
                    Text("Call 911", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Filter chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MedSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(facilityTypes) { type ->
                val isSelected = selectedType == type
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedType = type },
                    label = { Text(type, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MedBluePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Facilities List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered, key = { it.id }) { fac ->
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
                            .padding(16.dp)
                    ) {
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
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (fac.emergencyReady) MedErrorLight else MedBlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (fac.type) {
                                            "Pharmacy" -> Icons.Default.LocalPharmacy
                                            "Urgent Care" -> Icons.Default.MedicalServices
                                            else -> Icons.Default.LocalHospital
                                        },
                                        contentDescription = null,
                                        tint = if (fac.emergencyReady) MedError else MedBluePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(fac.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MedTextPrimary)
                                    Text("${fac.type} • ${fac.distance} away", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(fac.address, style = MaterialTheme.typography.bodySmall, color = MedTextPrimary)
                        Text(fac.openStatus, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = MedSuccess))

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val uri = Uri.parse("geo:0,0?q=${Uri.encode(fac.name + " " + fac.address)}")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = "Directions", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Directions", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${fac.phone}")
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call Facility", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
