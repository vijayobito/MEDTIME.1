package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedTimeViewModel

data class DoctorConversation(
    val id: String,
    val patientId: String,
    val patientName: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val condition: String = "Cardiology"
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val isFromDoctor: Boolean,
    val text: String,
    val timestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorMessagesScreen(
    viewModel: MedTimeViewModel,
    initialPatientId: String = "",
    initialPatientName: String = "",
    modifier: Modifier = Modifier
) {
    var selectedConversation by remember { mutableStateOf<DoctorConversation?>(null) }
    var messageInput by remember { mutableStateOf("") }

    var conversations by remember {
        mutableStateOf(
            listOf(
                DoctorConversation(
                    id = "conv_1",
                    patientId = "patient_1",
                    patientName = "Vijay Kumar",
                    lastMessage = "Thank you Doctor, my BP is 120/80 this morning.",
                    timestamp = "10:15 AM",
                    unreadCount = 1,
                    condition = "Hypertension"
                ),
                DoctorConversation(
                    id = "conv_2",
                    patientId = "patient_2",
                    patientName = "Robert Chen",
                    lastMessage = "I uploaded the new fasting blood sugar report.",
                    timestamp = "Yesterday",
                    unreadCount = 1,
                    condition = "Type 2 Diabetes"
                ),
                DoctorConversation(
                    id = "conv_3",
                    patientId = "patient_3",
                    patientName = "Maria Garcia",
                    lastMessage = "Should I take the medicine before or after breakfast?",
                    timestamp = "Sep 18",
                    unreadCount = 0,
                    condition = "General Follow-up"
                )
            )
        )
    }

    var chatHistory by remember {
        mutableStateOf(
            listOf(
                ChatMessage("1", "Vijay Kumar", false, "Good morning Dr. Mitchell. I took the Lisinopril 10mg as instructed.", "08:30 AM"),
                ChatMessage("2", "Dr. Sarah Mitchell", true, "Good morning Vijay. How are you feeling today? Any dizziness?", "08:45 AM"),
                ChatMessage("3", "Vijay Kumar", false, "Feeling good! No dizziness. My BP is 120/80 this morning.", "10:15 AM")
            )
        )
    }

    val quickReplies = listOf(
        "Take dosage after breakfast with water.",
        "Please share your latest blood pressure reading.",
        "Your lab report is normal. Continue current medications.",
        "Please schedule a clinic visit for an in-person checkup."
    )

    if (selectedConversation != null) {
        val conv = selectedConversation!!
        // CHAT THREAD VIEW
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MedBackground)
                .testTag("doctor_chat_thread")
        ) {
            // Chat Header
            Surface(
                color = MedNavy,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = { selectedConversation = null },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape,
                        color = MedBluePrimary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(conv.patientName.take(1), fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(conv.patientName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        Text("${conv.condition} • Connected Patient", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                    }
                }
            }

            // Messages list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chatHistory, key = { it.id }) { msg ->
                    val isDoc = msg.isFromDoctor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isDoc) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isDoc) 16.dp else 4.dp,
                                bottomEnd = if (isDoc) 4.dp else 16.dp
                            ),
                            color = if (isDoc) MedBluePrimary else MedSurface,
                            border = if (!isDoc) androidx.compose.foundation.BorderStroke(1.dp, MedBorder) else null,
                            shadowElevation = 1.dp,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDoc) Color.White else MedTextPrimary
                                )
                                Text(
                                    text = msg.timestamp,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (isDoc) Color.White.copy(alpha = 0.7f) else MedTextSecondary,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Reply Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(quickReplies) { reply ->
                    SuggestionChip(
                        onClick = {
                            messageInput = reply
                        },
                        label = { Text(reply, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            // Input bar
            Surface(
                color = MedSurface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = { Text("Type clinical message...") },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            if (messageInput.isNotBlank()) {
                                chatHistory = chatHistory + ChatMessage(
                                    id = "msg_${System.currentTimeMillis()}",
                                    senderName = "Dr. Sarah Mitchell",
                                    isFromDoctor = true,
                                    text = messageInput.trim(),
                                    timestamp = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                                )
                                messageInput = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MedBluePrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    } else {
        // CONVERSATIONS LIST
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MedBackground)
                .testTag("doctor_messages_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Clinical Messages",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedTextPrimary
                    )
                    Text(
                        text = "Secure patient & caregiver clinical inquiries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MedTextSecondary
                    )
                }
            }

            items(conversations, key = { it.id }) { conv ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MedBorder, RoundedCornerShape(16.dp))
                        .clickable { selectedConversation = conv },
                    colors = CardDefaults.cardColors(containerColor = MedSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MedBlueLight
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = conv.patientName.take(1),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MedBluePrimary
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = conv.patientName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MedTextPrimary
                                )
                                Text(
                                    text = conv.timestamp,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MedTextSecondary
                                )
                            }

                            Text(
                                text = conv.lastMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MedTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (conv.unreadCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = MedBluePrimary,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${conv.unreadCount}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        fontSize = 11.sp
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
