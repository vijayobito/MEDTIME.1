package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CaretakerAssistanceRequestEntity
import com.example.data.model.WalletEntity
import com.example.data.model.WalletTransactionEntity
import com.example.ui.theme.*
import com.example.util.LocationUtils
import com.example.util.VisitFeeResult
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

/**
 * Caretaker Wallet Dialog / Management Portal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakerWalletDialog(
    onDismiss: () -> Unit,
    wallet: WalletEntity?,
    transactions: List<WalletTransactionEntity>,
    onAddMoneyClick: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    val currentBalance = wallet?.balance ?: 450.0
    val maxBalance = wallet?.maxBalance ?: 1000.0

    val filteredTransactions = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            "TOP_UP" -> transactions.filter { it.type == "TOP_UP" }
            "VISIT_PAYMENT" -> transactions.filter { it.type == "VISIT_PAYMENT" }
            "REFUND" -> transactions.filter { it.type == "REFUND" }
            else -> transactions
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("caretaker_wallet_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MedBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MedBluePrimary)
                    }
                    Text(
                        text = "MY WALLET",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Balance & Top-up Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wallet_balance_card"),
                        colors = CardDefaults.cardColors(containerColor = MedBluePrimary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Available Balance",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "₹${String.format(Locale.US, "%.2f", currentBalance)}",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                )

                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Max Limit: ₹1,000",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }

                            // Progress bar toward 1000 limit
                            val balanceRatio = (currentBalance / maxBalance).toFloat().coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { balanceRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MedSuccess,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )

                            Button(
                                onClick = onAddMoneyClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("wallet_add_money_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = MedBluePrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                enabled = currentBalance < maxBalance
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentBalance < maxBalance) "+ Add Money" else "Wallet Full (₹1,000)",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Filter Tabs
                item {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filters = listOf(
                            "ALL" to "All",
                            "TOP_UP" to "Top-ups",
                            "VISIT_PAYMENT" to "Visits",
                            "REFUND" to "Refunds"
                        )

                        filters.forEach { (key, label) ->
                            val isSelected = selectedFilter == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = key },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.testTag("txn_filter_${key.lowercase()}")
                            )
                        }
                    }
                }

                // Transactions List
                if (filteredTransactions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MedSurfaceVariant),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No transactions found.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MedTextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(filteredTransactions, key = { it.transactionId }) { txn ->
                        val isPositive = txn.type == "TOP_UP" || txn.type == "REFUND"
                        val typeColor = when (txn.type) {
                            "TOP_UP" -> MedSuccess
                            "VISIT_PAYMENT" -> MedError
                            "REFUND" -> MedBluePrimary
                            else -> MedTextPrimary
                        }
                        val typeIcon = when (txn.type) {
                            "TOP_UP" -> Icons.Default.ArrowDownward
                            "VISIT_PAYMENT" -> Icons.Default.DirectionsCar
                            "REFUND" -> Icons.Default.Replay
                            else -> Icons.Default.Payment
                        }

                        val dateFormatted = remember(txn.createdAt) {
                            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(txn.createdAt))
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("txn_item_${txn.transactionId}"),
                            colors = CardDefaults.cardColors(containerColor = MedSurface),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(typeColor.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(20.dp))
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when (txn.type) {
                                            "TOP_UP" -> "Wallet Top-up"
                                            "VISIT_PAYMENT" -> "Patient Visit (${txn.patientName.ifBlank { "Home Visit" }})"
                                            "REFUND" -> "Admin Refund"
                                            else -> txn.type
                                        },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MedTextPrimary
                                    )
                                    Text(
                                        text = dateFormatted,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MedTextSecondary
                                    )
                                    Text(
                                        text = "Balance: ₹${txn.balanceBefore.toInt()} → ₹${txn.balanceAfter.toInt()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MedTextTertiary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${if (isPositive) "+" else "-"} ₹${txn.amount.toInt()}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = typeColor
                                    )
                                    Surface(
                                        color = if (txn.status == "SUCCESS") MedSuccessLight else MedWarningLight,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = txn.status,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = if (txn.status == "SUCCESS") MedSuccess else MedWarning
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
            ) {
                Text("Done")
            }
        }
    )
}

/**
 * Add Money Modal with Razorpay Test Mode integration simulation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMoneyRazorpayDialog(
    onDismiss: () -> Unit,
    currentBalance: Double,
    maxBalance: Double = 1000.0,
    onConfirmTopUp: (amount: Double, orderId: String, paymentId: String) -> Unit
) {
    val maxAllowedAdd = remember(currentBalance, maxBalance) {
        (maxBalance - currentBalance).coerceAtLeast(0.0)
    }

    val predefinedAmounts = listOf(100, 200, 300, 500, 1000)
    var selectedPredefined by remember { mutableStateOf<Int?>(if (maxAllowedAdd >= 200) 200 else if (maxAllowedAdd >= 100) 100 else null) }
    var customAmountText by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("UPI") } // "UPI", "CARD", "NETBANKING", "WALLET"
    var isProcessingPayment by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val effectiveAmount = remember(selectedPredefined, customAmountText) {
        if (selectedPredefined != null) {
            selectedPredefined!!.toDouble()
        } else {
            customAmountText.toDoubleOrNull() ?: 0.0
        }
    }

    val isAmountValid = effectiveAmount >= 10.0 && effectiveAmount <= maxAllowedAdd

    AlertDialog(
        onDismissRequest = { if (!isProcessingPayment) onDismiss() },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("add_money_razorpay_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AddCard, contentDescription = null, tint = MedBluePrimary)
                Text("Add Money to Wallet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Wallet Limit Info Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MedBlueLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Current Balance:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                Text("₹${String.format(Locale.US, "%.2f", currentBalance)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Maximum Wallet Balance:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                Text("₹1,000.00", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                            HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Maximum you can add:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MedBluePrimary)
                                Text("₹${maxAllowedAdd.toInt()}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MedBluePrimary)
                            }
                        }
                    }
                }

                // Predefined Amount Chips
                item {
                    Text("Select Amount", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        predefinedAmounts.forEach { amt ->
                            val isAllowed = amt <= maxAllowedAdd
                            val isSelected = selectedPredefined == amt
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isAllowed) {
                                        selectedPredefined = amt
                                        customAmountText = ""
                                        errorMessage = null
                                    } else {
                                        errorMessage = "Maximum amount you can add is ₹${maxAllowedAdd.toInt()}."
                                    }
                                },
                                label = { Text("₹$amt") },
                                enabled = isAllowed,
                                modifier = Modifier.testTag("topup_chip_$amt")
                            )
                        }
                    }
                }

                // Custom Amount Input
                item {
                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = { input ->
                            val cleaned = input.filter { it.isDigit() }
                            customAmountText = cleaned
                            selectedPredefined = null
                            val num = cleaned.toDoubleOrNull() ?: 0.0
                            if (num > maxAllowedAdd) {
                                errorMessage = "Your wallet can hold a maximum of ₹1,000. Maximum you can add: ₹${maxAllowedAdd.toInt()}."
                            } else {
                                errorMessage = null
                            }
                        },
                        label = { Text("Or Enter Custom Amount (₹)") },
                        placeholder = { Text("Min ₹10, Max ₹${maxAllowedAdd.toInt()}") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("topup_custom_amount_input"),
                        shape = RoundedCornerShape(10.dp),
                        prefix = { Text("₹ ") },
                        isError = errorMessage != null || (customAmountText.isNotEmpty() && !isAmountValid)
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = MedError,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                }

                // Gateway & Payment Method Selection (Razorpay)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MedSurface),
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(16.dp))
                                Text(
                                    "Razorpay Secure Checkout (Test Mode)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MedTextPrimary
                                )
                            }

                            Text(
                                "Supported Indian Payment Methods:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextSecondary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val methods = listOf("UPI", "CARD", "NETBANKING")
                                methods.forEach { m ->
                                    val isSelected = selectedPaymentMethod == m
                                    Surface(
                                        onClick = { selectedPaymentMethod = m },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MedBlueLight else MedSurface,
                                        border = CardDefaults.outlinedCardBorder(),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = m,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) MedBluePrimary else MedTextSecondary
                                        )
                                    }
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
                    if (isAmountValid) {
                        isProcessingPayment = true
                        val orderId = "order_rzp_" + System.currentTimeMillis()
                        val payId = "pay_rzp_test_" + (100000..999999).random()
                        onConfirmTopUp(effectiveAmount, orderId, payId)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("btn_confirm_razorpay_pay"),
                enabled = isAmountValid && !isProcessingPayment,
                colors = ButtonDefaults.buttonColors(containerColor = MedSuccess)
            ) {
                if (isProcessingPayment) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Verifying...")
                } else {
                    Text("Pay ₹${effectiveAmount.toInt()} via Razorpay")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isProcessingPayment
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Visit Payment Confirmation Screen (Strict Pre-Payment Requirement)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitPaymentConfirmationDialog(
    onDismiss: () -> Unit,
    patientName: String,
    distanceKm: Double,
    walletBalance: Double,
    visitAddress: String = "",
    onOpenAddMoney: () -> Unit,
    onConfirmWalletPayment: (feeResult: VisitFeeResult) -> Unit
) {
    val feeResult = remember(distanceKm) {
        LocationUtils.calculateVisitFee(distanceKm)
    }

    val hasSufficientBalance = walletBalance >= feeResult.totalFee
    val balanceAfter = (walletBalance - feeResult.totalFee).coerceAtLeast(0.0)
    val shortage = (feeResult.totalFee - walletBalance).coerceAtLeast(0.0)
    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("visit_payment_confirmation_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Payment, contentDescription = null, tint = MedBluePrimary)
                Text(
                    text = "Confirm Visit Payment",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Trip Details Card
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
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Patient", style = MaterialTheme.typography.bodyMedium, color = MedTextSecondary)
                            Text(patientName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        if (visitAddress.isNotBlank()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text("Visit Address", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                                Text(
                                    text = "📍 $visitAddress",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MedTextPrimary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Actual Distance", style = MaterialTheme.typography.bodyMedium, color = MedTextSecondary)
                            Text("${feeResult.distanceKm} km", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Base Fare (1–3 km)", style = MaterialTheme.typography.bodyMedium, color = MedTextSecondary)
                            Text("₹${feeResult.baseFee.toInt()}", style = MaterialTheme.typography.bodyMedium)
                        }

                        if (feeResult.additionalFee > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Addl. Distance (${feeResult.billableKm - 3} km)", style = MaterialTheme.typography.bodyMedium, color = MedTextSecondary)
                                Text("+₹${feeResult.additionalFee.toInt()}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        HorizontalDivider(color = MedDivider)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Visit Charge", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                "₹${feeResult.totalFee.toInt()}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MedSuccess
                                )
                            )
                        }
                    }
                }

                // Wallet Accounting Balance Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasSufficientBalance) MedBlueLight else MedErrorLight
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Wallet Balance", style = MaterialTheme.typography.bodySmall, color = MedTextPrimary)
                            Text("₹${walletBalance.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }

                        if (hasSufficientBalance) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Remaining After Payment", style = MaterialTheme.typography.bodySmall, color = MedTextPrimary)
                                Text("₹${balanceAfter.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MedSuccess)
                            }
                        } else {
                            Text(
                                text = "⚠️ Insufficient Wallet Balance. You need ₹${shortage.toInt()} more.",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MedError
                            )
                        }
                    }
                }

                Text(
                    text = "Payment is securely deducted from your MedTime Wallet. The request will be dispatched to Admin only after payment confirmation.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MedTextTertiary
                )
            }
        },
        confirmButton = {
            if (hasSufficientBalance) {
                Button(
                    onClick = {
                        if (!isProcessing) {
                            isProcessing = true
                            onConfirmWalletPayment(feeResult)
                        }
                    },
                    modifier = Modifier.testTag("btn_pay_visit_from_wallet"),
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = MedSuccess)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Processing...")
                    } else {
                        Text("PAY ₹${feeResult.totalFee.toInt()} FROM WALLET")
                    }
                }
            } else {
                Button(
                    onClick = onOpenAddMoney,
                    modifier = Modifier.testTag("btn_open_topup_from_visit"),
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Add Money")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isProcessing
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Payment Receipt / Confirmation Screen
 */
