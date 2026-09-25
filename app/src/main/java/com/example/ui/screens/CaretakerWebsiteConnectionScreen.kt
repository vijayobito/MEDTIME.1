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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.scanner.QrScannerDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerWebsiteConnectionScreen(
    viewModel: MedTimeViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showScanner by remember { mutableStateOf(false) }
    var isConnectedToWeb by remember { mutableStateOf(true) }

    if (showScanner) {
        QrScannerDialog(
            onDismiss = { showScanner = false },
            onAccountScanned = {
                showScanner = false
                isConnectedToWeb = true
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Connect to MedTime Web", fontWeight = FontWeight.Bold, color = MedTextPrimary)
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
            // Hero card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MedBluePrimary)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Devices, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Column {
                                Text("Caretaker Web Portal", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Text("https://caretaker.medtime.care", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                            }
                        }
                        Text(
                            text = "Access your patient management dashboard on your desktop browser. Open the portal and scan the QR code to pair your session instantly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Scanner trigger
            item {
                Button(
                    onClick = { showScanner = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Link a New Desktop Browser", fontWeight = FontWeight.Bold)
                }
            }

            // Linked web sessions
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
                        Text("Active Web Sessions", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)

                        if (isConnectedToWeb) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MedSuccessLight), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.LaptopMac, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(20.dp))
                                    }
                                    Column {
                                        Text("Chrome on macOS 14.5", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("Active now • IP: 192.168.1.42", style = MaterialTheme.typography.labelSmall, color = MedSuccess)
                                    }
                                }
                                TextButton(onClick = { isConnectedToWeb = false }) {
                                    Text("Unlink", color = MedError)
                                }
                            }
                        } else {
                            Text("No active web sessions connected.", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        }
                    }
                }
            }
        }
    }
}
