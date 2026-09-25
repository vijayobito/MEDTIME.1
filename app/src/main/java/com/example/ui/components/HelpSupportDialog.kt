package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

data class FaqItem(
    val question: String,
    val answer: String
)

@Composable
fun HelpSupportDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }

    val faqs = remember {
        listOf(
            FaqItem(
                question = "How do I schedule a medication dose?",
                answer = "Navigate to the Medicines screen from the bottom menu or side drawer. Tap the '+ Add Medicine' button to scan a pill barcode or enter the name, dosage, frequency, and custom reminder alarm times."
            ),
            FaqItem(
                question = "How does caretaker live adherence sync work?",
                answer = "In the Connections > My Caretaker section, add your caregiver's email or phone number. When you take or miss a scheduled dose, MedTime updates them in real time so you stay safe."
            ),
            FaqItem(
                question = "How can I connect MedTime to my desktop browser?",
                answer = "Open web.medtime.app on your desktop or laptop. In the MedTime app, tap 'Connect to Website' in the side drawer and scan the QR code displayed on your screen to authorize a secure session."
            ),
            FaqItem(
                question = "How do I book a doctor consultation?",
                answer = "Go to Doctor Appointments or My Doctors in the side drawer. Choose a certified specialist from cardiology, neurology, general medicine, or pediatrics and pick your preferred time slot."
            ),
            FaqItem(
                question = "Are my medical documents and lab reports private?",
                answer = "Yes. All prescriptions, lab reports, and doctor clinical summaries are encrypted locally on your device with AES-256 standards and accessible only by you and your approved healthcare providers."
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MedBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Help & Support",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MedTextPrimary
                            )
                            Text(
                                text = "Patient FAQ & Emergency Assistance",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("help_support_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MedTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Emergency Quick Helpline Card
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MedErrorLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MedError.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MedError),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Emergency, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Medical Emergency Helpline", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedError)
                                    Text("If you are experiencing life-threatening symptoms, call emergency services immediately.", fontSize = 11.sp, color = MedTextPrimary)
                                }
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MedError),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Call 911", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Contact Support Channels
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MedBlueLight.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MedBluePrimary.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Patient Support Desk", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MedBlueDark)
                                Text("Have questions about your account or app features? Our healthcare support team is available 24/7.", fontSize = 12.sp, color = MedTextSecondary)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                data = Uri.parse("mailto:support@medtime.app")
                                                putExtra(Intent.EXTRA_SUBJECT, "MedTime Patient Support Request")
                                            }
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Email Support", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:18005550199"))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("1-800-MEDTIME", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    // Frequently Asked Questions
                    item {
                        Text(
                            text = "FREQUENTLY ASKED QUESTIONS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = MedTextSecondary
                            ),
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }

                    items(faqs.indices.toList()) { index ->
                        val item = faqs[index]
                        val isExpanded = expandedFaqIndex == index

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    expandedFaqIndex = if (isExpanded) null else index
                                },
                            colors = CardDefaults.cardColors(containerColor = MedSurfaceVariant.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.question,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MedTextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MedBluePrimary
                                    )
                                }

                                AnimatedVisibility(visible = isExpanded) {
                                    Column(modifier = Modifier.padding(top = 8.dp)) {
                                        HorizontalDivider(color = MedDivider)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = item.answer,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MedTextSecondary,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