@Composable
fun VisitPaymentReceiptDialog(
    onDismiss: () -> Unit,
    patientName: String,
    distanceKm: Double,
    billableKm: Int,
    visitCharge: Double,
    remainingBalance: Double,
    paymentId: String,
    visitAddress: String = ""
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("visit_payment_receipt_dialog"),
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MedSuccessLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(36.dp))
            }
        },
        title = {
            Text(
                text = "Payment Successful",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Card(
                colors = CardDefaults.cardColors(containerColor = MedSurface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Patient:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        Text(patientName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                    if (visitAddress.isNotBlank()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Visit Address:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                            Text("📍 $visitAddress", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Visit Distance:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        Text("$distanceKm km ($billableKm km billable)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Visit Charge:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        Text("₹${visitCharge.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MedSuccess)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Paid From:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        Text("MedTime Wallet", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Remaining Balance:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        Text("₹${remainingBalance.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MedBluePrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Payment ID:", style = MaterialTheme.typography.bodySmall, color = MedTextSecondary)
                        Text(paymentId.take(16), style = MaterialTheme.typography.labelSmall, color = MedTextTertiary)
                    }

                    HorizontalDivider(color = MedDivider, modifier = Modifier.padding(vertical = 4.dp))

                    Surface(
                        color = MedWarningLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Status: PAID — Waiting for Admin Review",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MedWarning,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_close_visit_receipt"),
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
            ) {
                Text("View Request Status")
            }
        }
    )
}
