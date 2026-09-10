package com.example.ui.screens

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: MedTimeViewModel,
    modifier: Modifier = Modifier
) {
    val allUsers by viewModel.allUsers.collectAsState()
    val doctors by viewModel.doctors.collectAsState()
    val allAppointments by viewModel.allAppointments.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Doctor Verification, 1 = User Directory, 2 = Audit Logs
    var roleFilter by remember { mutableStateOf("ALL") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin Portal Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
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
                        Column {
                            Text(
                                text = "MedTime Platform Administration",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Clinical Compliance, User Access, and Audit Trail",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        }

        // Stats Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Users",
                    value = allUsers.size.toString(),
                    subtitle = "All accounts",
                    icon = Icons.Default.People,
                    containerColor = MedBlueLight,
                    contentColor = MedBlueDark,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Doctors",
                    value = doctors.size.toString(),
                    subtitle = "${doctors.count { it.isDoctorVerified }} verified",
                    icon = Icons.Default.MedicalServices,
                    containerColor = MedSuccessLight,
                    contentColor = MedSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Audit Logs",
                    value = auditLogs.size.toString(),
                    subtitle = "Security events",
                    icon = Icons.Default.Shield,
                    containerColor = Color(0xFFEDE7F6),
                    contentColor = Color(0xFF512DA8),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Navigation Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MedSurface,
                contentColor = MedBluePrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Doctor Licenses", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Users Directory", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Audit Trail", fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Tab 0: Doctor Verification Management
        if (selectedTab == 0) {
            item {
                Text(
                    text = "Medical Practitioner Credentials Review",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
            }

            if (doctors.isEmpty()) {
                item {
                    Text("No doctors registered yet.", color = MedTextSecondary)
                }
            } else {
                items(doctors, key = { it.id }) { doc ->
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
                                Column {
                                    Text(doc.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("${doc.doctorSpecialty} • ${doc.doctorHospital}", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                    Text("License ID: ${doc.doctorLicense}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (doc.isDoctorVerified) MedSuccessLight else MedWarningLight
                                ) {
                                    Text(
                                        text = if (doc.isDoctorVerified) "APPROVED" else "PENDING",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (doc.isDoctorVerified) MedSuccess else MedWarning
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.verifyDoctor(doc.id, false) },
                                    modifier = Modifier.height(36.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MedError)
                                ) {
                                    Text("Revoke / Reject", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.verifyDoctor(doc.id, true) },
                                    modifier = Modifier.height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MedSuccess)
                                ) {
                                    Text("Approve License", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // Tab 1: User Directory
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val roles = listOf("ALL", "PATIENT", "DOCTOR", "CARETAKER", "ADMIN")
                    items(roles) { r ->
                        FilterChip(
                            selected = roleFilter == r,
                            onClick = { roleFilter = r },
                            label = { Text(r) }
                        )
                    }
                }
            }

            val filteredUsers = if (roleFilter == "ALL") allUsers else allUsers.filter { it.role == roleFilter }

            items(filteredUsers, key = { it.id }) { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, MedBorder, RoundedCornerShape(14.dp)),
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (user.role) {
                                            "DOCTOR" -> MedSuccessLight
                                            "CARETAKER" -> MedWarningLight
                                            "ADMIN" -> Color(0xFFEDE7F6)
                                            else -> MedBlueLight
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (user.role) {
                                        "DOCTOR" -> Icons.Default.MedicalServices
                                        "CARETAKER" -> Icons.Default.Favorite
                                        "ADMIN" -> Icons.Default.AdminPanelSettings
                                        else -> Icons.Default.Person
                                    },
                                    contentDescription = null,
                                    tint = when (user.role) {
                                        "DOCTOR" -> MedSuccess
                                        "CARETAKER" -> MedWarning
                                        "ADMIN" -> Color(0xFF512DA8)
                                        else -> MedBluePrimary
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("${user.email} • ${user.phone}", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MedSurfaceVariant
                        ) {
                            Text(
                                text = user.role,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                        }
                    }
                }
            }
        } else {
            // Tab 2: Audit Logs
            item {
                Text(
                    text = "System Audit Trail & Security Events (${auditLogs.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
            }

            if (auditLogs.isEmpty()) {
                item {
                    Text("No audit logs recorded yet.", color = MedTextSecondary)
                }
            } else {
                items(auditLogs, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MedBorder, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = MedSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = log.action,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedBluePrimary
                                )
                                val timeStr = SimpleDateFormat("MMM dd • hh:mm:ss a", Locale.getDefault()).format(Date(log.timestamp))
                                Text(timeStr, style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(log.details, style = MaterialTheme.typography.bodySmall, color = MedTextPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Actor: ${log.performedBy} • Target: ${log.targetResource}", style = MaterialTheme.typography.labelSmall, color = MedTextTertiary)
                        }
                    }
                }
            }
        }
    }
}
