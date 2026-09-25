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
import com.example.data.model.WalletEntity
import com.example.data.model.WalletTransactionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel
import com.example.util.LocationUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerHelpAssistanceScreen(
    viewModel: MedTimeViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val caretakerLinks by viewModel.caretakerLinks.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val caretakerWallet by viewModel.caretakerWallet.collectAsState()
    val walletTransactions by viewModel.caretakerWalletTransactions.collectAsState()

    val walletBalance = caretakerWallet?.balance ?: 0.0

    val approvedLinks = remember(caretakerLinks) {
        caretakerLinks.filter { it.status.equals("APPROVED", ignoreCase = true) }
    }

    var selectedPatientId by remember(approvedLinks) {
        mutableStateOf(approvedLinks.firstOrNull()?.patientId ?: "patient_1")
    }

    val selectedPatientUser = remember(allUsers, selectedPatientId) {
        allUsers.firstOrNull { it.id == selectedPatientId } ?: allUsers.firstOrNull { it.role == "PATIENT" }
    }

    // Distance calculation state
    var travelDistanceKm by remember { mutableFloatStateOf(4.5f) }

    // Authoritative pricing rule: 1-3 km = ₹30. Above 3 km = ₹30 + ₹10/km with ceiling rounding
    val calculatedFare = remember(travelDistanceKm) {
        LocationUtils.calculateVisitFee(travelDistanceKm.toDouble())
    }

    var showBookingSuccessDialog by remember { mutableStateOf(false) }
    var showTopUpDialog by remember { mutableStateOf(false) }
    var topUpAmountInput by remember { mutableStateOf("500") }

    if (showBookingSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showBookingSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedSuccess)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Visit Scheduled & Paid!")
                }
            },
            text = {
                Text(
                    text = "Home care visit to ${selectedPatientUser?.name ?: "Patient"} (${String.format(Locale.US, "%.1f", travelDistanceKm)} km) has been booked. ₹${String.format(Locale.US, "%.2f", calculatedFare.totalFee)} was deducted from your Caregiver Wallet balance and submitted to Administrator.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MedTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showBookingSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MedSuccess)
                ) {
                    Text("Done")
                }
            }
        )
    }

    if (showTopUpDialog) {
        AlertDialog(
            onDismissRequest = { showTopUpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MedBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Top-Up Caregiver Wallet")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val maxAllowed = maxOf(0.0, 1000.0 - walletBalance)
                    Text("Select or enter the amount to add (Max wallet limit: ₹1,000. Maximum addable: ₹${maxAllowed.toInt()}):", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("100", "200", "300", "500").forEach { amt ->
                            FilterChip(
                                selected = topUpAmountInput == amt,
                                onClick = { topUpAmountInput = amt },
                                label = { Text("₹$amt") }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = topUpAmountInput,
                        onValueChange = { topUpAmountInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Amount (₹)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = topUpAmountInput.toDoubleOrNull() ?: 500.0
                        viewModel.topUpCaretakerWallet(amount)
                        showTopUpDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Text("Add Funds (Razorpay)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTopUpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Caregiver Assistance & Dispatch", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MedSurface,
                    titleContentColor = MedTextPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Patient Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("1. Linked Patient", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        if (approvedLinks.isEmpty()) {
                            Text("No linked patients found. Please request patient link first.", color = MedTextSecondary, style = MaterialTheme.typography.bodySmall)
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(approvedLinks) { link ->
                                    val isSelected = selectedPatientId == link.patientId
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedPatientId = link.patientId },
                                        label = { Text(link.patientName) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Call & Distance Visit Booking
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
                        Text("2. Visit Distance & Fare Estimate", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                        Text("Adjust simulated or real GPS distance to patient:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        Slider(
                            value = travelDistanceKm,
                            onValueChange = { travelDistanceKm = it },
                            valueRange = 1f..25f,
                            steps = 24
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Distance: ${String.format(Locale.US, "%.1f", travelDistanceKm)} km (Billable: ${calculatedFare.billableKm} km)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Visit Fare: ₹${calculatedFare.totalFee.toInt()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = MedSuccess)
                        }

                        Button(
                            onClick = {
                                if (selectedPatientUser != null && currentUser != null) {
                                    viewModel.payAndSubmitVisitRequest(
                                        patientId = selectedPatientUser.id,
                                        patientName = selectedPatientUser.name,
                                        reason = "Patient Home Care Visit",
                                        notes = "Dispatched via Caregiver Assistance Hub.",
                                        isEmergency = false,
                                        distanceKm = travelDistanceKm.toDouble(),
                                        patientAddress = selectedPatientUser.address,
                                        patientLat = selectedPatientUser.latitude,
                                        patientLon = selectedPatientUser.longitude,
                                        caretakerLocationName = "Bangalore, India",
                                        caretakerLat = 12.9716,
                                        caretakerLon = 77.5946,
                                        onResult = { success, _, _ ->
                                            if (success) {
                                                showBookingSuccessDialog = true
                                            }
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("caretaker_book_visit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                        ) {
                            Icon(Icons.Default.ScheduleSend, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pay ₹${calculatedFare.totalFee.toInt()} & Request Visit", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. CAREGIVER WALLET
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MedBlueLight), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(20.dp))
                                }
                                Column {
                                    Text("Caregiver Wallet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Available Balance: ₹${String.format(Locale.US, "%.2f", walletBalance)}", style = MaterialTheme.typography.labelSmall, color = MedSuccess, fontWeight = FontWeight.Bold)
                                }
                            }

                            FilledTonalButton(
                                onClick = { showTopUpDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = MedBlueLight, contentColor = MedBluePrimary)
                            ) {
                                Text("+ Top-Up", fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(color = MedBorderLight)

                        Text("Recent Wallet Transactions", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)

                        if (walletTransactions.isEmpty()) {
                            Text("No transactions logged yet.", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
                                walletTransactions.forEach { tx ->
                                    val isCredit = tx.type == "TOP_UP" || tx.type == "REFUND"
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(tx.description, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MedTextPrimary)
                                            Text(dateFormatter.format(Date(tx.createdAt)), style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                                        }
                                        Text(
                                            text = "${if (isCredit) "+" else "-"}₹${String.format(Locale.US, "%.2f", kotlin.math.abs(tx.amount))}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isCredit) MedSuccess else MedError
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
