package com.example.ui.scanner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.util.QrCodeUtils

/**
 * Beautiful Dialog displaying the user's high-resolution QR code and linking code
 * for quick scanning and linking by doctors, caregivers, or patients.
 */
@Composable
fun ShowQrCodeDialog(
    user: UserEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val linkingCode = user.caretakerLinkingCode.ifBlank { "MED-${user.id.takeLast(4)}" }

    val qrPayloadString = remember(user) {
        QrCodeUtils.generateUserQrPayload(user)
    }

    val qrBitmap: Bitmap? = remember(qrPayloadString) {
        QrCodeUtils.generateQrBitmap(
            content = qrPayloadString,
            width = 512,
            height = 512,
            foregroundColor = android.graphics.Color.rgb(13, 71, 161), // MedBlueDark
            backgroundColor = android.graphics.Color.WHITE
        )
    }

    var isCopied by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, MedBorder, RoundedCornerShape(24.dp))
                .testTag("show_qr_code_dialog"),
            colors = CardDefaults.cardColors(containerColor = MedSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = MedBlueLight
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.QrCode,
                                    contentDescription = null,
                                    tint = MedBluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Account Linking QR",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = when (user.role.uppercase()) {
                                    "DOCTOR" -> "Physician Connect QR"
                                    "CARETAKER" -> "Caregiver Profile QR"
                                    else -> "Patient Profile QR"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("btn_dismiss_qr_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MedTextSecondary)
                    }
                }

                HorizontalDivider(color = MedBorder)

                // QR Code Image Container
                Surface(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, MedBorder, RoundedCornerShape(16.dp)),
                    color = Color.White
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "MedTime Account Linking QR Code",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("img_user_qr_code")
                            )
                        } else {
                            CircularProgressIndicator(color = MedBluePrimary)
                        }
                    }
                }

                // Linking Code Pill with Copy Action
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MedBackground
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LINKING CODE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MedTextSecondary
                            )
                            Text(
                                text = linkingCode,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                ),
                                color = MedBluePrimary
                            )
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("MedTime Linking Code", linkingCode)
                                clipboard.setPrimaryClip(clip)
                                isCopied = true
                            },
                            modifier = Modifier.testTag("btn_copy_linking_code")
                        ) {
                            Icon(
                                imageVector = if (isCopied) Icons.Default.Check else Icons.Outlined.ContentCopy,
                                contentDescription = "Copy code",
                                tint = if (isCopied) MedSuccess else MedBluePrimary
                            )
                        }
                    }
                }

                // Instructional Text
                Text(
                    text = when (user.role.uppercase()) {
                        "DOCTOR" -> "Patients or caregivers can scan this QR code using the MedTime app to instantly request medical consultations and link health records."
                        "CARETAKER" -> "Patients can scan this QR code to grant you caregiver monitoring access to their daily medication adherence and appointments."
                        else -> "Your doctor or family caregiver can scan this code to link with your account and monitor your medication schedule."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MedTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                // Share Button
                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "My MedTime Linking Code")
                            putExtra(Intent.EXTRA_TEXT, "Connect with me on MedTime! Use my linking code: $linkingCode")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Linking Code"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_share_qr_code")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Linking Code")
                }
            }
        }
    }
}
