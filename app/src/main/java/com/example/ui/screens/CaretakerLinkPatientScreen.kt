package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.scanner.QrScannerDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerLinkPatientScreen(
    viewModel: MedTimeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLinkedPatients: () -> Unit,
    modifier: Modifier = Modifier
) {
    var linkingCodeInput by remember { mutableStateOf("") }
    var showQrScannerDialog by remember { mutableStateOf(false) }

    val userFeedback by viewModel.userFeedback.collectAsState()

    if (showQrScannerDialog) {
        QrScannerDialog(
            onDismiss = { showQrScannerDialog = false },
            onAccountScanned = { payload ->
                showQrScannerDialog = false
                if (payload.code.isNotBlank()) {
                    linkingCodeInput = payload.code
                    viewModel.requestCaretakerLink(payload.code)
                } else if (payload.userId.isNotBlank()) {
                    viewModel.requestCaretakerLink("MED-7842")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Link New Patient", fontWeight = FontWeight.Bold, color = MedTextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MedTextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = onNavigateToLinkedPatients) {
                        Text("View Linked", color = MedBluePrimary, fontWeight = FontWeight.Bold)
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
            // Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MedBluePrimary)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Column {
                                Text("Connect Patient Profile", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Text("Real-time caregiver link & authorization", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                            }
                        }
                        Text(
                            text = "To monitor a family member or dependent, ask them for their 6-digit MedTime sync code or scan their QR code from their Caretaker screen.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Enter Code Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Enter Patient Sync Code",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )

                        OutlinedTextField(
                            value = linkingCodeInput,
                            onValueChange = { linkingCodeInput = it.uppercase() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("caretaker_link_code_input"),
                            placeholder = { Text("e.g. MED-7842") },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = MedBluePrimary) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MedBluePrimary,
                                unfocusedBorderColor = MedBorderLight
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (linkingCodeInput.isNotBlank()) {
                                        viewModel.requestCaretakerLink(linkingCodeInput.trim())
                                    }
                                },
                                enabled = linkingCodeInput.isNotBlank(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("caretaker_submit_link_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send Request")
                            }

                            FilledTonalButton(
                                onClick = { showQrScannerDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("caretaker_scan_qr_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = MedBlueLight, contentColor = MedBluePrimary)
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan QR Code")
                            }
                        }

                        // Demo quick fill hint
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MedSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Demo Patient Code: MED-7842", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
                                    Text("Patient: Vijay Kumar", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                }
                                TextButton(onClick = { linkingCodeInput = "MED-7842" }) {
                                    Text("Auto Fill", style = MaterialTheme.typography.labelMedium, color = MedBluePrimary)
                                }
                            }
                        }
                    }
                }
            }

            // 3-Step Guide Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "How Linking Works",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )

                        // Step 1
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier.size(28.dp).clip(CircleShape).background(MedBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("1", fontWeight = FontWeight.Bold, color = MedBluePrimary, fontSize = 14.sp)
                            }
                            Column {
                                Text("Enter or Scan Sync Code", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Obtain the 6-character code from the patient's MedTime app.", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            }
                        }

                        // Step 2
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier.size(28.dp).clip(CircleShape).background(MedBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("2", fontWeight = FontWeight.Bold, color = MedBluePrimary, fontSize = 14.sp)
                            }
                            Column {
                                Text("Caretaker Sends Connection Request", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("The patient receives an instant in-app request notification.", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            }
                        }

                        // Step 3
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier.size(28.dp).clip(CircleShape).background(MedBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("3", fontWeight = FontWeight.Bold, color = MedBluePrimary, fontSize = 14.sp)
                            }
                            Column {
                                Text("Patient Authorizes Permissions", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Once approved, their medication schedules and alerts become visible in your dashboard.", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
