package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CaretakerAssistanceRequestEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.WalletTransactionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerNotificationDetailScreen(
    notification: NotificationEntity,
    viewModel: MedTimeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToWallet: () -> Unit = {},
    onNavigateToChat: (patientId: String) -> Unit = {},
    onNavigateToPatientInfo: (patientId: String) -> Unit = {},
    onNavigateToMedicines: () -> Unit = {},
    onNavigateToAppointments: () -> Unit = {},
    onNavigateToRequestTracker: (requestId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val allAssistanceRequests by viewModel.allAssistanceRequests.collectAsState()
    val allTransactions by viewModel.allWalletTransactions.collectAsState()
    val allAppointments by viewModel.allAppointments.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val caretakerWallet by viewModel.caretakerWallet.collectAsState()

    // Real-time resolved entity
    val relatedAssistanceRequest = remember(allAssistanceRequests, notification.relatedEntityId, notification.title) {
        allAssistanceRequests.firstOrNull { it.id == notification.relatedEntityId }
            ?: allAssistanceRequests.firstOrNull {
                it.caretakerId == notification.userId && (
                    (notification.type.contains("VISIT", ignoreCase = true) && it.requestType == "VISIT") ||
                    (notification.type.contains("CALL", ignoreCase = true) && it.requestType == "CALL")
                )
            }
    }

    val relatedTransaction = remember(allTransactions, notification.relatedEntityId, notification.title) {
        allTransactions.firstOrNull { it.transactionId == notification.relatedEntityId }
            ?: allTransactions.firstOrNull { it.relatedRequestId == notification.relatedEntityId }
            ?: allTransactions.firstOrNull {
                it.caretakerId == notification.userId &&
                notification.type.contains("WALLET", ignoreCase = true)
            }
    }

    val relatedAppointment = remember(allAppointments, notification.relatedEntityId) {
        allAppointments.firstOrNull { it.id == notification.relatedEntityId }
    }

    val dateFormatted = remember(notification.timestamp) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(notification.timestamp))
    }
    val timeFormatted = remember(notification.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(notification.timestamp))
    }

    // Determine semantic type
    val isWalletType = notification.type == "WALLET_TOPUP" ||
            notification.type == "WALLET_PAYMENT" ||
            notification.title.contains("Wallet", ignoreCase = true) ||
            notification.title.contains("Top-up", ignoreCase = true)

    val isVisitRequest = notification.type == "VISIT_REQUEST" ||
            notification.type == "VISIT_APPROVED" ||
            notification.type == "VISIT_REJECTED" ||
            notification.type == "VISIT_COMPLETED" ||
            (notification.type == "CARETAKER" && (notification.title.contains("Visit", ignoreCase = true) || relatedAssistanceRequest?.requestType == "VISIT"))

    val isCallRequest = notification.type == "CALL_REQUEST" ||
            (notification.type == "CARETAKER" && (notification.title.contains("Call", ignoreCase = true) || relatedAssistanceRequest?.requestType == "CALL"))

    val isMedicineAlert = notification.type == "MEDICINE" ||
            notification.type == "MEDICATION_ALERT" ||
            notification.type == "MISSED_DOSE" ||
            notification.type == "LOW_STOCK"

    val isAppointment = notification.type == "APPOINTMENT"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notification",
                        fontWeight = FontWeight.Bold,
                        color = MedTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("notification_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MedTextPrimary
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("notification_detail_more_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MedTextPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Mark as Unread") },
                                onClick = {
                                    showMenu = false
                                    viewModel.markNotificationUnread(notification.id)
                                    onNavigateBack()
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.MarkEmailUnread, contentDescription = null, tint = MedBluePrimary)
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Delete", color = MedError) },
                                onClick = {
                                    showMenu = false
                                    viewModel.deleteNotification(notification.id)
                                    onNavigateBack()
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = MedError)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MedSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MedBackground)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Hero Card (Message-like header)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MedSurface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val headerIcon = when {
                        isWalletType -> Icons.Default.AccountBalanceWallet
                        isVisitRequest -> Icons.Default.PersonPinCircle
                        isCallRequest -> Icons.Default.PhoneCallback
                        notification.type == "MISSED_DOSE" -> Icons.Default.AccessTime
                        notification.type == "LOW_STOCK" -> Icons.Default.Warning
                        notification.type == "EMERGENCY_SOS" -> Icons.Default.Emergency
                        isAppointment -> Icons.Default.CalendarMonth
                        else -> Icons.Default.Notifications
                    }

                    val headerColor = when {
                        notification.severity == "CRITICAL" || notification.type == "EMERGENCY_SOS" -> MedError
                        notification.severity == "WARNING" || notification.type == "MISSED_DOSE" -> MedWarning
                        isWalletType -> MedSuccess
                        else -> MedBluePrimary
                    }

                    val headerBg = when {
                        notification.severity == "CRITICAL" || notification.type == "EMERGENCY_SOS" -> MedErrorLight
                        notification.severity == "WARNING" || notification.type == "MISSED_DOSE" -> MedWarningLight
                        isWalletType -> MedSuccessLight
                        else -> MedBlueLight
                    }

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(headerBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = headerIcon,
                            contentDescription = null,
                            tint = headerColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MedTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MedBackground,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = MedTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "$dateFormatted • $timeFormatted",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextSecondary
                            )
                        }
                    }
                }
            }

            // Context-Specific Detail Body
            when {
                isWalletType -> {
                    WalletNotificationDetailContent(
                        notification = notification,
                        transaction = relatedTransaction,
                        walletBalance = caretakerWallet?.balance ?: 1000.0,
                        dateFormatted = dateFormatted,
                        timeFormatted = timeFormatted,
                        onViewWallet = onNavigateToWallet
                    )
                }
                isVisitRequest -> {
                    VisitRequestNotificationDetailContent(
                        notification = notification,
                        request = relatedAssistanceRequest,
                        dateFormatted = dateFormatted,
                        timeFormatted = timeFormatted,
                        onViewRequest = {
                            if (relatedAssistanceRequest != null) {
                                onNavigateToRequestTracker(relatedAssistanceRequest.id)
                            }
                        },
                        onViewPatient = {
                            val pId = relatedAssistanceRequest?.patientId ?: "patient_1"
                            onNavigateToPatientInfo(pId)
                        }
                    )
                }
                isCallRequest -> {
                    CallRequestNotificationDetailContent(
                        notification = notification,
                        request = relatedAssistanceRequest,
                        dateFormatted = dateFormatted,
                        timeFormatted = timeFormatted,
                        onViewRequest = {
                            if (relatedAssistanceRequest != null) {
                                onNavigateToRequestTracker(relatedAssistanceRequest.id)
                            }
                        },
                        onOpenChat = {
                            val pId = relatedAssistanceRequest?.patientId ?: "patient_1"
                            onNavigateToChat(pId)
                        }
                    )
                }
                isMedicineAlert -> {
                    MedicineAlertNotificationDetailContent(
                        notification = notification,
                        onViewMedicine = onNavigateToMedicines,
                        onSendNudge = {
                            viewModel.sendCaretakerNudge("patient_1", "Vijay Kumar", "")
                        }
                    )
                }
                isAppointment -> {
                    AppointmentNotificationDetailContent(
                        notification = notification,
                        appointment = relatedAppointment,
                        onViewAppointment = onNavigateToAppointments
                    )
                }
                else -> {
                    GeneralNotificationDetailContent(
                        notification = notification,
                        dateFormatted = dateFormatted,
                        timeFormatted = timeFormatted
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 1. WALLET NOTIFICATION DETAIL
// ---------------------------------------------------------------------------
@Composable
private fun WalletNotificationDetailContent(
    notification: NotificationEntity,
    transaction: WalletTransactionEntity?,
    walletBalance: Double,
    dateFormatted: String,
    timeFormatted: String,
    onViewWallet: () -> Unit
) {
    val amount = transaction?.amount ?: run {
        // Extract amount from title e.g. "Wallet Top-up Successful (+₹800)"
        val regex = Regex("""[₹$]?(\d+(?:\.\d+)?)""")
        regex.find(notification.title)?.groupValues?.get(1)?.toDoubleOrNull() ?: 800.0
    }
    val balanceBefore = transaction?.balanceBefore ?: (walletBalance - amount).coerceAtLeast(0.0)
    val balanceAfter = transaction?.balanceAfter ?: walletBalance
    val txnId = transaction?.transactionId ?: (if (notification.relatedEntityId.isNotBlank()) notification.relatedEntityId else "MT-WALLET-" + System.currentTimeMillis().toString().takeLast(8))
    val paymentProvider = transaction?.paymentProvider ?: "Razorpay Test Mode"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MedSurface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction Details",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MedSuccessLight
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Successful",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MedSuccess
                        )
                    }
                }
            }

            HorizontalDivider(color = MedBorder)

            DetailRow(label = "Amount", value = "₹${String.format(Locale.US, "%.2f", amount)}", isBold = true, valueColor = MedSuccess)
            DetailRow(label = "Transaction Type", value = if (transaction?.type == "VISIT_PAYMENT") "Visit Payment" else "Wallet Top-up")
            DetailRow(label = "Status", value = "Successful")
            DetailRow(label = "Previous Balance", value = "₹${String.format(Locale.US, "%.2f", balanceBefore)}")
            DetailRow(label = "Wallet Balance", value = "₹${String.format(Locale.US, "%.2f", balanceAfter)}", isBold = true, valueColor = MedBluePrimary)
            DetailRow(label = "Payment Provider", value = paymentProvider)
            DetailRow(label = "Date", value = dateFormatted)
            DetailRow(label = "Time", value = timeFormatted)
            DetailRow(label = "Transaction ID", value = txnId)

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onViewWallet,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("notification_view_wallet_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Wallet", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 2. VISIT PATIENT REQUEST NOTIFICATION DETAIL
// ---------------------------------------------------------------------------
@Composable
private fun VisitRequestNotificationDetailContent(
    notification: NotificationEntity,
    request: CaretakerAssistanceRequestEntity?,
    dateFormatted: String,
    timeFormatted: String,
    onViewRequest: () -> Unit,
    onViewPatient: () -> Unit
) {
    val patientName = request?.patientName ?: "Vijay Kumar"
    val reason = request?.reason ?: "Medicine Assistance"
    val status = request?.status ?: "PAID_PENDING_ADMIN"
    val address = request?.addressSnapshot?.ifBlank { request.patientAddress } ?: "124 Indiranagar 100ft Rd, Bengaluru, Karnataka 560038"
    val distanceKm = request?.distanceKm ?: 5.0
    val visitFee = request?.totalVisitCharge ?: 50.0
    val requestId = request?.id ?: (if (notification.relatedEntityId.isNotBlank()) notification.relatedEntityId else "MT-VISIT-849201")

    val statusLabel = when (status) {
        "PENDING", "PAID_PENDING_ADMIN" -> "Waiting for Admin Review"
        "ACCEPTED" -> "Approved by Admin"
        "CARETAKER_ON_THE_WAY" -> "Caregiver On The Way"
        "ARRIVED" -> "Arrived at Patient"
        "COMPLETED" -> "Completed"
        "REJECTED" -> "Rejected by Admin"
        else -> status.replace("_", " ")
    }

    val statusColor = when (status) {
        "COMPLETED" -> MedSuccess
        "ACCEPTED", "CARETAKER_ON_THE_WAY", "ARRIVED" -> MedBluePrimary
        "REJECTED" -> MedError
        else -> MedWarning
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MedSurface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Visit Patient Request",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
            }

            HorizontalDivider(color = MedBorder)

            DetailRow(label = "Patient", value = patientName, isBold = true)
            DetailRow(label = "Request Type", value = "Visit Patient")
            DetailRow(label = "Reason", value = reason)
            DetailRow(label = "Status", value = statusLabel, valueColor = statusColor, isBold = true)
            DetailRow(label = "Submitted", value = "$dateFormatted • $timeFormatted")
            DetailRow(label = "Visit Address", value = address)
            DetailRow(label = "Distance", value = "${String.format(Locale.US, "%.1f", distanceKm)} km")
            DetailRow(label = "Visit Fee", value = "₹${visitFee.toInt()}", isBold = true, valueColor = MedBluePrimary)
            DetailRow(label = "Payment", value = "PAID (Wallet Deduction)", isBold = true, valueColor = MedSuccess)
            DetailRow(label = "Request ID", value = requestId)

            if (!request?.adminNotes.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MedBlueLight.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Admin Note: ${request?.adminNotes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Status Timeline Component
            Text(
                text = "Request Timeline",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MedTextPrimary
            )

            StatusTimeline(
                steps = listOf(
                    TimelineStep("Request Submitted", true),
                    TimelineStep("Payment Completed", true),
                    TimelineStep(
                        title = when (status) {
                            "REJECTED" -> "Admin Rejected"
                            "ACCEPTED", "CARETAKER_ON_THE_WAY", "ARRIVED", "COMPLETED" -> "Admin Approved"
                            else -> "Waiting for Admin"
                        },
                        isCompleted = status != "PENDING" && status != "PAID_PENDING_ADMIN",
                        isActive = status == "PENDING" || status == "PAID_PENDING_ADMIN"
                    ),
                    TimelineStep(
                        title = "Caregiver Visit",
                        isCompleted = status == "COMPLETED",
                        isActive = status == "ACCEPTED" || status == "CARETAKER_ON_THE_WAY" || status == "ARRIVED"
                    )
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onViewPatient,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("notification_view_patient_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Patient")
                }

                Button(
                    onClick = onViewRequest,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("notification_view_request_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Request", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3. CALL PATIENT REQUEST NOTIFICATION DETAIL
// ---------------------------------------------------------------------------
@Composable
private fun CallRequestNotificationDetailContent(
    notification: NotificationEntity,
    request: CaretakerAssistanceRequestEntity?,
    dateFormatted: String,
    timeFormatted: String,
    onViewRequest: () -> Unit,
    onOpenChat: () -> Unit
) {
    val patientName = request?.patientName ?: "Vijay Kumar"
    val reason = request?.reason ?: "Patient Check"
    val status = request?.status ?: "PENDING"
    val requestId = request?.id ?: (if (notification.relatedEntityId.isNotBlank()) notification.relatedEntityId else "MT-CALL-719302")

    val statusLabel = when (status) {
        "PENDING", "PAID_PENDING_ADMIN" -> "Waiting for Admin Review"
        "ACCEPTED" -> "Admin Calling Patient"
        "COMPLETED" -> "Call Completed"
        "REJECTED" -> "Rejected"
        else -> status
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MedSurface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Call Patient Request",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MedTextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MedWarningLight
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedWarning
                    )
                }
            }

            HorizontalDivider(color = MedBorder)

            DetailRow(label = "Patient", value = patientName, isBold = true)
            DetailRow(label = "Request Type", value = "Call Patient")
            DetailRow(label = "Reason", value = reason)
            DetailRow(label = "Status", value = statusLabel, valueColor = MedWarning, isBold = true)
            DetailRow(label = "Submitted", value = "$dateFormatted • $timeFormatted")
            DetailRow(label = "Request ID", value = requestId)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Request Timeline",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MedTextPrimary
            )

            StatusTimeline(
                steps = listOf(
                    TimelineStep("Request Submitted", true),
                    TimelineStep(
                        title = "Waiting for Admin",
                        isCompleted = status != "PENDING",
                        isActive = status == "PENDING"
                    ),
                    TimelineStep(
                        title = "Admin Tele-Call",
                        isCompleted = status == "COMPLETED",
                        isActive = status == "ACCEPTED"
                    )
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenChat,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("notification_open_chat_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Chat")
                }

                Button(
                    onClick = onViewRequest,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("notification_view_call_request_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Icon(Icons.Default.PhoneCallback, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Request", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4. MEDICINE / MISSED DOSE / LOW STOCK NOTIFICATION DETAIL
// ---------------------------------------------------------------------------
@Composable
private fun MedicineAlertNotificationDetailContent(
    notification: NotificationEntity,
    onViewMedicine: () -> Unit,
    onSendNudge: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MedSurface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Medication Alert Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MedTextPrimary
            )

            HorizontalDivider(color = MedBorder)

            DetailRow(label = "Patient", value = "Vijay Kumar", isBold = true)
            DetailRow(label = "Alert Type", value = notification.type.replace("_", " "))
            DetailRow(label = "Severity", value = notification.severity, valueColor = if (notification.severity == "CRITICAL") MedError else MedWarning, isBold = true)
            DetailRow(label = "Delivery Channels", value = notification.deliveryChannels)

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onSendNudge,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("notification_send_nudge_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Nudge")
                }

                Button(
                    onClick = onViewMedicine,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("notification_view_medicine_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Medicine", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 5. APPOINTMENT NOTIFICATION DETAIL
// ---------------------------------------------------------------------------
@Composable
private fun AppointmentNotificationDetailContent(
    notification: NotificationEntity,
    appointment: com.example.data.model.AppointmentEntity?,
    onViewAppointment: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MedSurface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Appointment Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MedTextPrimary
            )

            HorizontalDivider(color = MedBorder)

            DetailRow(label = "Patient", value = appointment?.patientName ?: "Vijay Kumar", isBold = true)
            DetailRow(label = "Doctor", value = appointment?.doctorName ?: "Dr. Sarah Mitchell, MD")
            DetailRow(label = "Specialty", value = appointment?.doctorSpecialty ?: "Cardiologist")
            DetailRow(label = "Reason", value = appointment?.reason ?: "Routine Consultation")
            DetailRow(label = "Scheduled Date", value = appointment?.appointmentDate ?: "2026-09-25")
            DetailRow(label = "Scheduled Time", value = appointment?.appointmentTime ?: "10:30 AM")
            DetailRow(label = "Status", value = appointment?.status ?: "CONFIRMED", isBold = true, valueColor = MedSuccess)

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onViewAppointment,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("notification_view_appointment_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Appointment", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 6. GENERAL NOTIFICATION DETAIL
// ---------------------------------------------------------------------------
@Composable
private fun GeneralNotificationDetailContent(
    notification: NotificationEntity,
    dateFormatted: String,
    timeFormatted: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MedSurface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "System Notice Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MedTextPrimary
            )

            HorizontalDivider(color = MedBorder)

            DetailRow(label = "Type", value = notification.type)
            DetailRow(label = "Recipient Role", value = notification.recipientRole)
            DetailRow(label = "Delivery Channels", value = notification.deliveryChannels)
            DetailRow(label = "Date", value = dateFormatted)
            DetailRow(label = "Time", value = timeFormatted)
        }
    }
}

// ---------------------------------------------------------------------------
// HELPERS: DetailRow & StatusTimeline
// ---------------------------------------------------------------------------
@Composable
private fun DetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = MedTextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MedTextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            ),
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.3f)
        )
    }
}

data class TimelineStep(
    val title: String,
    val isCompleted: Boolean = false,
    val isActive: Boolean = false
)

@Composable
fun StatusTimeline(
    steps: List<TimelineStep>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        steps.forEachIndexed { index, step ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                step.isCompleted -> MedSuccess
                                step.isActive -> MedBluePrimary
                                else -> MedBorder
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        step.isCompleted -> {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        step.isActive -> {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MedTextSecondary)
                            )
                        }
                    }
                }

                Text(
                    text = step.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (step.isActive || step.isCompleted) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (step.isCompleted || step.isActive) MedTextPrimary else MedTextSecondary
                )
            }
        }
    }
}
