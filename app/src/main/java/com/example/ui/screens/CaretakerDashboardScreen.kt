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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.model.AppointmentEntity
import com.example.data.model.CaretakerAssistanceRequestEntity
import com.example.data.model.CaretakerLinkEntity
import com.example.data.model.MedicineReminderEntity
import com.example.data.model.UserEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel
import kotlinx.coroutines.flow.collect
import java.util.Locale
import java.util.Date

@Composable
fun CaretakerDashboardScreen(
    viewModel: MedTimeViewModel,
    onNavigateToPatientInfo: (String) -> Unit,
    onNavigateToPatientMonitor: (String) -> Unit,
    onNavigateToLinkPatient: () -> Unit,
    onNavigateToHelpAssistance: () -> Unit,
    onNavigateToMessages: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val caretakerLinks by viewModel.caretakerLinks.collectAsState()
    val allAppointments by viewModel.allAppointments.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val approvedLinks = remember(caretakerLinks) {
        caretakerLinks.filter { it.status.equals("APPROVED", ignoreCase = true) }
    }

    var selectedPatientId by remember(approvedLinks) {
        mutableStateOf(approvedLinks.firstOrNull()?.patientId ?: "patient_1")
    }

    val selectedPatientUser = remember(allUsers, selectedPatientId) {
        allUsers.firstOrNull { it.id == selectedPatientId } ?: allUsers.firstOrNull { it.role == "PATIENT" }
    }

    // Collect reminders for the selected patient
    var patientReminders by remember { mutableStateOf<List<MedicineReminderEntity>>(emptyList()) }
    LaunchedEffect(selectedPatientId) {
        viewModel.getTodayRemindersForPatient(selectedPatientId).collect {
            patientReminders = it
        }
    }

    // Collect appointments for selected patient
    val patientAppointments = remember(allAppointments, selectedPatientId) {
        allAppointments.filter { it.patientId == selectedPatientId }
    }

    val nextAppointment = remember(patientAppointments) {
        patientAppointments.firstOrNull { it.status == "ACCEPTED" || it.status == "PENDING" }
    }

    val totalDoses = patientReminders.size
    val takenDoses = patientReminders.count { it.status == "TAKEN" }
    val pendingDoses = patientReminders.count { it.status == "PENDING" || it.status == "SNOOZED" }
    val missedDoses = patientReminders.count { it.status == "MISSED" || it.status == "SKIPPED" }
    val adherencePercent = if (totalDoses > 0) ((takenDoses.toFloat() / totalDoses.toFloat()) * 100).toInt() else 100

    val caretakerAssistanceRequests by viewModel.caretakerAssistanceRequests.collectAsState()
    val caretakerWallet by viewModel.caretakerWallet.collectAsState()
    val caretakerWalletTransactions by viewModel.caretakerWalletTransactions.collectAsState()

    var showCallPatientRequestDialog by remember { mutableStateOf(false) }
    var showVisitPatientRequestDialog by remember { mutableStateOf(false) }
    var showNudgeDialog by remember { mutableStateOf(false) }
    var showWalletDialog by remember { mutableStateOf(false) }
    var showAddMoneyDialog by remember { mutableStateOf(false) }

    // Staged visit request for pre-payment step
    var stagedVisitPatientId by remember { mutableStateOf("") }
    var stagedVisitPatientName by remember { mutableStateOf("") }
    var stagedVisitReason by remember { mutableStateOf("") }
    var stagedVisitNotes by remember { mutableStateOf("") }
    var stagedVisitIsEmergency by remember { mutableStateOf(false) }
    var stagedVisitDistanceKm by remember { mutableDoubleStateOf(0.0) }
    var stagedVisitAddress by remember { mutableStateOf("") }
    var stagedVisitLat by remember { mutableStateOf<Double?>(null) }
    var stagedVisitLon by remember { mutableStateOf<Double?>(null) }
    var stagedVisitCaretakerLocName by remember { mutableStateOf("") }
    var stagedVisitCaretakerLat by remember { mutableStateOf<Double?>(null) }
    var stagedVisitCaretakerLon by remember { mutableStateOf<Double?>(null) }
    var stagedVisitSelectedAddressId by remember { mutableStateOf("home") }
    var stagedVisitAddressSnapshot by remember { mutableStateOf("") }

    var showVisitPaymentDialog by remember { mutableStateOf(false) }
    var completedVisitReceipt by remember { mutableStateOf<CaretakerAssistanceRequestEntity?>(null) }
    var showReceiptDialog by remember { mutableStateOf(false) }

    // Full Wallet Management Dialog
    if (showWalletDialog) {
        CaretakerWalletDialog(
            onDismiss = { showWalletDialog = false },
            wallet = caretakerWallet,
            transactions = caretakerWalletTransactions,
            onAddMoneyClick = {
                showAddMoneyDialog = true
            }
        )
    }

    // Add Money Modal (Razorpay simulation)
    if (showAddMoneyDialog) {
        AddMoneyRazorpayDialog(
            onDismiss = { showAddMoneyDialog = false },
            currentBalance = caretakerWallet?.balance ?: 450.0,
            maxBalance = caretakerWallet?.maxBalance ?: 1000.0,
            onConfirmTopUp = { amount, orderId, paymentId ->
                viewModel.topUpCaretakerWallet(
                    amount = amount,
                    providerPaymentId = paymentId,
                    orderId = orderId
                )
            }
        )
    }

    // Visit Payment Confirmation Modal (Strict Pre-payment)
    if (showVisitPaymentDialog) {
        VisitPaymentConfirmationDialog(
            onDismiss = { showVisitPaymentDialog = false },
            patientName = stagedVisitPatientName,
            distanceKm = stagedVisitDistanceKm,
            walletBalance = caretakerWallet?.balance ?: 0.0,
            visitAddress = stagedVisitAddressSnapshot.ifBlank { stagedVisitAddress },
            onOpenAddMoney = {
                showAddMoneyDialog = true
            },
            onConfirmWalletPayment = { feeResult ->
                viewModel.payAndSubmitVisitRequest(
                    patientId = stagedVisitPatientId,
                    patientName = stagedVisitPatientName,
                    reason = stagedVisitReason,
                    notes = stagedVisitNotes,
                    isEmergency = stagedVisitIsEmergency,
                    distanceKm = stagedVisitDistanceKm,
                    patientAddress = stagedVisitAddress,
                    patientLat = stagedVisitLat,
                    patientLon = stagedVisitLon,
                    caretakerLocationName = stagedVisitCaretakerLocName,
                    caretakerLat = stagedVisitCaretakerLat,
                    caretakerLon = stagedVisitCaretakerLon,
                    selectedAddressId = stagedVisitSelectedAddressId,
                    addressSnapshot = stagedVisitAddressSnapshot.ifBlank { stagedVisitAddress },
                    onResult = { success, msg, createdReq ->
                        if (success && createdReq != null) {
                            showVisitPaymentDialog = false
                            completedVisitReceipt = createdReq
                            showReceiptDialog = true
                        }
                    }
                )
            }
        )
    }

    // Payment Receipt Dialog
    if (showReceiptDialog && completedVisitReceipt != null) {
        val req = completedVisitReceipt!!
        VisitPaymentReceiptDialog(
            onDismiss = {
                showReceiptDialog = false
                completedVisitReceipt = null
            },
            patientName = req.patientName,
            distanceKm = req.distanceKm,
            billableKm = req.billableKm,
            visitCharge = req.totalVisitCharge,
            remainingBalance = caretakerWallet?.balance ?: 0.0,
            paymentId = req.paymentId,
            visitAddress = req.addressSnapshot.ifBlank { req.patientAddress }
        )
    }

    if (showCallPatientRequestDialog) {
        CallPatientRequestDialog(
            onDismiss = { showCallPatientRequestDialog = false },
            caretakerLinks = caretakerLinks,
            allUsers = allUsers,
            onSubmit = { patientId, patientName, reason, notes, isEmergency, locationName, lat, lon ->
                viewModel.submitCallAssistanceRequest(
                    patientId = patientId,
                    patientName = patientName,
                    reason = reason,
                    notes = notes,
                    isEmergency = isEmergency,
                    caretakerLocationName = locationName,
                    lat = lat,
                    lon = lon
                )
            }
        )
    }

    if (showVisitPatientRequestDialog) {
        VisitPatientRequestDialog(
            onDismiss = { showVisitPatientRequestDialog = false },
            caretakerLinks = caretakerLinks,
            allUsers = allUsers,
            walletBalance = caretakerWallet?.balance ?: 0.0,
            onOpenAddMoney = { showAddMoneyDialog = true },
            onSubmit = { patientId, patientName, reason, notes, isEmergency, distanceKm, baseCharge, additionalCharge, totalCharge, patientAddress, patientLat, patientLon, caretakerLocationName, caretakerLat, caretakerLon, selectedAddressId, addressSnapshot ->
                // Transition directly to Payment Confirmation step
                stagedVisitPatientId = patientId
                stagedVisitPatientName = patientName
                stagedVisitReason = reason
                stagedVisitNotes = notes
                stagedVisitIsEmergency = isEmergency
                stagedVisitDistanceKm = distanceKm
                stagedVisitAddress = patientAddress
                stagedVisitLat = patientLat
                stagedVisitLon = patientLon
                stagedVisitCaretakerLocName = caretakerLocationName
                stagedVisitCaretakerLat = caretakerLat
                stagedVisitCaretakerLon = caretakerLon
                stagedVisitSelectedAddressId = selectedAddressId
                stagedVisitAddressSnapshot = addressSnapshot

                showVisitPatientRequestDialog = false
                showVisitPaymentDialog = true
            }
        )
    }

    if (showNudgeDialog && selectedPatientUser != null) {
        AlertDialog(
            onDismissRequest = { showNudgeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MedBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Dose Reminder")
                }
            },
            text = {
                Text(
                    text = "Would you like to send a gentle medication reminder notification to ${selectedPatientUser.name}?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MedTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNudgeDialog = false
                        viewModel.sendCaretakerNudge(
                            patientId = selectedPatientUser.id,
                            patientName = selectedPatientUser.name
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Nudge")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNudgeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MedBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Caretaker Welcome Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MedBluePrimary)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hello, ${currentUser?.name ?: "Emily"} 👋",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Caretaker Management Dashboard",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.clickable { onNavigateToLinkPatient() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.AddLink, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text("Link Patient", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick stats pill row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${approvedLinks.size}",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Linked Patients",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$adherencePercent%",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Avg. Adherence",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$totalDoses",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Today's Doses",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Caretaker Wallet Quick Access Card
        item {
            val walletBal = caretakerWallet?.balance ?: 450.0
            val maxBal = caretakerWallet?.maxBalance ?: 1000.0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("caretaker_dashboard_wallet_card")
                    .clickable { showWalletDialog = true },
                colors = CardDefaults.cardColors(containerColor = MedSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MedBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(24.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "MY WALLET",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MedTextSecondary
                            )
                            Surface(
                                color = MedSuccessLight,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Max ₹1,000",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MedSuccess
                                )
                            }
                        }

                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", walletBal)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MedTextPrimary
                        )

                        Text(
                            text = "Used for Paid Patient Home Visits",
                            style = MaterialTheme.typography.labelSmall,
                            color = MedTextTertiary
                        )
                    }

                    Button(
                        onClick = { showAddMoneyDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("btn_dashboard_add_money")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Money", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Linked Patients Selector / Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Linked Patients",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    TextButton(onClick = onNavigateToLinkPatient) {
                        Text("+ Add Patient", style = MaterialTheme.typography.labelMedium, color = MedBluePrimary)
                    }
                }

                if (approvedLinks.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MedSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.PersonAddAlt1, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(36.dp))
                            Text("No Linked Patients Yet", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                "Enter your patient's 6-digit MedTime sync code or scan their QR code to start monitoring.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = onNavigateToLinkPatient,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                            ) {
                                Text("Link Patient Now")
                            }
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(approvedLinks) { link ->
                            val isSelected = link.patientId == selectedPatientId
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) MedBlueLight else MedSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MedBluePrimary else MedBorderLight
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedPatientId = link.patientId }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val initials = link.patientName.take(2).uppercase()
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) MedBluePrimary else MedSurfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initials,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) Color.White else MedTextPrimary
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = link.patientName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MedTextPrimary
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(MedSuccess)
                                            )
                                            Text(
                                                text = "Connected",
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
            }
        }

        // Active Patient Overview Card
        if (selectedPatientUser != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MedSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = selectedPatientUser.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MedTextPrimary
                                )
                                Text(
                                    text = "Blood Group: ${selectedPatientUser.bloodGroup.ifBlank { "O+" }} • Phone: ${selectedPatientUser.phone.ifBlank { "Not set" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary
                                )
                            }
                            IconButton(
                                onClick = { onNavigateToPatientInfo(selectedPatientUser.id) }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "View Profile", tint = MedBluePrimary, modifier = Modifier.size(18.dp))
                            }
                        }

                        HorizontalDivider(color = MedBorderLight)

                        // Medication Status Summary
                        Text(
                            text = "Today's Medication Status",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Taken
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MedSuccessLight,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("$takenDoses", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MedSuccess)
                                    Text("Taken", style = MaterialTheme.typography.labelSmall, color = MedSuccess)
                                }
                            }
                            // Upcoming
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MedBlueLight,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("$pendingDoses", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MedBluePrimary)
                                    Text("Upcoming", style = MaterialTheme.typography.labelSmall, color = MedBluePrimary)
                                }
                            }
                            // Missed
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MedErrorLight,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("$missedDoses", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MedError)
                                    Text("Missed", style = MaterialTheme.typography.labelSmall, color = MedError)
                                }
                            }
                        }

                        // Adherence Progress Bar
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Daily Adherence Rate", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                Text("$adherencePercent%", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MedBluePrimary)
                            }
                            LinearProgressIndicator(
                                progress = { (adherencePercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (adherencePercent >= 80) MedSuccess else MedWarning,
                                trackColor = MedSurfaceVariant
                            )
                        }

                        // Quick Nudge Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showNudgeDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MedBluePrimary)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send Dose Nudge", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = { onNavigateToPatientMonitor(selectedPatientUser.id) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                            ) {
                                Icon(Icons.Default.MonitorHeart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Monitor Live", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }

        // Upcoming Doctor Appointment Card
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
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Upcoming Doctor Consultation",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                        }
                        if (nextAppointment != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MedSuccessLight
                            ) {
                                Text(
                                    text = nextAppointment.status,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MedSuccess
                                )
                            }
                        }
                    }

                    if (nextAppointment != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MedSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MedBlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MedBluePrimary)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(nextAppointment.doctorName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(nextAppointment.doctorSpecialty, style = MaterialTheme.typography.labelSmall, color = MedBluePrimary)
                                    Text("${nextAppointment.appointmentDate} at ${nextAppointment.appointmentTime}", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No upcoming doctor appointments scheduled for this patient.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextSecondary
                        )
                    }
                }
            }
        }

        // Quick Actions Grid (Call Patient, Visit Patient, Messages, Help/Assistance)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Call Patient
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showCallPatientRequestDialog = true }
                            .testTag("caretaker_call_patient_card"),
                        colors = CardDefaults.cardColors(containerColor = MedSurface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MedSuccessLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = MedSuccess)
                            }
                            Text("Call Patient", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Request Admin Call", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                        }
                    }

                    // Visit Patient
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showVisitPatientRequestDialog = true }
                            .testTag("caretaker_visit_patient_card"),
                        colors = CardDefaults.cardColors(containerColor = MedSurface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MedWarningLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MedWarning)
                            }
                            Text("Visit Patient", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Distance & Fare Calc", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Chat with Patient
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (selectedPatientUser != null) {
                                    onNavigateToMessages(selectedPatientUser.id)
                                }
                            }
                            .testTag("caretaker_chat_card"),
                        colors = CardDefaults.cardColors(containerColor = MedSurface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MedBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = MedBluePrimary)
                            }
                            Text("Care Messages", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Direct Chat Line", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                        }
                    }

                    // Notifications Center
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigateToNotifications() }
                            .testTag("caretaker_alerts_card"),
                        colors = CardDefaults.cardColors(containerColor = MedSurface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MedErrorLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = MedError)
                            }
                            Text("Alerts & Log", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Missed dose warnings", style = MaterialTheme.typography.labelSmall, color = MedTextSecondary)
                        }
                    }
                }
            }
        }

        // Caretaker Assistance Requests (Calls & Visits)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Patient Assistance Requests",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MedTextPrimary
                        )
                    }
                    Text(
                        "${caretakerAssistanceRequests.size} Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MedTextSecondary
                    )
                }

                if (caretakerAssistanceRequests.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MedSurface),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "No active assistance requests.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MedTextPrimary
                            )
                            Text(
                                "Tap 'Call Patient' or 'Visit Patient' above to request Admin assistance.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        caretakerAssistanceRequests.take(4).forEach { req ->
                            CaretakerAssistanceRequestCard(
                                request = req,
                                onUpdateProgress = { reqId, status ->
                                    viewModel.caretakerUpdateVisitProgress(reqId, status)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
