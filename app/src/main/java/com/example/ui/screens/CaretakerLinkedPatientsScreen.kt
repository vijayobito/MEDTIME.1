package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.model.CaretakerLinkEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerLinkedPatientsScreen(
    viewModel: MedTimeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLinkPatient: () -> Unit,
    onNavigateToPatientInfo: (String) -> Unit,
    onNavigateToPermissions: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val caretakerLinks by viewModel.caretakerLinks.collectAsState()

    var linkToDelete by remember { mutableStateOf<CaretakerLinkEntity?>(null) }

    if (linkToDelete != null) {
        val link = linkToDelete!!
        AlertDialog(
            onDismissRequest = { linkToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MedError)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remove Connection?")
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to remove your caretaker connection to ${link.patientName}? You will no longer receive alerts or view their health records.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MedTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCaretakerLink(link.id)
                        linkToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedError)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { linkToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Linked Patients", fontWeight = FontWeight.Bold, color = MedTextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MedTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToLinkPatient) {
                        Icon(Icons.Default.Add, contentDescription = "Add Patient", tint = MedBluePrimary)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "All Connected Profiles (${caretakerLinks.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
            }

            if (caretakerLinks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MedSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.PeopleOutline, contentDescription = null, tint = MedTextSecondary, modifier = Modifier.size(48.dp))
                            Text("No Linked Patients", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("You are not currently linked to any patient profile. Tap below to link a patient.", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Button(
                                onClick = onNavigateToLinkPatient,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                            ) {
                                Text("Link Patient")
                            }
                        }
                    }
                }
            } else {
                items(caretakerLinks) { link ->
                    val isApproved = link.status.equals("APPROVED", ignoreCase = true)
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
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    val initials = link.patientName.take(2).uppercase()
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(if (isApproved) MedBlueLight else MedSurfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initials,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isApproved) MedBluePrimary else MedTextSecondary
                                        )
                                    }
                                    Column {
                                        Text(link.patientName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text("Sync Code: ${link.linkingCode}", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isApproved) MedSuccessLight else MedWarningLight
                                ) {
                                    Text(
                                        text = link.status,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isApproved) MedSuccess else MedWarning
                                    )
                                }
                            }

                            HorizontalDivider(color = MedBorderLight)

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isApproved) {
                                    FilledTonalButton(
                                        onClick = { onNavigateToPatientInfo(link.patientId) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = MedBlueLight, contentColor = MedBluePrimary)
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("View Info", style = MaterialTheme.typography.labelSmall)
                                    }

                                    OutlinedButton(
                                        onClick = { onNavigateToPermissions(link.patientId) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MedTextPrimary)
                                    ) {
                                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Permissions", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                IconButton(
                                    onClick = { linkToDelete = link }
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Remove Link", tint = MedError)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
